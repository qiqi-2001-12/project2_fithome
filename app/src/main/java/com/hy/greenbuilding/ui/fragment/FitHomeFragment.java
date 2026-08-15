package com.hy.greenbuilding.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.app.ProgressDialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.app.AlertDialog;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;
import com.hy.greenbuilding.R;
import com.hy.greenbuilding.HyApplication;
import com.hy.greenbuilding.config.SaveFilterScreen;
import com.hy.greenbuilding.event.OTAErrorEvent;
import com.hy.greenbuilding.event.OTAStatusEvent;
import com.hy.greenbuilding.event.VersionUpdateEvent;
import com.hy.greenbuilding.event.WeatherDataEvent;
import com.hy.greenbuilding.config.SaveControlInfo;
import com.hy.greenbuilding.model.MainControlUiState;
import com.hy.greenbuilding.model.RoomInfo;
import com.hy.greenbuilding.mqtt.HyServiceConnection;
import com.hy.greenbuilding.mqtt.IGetMessageCallBack;
import com.hy.greenbuilding.mqtt.HDTopic;
import com.hy.greenbuilding.mqtt.MqttUploadManager;
import com.hy.greenbuilding.mqtt.MyMqttService;
import com.hy.greenbuilding.protocol.ResPonseInfo.EnvironmentDataInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.ElectricityMeterInfo;
import com.hy.greenbuilding.protocol.ResPonseInfo.MainControlInfo;
import com.hy.greenbuilding.protocol.FunctionObject;
import com.hy.greenbuilding.protocol.SpDataProcessor;
import com.hy.greenbuilding.protocol.command.ControlCommand;
import com.hy.greenbuilding.protocol.command.EnvironmentCommand;
import com.hy.greenbuilding.protocol.command.FanCommand;
import com.hy.greenbuilding.protocol.command.MeterCommand;
import com.hy.greenbuilding.protocol.command.OTARequestCommand;
import com.hy.greenbuilding.ui.activity.ManagerActivity;
import com.hy.greenbuilding.ui.viewmodel.FitHomeViewModel;
import com.hy.greenbuilding.utils.ByteUtils;
import com.hy.greenbuilding.utils.MySpUtil;
import com.hy.greenbuilding.utils.PackageUtil;
import com.hy.greenbuilding.utils.StringUtils;
import com.hy.greenbuilding.utils.ToastUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.SimpleDateFormat;
import java.io.File;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FitHomeFragment extends Fragment implements IGetMessageCallBack {
    private static final String TAG = "FitHomeFragment";
    private static final String ENERGY_DATE_KEY = "energy_date";
    private static final String ENERGY_START_TOTAL_KEY = "energy_start_total";
    private static final String ENERGY_TODAY_USAGE_KEY = "energy_today_usage";
    private static final String ENERGY_LAST_TOTAL_KEY = "energy_last_total";
    private static final String ENERGY_YESTERDAY_DATE_KEY = "energy_yesterday_date";
    private static final String ENERGY_YESTERDAY_USAGE_KEY = "energy_yesterday_usage";
    private static final String ENERGY_TODAY_HOURLY_USAGE_KEY = "energy_today_hourly_usage";
    private static final String ENERGY_YESTERDAY_HOURLY_USAGE_KEY = "energy_yesterday_hourly_usage";
    private static final String ENERGY_OLD_KEY_PREFIX = "fit_home_";
    private static final BigDecimal ENERGY_MAX_SINGLE_DELTA = new BigDecimal("20.0");

    private View rootView;
    private View panel;
    private View leftCard;
    private View middleCard;
    private View rightCard;
    private View filterCard;
    private View energyCard;
    private View targetPanel;
    private View fanPanel;
    private View fanOffButton;
    private View fanLowButton;
    private View fanMidButton;
    private View fanHighButton;
    private View modePanel;
    private View seasonPanel;
    private View tempMinusButton;
    private View tempPlusButton;
    private View humidityMinusButton;
    private View humidityPlusButton;
    private TextView realtimePill;
    private TextView sceneSwitchPill;
    private TextView energyDropPill;
    private View outdoorPill;
    private TextView outdoorStatusView;
    private TextView indoorQualityView;
    private TextView outdoorTempView;
    private ImageView weatherIconView;
    private ImageView adminIconView;
    private ImageView wifiIconView;
    private TextView timeView;
    private TextView titleView;
    private TextView indoorTempUnitView;
    private TextView indoorTempLabelView;
    private TextView indoorTempView;
    private TextView indoorHumidityUnitView;
    private TextView indoorHumidityLabelView;
    private TextView indoorHumidityView;
    private TextView pm25LabelView;
    private TextView pm25UnitView;
    private TextView pm25View;
    private TextView co2LabelView;
    private TextView co2UnitView;
    private TextView co2View;
    private TextView footerStatusView;
    private TextView filterValueView;
    private TextView energyValueView;
    private TextView tempSettingTitle;
    private TextView humiditySettingTitle;
    private TextView targetTempView;
    private TextView targetTempUnitView;
    private TextView targetHumidityView;
    private TextView targetHumidityUnitView;
    private ProgressBar filterProgressBar;
    private TextView fanTitleView;
    private View fanOffLabel;
    private View fanLowLabel;
    private View fanMidLabel;
    private View fanHighLabel;
    private TextView classicModeButton;
    private TextView careModeButton;
    private TextView summerButton;
    private TextView winterButton;
    private View sceneEcoButton;
    private View sceneComfortButton;
    private View sceneVacationButton;
    private View sceneCustomButton;
    private View careSceneContent;
    private View careSceneEcoButton;
    private View careSceneComfortButton;
    private View careSceneVacationButton;
    private View careSceneCustomButton;
    private View careTempCard;
    private View careHumidityCard;
    private View carePm25Card;
    private View careCo2Card;
    private TextView careTempView;
    private TextView careTempLabelView;
    private TextView careTempUnitView;
    private TextView careHumidityView;
    private TextView careHumidityLabelView;
    private TextView careHumidityUnitView;
    private TextView carePm25View;
    private TextView carePm25LabelView;
    private TextView carePm25UnitView;
    private TextView careCo2View;
    private TextView careCo2LabelView;
    private TextView careCo2UnitView;
    private TextView sceneEcoTitle;
    private TextView sceneComfortTitle;
    private TextView sceneVacationTitle;
    private TextView sceneCustomTitle;
    private TextView sceneEcoPreset;
    private TextView sceneComfortPreset;
    private TextView sceneVacationPreset;
    private TextView sceneCustomPreset;
    private TextView sceneEcoCheck;
    private TextView sceneComfortCheck;
    private TextView sceneVacationCheck;
    private TextView sceneCustomCheck;
    private FitHomeViewModel viewModel;
    private HyServiceConnection serviceConnection;

    private int targetTemp = 26;
    private int targetHumidity = 60;
    private String selectedScene = "comfort";
    private boolean classicModeSelected = true;
    private boolean winterThemeSelected = true;
    private boolean isOtaOpen;
    private boolean isUpdating;
    private static final int MAX_OTA_RETRY = 3;//主板OTA失败重试上限，达到后跳过主板升级直接安装App
    private static final long BOARD_OTA_TIMEOUT_MS = 5 * 60 * 1000;//主板OTA看门狗：超时后放弃并安装App
    private int otaFailCount = 0;//主板OTA连续失败次数
    private long otaStartTime = 0;//主板OTA开始时间，用于超时看门狗
    private String controlVersion = "";
    private String appVersion = "";
    private String updateStatus = "";
    private ProgressDialog progressDialog;
    private int latestFreshAirLevel;
    private int latestPurifyLevel;
    private long windUseTime = 0;
    private long exhaustTime = 0;
    private long circle1UseTime = 0;
    private long circle2UseTime = 0;
    private final Handler otaHandler = new Handler(Looper.getMainLooper());
    private final Runnable otaRunnable = new Runnable() {
        @Override
        public void run() {
            if (isUpdating) {
                if (SystemClock.elapsedRealtime() - otaStartTime > BOARD_OTA_TIMEOUT_MS) {
                    // 主板OTA长时间无响应/未完成：放弃并确保App能安装
                    giveUpBoardOtaAndInstallApp();
                } else {
                    sendUpdateRequest();
                }
            }
        }
    };
    private File file_path;
    private View selectedFanButton;
    private boolean warmTheme;
    private final List<TextView> primaryTextViews = new ArrayList<>();
    private final List<TextView> secondaryTextViews = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.CHINA);
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.CHINA);
    private final SimpleDateFormat hourFormat = new SimpleDateFormat("H", Locale.CHINA);
    private final Runnable timeTicker = new Runnable() {
        @Override
        public void run() {
            if (timeView != null) {
                timeView.setText(timeFormat.format(new Date()));
            }
            handler.postDelayed(this, 1000);
        }
    };
    private final Runnable filterUsageTicker = new Runnable() {
        @Override
        public void run() {
            updateFilterUseTimeFromOriginalLogic();
            renderFilterRemainingRate();
            handler.postDelayed(this, 15 * 1000);
        }
    };
    private final Runnable energyTicker = new Runnable() {
        @Override
        public void run() {
            requestElectricityMeter();
            handler.postDelayed(this, 60 * 1000);
        }
    };
    private final BroadcastReceiver wifiStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateWifiIconVisibility();
        }
    };
    private static final String FOOTER_BASE_TEXT = "系统运行正常      ·      IPv6 已连接";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fit_home, container, false);
        viewModel = new ViewModelProvider(this).get(FitHomeViewModel.class);
        isOtaOpen = (boolean) MySpUtil.getParam(requireContext(), MySpUtil.OTA_STATUS, false);
        bindViews(view);
        collectThemeTextViews(view);
        initPlaceholders(view);
        initClicks(view);
        setTitleText();
        bindMqttService();
        view.post(() -> scalePanelToScreen(view));
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        observeViewModel();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        IntentFilter wifiFilter = new IntentFilter();
        wifiFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        wifiFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        requireContext().registerReceiver(wifiStateReceiver, wifiFilter);
        updateWifiIconVisibility();
        handler.removeCallbacks(timeTicker);
        handler.post(timeTicker);
        handler.removeCallbacks(filterUsageTicker);
        handler.postDelayed(filterUsageTicker, 15 * 1000);
        handler.removeCallbacks(energyTicker);
        handler.post(energyTicker);
        if (viewModel != null) {
            viewModel.startPolling();
        }
    }

    @Override
    public void onStop() {
        handler.removeCallbacks(timeTicker);
        handler.removeCallbacks(filterUsageTicker);
        handler.removeCallbacks(energyTicker);
        try {
            requireContext().unregisterReceiver(wifiStateReceiver);
        } catch (Exception ignored) {
        }
        if (viewModel != null) {
            viewModel.stopPolling();
        }
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        if (serviceConnection != null && getContext() != null) {
            try {
                if (serviceConnection.getMqttService() != null) {
                    serviceConnection.getMqttService().setIGetMessageCallBack(null);
                }
                requireContext().unbindService(serviceConnection);
            } catch (Exception ignored) {
            }
            serviceConnection = null;
        }
        otaHandler.removeCallbacks(otaRunnable);
        dismissProgressDialog();
        super.onDestroyView();
    }

    public void resetToDefaultView() {
        classicModeSelected = true;
        selectedScene = "comfort";
        targetTemp = getSceneTemp(selectedScene);
        targetHumidity = getSceneHumidity(selectedScene);
        updateTargetViews();
        selectFan(fanLowButton);
        selectScene(selectedScene);
        applyLayoutMode();
    }

    private void bindViews(View root) {
        rootView = root.findViewById(R.id.fit_home_root);
        panel = root.findViewById(R.id.fit_home_panel);
        leftCard = root.findViewById(R.id.fit_home_left_card);
        middleCard = root.findViewById(R.id.fit_home_middle_card);
        rightCard = root.findViewById(R.id.fit_home_right_card);
        filterCard = root.findViewById(R.id.card_fit_home_filter);
        energyCard = root.findViewById(R.id.card_fit_home_energy);
        targetPanel = root.findViewById(R.id.fit_home_target_panel);
        fanPanel = root.findViewById(R.id.fit_home_fan_panel);
        modePanel = root.findViewById(R.id.fit_home_mode_panel);
        seasonPanel = root.findViewById(R.id.fit_home_season_panel);
        tempMinusButton = root.findViewById(R.id.btn_fit_home_temp_minus);
        tempPlusButton = root.findViewById(R.id.btn_fit_home_temp_plus);
        humidityMinusButton = root.findViewById(R.id.btn_fit_home_humidity_minus);
        humidityPlusButton = root.findViewById(R.id.btn_fit_home_humidity_plus);
        realtimePill = root.findViewById(R.id.pill_fit_home_realtime);
        sceneSwitchPill = root.findViewById(R.id.pill_fit_home_scene_switch);
        energyDropPill = root.findViewById(R.id.pill_fit_home_energy_drop);
        outdoorPill = root.findViewById(R.id.pill_fit_home_outdoor);
        outdoorStatusView = root.findViewById(R.id.tv_fit_home_outdoor_status);
        indoorQualityView = root.findViewById(R.id.tv_fit_home_indoor_quality);
        outdoorTempView = root.findViewById(R.id.tv_fit_home_outdoor_temp);
        weatherIconView = root.findViewById(R.id.tv_fit_home_weather_icon);
        adminIconView = root.findViewById(R.id.iv_fit_home_admin);
        wifiIconView = root.findViewById(R.id.iv_fit_home_wifi);
        timeView = root.findViewById(R.id.tv_fit_home_time);
        titleView = root.findViewById(R.id.tv_fit_home_title);
        indoorTempUnitView = root.findViewById(R.id.tv_fit_home_temp_big_unit);
        indoorTempLabelView = root.findViewById(R.id.tv_fit_home_temp_big_label);
        indoorTempView = root.findViewById(R.id.tv_fit_home_temp_big);
        indoorHumidityUnitView = root.findViewById(R.id.tv_fit_home_humidity_big_unit);
        indoorHumidityLabelView = root.findViewById(R.id.tv_fit_home_humidity_big_label);
        indoorHumidityView = root.findViewById(R.id.tv_fit_home_humidity_big);
        pm25LabelView = root.findViewById(R.id.tv_fit_home_pm25_label);
        pm25UnitView = root.findViewById(R.id.tv_fit_home_pm25_unit);
        pm25View = root.findViewById(R.id.tv_fit_home_pm25);
        co2LabelView = root.findViewById(R.id.tv_fit_home_co2_label);
        co2UnitView = root.findViewById(R.id.tv_fit_home_co2_unit);
        co2View = root.findViewById(R.id.tv_fit_home_co2);
        footerStatusView = root.findViewById(R.id.tv_fit_home_footer_status);
        filterValueView = root.findViewById(R.id.tv_fit_home_filter);
        energyValueView = root.findViewById(R.id.tv_fit_home_energy);
        tempSettingTitle = root.findViewById(R.id.tv_fit_home_temp_setting_title);
        humiditySettingTitle = root.findViewById(R.id.tv_fit_home_humidity_setting_title);
        targetTempView = root.findViewById(R.id.tv_fit_home_target_temp);
        targetTempUnitView = root.findViewById(R.id.tv_fit_home_target_temp_unit);
        targetHumidityView = root.findViewById(R.id.tv_fit_home_target_humidity);
        targetHumidityUnitView = root.findViewById(R.id.tv_fit_home_target_humidity_unit);
        filterProgressBar = root.findViewById(R.id.progress_fit_home_filter);
        fanOffButton = root.findViewById(R.id.btn_fit_home_fan_off);
        fanLowButton = root.findViewById(R.id.btn_fit_home_fan_low);
        fanMidButton = root.findViewById(R.id.btn_fit_home_fan_mid);
        fanHighButton = root.findViewById(R.id.btn_fit_home_fan_high);
        fanTitleView = root.findViewById(R.id.tv_fit_home_fan_title);
        fanOffLabel = root.findViewById(R.id.tv_fit_home_fan_off_label);
        fanLowLabel = root.findViewById(R.id.tv_fit_home_fan_low_label);
        fanMidLabel = root.findViewById(R.id.tv_fit_home_fan_mid_label);
        fanHighLabel = root.findViewById(R.id.tv_fit_home_fan_high_label);
        classicModeButton = root.findViewById(R.id.btn_fit_home_classic_mode);
        careModeButton = root.findViewById(R.id.btn_fit_home_care_mode);
        summerButton = root.findViewById(R.id.btn_fit_home_summer);
        winterButton = root.findViewById(R.id.btn_fit_home_winter);
        sceneEcoButton = root.findViewById(R.id.btn_fit_home_scene_eco);
        sceneComfortButton = root.findViewById(R.id.btn_fit_home_scene_comfort);
        sceneVacationButton = root.findViewById(R.id.btn_fit_home_scene_vacation);
        sceneCustomButton = root.findViewById(R.id.btn_fit_home_scene_custom);
        careSceneContent = root.findViewById(R.id.fit_home_care_scene_content);
        careSceneEcoButton = root.findViewById(R.id.btn_fit_home_care_scene_eco);
        careSceneComfortButton = root.findViewById(R.id.btn_fit_home_care_scene_comfort);
        careSceneVacationButton = root.findViewById(R.id.btn_fit_home_care_scene_vacation);
        careSceneCustomButton = root.findViewById(R.id.btn_fit_home_care_scene_custom);
        careTempCard = root.findViewById(R.id.card_fit_home_care_temp);
        careHumidityCard = root.findViewById(R.id.card_fit_home_care_humidity);
        carePm25Card = root.findViewById(R.id.card_fit_home_care_pm25);
        careCo2Card = root.findViewById(R.id.card_fit_home_care_co2);
        careTempView = root.findViewById(R.id.tv_fit_home_care_temp);
        careTempLabelView = root.findViewById(R.id.tv_fit_home_care_temp_label);
        careTempUnitView = root.findViewById(R.id.tv_fit_home_care_temp_unit);
        careHumidityView = root.findViewById(R.id.tv_fit_home_care_humidity);
        careHumidityLabelView = root.findViewById(R.id.tv_fit_home_care_humidity_label);
        careHumidityUnitView = root.findViewById(R.id.tv_fit_home_care_humidity_unit);
        carePm25View = root.findViewById(R.id.tv_fit_home_care_pm25);
        carePm25LabelView = root.findViewById(R.id.tv_fit_home_care_pm25_label);
        carePm25UnitView = root.findViewById(R.id.tv_fit_home_care_pm25_unit);
        careCo2View = root.findViewById(R.id.tv_fit_home_care_co2);
        careCo2LabelView = root.findViewById(R.id.tv_fit_home_care_co2_label);
        careCo2UnitView = root.findViewById(R.id.tv_fit_home_care_co2_unit);
        sceneEcoTitle = root.findViewById(R.id.tv_fit_home_scene_eco_title);
        sceneComfortTitle = root.findViewById(R.id.tv_fit_home_scene_comfort_title);
        sceneVacationTitle = root.findViewById(R.id.tv_fit_home_scene_vacation_title);
        sceneCustomTitle = root.findViewById(R.id.tv_fit_home_scene_custom_title);
        sceneEcoPreset = root.findViewById(R.id.tv_fit_home_scene_eco_preset);
        sceneComfortPreset = root.findViewById(R.id.tv_fit_home_scene_comfort_preset);
        sceneVacationPreset = root.findViewById(R.id.tv_fit_home_scene_vacation_preset);
        sceneCustomPreset = root.findViewById(R.id.tv_fit_home_scene_custom_preset);
        sceneEcoCheck = root.findViewById(R.id.tv_fit_home_scene_eco_check);
        sceneComfortCheck = root.findViewById(R.id.tv_fit_home_scene_comfort_check);
        sceneVacationCheck = root.findViewById(R.id.tv_fit_home_scene_vacation_check);
        sceneCustomCheck = root.findViewById(R.id.tv_fit_home_scene_custom_check);
    }

    private void collectThemeTextViews(View root) {
        primaryTextViews.clear();
        secondaryTextViews.clear();
        collectTextViews(root, primaryTextViews);
        secondaryTextViews.add(timeView);
        secondaryTextViews.add(sceneEcoPreset);
        secondaryTextViews.add(sceneComfortPreset);
        secondaryTextViews.add(sceneVacationPreset);
        secondaryTextViews.add(sceneCustomPreset);
    }

    private void collectTextViews(View view, List<TextView> out) {
        if (view instanceof TextView) {
            out.add((TextView) view);
            return;
        }
        if (!(view instanceof ViewGroup)) {
            return;
        }
        ViewGroup group = (ViewGroup) view;
        for (int i = 0; i < group.getChildCount(); i++) {
            collectTextViews(group.getChildAt(i), out);
        }
    }

    private void initPlaceholders(View root) {
        ((TextView) root.findViewById(R.id.tv_fit_home_outdoor_temp)).setText("--℃");
        ((TextView) root.findViewById(R.id.tv_fit_home_outdoor_status)).setText("室外");
        weatherIconView.setImageResource(R.drawable.weather_qing);
        updateWifiIconVisibility();
        indoorQualityView.setText("--");
        setMainControlPlaceholders();
        outdoorStatusView.setText("室外");
        indoorQualityView.setTextColor(Color.WHITE);
        updateFooterStatusText(FOOTER_BASE_TEXT);
        resetToDefaultView();
    }

    private void setMainControlPlaceholders() {
        indoorTempView.setText("--");
        indoorHumidityView.setText("--");
        pm25View.setText("--");
        co2View.setText("--");
        filterValueView.setText("--%");
        filterProgressBar.setProgress(0);
        energyValueView.setText("--度");
        energyDropPill.setText("--%");
        if (careTempView != null) {
            careTempView.setText("--");
            careHumidityView.setText("--");
            carePm25View.setText("--");
            careCo2View.setText("--");
        }
    }

    private void updateWifiIconVisibility() {
        if (wifiIconView == null || getContext() == null) {
            return;
        }
        boolean wifiConnected = false;
        try {
            ConnectivityManager manager = (ConnectivityManager) requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = manager == null ? null : manager.getActiveNetworkInfo();
            wifiConnected = info != null && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI;
        } catch (Exception ignored) {
        }
        wifiIconView.setVisibility(wifiConnected ? View.VISIBLE : View.GONE);
    }

    private void bindMqttService() {
        if (getContext() == null) {
            return;
        }
        serviceConnection = new HyServiceConnection();
        serviceConnection.setIGetMessageCallBack(this);
        Intent intent = new Intent(requireContext(), MyMqttService.class);
        requireContext().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void observeViewModel() {
        viewModel.getMainControlState().observe(getViewLifecycleOwner(), this::renderMainControlState);
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), message -> {
            if (message != null && footerStatusView != null) {
                updateFooterStatusText("通讯异常");
            }
        });
    }

    private void renderMainControlState(MainControlUiState state) {
        if (state == null || !state.isValid()) {
            return;
        }
        indoorTempView.setText(String.valueOf(state.getIndoorTemp()));
        indoorHumidityView.setText(String.valueOf(state.getIndoorHumidity()));
        pm25View.setText(String.valueOf(state.getPm25()));
        co2View.setText(String.valueOf(state.getCo2()));
        latestFreshAirLevel = state.getFreshAirLevel();
        latestPurifyLevel = state.getPurifyLevel();
        renderFilterRemainingRate();
        updateFanSelection(fanButtonForLevel(Math.max(state.getFreshAirLevel(), state.getPurifyLevel())));
        if (careTempView != null) {
            careTempView.setText(String.valueOf(state.getIndoorTemp()));
            careHumidityView.setText(String.valueOf(state.getIndoorHumidity()));
            carePm25View.setText(String.valueOf(state.getPm25()));
            careCo2View.setText(String.valueOf(state.getCo2()));
        }
        // 主板信息到达时刷新 footer（固件版本跟随主板实际版本）
        updateFooterStatusText(FOOTER_BASE_TEXT);
    }

    private void renderFilterRemainingRate() {
        if (filterValueView == null || filterProgressBar == null || getActivity() == null) {
            return;
        }
        int remainingRate = getOriginalFilterRemainingRate();
        filterValueView.setText(remainingRate + "%");
        filterProgressBar.setProgress(clamp(remainingRate, 0, 100));
    }

    private int getOriginalFilterRemainingRate() {
        SaveFilterScreen saveFilterScreen = MySpUtil.getFilterScreen(getActivity());
        String[][] filterData = {
                {saveFilterScreen.getFreshAirUse(), saveFilterScreen.getFreshAirChange()},
                {saveFilterScreen.getExhaustUse(), saveFilterScreen.getExhaustChange()},
                {saveFilterScreen.getCircle1Use(), saveFilterScreen.getCircle1Change()},
                {saveFilterScreen.getCircle2Use(), saveFilterScreen.getCircle2Change()}
        };

        double usedRate = 0;
        for (String[] data : filterData) {
            double result = calculateFilterUsedRate(parseInt(data[0]), parseInt(data[1]) * 3600);
            if (result > 0) {
                usedRate = result;
                break;
            }
        }
        int utilizationRate = (int) (Math.round(usedRate * 100 * 100) / 100.0);
        return clamp(100 - utilizationRate, 0, 100);
    }

    private void updateFilterUseTimeFromOriginalLogic() {
        if (getActivity() == null) {
            return;
        }
        SaveFilterScreen saveFilterScreen = MySpUtil.getFilterScreen(getActivity());
        windUseTime = parseLong(saveFilterScreen.getFreshAirUse());
        exhaustTime = parseLong(saveFilterScreen.getExhaustUse());
        circle1UseTime = parseLong(saveFilterScreen.getCircle1Use());
        circle2UseTime = parseLong(saveFilterScreen.getCircle2Use());

        if (latestFreshAirLevel > 0) {
            if (saveFilterScreen.isFreshAirUseTime()) {
                windUseTime += 15;
            }
            if (saveFilterScreen.isExhaustUseTime()) {
                exhaustTime += 15;
            }
            saveFilterScreen.setFreshAirUse(String.valueOf(windUseTime));
            saveFilterScreen.setExhaustUse(String.valueOf(exhaustTime));
        }
        if (latestPurifyLevel > 0) {
            if (saveFilterScreen.isCircle1UseTime()) {
                circle1UseTime += 15;
            }
            if (saveFilterScreen.isCircle2UseTime()) {
                circle2UseTime += 15;
            }
            saveFilterScreen.setCircle1Use(String.valueOf(circle1UseTime));
            saveFilterScreen.setCircle2Use(String.valueOf(circle2UseTime));
        }
        MySpUtil.setParam(getActivity(), MySpUtil.FILTER_SCREEN_DATA, new Gson().toJson(saveFilterScreen));
    }

    private double calculateFilterUsedRate(int useTimeSeconds, int changeTimeSeconds) {
        if (changeTimeSeconds == 0) {
            return 0.0;
        }
        BigDecimal rate = new BigDecimal((double) useTimeSeconds / changeTimeSeconds);
        return rate.setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    private int parseInt(String value) {
        if (StringUtils.isNullOrEmpty(value)) {
            return 0;
        }
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }

    private void requestElectricityMeter() {
        try {
            SpDataProcessor.getInstance().send(new MeterCommand(1));
        } catch (Exception e) {
            Log.e(TAG, "requestElectricityMeter: " + e.getMessage());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFitHomeElectricEvent(ElectricityMeterInfo info) {
        if (info == null || getContext() == null || energyValueView == null || energyDropPill == null) {
            return;
        }
        BigDecimal total = info.getTotalElectricity();
        if (total == null) {
            return;
        }
        updateTodayEnergy(total);
    }

    private void updateTodayEnergy(BigDecimal totalElectricity) {
        String today = dateFormat.format(new Date());
        String savedDate = getEnergyString(ENERGY_DATE_KEY);
        BigDecimal todayUsage = getEnergyDecimal(ENERGY_TODAY_USAGE_KEY, BigDecimal.ZERO);
        BigDecimal startTotal = getEnergyDecimal(ENERGY_START_TOTAL_KEY, null);
        BigDecimal lastTotal = getEnergyDecimal(ENERGY_LAST_TOTAL_KEY, null);

        if (StringUtils.isNullOrEmpty(savedDate)) {
            savedDate = today;
            todayUsage = BigDecimal.ZERO;
            startTotal = totalElectricity;
            lastTotal = totalElectricity;
            saveEnergyDayState(savedDate, todayUsage, startTotal, lastTotal);
        } else if (!today.equals(savedDate)) {
            BigDecimal yesterdayUsage = todayUsage == null ? BigDecimal.ZERO : todayUsage;
            MySpUtil.setParam(requireContext(), ENERGY_YESTERDAY_DATE_KEY, savedDate);
            MySpUtil.setParam(requireContext(), ENERGY_YESTERDAY_USAGE_KEY, energyToString(yesterdayUsage));
            MySpUtil.setParam(requireContext(), ENERGY_YESTERDAY_HOURLY_USAGE_KEY,
                    getEnergyString(ENERGY_TODAY_HOURLY_USAGE_KEY));
            MySpUtil.setParam(requireContext(), ENERGY_TODAY_HOURLY_USAGE_KEY, "");
            savedDate = today;
            todayUsage = BigDecimal.ZERO;
            startTotal = totalElectricity;
            lastTotal = totalElectricity;
            saveEnergyDayState(savedDate, todayUsage, startTotal, lastTotal);
        } else {
            if (todayUsage == null) {
                todayUsage = BigDecimal.ZERO;
            }
            if (lastTotal == null) {
                if (startTotal != null && startTotal.compareTo(BigDecimal.ZERO) > 0
                        && totalElectricity.compareTo(startTotal) >= 0) {
                    BigDecimal migratedUsage = totalElectricity.subtract(startTotal);
                    if (migratedUsage.compareTo(todayUsage) > 0) {
                        todayUsage = migratedUsage;
                    }
                } else {
                    startTotal = totalElectricity;
                }
            } else if (totalElectricity.compareTo(lastTotal) >= 0) {
                BigDecimal delta = totalElectricity.subtract(lastTotal);
                if (delta.compareTo(ENERGY_MAX_SINGLE_DELTA) <= 0) {
                    todayUsage = todayUsage.add(delta);
                } else {
                    startTotal = totalElectricity;
                }
            } else {
                startTotal = totalElectricity;
            }
            lastTotal = totalElectricity;
            saveEnergyDayState(savedDate, todayUsage, startTotal, lastTotal);
        }

        updateTodayHourlyUsage(todayUsage);
        todayUsage = todayUsage.setScale(1, BigDecimal.ROUND_DOWN);
        energyValueView.setText(todayUsage.toPlainString() + "度");
        renderEnergyCompare(todayUsage);
    }

    private void saveEnergyDayState(String date, BigDecimal todayUsage, BigDecimal startTotal, BigDecimal lastTotal) {
        MySpUtil.setParam(requireContext(), ENERGY_DATE_KEY, date);
        MySpUtil.setParam(requireContext(), ENERGY_TODAY_USAGE_KEY, energyToString(todayUsage));
        MySpUtil.setParam(requireContext(), ENERGY_START_TOTAL_KEY, energyToString(startTotal));
        MySpUtil.setParam(requireContext(), ENERGY_LAST_TOTAL_KEY, energyToString(lastTotal));
    }

    private void renderEnergyCompare(BigDecimal todayUsage) {
        BigDecimal compareBase = getYesterdaySameHourUsage();
        if (compareBase.compareTo(new BigDecimal("1.0")) < 0) {
            energyDropPill.setText("--%");
            energyDropPill.setTextColor(Color.parseColor(warmTheme ? "#4C8D68" : "#34C759"));
            return;
        }
        BigDecimal diffRate = todayUsage.subtract(compareBase)
                .multiply(new BigDecimal("100"))
                .divide(compareBase, 0, BigDecimal.ROUND_HALF_UP);
        int percent = diffRate.intValue();
        percent = Math.max(-99, Math.min(99, percent));
        energyDropPill.setText((percent > 0 ? "+" : "") + percent + "%");
        energyDropPill.setTextColor(Color.parseColor(percent > 0 ? "#FF6B4A" : (warmTheme ? "#4C8D68" : "#34C759")));
    }

    private BigDecimal migrateOldTodayUsage(BigDecimal totalElectricity) {
        String savedDate = String.valueOf(MySpUtil.getParam(requireContext(), ENERGY_DATE_KEY, ""));
        if (!dateFormat.format(new Date()).equals(savedDate)) {
            return BigDecimal.ZERO;
        }
        float oldStart = (float) MySpUtil.getParam(requireContext(), ENERGY_START_TOTAL_KEY, 0f);
        if (oldStart <= 0f) {
            return BigDecimal.ZERO;
        }
        BigDecimal migrated = totalElectricity.subtract(new BigDecimal(Float.toString(oldStart)));
        return migrated.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : migrated;
    }

    private BigDecimal getYesterdaySameHourUsage() {
        BigDecimal[] yesterdayHourly = parseHourlyUsage(String.valueOf(
                getEnergyString(ENERGY_YESTERDAY_HOURLY_USAGE_KEY)));
        int hour = currentHour();
        if (hour >= 0 && hour < yesterdayHourly.length && yesterdayHourly[hour].compareTo(BigDecimal.ZERO) > 0) {
            return yesterdayHourly[hour];
        }
        return getEnergyDecimal(ENERGY_YESTERDAY_USAGE_KEY, BigDecimal.ZERO);
    }

    private void updateTodayHourlyUsage(BigDecimal todayUsage) {
        BigDecimal[] hourly = parseHourlyUsage(String.valueOf(
                getEnergyString(ENERGY_TODAY_HOURLY_USAGE_KEY)));
        int hour = currentHour();
        if (hour >= 0 && hour < hourly.length && todayUsage.compareTo(hourly[hour]) > 0) {
            hourly[hour] = todayUsage;
            MySpUtil.setParam(requireContext(), ENERGY_TODAY_HOURLY_USAGE_KEY, formatHourlyUsage(hourly));
        }
    }

    private int currentHour() {
        try {
            return Integer.parseInt(hourFormat.format(new Date()));
        } catch (Exception e) {
            return 0;
        }
    }

    private BigDecimal[] parseHourlyUsage(String value) {
        BigDecimal[] result = new BigDecimal[24];
        for (int i = 0; i < result.length; i++) {
            result[i] = BigDecimal.ZERO;
        }
        if (StringUtils.isNullOrEmpty(value)) {
            return result;
        }
        String[] parts = value.split(",");
        for (int i = 0; i < parts.length && i < result.length; i++) {
            result[i] = parseEnergyDecimal(parts[i], BigDecimal.ZERO);
        }
        return result;
    }

    private String formatHourlyUsage(BigDecimal[] values) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(energyToString(values[i]));
        }
        return builder.toString();
    }

    private BigDecimal getEnergyDecimal(String key, BigDecimal defaultValue) {
        try {
            return parseEnergyDecimal(getEnergyString(key), defaultValue);
        } catch (ClassCastException e) {
            try {
                float oldValue = (float) MySpUtil.getParam(requireContext(), key, 0f);
                return new BigDecimal(Float.toString(oldValue));
            } catch (Exception ignored) {
                return defaultValue;
            }
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String getEnergyString(String key) {
        String value = String.valueOf(MySpUtil.getParam(requireContext(), key, ""));
        if (!StringUtils.isNullOrEmpty(value)) {
            return value;
        }
        return String.valueOf(MySpUtil.getParam(requireContext(), ENERGY_OLD_KEY_PREFIX + key, ""));
    }

    private BigDecimal parseEnergyDecimal(String value, BigDecimal defaultValue) {
        if (StringUtils.isNullOrEmpty(value)) {
            return defaultValue;
        }
        try {
            return new BigDecimal(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String energyToString(BigDecimal value) {
        return value == null ? "0" : value.setScale(3, BigDecimal.ROUND_DOWN).toPlainString();
    }

    private long parseLong(String value) {
        if (StringUtils.isNullOrEmpty(value)) {
            return 0;
        }
        try {
            return Long.parseLong(value);
        } catch (Exception e) {
            return 0;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFitHomeRoomEvent(EnvironmentDataInfo info) {
        if (info == null || getContext() == null) {
            return;
        }
        HyApplication.setRoomError(info.getRoomError());
        List<RoomInfo> rooms = info.getRoomData(requireContext());
        renderProject2IndoorState(rooms);
    }

    private void renderProject2IndoorState(List<RoomInfo> rooms) {
        if (rooms == null || rooms.isEmpty()) {
            return;
        }
        List<RoomInfo> displayRooms = new ArrayList<>(rooms);
        sortRoomsForTemp(displayRooms);
        RoomInfo tempRoom = firstNonZeroTempRoom(displayRooms);
        int temp = tempRoom == null ? displayRooms.get(0).getTemp() : tempRoom.getTemp();

        Collections.sort(displayRooms, new Comparator<RoomInfo>() {
            @Override
            public int compare(RoomInfo left, RoomInfo right) {
                return right.getHumidity() - left.getHumidity();
            }
        });
        int humidity = displayRooms.get(0).getHumidity();

        Collections.sort(displayRooms, new Comparator<RoomInfo>() {
            @Override
            public int compare(RoomInfo left, RoomInfo right) {
                return right.getCo2() - left.getCo2();
            }
        });
        int co2 = displayRooms.get(0).getCo2();

        Collections.sort(displayRooms, new Comparator<RoomInfo>() {
            @Override
            public int compare(RoomInfo left, RoomInfo right) {
                return right.getPm() - left.getPm();
            }
        });
        int pm = displayRooms.get(0).getPm();

        indoorTempView.setText(String.valueOf(temp));
        indoorHumidityView.setText(String.valueOf(humidity));
        pm25View.setText(String.valueOf(pm));
        co2View.setText(String.valueOf(co2));
        indoorQualityView.setText(indoorQualityText(pm));
        if (careTempView != null) {
            careTempView.setText(String.valueOf(temp));
            careHumidityView.setText(String.valueOf(humidity));
            carePm25View.setText(String.valueOf(pm));
            careCo2View.setText(String.valueOf(co2));
        }

        HDTopic hdTopic = MqttUploadManager.getInstance().getmHDTopic();
        hdTopic.setInTemp((byte) temp);
        hdTopic.setInHumidity((byte) humidity);
        hdTopic.setInCo2(ByteUtils.int16ToByteArray(co2));
        hdTopic.setInPM(ByteUtils.int16ToByteArray(pm));
    }

    private void sortRoomsForTemp(List<RoomInfo> rooms) {
        SaveControlInfo saveControlInfo = MySpUtil.getControlData(requireContext());
        if (saveControlInfo == null
                || StringUtils.isNullOrEmpty(saveControlInfo.getTempMin())
                || StringUtils.isNullOrEmpty(saveControlInfo.getTempMax())) {
            return;
        }
        int min = Integer.parseInt(saveControlInfo.getTempMin()) * 10;
        int max = Integer.parseInt(saveControlInfo.getTempMax()) * 10;
        int outTemp = HyApplication.getOutTemp().intValue();
        boolean chooseMin;
        if (outTemp < min) {
            chooseMin = true;
        } else if (outTemp > max) {
            chooseMin = false;
        } else {
            BigDecimal middle = new BigDecimal(saveControlInfo.getTempMin())
                    .add(new BigDecimal(saveControlInfo.getTempMax()))
                    .divide(new BigDecimal(2), 1, BigDecimal.ROUND_DOWN)
                    .multiply(new BigDecimal(10))
                    .setScale(0, BigDecimal.ROUND_DOWN);
            chooseMin = outTemp < middle.intValue();
        }
        Collections.sort(rooms, new Comparator<RoomInfo>() {
            @Override
            public int compare(RoomInfo left, RoomInfo right) {
                return chooseMin ? left.getTemp() - right.getTemp() : right.getTemp() - left.getTemp();
            }
        });
    }

    private RoomInfo firstNonZeroTempRoom(List<RoomInfo> rooms) {
        for (RoomInfo room : rooms) {
            if (room.getTemp() != 0) {
                return room;
            }
        }
        return rooms.isEmpty() ? null : rooms.get(0);
    }

    private String indoorQualityText(int pm) {
        if (pm <= 35) {
            return "\u4f18";
        }
        if (pm <= 75) {
            return "\u826f";
        }
        if (pm <= 115) {
            return "\u8f7b\u5ea6\u6c61\u67d3";
        }
        if (pm <= 150) {
            return "\u4e2d\u5ea6\u6c61\u67d3";
        }
        if (pm <= 250) {
            return "\u91cd\u5ea6\u6c61\u67d3";
        }
        return "\u4e25\u91cd\u6c61\u67d3";
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFitHomeMainControlInfo(MainControlInfo info) {
        if (info == null) {
            return;
        }
        String version = info.softwareVersion();
        if (StringUtils.isNullOrEmpty(version)) {
            return;
        }
        HyApplication.setControlVersion(version);
        EventBus.getDefault().post(new VersionUpdateEvent(2, version));
        updateFooterStatusText(FOOTER_BASE_TEXT);
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWeatherDataUpdate(WeatherDataEvent event) {
        if (event == null || outdoorTempView == null) {
            return;
        }
        outdoorTempView.setText(event.getOutdoorTemp() + "\u2103");
        weatherIconView.setImageResource(getWeatherImageResId(event.getWeatherCode()));
        outdoorStatusView.setText("\u5ba4\u5916");
    }

    private void receiveData(byte[] data) {
        if (isOtaBlocked()) {
            return;
        }
        if (trySendRawMainControlFrame(data)) {
            return;
        }
        if (data == null || data.length < 7) {
            return;
        }
        int type = data[2] & 0xFF;
        int bodyEnd = data.length >= 9 ? data.length - 2 : 7;
        byte[] receiveData = Arrays.copyOfRange(data, 7, Math.max(7, bodyEnd));
        if (trySendRawMainControlFrame(receiveData)) {
            return;
        }
        if (type == 0x0D) {
            handleWeatherData(receiveData);
            return;
        }
        handleCloudMainControlCommand(type, receiveData);
    }
    private void handleWeatherData(byte[] receiveData) {
        if (receiveData == null || receiveData.length != 8) {
            return;
        }
        int outdoorTemp = receiveData[0];
        int outdoorHumidity = receiveData[1] & 0xFF;
        int pm25Raw = ByteUtils.byteArrayToInt(receiveData, 2, 2, ByteUtils.Endian.Little);
        double outdoorPM25 = pm25Raw / 10.0;
        int weatherCode = receiveData[4] & 0xFF;
        int windDirectionCode = receiveData[5] & 0xFF;
        int windForce = receiveData[6] & 0xFF;
        int pollutionLevel = receiveData[7] & 0xFF;

        Log.i(TAG, "weather frame received, temp=" + outdoorTemp + ", code=" + weatherCode);
        WeatherDataEvent event = new WeatherDataEvent(outdoorTemp, outdoorHumidity, outdoorPM25,
                weatherCode, windDirectionCode, windForce, pollutionLevel);
        onWeatherDataUpdate(event);
        EventBus.getDefault().post(event);
    }

    private boolean trySendRawMainControlFrame(byte[] frame) {
        if (isOtaBlocked()) {
            Log.i(TAG, "cloud raw frame ignored because OTA is active");
            return true;
        }
        if (!isProject2MainControlFrame(frame)) {
            return false;
        }
        if ((frame[1] & 0xFF) != 0x03) {
            Log.i(TAG, "cloud raw main-control write ignored on FitHome, function=0x"
                    + Integer.toHexString(frame[1] & 0xFF));
            return false;
        }
        if (viewModel != null) {
            viewModel.writeRawMainControlFrame(frame);
            Log.i(TAG, "cloud raw main-control query forwarded");
        }
        return true;
    }

    private boolean isProject2MainControlFrame(byte[] frame) {
        return frame != null && frame.length >= 2 && (frame[0] & 0xFF) == 0x00;
    }

    private void handleCloudMainControlCommand(int type, byte[] payload) {
        if (viewModel == null || isOtaBlocked()) {
            return;
        }
        int value = cloudValue(payload);
        switch (type) {
            case 0x01:
                writeTempControlSwitch(value == 1);
                break;
            case 0x02:
                applyCloudFanLevel(value);
                break;
            case 0x03:
                applyCloudCircleLevel(value);
                break;
            case 0x04:
                writeControlMode(false, value == 0, 0);
                break;
            case 0x05:
                writeCloudTempMin(value);
                break;
            case 0x06:
                writeCloudTempMax(value);
                break;
            case 0x09:
                writeCloudHumidity(value, null);
                break;
            case 0x0B:
                writeHumiditySwitch(value == 1);
                break;
            case 0x12:
                updateFilterChange(payload);
                break;
            case 0x13:
                updateFilterPressure(payload);
                break;
            case 0x15:
                writeAirThreshold(FunctionObject.SET_CO2_VALUE, payload);
                break;
            case 0x16:
                writeAirThreshold(FunctionObject.SET_PM_VALUE, payload);
                break;
            case 0x20:
                writeCloudHumidity(null, value);
                break;
            case 0x21:
                applyCloudTargetTemp(value);
                break;
            case 0x22:
                writeHumiTemp(value);
                break;
            case 0x5A:
                applyCloudSystemSwitch(value);
                break;
            case 0x5B:
                applyCloudManualMode(value);
                break;
            case 0x5C:
                HyApplication.isLocking = value == 1;
                break;
            case 0x5D:
                applyCloudUiMode(value);
                break;
            case 0x5E:
                if (value == 1) {
                    OTARequestCommand otaRequestCommand = new OTARequestCommand(3);
                    SpDataProcessor.getInstance().send3(otaRequestCommand);
                }
                break;
            case 0x5F:
                applyCloudSeasonMode(value);
                break;
            default:
                Log.i(TAG, "cloud command ignored on FitHome, type=0x"
                        + Integer.toHexString(type));
                break;
        }
    }

    private void applyCloudFanLevel(int level) {
        int fanLevel = clamp(level, 0, 3);
        writeFanPair((byte) 0x00, fanLevel);
        MqttUploadManager.getInstance().getmHDTopic().setWindStatus((byte) fanLevel);
        updateFanSelection(fanButtonForLevel(fanLevel));
    }

    private void applyCloudCircleLevel(int level) {
        int fanLevel = clamp(level, 0, 3);
        writeFanPair((byte) 0x02, fanLevel);
        MqttUploadManager.getInstance().getmHDTopic().setCircleStatus((byte) fanLevel);
        updateFanSelection(fanButtonForLevel(fanLevel));
    }

    private void applyCloudTargetTemp(int value) {
        int temp = normalizeCloudTemp(value);
        targetTemp = temp;
        updateTargetViews();
        selectScene("custom");
        viewModel.writeCustomTempTarget(temp, winterThemeSelected);
    }

    private void applyCloudTargetHumidity(int value) {
        int humidity = clamp(value, 30, 80);
        boolean decreasing = humidity < targetHumidity;
        targetHumidity = humidity;
        updateTargetViews();
        selectScene("custom");
        viewModel.writeCustomHumidityTarget(humidity, decreasing);
    }

    private void applyCloudUiMode(int value) {
        boolean careMode = value == 1;
        if (getContext() != null) {
            MySpUtil.setParam(requireContext(), MySpUtil.CARE_MODE, careMode);
        }
        selectMode(!careMode);
    }

    private void applyCloudSeasonMode(int value) {
        selectSeason(value == 1);
    }

    private int normalizeCloudTemp(int value) {
        int temp = value;
        if (temp > 100 && temp % 10 == 0) {
            temp = temp / 10;
        }
        return clamp(temp, 16, 32);
    }

    private int cloudValue(byte[] payload) {
        if (payload == null || payload.length == 0) {
            return 0;
        }
        return ByteUtils.byteArrayToInt(payload, 0, payload.length, ByteUtils.Endian.Little);
    }

    private void writeFanPair(byte type, int level) {
        writeFanLevel(type, level);
        if (type == 0x00) {
            writeFanLevel((byte) 0x01, level);
        } else if (type == 0x02) {
            writeFanLevel((byte) 0x03, level);
        }
    }

    private void writeFanLevel(byte type, int level) {
        FanCommand command = new FanCommand(FunctionObject.SET_SPEED);
        command.setData(new byte[]{type, (byte) clamp(level, 0, 3)});
        SpDataProcessor.getInstance().send(command);
    }

    private void writeControlMode(boolean timing, boolean manual, int mode) {
        ControlCommand command = new ControlCommand(FunctionObject.SET_CONTROL_MODE);
        command.setData(new byte[]{(byte) (timing ? 1 : 0), (byte) (manual ? 1 : 0), (byte) clamp(mode, 0, 3)});
        SpDataProcessor.getInstance().send(command);
    }

    private void writeTempControlSwitch(boolean enabled) {
        ControlCommand command = new ControlCommand(FunctionObject.GET_TEMP_SWITCH);
        command.setData(new byte[]{(byte) (enabled ? 1 : 0)});
        SpDataProcessor.getInstance().send(command);
    }

    private void writeHumiditySwitch(boolean enabled) {
        ControlCommand command = new ControlCommand(FunctionObject.SET_HUMI_SWITCH);
        command.setData(new byte[]{(byte) (enabled ? 1 : 0)});
        SpDataProcessor.getInstance().send(command);
        MqttUploadManager.getInstance().getmHDTopic().setDeHumiditySwitch((byte) (enabled ? 1 : 0));
    }

    private void writeCloudTempMin(int value) {
        SaveControlInfo controlInfo = MySpUtil.getControlData(requireContext());
        int min = normalizeCloudTemp(value);
        int max = controlInfo == null || StringUtils.isNullOrEmpty(controlInfo.getTempMax())
                ? min + 3 : parseInt(controlInfo.getTempMax());
        writeTempSection(Math.min(min, max - 3), Math.max(max, min + 3));
    }

    private void writeCloudTempMax(int value) {
        SaveControlInfo controlInfo = MySpUtil.getControlData(requireContext());
        int max = normalizeCloudTemp(value);
        int min = controlInfo == null || StringUtils.isNullOrEmpty(controlInfo.getTempMin())
                ? max - 3 : parseInt(controlInfo.getTempMin());
        writeTempSection(Math.min(min, max - 3), Math.max(max, min + 3));
    }

    private void writeTempSection(int min, int max) {
        int safeMin = clamp(min, 16, 30);
        int safeMax = clamp(max, 16, 30);
        if (safeMax - safeMin < 3) {
            if (safeMax >= 27) {
                safeMin = safeMax - 3;
            } else {
                safeMax = safeMin + 3;
            }
        }
        ControlCommand command = new ControlCommand(FunctionObject.SET_TEMP_SECTION);
        command.setData(ByteUtils.splicingBytes(ByteUtils.int16ToByteArray(safeMax * 10), ByteUtils.int16ToByteArray(safeMin * 10)));
        SpDataProcessor.getInstance().send(command);
    }

    private void writeCloudHumidity(Integer dehumidify, Integer humidify) {
        SaveControlInfo controlInfo = MySpUtil.getControlData(requireContext());
        int dehumidifyValue = dehumidify == null
                ? parseInt(controlInfo == null ? "" : controlInfo.getHumidity())
                : dehumidify;
        int humidifyValue = humidify == null
                ? (controlInfo == null ? 35 : controlInfo.getHumidity1())
                : humidify;
        dehumidifyValue = clamp(dehumidifyValue, 45, 99);
        humidifyValue = clamp(humidifyValue, 35, 80);
        ByteBuffer buffer = ByteBuffer.allocate(3);
        buffer.put(ByteUtils.int16ToByteArray(dehumidifyValue));
        buffer.put((byte) humidifyValue);
        ControlCommand command = new ControlCommand(FunctionObject.SET_HUMIDITY);
        command.setData(buffer.array());
        SpDataProcessor.getInstance().send(command);
    }

    private void writeHumiTemp(int value) {
        ControlCommand command = new ControlCommand(FunctionObject.SET_HUMI_TEMP);
        command.setData(new byte[]{(byte) value});
        SpDataProcessor.getInstance().send(command);
    }

    private void writeAirThreshold(int function, byte[] payload) {
        if (payload == null || payload.length < 6) {
            return;
        }
        ByteBuffer buffer = ByteBuffer.allocate(6);
        buffer.put(ByteUtils.changeBytes(Arrays.copyOfRange(payload, 0, 2)));
        buffer.put(ByteUtils.changeBytes(Arrays.copyOfRange(payload, 2, 4)));
        buffer.put(ByteUtils.changeBytes(Arrays.copyOfRange(payload, 4, 6)));
        EnvironmentCommand command = new EnvironmentCommand(function);
        command.setData(buffer.array());
        SpDataProcessor.getInstance().send(command);
        EventBus.getDefault().post("update");
    }

    private void updateFilterChange(byte[] payload) {
        updateFilterSetting(payload, true);
    }

    private void updateFilterPressure(byte[] payload) {
        updateFilterSetting(payload, false);
    }

    private void updateFilterSetting(byte[] payload, boolean changeCycle) {
        if (payload == null || payload.length < 3 || getContext() == null) {
            return;
        }
        SaveFilterScreen data = MySpUtil.getFilterScreen(requireContext());
        int type = payload[0] & 0xFF;
        int value = ByteUtils.byteArrayToInt(Arrays.copyOfRange(payload, 1, 3), 0, 2, ByteUtils.Endian.Little);
        if (changeCycle) {
            if (type == 0) data.setFreshAirChange(String.valueOf(value));
            else if (type == 1) data.setExhaustChange(String.valueOf(value));
            else if (type == 2) data.setCircle1Change(String.valueOf(value));
            else if (type == 3) data.setCircle2Change(String.valueOf(value));
        } else {
            if (type == 0) data.setFreshAirPressure(String.valueOf(value));
            else if (type == 1) data.setExhaustPressure(String.valueOf(value));
            else if (type == 2) data.setCircle1Pressure(String.valueOf(value));
            else if (type == 3) data.setCircle2Pressure(String.valueOf(value));
        }
        MySpUtil.setParam(requireContext(), MySpUtil.FILTER_SCREEN_DATA, new Gson().toJson(data));
    }

    private void applyCloudSystemSwitch(int value) {
        boolean open = value != 0;
        MqttUploadManager.getInstance().getmHxTopic().setSystemSwitch((byte) (open ? 1 : 0));
        if (open) {
            writeControlMode(false, false, 0);
        } else {
            writeControlMode(false, true, 0);
            writeFanPair((byte) 0x00, 0);
            writeFanPair((byte) 0x02, 0);
            writeTempControlSwitch(false);
        }
    }

    private void applyCloudManualMode(int value) {
        int mode = clamp(value, 0, 3);
        if (mode == 3) {
            writeHumiditySwitch(true);
        }
        writeControlMode(false, true, mode);
        if (mode > 0) {
            writeFanPair((byte) 0x02, 2);
            writeTempControlSwitch(true);
        }
        MqttUploadManager.getInstance().getmHxTopic().setAdditionalManualMode((byte) mode);
    }

    private TextView requireViewById(int id) {
        View root = getView();
        if (root == null) {
            throw new IllegalStateException("View is not created");
        }
        return root.findViewById(id);
    }

    private void initClicks(View root) {
        adminIconView.setOnClickListener(v -> showPasswordDialog());
        tempMinusButton.setOnClickListener(v -> adjustTemp(-1));
        tempPlusButton.setOnClickListener(v -> adjustTemp(1));
        humidityMinusButton.setOnClickListener(v -> adjustHumidity(-1));
        humidityPlusButton.setOnClickListener(v -> adjustHumidity(1));
        fanOffButton.setOnClickListener(v -> onFanClicked(fanOffButton));
        fanLowButton.setOnClickListener(v -> onFanClicked(fanLowButton));
        fanMidButton.setOnClickListener(v -> onFanClicked(fanMidButton));
        fanHighButton.setOnClickListener(v -> onFanClicked(fanHighButton));
        sceneEcoButton.setOnClickListener(v -> applyScene("eco"));
        sceneComfortButton.setOnClickListener(v -> applyScene("comfort"));
        sceneVacationButton.setOnClickListener(v -> applyScene("vacation"));
        sceneCustomButton.setOnClickListener(v -> selectScene("custom"));
        careSceneEcoButton.setOnClickListener(v -> applyScene("eco"));
        careSceneComfortButton.setOnClickListener(v -> applyScene("comfort"));
        careSceneVacationButton.setOnClickListener(v -> applyScene("vacation"));
        careSceneCustomButton.setOnClickListener(v -> selectScene("custom"));
        classicModeButton.setOnClickListener(v -> selectMode(true));
        careModeButton.setOnClickListener(v -> selectMode(false));
        root.findViewById(R.id.hit_fit_home_classic_mode).setOnClickListener(v -> selectMode(true));
        root.findViewById(R.id.hit_fit_home_care_mode).setOnClickListener(v -> selectMode(false));
        middleCard.setOnTouchListener((v, event) -> {
            if (event.getAction() != MotionEvent.ACTION_UP || event.getY() > dp(92)) {
                return false;
            }
            selectMode(event.getX() < v.getWidth() / 2f);
            return true;
        });
        summerButton.setOnClickListener(v -> selectSeason(false));
        winterButton.setOnClickListener(v -> selectSeason(true));
    }

    private void showPasswordDialog() {
        if (!isAdded()) {
            return;
        }
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.manager_pwd_dialog, null, false);
        view.setVisibility(View.VISIBLE);
        AlertDialog pwdDialog = new AlertDialog.Builder(requireContext()).setView(view).create();
        Button sure = view.findViewById(R.id.bt_pwd_sure);
        EditText passwordEdit = view.findViewById(R.id.et_manager_pwd);
        sure.setOnClickListener(v -> {
            String password = passwordEdit.getText().toString().trim();
            if (!StringUtils.isNullOrEmpty(password) && password.equals(StringUtils.INIT_PASSWORD)) {
                Intent intent = new Intent(requireContext(), ManagerActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                pwdDialog.dismiss();
            } else {
                ToastUtil.showToast(requireContext(), "请输入正确的管理员密码！");
            }
        });
        pwdDialog.show();
        if (pwdDialog.getWindow() != null) {
            pwdDialog.getWindow().setLayout(550, ViewGroup.LayoutParams.WRAP_CONTENT);
            pwdDialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            pwdDialog.getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> {
                int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
                uiOptions |= 0x00001000;
                pwdDialog.getWindow().getDecorView().setSystemUiVisibility(uiOptions);
            });
        }
    }

    private void scalePanelToScreen(View root) {
        if (panel == null || root.getWidth() == 0 || root.getHeight() == 0) {
            return;
        }
        ViewGroup.LayoutParams params = panel.getLayoutParams();
        if (params.width <= 0 || params.height <= 0) {
            return;
        }
        float scale = Math.min(root.getWidth() / (float) params.width, root.getHeight() / (float) params.height);
        panel.setPivotX(params.width / 2f);
        panel.setPivotY(params.height / 2f);
        panel.setScaleX(scale);
        panel.setScaleY(scale);
    }

    private void adjustTemp(int delta) {
        if (isOtaBlocked()) {
            return;
        }
        targetTemp = clamp(targetTemp + delta, 16, 32);
        updateTargetViews();
        selectScene("custom");
        sendTargetTemp(delta);
    }

    private void adjustHumidity(int delta) {
        if (isOtaBlocked()) {
            return;
        }
        targetHumidity = clamp(targetHumidity + delta, 30, 80);
        updateTargetViews();
        selectScene("custom");
        sendTargetHumidity(delta);
    }

    private void applyScene(String scene) {
        if (isOtaBlocked()) {
            return;
        }
        targetTemp = getSceneTemp(scene);
        targetHumidity = getSceneHumidity(scene);
        updateTargetViews();
        selectScene(scene);
        sendSceneTarget();
    }

    private void updateTargetViews() {
        targetTempView.setText(String.valueOf(targetTemp));
        targetHumidityView.setText(String.valueOf(targetHumidity));
        updateScenePresetTexts();
        sceneCustomPreset.setText(targetTemp + "℃/" + targetHumidity + "%");
        updateThemeForTargetTemp();
    }

    private void updateScenePresetTexts() {
        sceneEcoPreset.setText(formatScenePreset("eco"));
        sceneComfortPreset.setText(formatScenePreset("comfort"));
        sceneVacationPreset.setText(formatScenePreset("vacation"));
    }

    private String formatScenePreset(String scene) {
        return getSceneTemp(scene) + "℃/" + getSceneHumidity(scene) + "%";
    }

    private int getSceneTemp(String scene) {
        if (winterThemeSelected) {
            if ("eco".equals(scene)) {
                return 20;
            }
            if ("comfort".equals(scene)) {
                return 23;
            }
            if ("vacation".equals(scene)) {
                return 18;
            }
        } else {
            if ("eco".equals(scene)) {
                return 26;
            }
            if ("comfort".equals(scene)) {
                return 25;
            }
            if ("vacation".equals(scene)) {
                return 28;
            }
        }
        return targetTemp;
    }

    private int getSceneHumidity(String scene) {
        if (winterThemeSelected) {
            if ("eco".equals(scene)) {
                return 30;
            }
            if ("comfort".equals(scene)) {
                return 40;
            }
            if ("vacation".equals(scene)) {
                return 30;
            }
        } else {
            if ("eco".equals(scene)) {
                return 60;
            }
            if ("comfort".equals(scene)) {
                return 50;
            }
            if ("vacation".equals(scene)) {
                return 70;
            }
        }
        return targetHumidity;
    }

    private void selectFan(View selected) {
        updateFanSelection(selected);
    }

    private void onFanClicked(View selected) {
        if (isOtaBlocked()) {
            return;
        }
        updateFanSelection(selected);
        selectScene("custom");
        sendFanLevel(fanLevelForView(selected));
    }

    private void updateFanSelection(View selected) {
        selectedFanButton = selected;
        setFanSelected(fanOffButton, selected == fanOffButton);
        setFanSelected(fanLowButton, selected == fanLowButton);
        setFanSelected(fanMidButton, selected == fanMidButton);
        setFanSelected(fanHighButton, selected == fanHighButton);
        setFanLabelSelected(fanOffLabel, selected == fanOffButton);
        setFanLabelSelected(fanLowLabel, selected == fanLowButton);
        setFanLabelSelected(fanMidLabel, selected == fanMidButton);
        setFanLabelSelected(fanHighLabel, selected == fanHighButton);
    }

    private void selectScene(String scene) {
        selectedScene = scene;
        boolean eco = "eco".equals(scene);
        boolean comfort = "comfort".equals(scene);
        boolean vacation = "vacation".equals(scene);
        boolean custom = "custom".equals(scene);
        sceneEcoButton.setBackgroundResource(eco ? R.drawable.fh_scene_active : R.drawable.fh_scene);
        sceneComfortButton.setBackgroundResource(comfort ? R.drawable.fh_scene_active : R.drawable.fh_scene);
        sceneVacationButton.setBackgroundResource(vacation ? R.drawable.fh_scene_active : R.drawable.fh_scene);
        sceneCustomButton.setBackgroundResource(custom ? R.drawable.fh_scene_active : R.drawable.fh_scene);
        sceneEcoTitle.setText("节能");
        sceneComfortTitle.setText("舒适");
        sceneVacationTitle.setText("度假");
        sceneCustomTitle.setText("自定义");
        updateScenePresetTexts();
        sceneCustomPreset.setText(targetTemp + "℃/" + targetHumidity + "%");
        sceneEcoCheck.setText(eco ? "✓" : "");
        sceneComfortCheck.setText(comfort ? "✓" : "");
        sceneVacationCheck.setText(vacation ? "✓" : "");
        sceneCustomCheck.setText(custom ? "✓" : "");
        sceneEcoPreset.setTextColor(Color.parseColor(eco ? "#5B9AFF" : "#B8C5D8"));
        sceneComfortPreset.setTextColor(Color.parseColor(comfort ? "#5B9AFF" : "#B8C5D8"));
        sceneVacationPreset.setTextColor(Color.parseColor(vacation ? "#5B9AFF" : "#B8C5D8"));
        sceneCustomPreset.setTextColor(Color.parseColor(custom ? "#5B9AFF" : "#B8C5D8"));
        updateThemeForTargetTemp();
    }

    private void setChipSelected(TextView view, boolean selected, int selectedBackground) {
        view.setBackgroundResource(selected ? selectedBackground : R.drawable.fh_chip_normal);
        view.setTextColor(selected ? Color.WHITE : Color.parseColor("#99FFFFFF"));
    }

    private void setFanSelected(View view, boolean selected) {
        view.setBackgroundColor(Color.TRANSPARENT);
    }

    private void setCardBackground(View view, boolean warm, String edgeColor) {
        if (warm) {
            view.setBackgroundResource(R.drawable.fh_card_warm);
            return;
        }
        view.setBackground(new TopEdgeCardDrawable(
                dp(18),
                dp(3),
                Color.parseColor("#223044"),
                Color.parseColor("#26FFFFFF"),
                Color.parseColor(edgeColor)
        ));
    }

    private void updateThemeForTargetTemp() {
        boolean shouldWarm = winterThemeSelected;
        warmTheme = shouldWarm;
        if (rootView == null || leftCard == null || middleCard == null || rightCard == null) {
            return;
        }
        rootView.setBackgroundResource(shouldWarm ? R.drawable.fh_page_warm : R.drawable.fh_page);
        panel.setBackgroundResource(shouldWarm ? R.drawable.fh_page_warm : R.drawable.fh_panel_summer);
        adminIconView.setImageResource(shouldWarm ? R.drawable.fit_home_guanli : R.drawable.fit_home_guanli_summer);
        wifiIconView.setImageResource(shouldWarm ? R.drawable.fit_home_wifi : R.drawable.fit_home_wifi_summer);
        setCardBackground(leftCard, shouldWarm, "#34C759");
        setCardBackground(middleCard, shouldWarm, "#5B9AFF");
        setCardBackground(rightCard, shouldWarm, "#FF9F0A");
        fanPanel.setBackgroundResource(shouldWarm ? R.drawable.fh_fan_warm : R.drawable.fh_fan);
        fanTitleView.setTextColor(Color.parseColor(shouldWarm ? "#6E5E4E" : "#99FFFFFF"));
        int metricBackground = shouldWarm ? R.drawable.fh_metric_warm : R.drawable.fh_metric;
        filterCard.setBackgroundResource(metricBackground);
        energyCard.setBackgroundResource(metricBackground);
        if (careTempCard != null) {
            int careMetricBackground = shouldWarm ? R.drawable.fh_care_metric_warm : R.drawable.fh_care_metric;
            careTempCard.setBackgroundResource(careMetricBackground);
            careHumidityCard.setBackgroundResource(careMetricBackground);
            carePm25Card.setBackgroundResource(careMetricBackground);
            careCo2Card.setBackgroundResource(careMetricBackground);
        }
        filterProgressBar.setProgressDrawable(getResources().getDrawable(shouldWarm ? R.drawable.fh_progress_filter_warm : R.drawable.fh_progress_filter));
        int stepBackground = shouldWarm ? R.drawable.fh_step_press_warm : R.drawable.fh_step_press;
        tempMinusButton.setBackgroundResource(stepBackground);
        tempPlusButton.setBackgroundResource(stepBackground);
        humidityMinusButton.setBackgroundResource(stepBackground);
        humidityPlusButton.setBackgroundResource(stepBackground);
        int commonPill = shouldWarm ? R.drawable.fh_pill_warm : R.drawable.fh_pill_dark;
        outdoorPill.setBackgroundResource(shouldWarm ? R.drawable.fh_outdoor_pill_warm : R.drawable.fh_pill_dark);
        realtimePill.setBackgroundResource(commonPill);
        sceneSwitchPill.setBackgroundResource(commonPill);
        energyDropPill.setBackgroundResource(shouldWarm ? R.drawable.fh_pill_drop_warm : R.drawable.fh_pill_drop_dark);
        realtimePill.setTextColor(Color.parseColor(shouldWarm ? "#7F705F" : "#66FFFFFF"));
        sceneSwitchPill.setTextColor(Color.parseColor(shouldWarm ? "#7F705F" : "#66FFFFFF"));
        energyDropPill.setTextColor(Color.parseColor(shouldWarm ? "#4C8D68" : "#34C759"));
        outdoorStatusView.setTextColor(Color.parseColor(shouldWarm ? "#2C2723" : "#FFFFFF"));
        int primary = Color.parseColor(shouldWarm ? "#2C2723" : "#E6FFFFFF");
        int secondary = Color.parseColor(shouldWarm ? "#8B8177" : "#73FFFFFF");
        for (TextView textView : primaryTextViews) {
            textView.setTextColor(primary);
        }
        for (TextView textView : secondaryTextViews) {
            textView.setTextColor(secondary);
        }
        outdoorStatusView.setText("室外");
        outdoorStatusView.setTextColor(Color.parseColor(shouldWarm ? "#2C2723" : "#FFFFFF"));
        indoorQualityView.setTextColor(Color.WHITE);
        outdoorTempView.setTextColor(Color.parseColor(shouldWarm ? "#2C2723" : "#FFFFFF"));
        int settingTitleColor = Color.parseColor(shouldWarm ? "#7F705F" : "#55FFFFFF");
        int targetUnitColor = Color.parseColor(shouldWarm ? "#7F705F" : "#73FFFFFF");
        indoorTempUnitView.setTextColor(targetUnitColor);
        indoorTempLabelView.setTextColor(settingTitleColor);
        indoorHumidityUnitView.setTextColor(targetUnitColor);
        indoorHumidityLabelView.setTextColor(settingTitleColor);
        pm25LabelView.setTextColor(targetUnitColor);
        pm25UnitView.setTextColor(targetUnitColor);
        co2LabelView.setTextColor(targetUnitColor);
        co2UnitView.setTextColor(targetUnitColor);
        footerStatusView.setTextColor(targetUnitColor);
        tempSettingTitle.setTextColor(settingTitleColor);
        humiditySettingTitle.setTextColor(settingTitleColor);
        targetTempUnitView.setTextColor(targetUnitColor);
        targetHumidityUnitView.setTextColor(targetUnitColor);
        if (careTempLabelView != null) {
            careTempLabelView.setTextColor(settingTitleColor);
            careHumidityLabelView.setTextColor(settingTitleColor);
            careTempUnitView.setTextColor(settingTitleColor);
            careHumidityUnitView.setTextColor(settingTitleColor);
            carePm25LabelView.setTextColor(settingTitleColor);
            careCo2LabelView.setTextColor(settingTitleColor);
            carePm25UnitView.setTextColor(settingTitleColor);
            careCo2UnitView.setTextColor(settingTitleColor);
        }
        applyModeButtons();
        applySeasonButtons();
        setTitleText();
        sceneEcoCheck.setTextColor(Color.parseColor(shouldWarm ? "#A97842" : "#5B9AFF"));
        sceneComfortCheck.setTextColor(Color.parseColor(shouldWarm ? "#A97842" : "#5B9AFF"));
        sceneVacationCheck.setTextColor(Color.parseColor(shouldWarm ? "#A97842" : "#5B9AFF"));
        sceneCustomCheck.setTextColor(Color.parseColor(shouldWarm ? "#A97842" : "#5B9AFF"));
        applySceneBackgrounds();
        if (selectedFanButton != null) {
            selectFan(selectedFanButton);
        }
        applyLayoutMode();
    }

    private void applySceneBackgrounds() {
        boolean eco = "eco".equals(selectedScene);
        boolean comfort = "comfort".equals(selectedScene);
        boolean vacation = "vacation".equals(selectedScene);
        boolean custom = "custom".equals(selectedScene);
        int active = warmTheme ? R.drawable.fh_scene_active_warm : R.drawable.fh_scene_active;
        int normal = warmTheme ? R.drawable.fh_scene_warm : R.drawable.fh_scene;
        sceneEcoButton.setBackgroundResource(eco ? active : normal);
        sceneComfortButton.setBackgroundResource(comfort ? active : normal);
        sceneVacationButton.setBackgroundResource(vacation ? active : normal);
        sceneCustomButton.setBackgroundResource(custom ? active : normal);
        setCareSceneButtonState(careSceneEcoButton, eco);
        setCareSceneButtonState(careSceneComfortButton, comfort);
        setCareSceneButtonState(careSceneVacationButton, vacation);
        setCareSceneButtonState(careSceneCustomButton, custom);
        int selectedColor = Color.parseColor(warmTheme ? "#A97842" : "#5B9AFF");
        int normalColor = Color.parseColor(warmTheme ? "#7F705F" : "#B8C5D8");
        sceneEcoPreset.setTextColor(eco ? selectedColor : normalColor);
        sceneComfortPreset.setTextColor(comfort ? selectedColor : normalColor);
        sceneVacationPreset.setTextColor(vacation ? selectedColor : normalColor);
        sceneCustomPreset.setTextColor(custom ? selectedColor : normalColor);
    }

    private void setCareSceneButtonState(View view, boolean selected) {
        if (view == null) {
            return;
        }
        int targetTextColor = Color.parseColor(warmTheme ? "#2C2723" : "#E6FFFFFF");
        if (selected) {
            view.setBackgroundResource(warmTheme ? R.drawable.fh_fan_label_active_warm : R.drawable.fh_fan_label_active_dark);
        } else {
            view.setBackgroundResource(warmTheme ? R.drawable.fh_fan_label_warm : R.drawable.fh_fan_label);
        }
        setTextColorRecursive(view, targetTextColor);
    }

    private void setFanLabelSelected(View view, boolean selected) {
        if (selected) {
            view.setBackgroundResource(warmTheme ? R.drawable.fh_mode_warm : R.drawable.fh_mode_dark_sel);
            setTextColorRecursive(view, Color.parseColor(warmTheme ? "#2C2723" : "#FFFFFF"));
        } else {
            view.setBackgroundResource(warmTheme ? R.drawable.fh_fan_label_warm : R.drawable.fh_fan_label);
            setTextColorRecursive(view, Color.parseColor(warmTheme ? "#7F705F" : "#99FFFFFF"));
        }
    }

    private void selectMode(boolean classic) {
        if (isOtaBlocked()) {
            return;
        }
        classicModeSelected = classic;
        boolean careMode = !classic;
        if (getContext() != null) {
            MySpUtil.setParam(requireContext(), MySpUtil.CARE_MODE, careMode);
        }
        MqttUploadManager.getInstance().getmHxTopic().setSystemInterfaceMode((byte) (careMode ? 1 : 0));
        applyLayoutMode();
        applyModeButtons();
    }

    private void selectSeason(boolean winter) {
        if (isOtaBlocked()) {
            return;
        }
        winterThemeSelected = winter;
        sendSeasonMode(winter);
        if ("eco".equals(selectedScene) || "comfort".equals(selectedScene) || "vacation".equals(selectedScene)) {
            applyScene(selectedScene);
        } else {
            updateScenePresetTexts();
            updateThemeForTargetTemp();
        }
    }

    private void sendSceneTarget() {
        if (viewModel != null && !isOtaBlocked()) {
            viewModel.writeAutoSceneTarget(targetTemp, targetHumidity, winterThemeSelected);
        }
    }

    private void sendTargetTemp(int delta) {
        if (viewModel != null && !isOtaBlocked()) {
            viewModel.writeCustomTempTarget(targetTemp, winterThemeSelected);
        }
    }

    private void sendTargetHumidity(int delta) {
        if (viewModel != null && !isOtaBlocked()) {
            viewModel.writeCustomHumidityTarget(targetHumidity, delta < 0);
        }
    }

    private void sendFanLevel(int level) {
        if (viewModel != null && !isOtaBlocked()) {
            viewModel.writeFanLevel(level);
        }
    }

    private void sendSeasonMode(boolean winter) {
        if (viewModel != null && !isOtaBlocked()) {
            viewModel.writeSeasonMode(winter);
        }
    }

    private boolean isOtaBlocked() {
        return isOtaOpen;
    }

    private int fanLevelForView(View selected) {
        if (selected == fanLowButton) {
            return 1;
        }
        if (selected == fanMidButton) {
            return 2;
        }
        if (selected == fanHighButton) {
            return 3;
        }
        return 0;
    }

    private View fanButtonForLevel(int level) {
        if (level == 1) {
            return fanLowButton;
        }
        if (level == 2) {
            return fanMidButton;
        }
        if (level == 3) {
            return fanHighButton;
        }
        return fanOffButton;
    }

    private void applyModeButtons() {
        if (classicModeButton == null || careModeButton == null) {
            return;
        }
        int selectedBg = warmTheme ? R.drawable.fh_mode_warm : R.drawable.fh_mode_dark_sel;
        int normalBg = warmTheme ? R.drawable.fh_mode_warm_norm : R.drawable.fh_mode_dark;
        int selectedColor = Color.parseColor(warmTheme ? "#2C2723" : "#FFFFFF");
        int normalColor = Color.parseColor(warmTheme ? "#7F705F" : "#80FFFFFF");
        classicModeButton.setBackgroundResource(classicModeSelected ? selectedBg : normalBg);
        careModeButton.setBackgroundResource(classicModeSelected ? normalBg : selectedBg);
        classicModeButton.setTextColor(classicModeSelected ? selectedColor : normalColor);
        careModeButton.setTextColor(classicModeSelected ? normalColor : selectedColor);
    }

    private void applySeasonButtons() {
        if (summerButton == null || winterButton == null) {
            return;
        }
        boolean summerSelected = !winterThemeSelected;
        int summerSelectedBg = R.drawable.fh_mode_dark_sel;
        int winterSelectedBg = R.drawable.fh_mode_warm;
        int normalBg = warmTheme ? R.drawable.fh_mode_warm_norm : R.drawable.fh_mode_dark;
        summerButton.setBackgroundResource(summerSelected ? summerSelectedBg : normalBg);
        winterButton.setBackgroundResource(winterThemeSelected ? winterSelectedBg : normalBg);
        summerButton.setTextColor(Color.parseColor(summerSelected ? "#FFFFFF" : (warmTheme ? "#7F705F" : "#80FFFFFF")));
        winterButton.setTextColor(Color.parseColor(winterThemeSelected ? "#2C2723" : (warmTheme ? "#7F705F" : "#80FFFFFF")));
    }

    private void applyLayoutMode() {
        if (leftCard == null || middleCard == null || rightCard == null) {
            return;
        }
        boolean careMode = !classicModeSelected;
        leftCard.setVisibility(careMode ? View.GONE : View.VISIBLE);
        if (careSceneContent != null) {
            careSceneContent.setVisibility(careMode ? View.VISIBLE : View.GONE);
        }
        int classicSceneVisibility = careMode ? View.GONE : View.VISIBLE;
        sceneEcoButton.setVisibility(classicSceneVisibility);
        sceneComfortButton.setVisibility(classicSceneVisibility);
        sceneVacationButton.setVisibility(classicSceneVisibility);
        sceneCustomButton.setVisibility(classicSceneVisibility);

        LinearLayout.LayoutParams middleParams = (LinearLayout.LayoutParams) middleCard.getLayoutParams();
        LinearLayout.LayoutParams rightParams = (LinearLayout.LayoutParams) rightCard.getLayoutParams();
        middleParams.weight = careMode ? 1f : 1.6f;
        rightParams.weight = careMode ? 1f : 1.2f;
        middleParams.leftMargin = careMode ? 0 : dp(14);
        rightParams.leftMargin = dp(14);
        middleCard.setLayoutParams(middleParams);
        rightCard.setLayoutParams(rightParams);

        setSize(seasonPanel, ViewGroup.LayoutParams.MATCH_PARENT, dp(careMode ? 42 : 38));
        setTopMargin(seasonPanel, dp(careMode ? 8 : 6));
        setSize(targetPanel, ViewGroup.LayoutParams.MATCH_PARENT, dp(careMode ? 240 : 230));
        setTopMargin(targetPanel, dp(careMode ? 8 : 6));
        setSize(fanPanel, careMode ? dp(400) : ViewGroup.LayoutParams.MATCH_PARENT, dp(careMode ? 100 : 104));
        setTopMargin(fanPanel, dp(careMode ? -10 : 18));
        setSize(modePanel, dp(520), ViewGroup.LayoutParams.MATCH_PARENT);

        targetTempView.setTextSize(careMode ? 68 : 66);
        targetHumidityView.setTextSize(careMode ? 68 : 66);
        targetTempUnitView.setTextSize(careMode ? 24 : 22);
        targetHumidityUnitView.setTextSize(careMode ? 24 : 22);
        setSquareSize(tempMinusButton, dp(careMode ? 58 : 60));
        setSquareSize(tempPlusButton, dp(careMode ? 58 : 60));
        setSquareSize(humidityMinusButton, dp(careMode ? 58 : 60));
        setSquareSize(humidityPlusButton, dp(careMode ? 58 : 60));

        setSize(fanOffLabel, careMode ? dp(54) : ViewGroup.LayoutParams.MATCH_PARENT, dp(careMode ? 50 : 48));
        setSize(fanLowLabel, careMode ? dp(54) : ViewGroup.LayoutParams.MATCH_PARENT, dp(careMode ? 50 : 48));
        setSize(fanMidLabel, careMode ? dp(54) : ViewGroup.LayoutParams.MATCH_PARENT, dp(careMode ? 50 : 48));
        setSize(fanHighLabel, careMode ? dp(54) : ViewGroup.LayoutParams.MATCH_PARENT, dp(careMode ? 50 : 48));
        setFanButtonContainerLayout(fanOffButton, careMode);
        setFanButtonContainerLayout(fanLowButton, careMode);
        setFanButtonContainerLayout(fanMidButton, careMode);
        setFanButtonContainerLayout(fanHighButton, careMode);
        setFanButtonGroupOffset(careMode);
        setSize(summerButton, dp(careMode ? 132 : 112), dp(careMode ? 36 : 32));
        setSize(winterButton, dp(careMode ? 132 : 112), dp(careMode ? 36 : 32));
        summerButton.setTextSize(careMode ? 15 : 14);
        winterButton.setTextSize(careMode ? 15 : 14);
        setSize(classicModeButton, dp(112), dp(34));
        setSize(careModeButton, dp(112), dp(34));
        classicModeButton.setTextSize(14);
        careModeButton.setTextSize(14);

        int sceneTitleSize = careMode ? 20 : 18;
        int scenePresetSize = careMode ? 20 : 18;
        sceneEcoTitle.setTextSize(sceneTitleSize);
        sceneComfortTitle.setTextSize(sceneTitleSize);
        sceneVacationTitle.setTextSize(sceneTitleSize);
        sceneCustomTitle.setTextSize(sceneTitleSize);
        sceneEcoPreset.setTextSize(scenePresetSize);
        sceneComfortPreset.setTextSize(scenePresetSize);
        sceneVacationPreset.setTextSize(scenePresetSize);
        sceneCustomPreset.setTextSize(scenePresetSize);
        setScenePresetWidth(careMode ? 118 : 104);
        setSceneCheckWidth(careMode ? 18 : 16);
    }

    private void setScenePresetWidth(int widthDp) {
        int width = dp(widthDp);
        setSize(sceneEcoPreset, width, ViewGroup.LayoutParams.MATCH_PARENT);
        setSize(sceneComfortPreset, width, ViewGroup.LayoutParams.MATCH_PARENT);
        setSize(sceneVacationPreset, width, ViewGroup.LayoutParams.MATCH_PARENT);
        setSize(sceneCustomPreset, width, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    private void setSceneCheckWidth(int widthDp) {
        int width = dp(widthDp);
        setSize(sceneEcoCheck, width, ViewGroup.LayoutParams.MATCH_PARENT);
        setSize(sceneComfortCheck, width, ViewGroup.LayoutParams.MATCH_PARENT);
        setSize(sceneVacationCheck, width, ViewGroup.LayoutParams.MATCH_PARENT);
        setSize(sceneCustomCheck, width, ViewGroup.LayoutParams.MATCH_PARENT);
    }

    private void setFanButtonContainerLayout(View view, boolean careMode) {
        ViewGroup.LayoutParams rawParams = view.getLayoutParams();
        if (rawParams instanceof LinearLayout.LayoutParams) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) rawParams;
            params.width = 0;
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            params.weight = 1f;
            view.setLayoutParams(params);
        }
    }

    private void setFanButtonGroupOffset(boolean careMode) {
        ViewGroup parent = (ViewGroup) fanOffButton.getParent();
        ViewGroup.LayoutParams rawParams = parent.getLayoutParams();
        if (rawParams instanceof LinearLayout.LayoutParams) {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) rawParams;
            params.leftMargin = dp(careMode ? 10 : 2);
            params.rightMargin = dp(careMode ? 0 : 8);
            parent.setLayoutParams(params);
        }
    }

    private void setSize(View view, int width, int height) {
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = width;
        params.height = height;
        view.setLayoutParams(params);
    }

    private void setSquareSize(View view, int size) {
        setSize(view, size, size);
    }

    private void setTopMargin(View view, int topMargin) {
        ViewGroup.LayoutParams rawParams = view.getLayoutParams();
        if (rawParams instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) rawParams;
            params.topMargin = topMargin;
            view.setLayoutParams(params);
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private void setTextColorRecursive(View view, int color) {
        if (view instanceof TextView) {
            ((TextView) view).setTextColor(color);
            return;
        }
        if (!(view instanceof ViewGroup)) {
            return;
        }
        ViewGroup group = (ViewGroup) view;
        for (int i = 0; i < group.getChildCount(); i++) {
            setTextColorRecursive(group.getChildAt(i), color);
        }
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private void setTitleText() {
        if (titleView == null) {
            return;
        }
        SpannableString title = new SpannableString("健康房 · 智慧家  ●");
        title.setSpan(new ForegroundColorSpan(Color.parseColor("#34C759")), title.length() - 1, title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        titleView.setText(title);
    }

    private int getWeatherImageResId(int code) {
        switch (code) {
            case 1:
                return R.drawable.weather_qing;
            case 2:
                return R.drawable.weather_duoyun;
            case 3:
                return R.drawable.weather_yin;
            case 4:
                return R.drawable.weather_zhenyu;
            case 5:
                return R.drawable.weather_leizhenyu;
            case 6:
                return R.drawable.weather_bingbao;
            case 7:
                return R.drawable.weather_yujiaxue;
            case 8:
                return R.drawable.weather_xiaoyu;
            case 9:
                return R.drawable.weather_zhongyu;
            case 10:
                return R.drawable.weather_dayu;
            case 11:
                return R.drawable.weather_baoyu;
            case 12:
                return R.drawable.weather_zhenxue;
            case 13:
                return R.drawable.weather_xiaoxue;
            case 14:
                return R.drawable.weather_zhongxue;
            case 15:
                return R.drawable.weather_daxue;
            case 16:
                return R.drawable.weather_baoxue;
            case 17:
                return R.drawable.weather_wu;
            case 18:
                return R.drawable.weather_dongyu;
            case 19:
                return R.drawable.weather_shachenbao;
            case 20:
                return R.drawable.weather_fuchen;
            case 21:
                return R.drawable.weather_mai;
            default:
                return R.drawable.weather_duoyun;
        }
    }

    @Override
    public void setMessage(String message) {
        controlVersion = message;
        if ("0".equals("0")) {
            // 手动升级模式：弹窗让用户确认是否升级主板固件
            confirmUpgrade("主板固件升级", "检测到主控板新固件 v" + message + "，是否立即升级？", this::sendUpdateRequest);
        } else {
            sendUpdateRequest();
        }
    }

    @Override
    public void setMessage1(String message) {
        // APK下载完成安装（仅下发App升级的场景）
        appVersion = message;
        if ("0".equals("0")) {
            // 手动升级模式：弹窗让用户确认是否升级App
            confirmUpgrade("App升级", "检测到App新版本 v" + message + "，是否立即升级？", this::installAppApk);
        } else {
            installAppApk();
        }
    }

    /**
     * 手动升级模式(sign=0)：弹窗让用户确认是否升级。
     * 点"更新"→执行升级；点"取消"→不升级。
     */
    private void confirmUpgrade(String title, String msg, Runnable onConfirm) {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(() -> {
            new AlertDialog.Builder(getActivity())
                    .setTitle(title)
                    .setMessage(msg)
                    .setCancelable(false)
                    .setPositiveButton("更新", (d, w) -> {
                        d.dismiss();
                        onConfirm.run();
                    })
                    .setNegativeButton("取消", (d, w) -> {
                        // 用户取消升级：同时关闭下载进度条，避免卡在进度条页面
                        d.dismiss();
                        dismissProgressDialog();
                    })
                    .show();
        });
    }

    /**
     * 安装已下载的App升级包。
     * 统一入口：仅App升级(setMessage1)、主板OTA成功(type==1)、主板OTA失败达上限时都走这里，
     * 避免APK安装被主板升级结果无限阻塞。
     */
    private void installAppApk() {
        if (getActivity() == null || StringUtils.isNullOrEmpty(appVersion)) {
            Log.i(TAG, "installAppApk skipped, activity=" + getActivity() + ", appVersion=" + appVersion);
            dismissProgressDialog();
            return;
        }
        // Android 8+ 安装APK需具备以下任一，否则 PackageInstaller.commit 直接抛 SecurityException（表现为"没反应"）：
        //   1) INSTALL_PACKAGES —— 系统应用/priv-app 部署，白名单授权后特权静默安装，全版本无需确认框（生产环境走这条）
        //   2) "安装未知应用"(REQUEST_INSTALL_PACKAGES) —— 三方安装部署走这条，需一次性授权：
        //      adb shell appops set com.hy.greenbuilding REQUEST_INSTALL_PACKAGES allow
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            boolean hasInstallPackages = getActivity().getPackageManager().checkPermission(
                    android.Manifest.permission.INSTALL_PACKAGES, getActivity().getPackageName())
                    == android.content.pm.PackageManager.PERMISSION_GRANTED;
            if (!hasInstallPackages && !getActivity().getPackageManager().canRequestPackageInstalls()) {
                Log.e(TAG, "install blocked: no INSTALL_PACKAGES(priv-app) and REQUEST_INSTALL_PACKAGES not granted. "
                        + "adb: appops set com.hy.greenbuilding REQUEST_INSTALL_PACKAGES allow");
                Toast.makeText(getActivity(), "App升级被系统拦截：请允许安装未知应用，或执行 adb shell appops set com.hy.greenbuilding REQUEST_INSTALL_PACKAGES allow", Toast.LENGTH_LONG).show();
                dismissProgressDialog();
                return;
            }
        }
        File file = new File(StringUtils.destFileDir, appVersion + ".apk");
        boolean installSuccess = PackageUtil.installAPK(getActivity(), file.getPath());
        Log.i(TAG, "silent app install started=" + installSuccess + ", path=" + file.getPath());
        // 停止主板升级重试循环
        isUpdating = false;
        otaHandler.removeCallbacks(otaRunnable);
        dismissProgressDialog();
        if (!installSuccess && getActivity() != null) {
            Toast.makeText(getActivity(), "App升级包安装失败，请查看日志", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void setMessage2(String message) {
        appVersion = message;
    }

    @Override
    public void updateWeather(boolean isConnect) {
    }

    @Override
    public void sendMessage3(byte[] bytes) {
        receiveData(bytes);
    }

    @Override
    public void sendOtaStatus(boolean status) {
        isOtaOpen = status;
        if (getContext() != null) {
            MySpUtil.setParam(requireContext(), MySpUtil.OTA_STATUS, status);
        }
    }

    @Override
    public void onDownloadProgressUpdate(int progress, int fileType, String message) {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(() -> {
            if (progressDialog == null) {
                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setTitle("OTA 升级下载");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setCancelable(false);
                progressDialog.setMax(100);
                progressDialog.show();
            }
            progressDialog.setTitle((fileType == 1) ? "主板程序 升级下载" : "app 升级下载");
            if (progress == -1) {
                dismissProgressDialog();
                Toast.makeText(getActivity(), "文件下载失败！", Toast.LENGTH_LONG).show();
            } else if (progress == 100) {
                String fileName = (fileType == 1) ? "主板程序下载完成，准备校验，请稍后..." : "准备安装app中，请稍后";
                progressDialog.setProgress(100);
                progressDialog.setMessage(fileName);
            } else {
                String fileName = (fileType == 1) ? "主板程序" : "APK 文件";
                progressDialog.setMessage("正在下载 V" + message + "-" + fileName + "...");
                progressDialog.setProgress(progress);
            }
        });
    }

    private void sendUpdateRequest() {
        File filePath = new File(StringUtils.destFileDir, controlVersion + ".bin");
        byte[] fileBytes = StringUtils.readFile(filePath);
        if (fileBytes == null) {
            // 主板升级文件缺失：无法升级主板，跳过并继续安装App，避免无限阻塞
            Log.e(TAG, "control firmware .bin not found: " + filePath.getAbsolutePath()
                    + ", skip board OTA and install app directly");
            otaFailCount = 0;
            installAppApk();
            return;
        }
        OTARequestCommand command = new OTARequestCommand(1);
        command.setByteLength(fileBytes.length);
        command.setCrc(fileBytes);
        int version = new BigDecimal(controlVersion).setScale(1, BigDecimal.ROUND_DOWN)
                .multiply(new BigDecimal(10)).intValue();
        command.setVersion(ByteUtils.int16ToByteArray(version));
        SpDataProcessor.getInstance().send1(command);
        updateStatus = "正在升级" + controlVersion + ".bin";
        EventBus.getDefault().post(new VersionUpdateEvent(1, updateStatus));
        isUpdating = true;
        otaStartTime = SystemClock.elapsedRealtime();
        otaHandler.removeCallbacks(otaRunnable);
        otaHandler.postDelayed(otaRunnable, 2000);
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                if (progressDialog == null) {
                    progressDialog = new ProgressDialog(getActivity());
                    progressDialog.setTitle("主板程序 升级");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.setCancelable(false);
                    progressDialog.setMax(100);
                    progressDialog.show();
                }
                progressDialog.setProgress(0);
                progressDialog.setMessage("主板程序升级中：" + updateStatus);
            });
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOtaStatusEvent(OTAStatusEvent event) {
        if (event == null) {
            return;
        }
        isUpdating = false;
        file_path = new File(StringUtils.destFileDir, controlVersion + ".bin");
        byte[] fileData = StringUtils.readFile(file_path);
        byte[] data = event.getOtaData();
        if (fileData == null || data == null || data.length < 8) {
            return;
        }
        int offset = ByteUtils.byteArrayToInt(Arrays.copyOfRange(data, 0, 4), 0,
                Arrays.copyOfRange(data, 0, 4).length);
        int serial = ByteUtils.byteArrayToInt16(Arrays.copyOfRange(data, 4, 6));
        int byteLength = ByteUtils.byteArrayToInt16(Arrays.copyOfRange(data, 6, 8));
        if (offset < 0 || byteLength <= 0 || offset + byteLength > fileData.length) {
            return;
        }
        // 按已发送字节数更新主板升级进度条（offset=当前要发送块的起始偏移=已传输字节数）
        if (progressDialog != null) {
            int progress = (int) ((long) offset * 100 / fileData.length);
            progressDialog.setProgress(progress);
            progressDialog.setMessage("主板程序升级中 " + progress + "%");
        }
        OTARequestCommand otaRequestCommand = new OTARequestCommand(2);
        otaRequestCommand.setByteLength(byteLength + 2);
        otaRequestCommand.setSerial(ByteUtils.shortToByteArray((short) serial));
        otaRequestCommand.setSendData(Arrays.copyOfRange(fileData, offset, offset + byteLength));
        SpDataProcessor.getInstance().send2(otaRequestCommand);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOtaErrorEvent(OTAErrorEvent event) {
        if (event == null) {
            return;
        }
        if (event.getType() == 1) {
            // 主板升级成功，安装App
            otaFailCount = 0;
            updateStatus = "";
            isUpdating = false;
            EventBus.getDefault().post(new VersionUpdateEvent(3, ""));
            // 主板 OTA 成功：固件版本更新为本次下发的版本，主页"固件 vX"跟着变
            if (!StringUtils.isNullOrEmpty(controlVersion)) {
                HyApplication.setControlVersion(controlVersion);
                updateFooterStatusText(FOOTER_BASE_TEXT);
            }
            installAppApk();
        } else if (event.getType() == 0) {
            dismissProgressDialog();
        } else if (event.getType() == 4) {
            // 超时：重试，但累计失败达上限后放弃主板升级，直接安装App
            if (++otaFailCount >= MAX_OTA_RETRY) {
                giveUpBoardOtaAndInstallApp();
            } else {
                updateStatus = "升级超时";
                EventBus.getDefault().post(new VersionUpdateEvent(1, updateStatus));
                sendUpdateRequest();
            }
        } else {
            if (++otaFailCount >= MAX_OTA_RETRY) {
                giveUpBoardOtaAndInstallApp();
            } else {
                updateStatus = "升级失败";
                EventBus.getDefault().post(new VersionUpdateEvent(1, updateStatus));
                sendUpdateRequest();
            }
        }
    }

    /**
     * 主板OTA失败达到上限：放弃主板升级，确保App仍能安装，避免APK安装被无限阻塞。
     */
    private void giveUpBoardOtaAndInstallApp() {
        Log.e(TAG, "board OTA failed after " + MAX_OTA_RETRY + " retries, give up and install app directly");
        updateStatus = "主板升级失败，继续安装App";
        isUpdating = false;
        otaHandler.removeCallbacks(otaRunnable);
        EventBus.getDefault().post(new VersionUpdateEvent(1, updateStatus));
        installAppApk();
    }

    private void dismissProgressDialog() {
        if (getActivity() != null && Looper.myLooper() != Looper.getMainLooper()) {
            getActivity().runOnUiThread(this::dismissProgressDialog);
            return;
        }
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    private void showSilentInstallFailedToast() {
        if (getActivity() == null) {
            return;
        }
        getActivity().runOnUiThread(() ->
                Toast.makeText(getActivity(), "静默安装失败", Toast.LENGTH_LONG).show());
    }

    private void updateFooterStatusText(String prefix) {
        if (footerStatusView == null) {
            return;
        }
        // 固件版本绑定主控板实际版本（HyApplication.getControlVersion 由主板信息同步，升级后自动变化）
        String controlVersion = HyApplication.getControlVersion();
        String controlText = StringUtils.isNullOrEmpty(controlVersion)
                ? "固件 v未知"
                : "固件 v" + controlVersion;
        String version = PackageUtil.getVersion(requireContext());
        if (StringUtils.isNullOrEmpty(version)) {
            footerStatusView.setText(prefix + "      ·      " + controlText);
        } else {
            footerStatusView.setText(prefix + "      ·      " + controlText
                    + "      ·      当前程序版本V" + version);
        }
    }

    private static final class TopEdgeCardDrawable extends Drawable {
        private final float radius;
        private final float edgeWidth;
        private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint edgePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final RectF rect = new RectF();
        private final Path clipPath = new Path();

        TopEdgeCardDrawable(float radius, float edgeWidth, int fillColor, int borderColor, int edgeColor) {
            this.radius = radius;
            this.edgeWidth = edgeWidth;
            fillPaint.setStyle(Paint.Style.FILL);
            fillPaint.setColor(fillColor);
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setStrokeWidth(1f);
            borderPaint.setColor(borderColor);
            edgePaint.setStyle(Paint.Style.STROKE);
            edgePaint.setStrokeWidth(edgeWidth);
            edgePaint.setStrokeCap(Paint.Cap.ROUND);
            edgePaint.setStrokeJoin(Paint.Join.ROUND);
            edgePaint.setColor(edgeColor);
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            float halfBorder = 0.5f;
            rect.set(halfBorder, halfBorder, getBounds().width() - halfBorder, getBounds().height() - halfBorder);
            canvas.drawRoundRect(rect, radius, radius, fillPaint);
            canvas.drawRoundRect(rect, radius, radius, borderPaint);

            int save = canvas.save();
            clipPath.reset();
            clipPath.addRect(0, 0, getBounds().width(), radius + edgeWidth, Path.Direction.CW);
            canvas.clipPath(clipPath);

            float halfEdge = edgeWidth / 2f;
            rect.set(halfEdge, halfEdge, getBounds().width() - halfEdge, getBounds().height() - halfEdge);
            canvas.drawRoundRect(rect, radius, radius, edgePaint);
            canvas.restoreToCount(save);
        }

        @Override
        public void setAlpha(int alpha) {
            fillPaint.setAlpha(alpha);
            borderPaint.setAlpha(alpha);
            edgePaint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(@Nullable android.graphics.ColorFilter colorFilter) {
            fillPaint.setColorFilter(colorFilter);
            borderPaint.setColorFilter(colorFilter);
            edgePaint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.TRANSLUCENT;
        }
    }
}
