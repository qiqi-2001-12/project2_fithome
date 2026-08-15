package com.hy.greenbuilding.ui.activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.hwellyi.smarthome.MainGatewayActivity;
import com.hy.greenbuilding.HyApplication;
import com.hy.greenbuilding.R;
import com.hy.greenbuilding.config.SaveControlInfo;
import com.hy.greenbuilding.event.SetStatusEvent;
import com.hy.greenbuilding.protocol.FunctionObject;
import com.hy.greenbuilding.protocol.SpDataProcessor;
import com.hy.greenbuilding.protocol.command.ControlCommand;
import com.hy.greenbuilding.protocol.command.OTARequestCommand;
import com.hy.greenbuilding.ui.fragment.AntiFreezingFragment;
import com.hy.greenbuilding.ui.fragment.LowTempFragment;
import com.hy.greenbuilding.ui.fragment.PVFragment;
import com.hy.greenbuilding.ui.fragment.RoomFragment;
import com.hy.greenbuilding.ui.fragment.SettingCodeShowFragment;
import com.hy.greenbuilding.ui.fragment.SettingElectricShowFragment;
import com.hy.greenbuilding.ui.fragment.SettingLocationFrament;
import com.hy.greenbuilding.ui.fragment.SettingScreenSetFragment;
import com.hy.greenbuilding.ui.fragment.UpTempFragment;
import com.hy.greenbuilding.ui.fragment.ValveSettingFragment;
import com.hy.greenbuilding.utils.AppManagerUtil;
import com.hy.greenbuilding.utils.InputLimitUtil;
import com.hy.greenbuilding.utils.MySpUtil;
import com.hy.greenbuilding.utils.StringUtils;
import com.hy.greenbuilding.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 管理员界面
 */
public class ManagerActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener {
    @BindView(R.id.li_back)
    ImageView mReturnView;
    @BindView(R.id.li_funTest)
    LinearLayout mFunTestView;
    @BindView(R.id.li_funReset)
    LinearLayout mFunResetView;
    @BindView(R.id.li_fangdong)
    LinearLayout mFangDongView;
    @BindView(R.id.li_lowTemp)
    RelativeLayout mLowTempView;
    @BindView(R.id.tv_spinner_title)
    TextView mTitleSpinner;
    @BindView(R.id.li_spinner)
    LinearLayout mLiSpinner;

    @BindView(R.id.li_gateway)
    LinearLayout mGateWay;

    @BindView(R.id.li_valve)
    LinearLayout mValveView;
    @BindView(R.id.li_air_valve)
    LinearLayout mAirValveView;
    @BindView(R.id.set_power_switch)
    Switch setPowerSwitch;

