package com.android.systemui.statusbar.slim;

import android.widget.TextView;

import com.android.systemui.BatteryMeterView;
import com.android.systemui.statusbar.phone.PhoneStatusBar;

public class SlimStatusBar extends PhoneStatusBar {

    private BatteryMeterView mBatteryView;
    private TextView mBatteryLevel;

    private boolean mShowBatteryText;
    private boolean mShowBatteryTextCharging;
    private boolean mBatteryIsCharging;
    private int mBatteryChargeLevel;

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(SlimSettings.Secure.getUriFor(
                    SlimSettings.Secure.STATUS_BAR_BATTERY_PERCENT),
                    false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(SlimSettings.Secure.getUriFor(
                    SlimSettings.Secure.STATUS_BAR_BATTERY_STYLE),
                    false, this, UserHandle.USER_ALL);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            if (uri.equals(SlimSettings.Secure.getUriFor(
                    SlimSettings.Secure.STATUS_BAR_BATTERY_PERCENT)) ||
                    uri.equals(SlimSettings.Secure.getUriFor(
                    SlimSettings.Secure.STATUS_BAR_BATTERY_STYLE))) {
                mBatteryView.updateBatteryIconSettings();
                mHeader.updateBatteryIconSettings();
                mKeyguardStatusBar.updateBatteryIconSettings();
            }
            update();
        }

        public void update() {
            loadShowBatteryTextSetting();
            updateBatteryLevelText();
            mBatteryLevel.setVisibility(mShowBatteryText ? View.VISIBLE : View.GONE);
        }
    }

    private void loadShowBatteryTextSetting() {
        ContentResolver resolver = mContext.getContentResolver();
        mShowBatteryText = SlimSettings.Secure.getInt(resolver,
                SlimSettings.Secure.STATUS_BAR_BATTERY_PERCENT, 0) == 2;
        int batteryStyle = SlimSettings.Secure.getInt(resolver,
                SlimSettings.Secure.STATUS_BAR_BATTERY_STYLE, 0);
        switch (batteryStyle) {
            case 4:
                //meterMode = BatteryMeterMode.BATTERY_METER_GONE;
                mShowBatteryText = false;
                mShowBatteryTextCharging = false;
                break;

            case 6:
                //meterMode = BatteryMeterMode.BATTERY_METER_TEXT;
                mShowBatteryText = true;
                mShowBatteryTextCharging = true;
                break;

            default:
                mShowBatteryTextCharging = false;
                break;
        }
    }

    private void updateBatteryLevelText() {
        if (mBatteryIsCharging & mShowBatteryTextCharging) {
            mBatteryLevel.setText(mContext.getResources().getString(
                    R.string.battery_level_template_charging, mBatteryChargeLevel));
        } else {
            mBatteryLevel.setText(mContext.getResources().getString(
                    R.string.battery_level_template, mBatteryChargeLevel));
        }
    }

    @Override
    protected PhoneStatusBarView makeStatusBarView() {
        super.makeStatusBarView();

        mBatteryView = (BatteryMeterView) mStatusBarView.findViewById(R.id.battery);
        mBatteryLevel = (TextView) mStatusBarView.findViewById(R.id.battery_level_text);

        if (mBatteryController != null) {
            mBatteryController.addStateChangedCallback(new BatteryStateChangeCallback() {
                @Override
                public void onPowerSaveChanged() {
                    mHandler.post(mCheckBarModes);
                    if (mDozeServiceHost != null) {
                        mDozeServiceHost.firePowerSaveChanged(mBatteryController.isPowerSave());
                    }
                }
                @Override
                public void onBatteryLevelChanged(int level, boolean pluggedIn, boolean charging) {
                    mBatteryIsCharging = charging;
                    mBatteryChargeLevel = level;
                    loadShowBatteryTextSetting();
                    updateBatteryLevelText();
                    mHeader.updateBatteryLevel(level, charging);
                    mKeyguardStatusBar.updateBatteryLevel(level, charging);
                }
            });
        }
        return mStatusBarView;
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        FontSizeUtils.updateFontSize(mBatteryLevel, R.dimen.battery_level_text_size);
    }
}
