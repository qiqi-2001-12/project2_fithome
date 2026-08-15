package com.hy.greenbuilding.ui.activity;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.hy.greenbuilding.HyApplication;
import com.hy.greenbuilding.R;
import com.hy.greenbuilding.ui.fragment.AddRoomFragment;
import com.hy.greenbuilding.ui.fragment.HomeFragment;
import com.hy.greenbuilding.ui.fragment.FitHomeFragment;
import com.hy.greenbuilding.ui.fragment.ManagerFragment;
import com.hy.greenbuilding.ui.fragment.SettingFragment;
import com.hy.greenbuilding.ui.widget.verticaltablayout.VerticalTabLayout;
import com.hy.greenbuilding.ui.widget.verticaltablayout.adapter.DarkHomeTabAdapter;
import com.hy.greenbuilding.ui.widget.verticaltablayout.widget.TabView;
import com.hy.greenbuilding.utils.MySpUtil;
import com.hy.greenbuilding.utils.ToastUtil;

public class HomeActivity extends BaseActivity {
    private VerticalTabLayout mTabLayout;
    private final String[] TAB_TITLES = {"棣栭〉", "璁剧疆", "鎴块棿鐜", "璁惧绠＄悊"};
    private Fragment[] mFragments;
    private FragmentManager mFragmentManager;
    private static final String CURRENT_TAB_KEY = "current_tab";
    private int currentTabIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mTabLayout = findViewById(R.id.vertical_tab_layout);
        mFragmentManager = getSupportFragmentManager();
        if (savedInstanceState != null) {
            currentTabIndex = savedInstanceState.getInt(CURRENT_TAB_KEY, 0);
        }

        initFragments(savedInstanceState);
        setupTabLayout();

        if (savedInstanceState != null) {
            mTabLayout.post(new Runnable() {
                @Override
                public void run() {
                    mTabLayout.setTabSelected(currentTabIndex);
                }
            });
        }
    }

    /**
     * 鍦?Activity 琚攢姣佸墠淇濆瓨褰撳墠鐘舵€?
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // 淇濆瓨褰撳墠閫変腑鐨?Tab 绱㈠紩
        outState.putInt(CURRENT_TAB_KEY, currentTabIndex);
    }



    private final int[] NORMAL_ICONS = {
            R.drawable.icon_home_white,
            R.drawable.icon_settings_white,
            R.drawable.icon_room_management_white,
            R.drawable.icon_equipment_management_white
    };

    private final int[] SELECTED_ICONS = {
            R.drawable.icon_home,

            R.drawable.icon_settings,
            R.drawable.icon_room_management,
            R.drawable.icon_equipment_management
    };
    private void setupTabLayout() {
        mTabLayout.setTabAdapter(new DarkHomeTabAdapter(TAB_TITLES,NORMAL_ICONS,SELECTED_ICONS),(boolean) MySpUtil.getParam(HomeActivity.this, MySpUtil.OTA_STATUS, false));
        mTabLayout.addOnTabSelectedListener(new VerticalTabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabView tab, int position) {
                boolean isOtaOpen = (boolean) MySpUtil.getParam(HomeActivity.this, MySpUtil.OTA_STATUS, false);
                if (isOtaOpen) {
                    ToastUtil.showToast(HomeActivity.this, getString(R.string.server_not_permission));
                    return;
                }

                if (HyApplication.isLocking) {
                    ToastUtil.showToast(HomeActivity.this, getString(R.string.device_locked));
                    return;
                }

                // 鏇存柊褰撳墠绱㈠紩
                currentTabIndex = position;
                showFragment(position);
            }

            @Override
            public void onTabReselected(TabView tab, int position) {
                boolean isOtaOpen = (boolean) MySpUtil.getParam(HomeActivity.this, MySpUtil.OTA_STATUS, false);
                if (isOtaOpen) {
                    ToastUtil.showToast(HomeActivity.this, getString(R.string.server_not_permission));
                    return;
                }

                if (HyApplication.isLocking) {
                    ToastUtil.showToast(HomeActivity.this, getString(R.string.device_locked));
                    return;
                }
                //澶勭悊 Tab 鍐嶆閫変腑浜嬩欢
                Fragment reselectedFragment = mFragments[position];
                if (reselectedFragment instanceof SettingFragment) {
                    ((SettingFragment) reselectedFragment).resetToDefaultView();
                }else if (reselectedFragment instanceof FitHomeFragment) {
                    ((FitHomeFragment) reselectedFragment).resetToDefaultView();
                }else if (reselectedFragment instanceof AddRoomFragment) {
                    ((AddRoomFragment) reselectedFragment).resetToDefaultView();
                }
            }
        });
    }

    private Fragment createNewFragment(int position) {
        switch (position) {
            case 0:
                return new FitHomeFragment();
            case 3:
                return new ManagerFragment();
            case 1:
                return new SettingFragment();
            case 2:
                return new AddRoomFragment();
            default:
                return new Fragment();
        }
    }

    private void initFragments(Bundle savedInstanceState) {
        mFragments = new Fragment[TAB_TITLES.length];
        FragmentTransaction transaction = mFragmentManager.beginTransaction();

        if (savedInstanceState == null) {
            // 浠呭垵濮嬪寲榛樿鏄剧ず鐨凢ragment锛堥椤碉級
            mFragments[0] = createNewFragment(0);
            transaction.add(R.id.content_frame, mFragments[0], TAB_TITLES[0]);
            // 鍏朵綑Fragment鍏堢疆涓簄ull锛岄娆″垏鎹㈡椂鍒涘缓
            for (int i = 1; i < TAB_TITLES.length; i++) {
                mFragments[i] = null;
            }
            transaction.commitAllowingStateLoss();
        } else {
            // 鎭㈠宸插垱寤虹殑Fragment
            for (int i = 0; i < TAB_TITLES.length; i++) {
                mFragments[i] = mFragmentManager.findFragmentByTag(TAB_TITLES[i]);
            }
        }
    }

    // 鏀归€爏howFragment鏂规硶锛屾噿鍔犺浇Fragment
    private void showFragment(int position) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        // 闅愯棌鎵€鏈夊凡鏄剧ず鐨凢ragment
        for (Fragment fragment : mFragments) {
            if (fragment != null && !fragment.isHidden()) {
                transaction.hide(fragment);
            }
        }
        // 鑻ョ洰鏍嘑ragment鏈垱寤猴紝鍏堝垱寤哄苟add
        if (mFragments[position] == null) {
            mFragments[position] = createNewFragment(position);
            transaction.add(R.id.content_frame, mFragments[position], TAB_TITLES[position]);
        }
        // 鏄剧ず鐩爣Fragment
        transaction.show(mFragments[position]);
        transaction.commitAllowingStateLoss();
        controlBaseLayoutVisibility(position == 0);
    }
}