    private AlertDialog mResetDialog;
    private ListView mTypeLv;
    private PopupWindow typeSelectPopup;
    private List<String> testData;
    private ArrayAdapter<String> testDataAdapter;
    private int termType = 2;
    private String mLowTemp = "低温增焓";
    private String mPV = "光伏";
    private String mUpTemp = "升温除湿";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.manager_main);
        ButterKnife.bind(this);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        init();
        AppManagerUtil.getAppManager().addActivity(this);
    }

    private void init() {
        SaveControlInfo controlData = getControlData();
        int type = controlData.getOutTermType();
        if (type == 1) {
            mTitleSpinner.setText(mLowTemp);
        } else if (type == 2) {
            mTitleSpinner.setText(mPV);
        } else if (type == 3) {
            mTitleSpinner.setText(mUpTemp);
        }
        setPowerSwitch.setOnCheckedChangeListener(this);
    }

    @OnClick({R.id.li_back})
    public void onReturnClick(View view) {
        finish();
    }

    @OnClick({R.id.li_spinner})
    public void onSpinnerClick(View view) {
        initSelectPopup();
        if (typeSelectPopup != null && !typeSelectPopup.isShowing()) {
            typeSelectPopup.showAsDropDown(mLiSpinner, 0, 0);
        }
    }

    @OnClick({R.id.li_funTest})
    public void onFunTestClick(View view) {
        if (InputLimitUtil.isFastDoubleClick()) {
            return;
        }
        Intent intent = new Intent(this, FanTestActivity.class);
        startActivity(intent);
    }

    @OnClick({R.id.li_funReset})
    public void onFunResetClick(View view) {
        if (InputLimitUtil.isFastDoubleClick()) {
            return;
        }
        Intent intent = new Intent(this, FanResetActivity.class);
        startActivity(intent);
    }

    @OnClick({R.id.li_valve})
    public void onValveClick(View view) {
        if (InputLimitUtil.isFastDoubleClick()) {
            return;
        }
        ValveSettingFragment fragment = new ValveSettingFragment();
        fragment.show(getSupportFragmentManager(), "valve");
    }

    @OnClick({R.id.li_air_valve})
    public void onAirValveClick(View view) {
        if (InputLimitUtil.isFastDoubleClick()) {
            return;
        }
        Intent intent = new Intent(this, AirValveControlActivity.class);
        startActivity(intent);
    }

    @OnClick({R.id.li_fangdong})
    public void onFangDongClick(View view) {
        if (InputLimitUtil.isFastDoubleClick()) {
            return;
        }
        AntiFreezingFragment fragment = new AntiFreezingFragment();
        fragment.show(getSupportFragmentManager(), "antiFreezing");
    }

    @OnClick({R.id.li_lowTemp})
    public void onLowTempClick(View view) {
        if (InputLimitUtil.isFastDoubleClick()) {
            return;
        }
        if (mTitleSpinner.getText().toString().equals(mLowTemp)) {
            LowTempFragment fragment = new LowTempFragment();
            fragment.show(getSupportFragmentManager(), "lowTemp");
        } else if (mTitleSpinner.getText().toString().equals(mPV)) {
            PVFragment fragment = new PVFragment();
            fragment.show(getSupportFragmentManager(), "PV");
        } else if (mTitleSpinner.getText().toString().equals(mUpTemp)) {
            UpTempFragment fragment = new UpTempFragment();
            fragment.show(getSupportFragmentManager(), "upTemp");
        }
    }

    @OnClick({R.id.li_gateway})
    public void onGatewayClick(View view) {
        if (InputLimitUtil.isFastDoubleClick()) {
            return;
        }
        Intent intent = new Intent(ManagerActivity.this, MainGatewayActivity.class);
        intent.putExtra("main", "2");
        startActivity(intent);
    }

    @OnClick(R.id.electric_tv)
    public void onElectricClick(View view) {
        if (InputLimitUtil.isFastDoubleClick()) {
            return;
        }
        SettingElectricShowFragment fragment = new SettingElectricShowFragment();
        fragment.show(getSupportFragmentManager(), "electric");
    }

    @OnClick(R.id.ll_screenSet)
    public void onScreenSetClick(View view) {
        if (InputLimitUtil.isFastDoubleClick()) {
            return;
        }
        SettingScreenSetFragment fragment = new SettingScreenSetFragment();
        fragment.show(getSupportFragmentManager(), "screenset");
    }

    @OnClick(R.id.ll_codeShow)
    public void onCodeShowClick(View view) {
        if (InputLimitUtil.isFastDoubleClick()) {
            return;
        }
        SettingCodeShowFragment fragment = new SettingCodeShowFragment();
        fragment.show(getSupportFragmentManager(), "sodeshow");
    }

    @OnClick(R.id.room_bt)
    public void onRoomClick(View view) {
        if (InputLimitUtil.isFastDoubleClick()) {
            return;
        }
        RoomFragment fragment = new RoomFragment();
        fragment.show(getSupportFragmentManager(), "roomshow");
    }

    @OnClick(R.id.ll_location)
    public void onLocationClick(View view) {
        if (InputLimitUtil.isFastDoubleClick()) {
            return;
        }
        SettingLocationFrament fragment = new SettingLocationFrament();
        fragment.show(getSupportFragmentManager(), "location");
    }

    @OnClick(R.id.reset_bt)
    public void onResetClick(View view) {
        showResetDialog();
    }

    private void showResetDialog() {
        View view = getLayoutInflater().inflate(R.layout.reset_system_dialog, null, false);
        mResetDialog = new AlertDialog.Builder(this).setView(view).create();
        Button sure = view.findViewById(R.id.bt_reset_sure);
        Button cancel = view.findViewById(R.id.bt_reset_cancel);
        cancel.setOnClickListener(v -> mResetDialog.dismiss());
        sure.setOnClickListener(v -> {
            OTARequestCommand otaRequestCommand = new OTARequestCommand(3);
            SpDataProcessor.getInstance().send3(otaRequestCommand);
            mResetDialog.dismiss();
        });
        mResetDialog.show();
        if (mResetDialog.getWindow() != null) {
            mResetDialog.getWindow().setLayout(550, 460);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTypeEvent(SetStatusEvent event) {
        if (event != null) {
            if (event.getType() == 5) {
                if (event.getStatus()) {
                    SaveControlInfo controlInfo = getControlData();
                    controlInfo.setOutTermType(termType);
                    String json = new Gson().toJson(controlInfo);
                    MySpUtil.setParam(ManagerActivity.this, MySpUtil.MAIN_CONTROL_STATUS, json);
                }
            } else if (event.getType() == 1) {
                if (event.getStatus()) {
                    if (HyApplication.isIsReboot()) {
                        ToastUtil.showToast(this, "低功耗设置成功！");
                        try {
                            Runtime.getRuntime().exec("reboot -p");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    ToastUtil.showToast(this, "低功耗设置失败！");
                }
            }
        }
    }

    private void initSelectPopup() {
        mTypeLv = new ListView(this);
        TestData();
        testDataAdapter = new ArrayAdapter<String>(this, R.layout.myspinner_dropdown, testData);
        mTypeLv.setAdapter(testDataAdapter);
        mTypeLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String value = testData.get(position);
                if (!value.equals(mTitleSpinner.getText())) {
                    mTitleSpinner.setText(value);
                    if (mTitleSpinner.getText().equals(mPV)) {
                        ControlCommand controlCommand = new ControlCommand(FunctionObject.SET_OUTDOOR_TYPE);
                        byte[] sendData = {(byte) 0x00, (byte) 0x02};
                        controlCommand.setData(sendData);
                        termType = 2;
                        SpDataProcessor.getInstance().send(controlCommand);
                    } else if (mTitleSpinner.getText().equals(mLowTemp)) {
                        ControlCommand controlCommand = new ControlCommand(FunctionObject.SET_OUTDOOR_TYPE);
                        byte[] sendData = {(byte) 0x00, (byte) 0x01};
                        controlCommand.setData(sendData);
                        termType = 1;
                        SpDataProcessor.getInstance().send(controlCommand);
                    } else if (mTitleSpinner.getText().equals(mUpTemp)) {
                        ControlCommand controlCommand = new ControlCommand(FunctionObject.SET_OUTDOOR_TYPE);
                        byte[] sendData = {(byte) 0x00, (byte) 0x03};
                        controlCommand.setData(sendData);
                        termType = 3;
                        SpDataProcessor.getInstance().send(controlCommand);
                    }

                }
                typeSelectPopup.dismiss();
            }
        });
        typeSelectPopup = new PopupWindow(mTypeLv, 200, 200, true);
        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.btn_bg_common1);
        typeSelectPopup.setBackgroundDrawable(drawable);
        typeSelectPopup.setFocusable(true);
        typeSelectPopup.setOutsideTouchable(true);
        typeSelectPopup.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                typeSelectPopup.dismiss();
            }
        });
    }

    private void TestData() {
        testData = new ArrayList<>();
        testData.add(mPV);
        testData.add(mLowTemp);
        testData.add(mUpTemp);
    }

    /**
     * 获取保存的主控板数据
     *
     * @return
     */
    private SaveControlInfo getControlData() {
        SaveControlInfo controlInfo;
        String json = MySpUtil.getParam(ManagerActivity.this, MySpUtil.MAIN_CONTROL_STATUS, "").toString();
        if (StringUtils.isNullOrEmpty(json)) {
            controlInfo = new SaveControlInfo();
        } else {
            controlInfo = new Gson().fromJson(json, SaveControlInfo.class);
        }
        return controlInfo;
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (compoundButton.getId() == R.id.set_power_switch) {
            ControlCommand controlCommand = new ControlCommand(FunctionObject.SET_LOW_POWER);
            controlCommand.setData(new byte[]{(byte) (isChecked ? 0x01 : 0x00)});
            HyApplication.setIsReboot(true);
            SpDataProcessor.getInstance().send(controlCommand);
        }
    }
}
