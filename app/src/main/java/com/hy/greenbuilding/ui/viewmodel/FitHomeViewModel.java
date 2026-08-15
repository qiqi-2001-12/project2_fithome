package com.hy.greenbuilding.ui.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.gson.Gson;
import com.hy.greenbuilding.HyApplication;
import com.hy.greenbuilding.config.SaveControlInfo;
import com.hy.greenbuilding.event.DefrostChangeEvent;
import com.hy.greenbuilding.event.SettingUpdateEvent;
import com.hy.greenbuilding.event.TempStatusUpdateEvent;
import com.hy.greenbuilding.event.TempSwitchUpdateEvent;
import com.hy.greenbuilding.event.VersionUpdateEvent;
import com.hy.greenbuilding.mqtt.HDTopic;
import com.hy.greenbuilding.mqtt.HXTopic;
import com.hy.greenbuilding.mqtt.MqttUploadManager;
import com.hy.greenbuilding.model.FanDataInfo;
import com.hy.greenbuilding.model.MainControlUiState;
import com.hy.greenbuilding.model.RoomInfo;
import com.hy.greenbuilding.protocol.FunctionObject;
import com.hy.greenbuilding.protocol.ResPonseInfo.CO2StatusInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.CustomDataInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.DCFanStatusInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.ElectricityMeterInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.EnvironmentDataInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.FanStatusInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.MainControlInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.OutDoorStatusInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.PIDStatusInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.PVStatusInfo;
import com.hy.greenbuilding.protocol.SpDataProcessor;
import com.hy.greenbuilding.protocol.command.ControlCommand;
import com.hy.greenbuilding.protocol.command.CustomCommand;
import com.hy.greenbuilding.protocol.command.DCFanCommand;
import com.hy.greenbuilding.protocol.command.EnvironmentCommand;
import com.hy.greenbuilding.protocol.command.FanCommand;
import com.hy.greenbuilding.protocol.command.LowTempCommand;
import com.hy.greenbuilding.protocol.command.MeterCommand;
import com.hy.greenbuilding.protocol.command.PIDCommand;
import com.hy.greenbuilding.protocol.command.PVCommand;
import com.hy.greenbuilding.protocol.command.UpTempCommand;
import com.hy.greenbuilding.utils.ByteUtils;
import com.hy.greenbuilding.utils.MySpUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class FitHomeViewModel extends AndroidViewModel {
    private static final long POLL_INTERVAL_SECONDS = 5L;

    private final MutableLiveData<MainControlUiState> mainControlState = new MutableLiveData<>(MainControlUiState.empty());
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final ScheduledExecutorService pollingExecutor = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> pollingTask;
    private MainControlInfo latestMainControl;
    private List<RoomInfo> latestRooms = new ArrayList<>();
    private List<FanDataInfo> latestFans = new ArrayList<>();
    private int tempMin = 18;
    private int tempMax = 30;
    private int humidifyValue = 45;
    private int dehumidifyValue = 60;
    private int latestManualMode = 0;
    private boolean winterTargetMode = false;
    private boolean legacyInitialStatusRequested = false;

    public FitHomeViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<MainControlUiState> getMainControlState() {
        return mainControlState;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void startPolling() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        if (pollingTask != null && !pollingTask.isCancelled()) {
            return;
        }
        requestLegacyInitialStatus();
        pollingTask = pollingExecutor.scheduleWithFixedDelay(this::readAllStatus, 0, POLL_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    public void stopPolling() {
        if (pollingTask != null) {
            pollingTask.cancel(false);
            pollingTask = null;
        }
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    public void writeTargetTemp(int temp) {
        setTargetTempRange(temp, winterTargetMode);
        writeTempSection();
    }

    public void writeTempMin(int temp) {
        tempMin = temp;
        writeTempSection();
    }

    public void writeTempMax(int temp) {
        tempMax = temp;
        writeTempSection();
    }

    public void writeTargetHumidity(int humidity) {
        humidifyValue = humidity;
        dehumidifyValue = humidity;
        writeHumidity();
    }

    public void writeHumidifyValue(int humidity) {
        humidifyValue = humidity;
        writeHumidity();
    }

    public void writeDehumidifyValue(int humidity) {
        dehumidifyValue = humidity;
        writeHumidity();
    }

    public void writeAutoSceneTarget(int temp, int humidity, boolean winter) {
        winterTargetMode = winter;
        setTargetTempRange(temp, winter);
        humidifyValue = humidity;
        dehumidifyValue = humidity;
        writeTempSection();
        writeHumidity();
        writeAutoMode();
    }

    public void writeCustomTempTarget(int temp, boolean winter) {
        winterTargetMode = winter;
        setTargetTempRange(temp, winter);
        writeTempSection();
        writeTempSwitch(true);
        writeManualMode(winter ? 2 : 1);
    }

    public void writeCustomHumidityTarget(int humidity, boolean decreasing) {
        humidifyValue = humidity;
        dehumidifyValue = humidity;
        writeHumidity();
        if (decreasing) {
            writeHumiditySwitch(true);
            writeManualMode(3);
        } else {
            writeManualMode(0);
        }
    }

    public void writeFanLevel(int level) {
        FanCommand command = new FanCommand(FunctionObject.SET_SPEED);
        command.setData(new byte[]{0x00, (byte) clamp(level, 0, 3)});
        SpDataProcessor.getInstance().send(command);
    }

    public void writeSeasonMode(boolean winter) {
        if (latestMainControl != null && latestMainControl.runMode() != 0) {
            writeManualMode(winter ? 2 : 1);
        }
    }

    public void readMainControlStatus() {
        ControlCommand command = new ControlCommand(FunctionObject.GET_CONTROL_STATUS);
        SpDataProcessor.getInstance().send(command);
    }

    public void writeRawMainControlFrame(byte[] frame) {
        if (frame != null && frame.length > 0) {
            readMainControlStatus();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMainControlInfo(MainControlInfo info) {
        latestMainControl = info;
        if (info != null) {
            String controlVersion = info.softwareVersion();
            HyApplication.setControlVersion(controlVersion);
            EventBus.getDefault().post(new VersionUpdateEvent(2, controlVersion));
            tempMin = info.setTempMin().intValue();
            tempMax = info.setTempMax().intValue();
            humidifyValue = info.getHumidity().intValue();
            dehumidifyValue = humidifyValue + info.getHumidity1().intValue();
            latestManualMode = info.newControlField();
            bridgeLegacyMainControlEvents(info);
            updateMainControlUpload(info);
        }
        publishState();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEnvironmentDataInfo(EnvironmentDataInfo info) {
        if (info != null) {
            latestRooms = info.getRoomData(getApplication());
        }
        publishState();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFanStatusInfo(FanStatusInfo info) {
        if (info != null) {
            latestFans = info.getFanData();
            updateFanUpload(latestFans);
        }
        publishState();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCO2StatusInfo(CO2StatusInfo info) {
        if (info != null) {
            info.sendData();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onElectricityMeterInfo(ElectricityMeterInfo info) {
        if (info != null) {
            info.sendData();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPIDStatusInfo(PIDStatusInfo info) {
        if (info != null) {
            info.sendData();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPVStatusInfo(PVStatusInfo info) {
        if (info != null) {
            info.uploadData();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOutDoorStatusInfo(OutDoorStatusInfo info) {
        if (info != null) {
            info.uploadData();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDCFanStatusInfo(DCFanStatusInfo info) {
        if (info != null && info.getType() == FunctionObject.GET_DC_FAN_STATUS) {
            info.sendData();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onCustomDataInfo(CustomDataInfo info) {
        if (info != null) {
            info.sendData();
        }
    }

    private void readAllStatus() {
        try {
            SpDataProcessor.getInstance().send(new EnvironmentCommand(FunctionObject.GET_ENVIRONMENT_STATUS));
            SpDataProcessor.getInstance().send(new FanCommand(FunctionObject.GET_FAN_STATUS));
            SpDataProcessor.getInstance().send(new ControlCommand(FunctionObject.GET_CONTROL_STATUS));
        } catch (Exception e) {
            errorMessage.postValue(e.getMessage());
        }
    }

    private void writeTempSection() {
        int min = Math.min(tempMin, tempMax);
        int max = Math.max(tempMin, tempMax);
        ControlCommand command = new ControlCommand(FunctionObject.SET_TEMP_SECTION);
        byte[] data = ByteUtils.splicingBytes(ByteUtils.int16ToByteArray(max * 10), ByteUtils.int16ToByteArray(min * 10));
        command.setData(data);
        SpDataProcessor.getInstance().send(command);
    }

    private void setTargetTempRange(int temp, boolean winter) {
        int target = clamp(temp, 16, 30);
        if (winter) {
            tempMin = target;
            tempMax = clamp(target + 3, 16, 30);
            if (tempMax - tempMin < 3) {
                tempMin = clamp(tempMax - 3, 16, 30);
            }
        } else {
            tempMax = target;
            tempMin = clamp(target - 3, 16, 30);
            if (tempMax - tempMin < 3) {
                tempMax = clamp(tempMin + 3, 16, 30);
            }
        }
    }

    private void updateMainControlUpload(MainControlInfo info) {
        HDTopic hdTopic = MqttUploadManager.getInstance().getmHDTopic();
        HXTopic hxTopic = MqttUploadManager.getInstance().getmHxTopic();
        int tempMode = info.tempControlMode();
        hdTopic.setAirSwitch((byte) (tempMode == 0 ? 0x00 : 0x01));
        hdTopic.setAirMode((byte) airModeForUpload(tempMode));
        hdTopic.setRunMode((byte) (info.runMode() == 0 ? 0x01 : 0x00));
        hdTopic.setSetHumidity((byte) info.getHumidity().intValue());
        hdTopic.setTempMin((byte) info.setTempMin().intValue());
        hdTopic.setTempMax((byte) info.setTempMax().intValue());
        hxTopic.setHumidity1((byte) info.getHumidity1().intValue());
        hxTopic.setAdditionalManualMode((byte) (info.newControlField() & 0xFF));
        int outTermType = info.getOutTermType();
        if (outTermType == 1) {
            hxTopic.setOutTermChoice((byte) 0x01);
        } else if (outTermType == 2) {
            hxTopic.setOutTermChoice((byte) 0x02);
        } else if (outTermType == 3) {
            hxTopic.setOutTermChoice((byte) 0x03);
        }
    }

    private void bridgeLegacyMainControlEvents(MainControlInfo info) {
        SaveControlInfo saveControlInfo = new SaveControlInfo();
        int dehumidifySetting = clamp(info.getHumidity().intValue(), 45, 99);
        int humidifySetting = clamp(info.getHumidity1().intValue(), 35, 80);
        saveControlInfo.setControl_version(info.softwareVersion());
        saveControlInfo.setOutTermType(info.getOutTermType());
        saveControlInfo.setHumidity(String.valueOf(dehumidifySetting));
        saveControlInfo.setHumidity1(humidifySetting);
        saveControlInfo.setLowPower(info.lowPower());
        saveControlInfo.setTempMax(info.setTempMax().toString());
        saveControlInfo.setTempMin(info.setTempMin().toString());
        saveControlInfo.setRunMode(info.runMode());
        saveControlInfo.setManualMode(info.newControlField());
        MySpUtil.setParam(getApplication(), MySpUtil.MAIN_CONTROL_STATUS, new Gson().toJson(saveControlInfo));

        SettingUpdateEvent controlUpdateEvent = new SettingUpdateEvent(1);
        controlUpdateEvent.setHumidity(saveControlInfo.getHumidity());
        controlUpdateEvent.setHumidity1(String.valueOf(saveControlInfo.getHumidity1()));
        controlUpdateEvent.setTempMax(saveControlInfo.getTempMax());
        controlUpdateEvent.setTempMin(saveControlInfo.getTempMin());
        EventBus.getDefault().post(controlUpdateEvent);

        int tempMode = info.tempControlMode();
        boolean tempSwitch = tempMode != 0;
        boolean defrost = info.getDefrostStatus() == 1;
        EventBus.getDefault().post(new TempStatusUpdateEvent(tempSwitch, tempMode));
        EventBus.getDefault().post(new DefrostChangeEvent(defrost));
        EventBus.getDefault().post(new TempSwitchUpdateEvent(info.getTempControlEnable() != 0));

        byte[] ntcError = ByteUtils.getBitArray((byte) info.ntcError());
        if (defrost && ntcError.length > 2) {
            ntcError[2] = 1;
        }
        HyApplication.setNtcError(ntcError);
        MySpUtil.setParam(getApplication(), MySpUtil.NTC_DATA, info.getNtc());
        EventBus.getDefault().post(new SettingUpdateEvent(6));

        requestLegacyDependentStatus(info.getOutTermType());
    }

    private void requestLegacyDependentStatus(int outTermType) {
        try {
            if (outTermType == 1) {
                SpDataProcessor.getInstance().send(new LowTempCommand(FunctionObject.GET_OUT_STATUS));
            } else if (outTermType == 2) {
                SpDataProcessor.getInstance().send(new PVCommand(FunctionObject.GET_OUT_STATUS));
            } else if (outTermType == 3) {
                SpDataProcessor.getInstance().send(new UpTempCommand(FunctionObject.UP_GET_OUT_STATUS));
            }
            SpDataProcessor.getInstance().send(new PIDCommand(FunctionObject.GET_PID_STATUS));
            SpDataProcessor.getInstance().send(new MeterCommand(1));
            SpDataProcessor.getInstance().send(new EnvironmentCommand(FunctionObject.GET_PM_CO2));
        } catch (Exception e) {
            errorMessage.postValue(e.getMessage());
        }
    }

    private void requestLegacyInitialStatus() {
        if (legacyInitialStatusRequested) {
            return;
        }
        legacyInitialStatusRequested = true;
        try {
            SpDataProcessor.getInstance().send(new CustomCommand(FunctionObject.GET_CUSTOM_DATA));
            SpDataProcessor.getInstance().send(new DCFanCommand(FunctionObject.GET_DC_FAN_STATUS));
        } catch (Exception e) {
            errorMessage.postValue(e.getMessage());
        }
    }

    private int airModeForUpload(int tempMode) {
        if (tempMode == 1) {
            return 0x00;
        }
        if (tempMode == 2) {
            return 0x01;
        }
        if (tempMode == 4) {
            return 0x02;
        }
        return 0x03;
    }

    private void updateFanUpload(List<FanDataInfo> fans) {
        if (fans == null || fans.size() < 4) {
            return;
        }
        HDTopic hdTopic = MqttUploadManager.getInstance().getmHDTopic();
        HXTopic hxTopic = MqttUploadManager.getInstance().getmHxTopic();
        updateFanChoice(hxTopic, fans.get(0), 0);
        updateFanChoice(hxTopic, fans.get(1), 1);
        updateFanChoice(hxTopic, fans.get(2), 2);
        updateFanChoice(hxTopic, fans.get(3), 3);
        hdTopic.setWindStatus((byte) fanLevel(fans.get(0)));
        hdTopic.setCircleStatus((byte) fanLevel(fans.get(2)));
    }

    private void updateFanChoice(HXTopic hxTopic, FanDataInfo fan, int index) {
        boolean rs485 = fan.getInterfaceType() == 1;
        byte value;
        if (index == 0) {
            value = (byte) (rs485 ? 0x02 : 0x01);
            hxTopic.setFanChoice1(value);
        } else if (index == 1) {
            value = (byte) (rs485 ? 0x05 : 0x04);
            hxTopic.setFanChoice2(value);
        } else if (index == 2) {
            value = (byte) (rs485 ? 0x08 : 0x07);
            hxTopic.setFanChoice3(value);
        } else if (index == 3) {
            value = (byte) (rs485 ? 0x0C : 0x0B);
            hxTopic.setFanChoice4(value);
        }
    }

    private int fanLevel(FanDataInfo fan) {
        int status = fan.getInterfaceType() == 0 ? fan.getPwmFanStatus() : fan.getFanStatus();
        return clamp(status, 0, 3);
    }

    private void writeHumidity() {
        int set = clamp(humidifyValue, 0, 100);
        int back = Math.max(0, clamp(dehumidifyValue, 0, 100) - set);
        ByteBuffer buffer = ByteBuffer.allocate(3);
        buffer.put(ByteUtils.int16ToByteArray(set));
        buffer.put((byte) back);
        ControlCommand command = new ControlCommand(FunctionObject.SET_HUMIDITY);
        command.setData(buffer.array());
        SpDataProcessor.getInstance().send(command);
    }

    private void writeAutoMode() {
        writeControlMode(false, false, latestManualMode);
    }

    private void writeManualMode(int mode) {
        latestManualMode = clamp(mode, 0, 3);
        writeControlMode(false, true, latestManualMode);
    }

    private void writeControlMode(boolean timing, boolean manual, int mode) {
        ControlCommand command = new ControlCommand(FunctionObject.SET_CONTROL_MODE);
        command.setData(new byte[]{(byte) (timing ? 1 : 0), (byte) (manual ? 1 : 0), (byte) clamp(mode, 0, 3)});
        SpDataProcessor.getInstance().send(command);
    }

    private void writeTempSwitch(boolean enabled) {
        ControlCommand command = new ControlCommand(FunctionObject.GET_TEMP_SWITCH);
        command.setData(new byte[]{(byte) (enabled ? 1 : 0)});
        SpDataProcessor.getInstance().send(command);
    }

    private void writeHumiditySwitch(boolean enabled) {
        ControlCommand command = new ControlCommand(FunctionObject.SET_HUMI_SWITCH);
        command.setData(new byte[]{(byte) (enabled ? 1 : 0)});
        SpDataProcessor.getInstance().send(command);
    }

    private void publishState() {
        mainControlState.setValue(MainControlUiState.fromProject2(latestMainControl, latestRooms, latestFans));
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    protected void onCleared() {
        stopPolling();
        pollingExecutor.shutdownNow();
        super.onCleared();
    }
}
