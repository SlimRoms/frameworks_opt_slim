package com.android.systemui.statusbar.slim;

public class SlimStatusBarHeaderView extends StatusBarHeaderView {

    private BatteryMeterView mBatteryView;

    public SlimStatusBarHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        loadShowBatteryTextSetting();
    }

    public void updateBatteryIconSettings() {
        mBatteryView.updateBatteryIconSettings();
        updateEverything();
        requestCaptureValues();
    }

    private void loadShowBatteryTextSetting() {
        int batteryText = SlimSettings.Secure.getInt(getContext().getContentResolver(),
                SlimSettings.Secure.STATUS_BAR_BATTERY_PERCENT, 0);
        int batteryStyle = SlimSettings.Secure.getInt(getContext().getContentResolver(),
                SlimSettings.Secure.STATUS_BAR_BATTERY_STYLE, 0);
        switch (batteryStyle) {
            case 4:
                //meterMode = BatteryMeterMode.BATTERY_METER_GONE;
                mShowBatteryText = false;
                mShowBatteryTextExpanded = true;
                mShowBatteryTextCharging = true;
                break;

            case 6:
                //meterMode = BatteryMeterMode.BATTERY_METER_TEXT;
                mShowBatteryText = true;
                mShowBatteryTextExpanded = true;
                mShowBatteryTextCharging = true;
                break;

            default:
                mShowBatteryText = (batteryText == 2);
                // Only show when percent is not already shown inside icon
                mShowBatteryTextExpanded = (batteryText != 1);
                mShowBatteryTextCharging = false;
                break;
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mBatteryView = (BatteryMeterView) findViewById(R.id.battery);
    }

    @Override
    public void updateEverything() {
        super.updateEverything();

        loadShowBatteryTextSetting();
        updateBatteryLevelText();
        mBatteryLevel.setVisibility(
                mExpanded ? (mShowBatteryTextExpanded ? View.VISIBLE : View.GONE)
                          : (mShowBatteryText         ? View.VISIBLE : View.GONE));
    }

    private void updateBatteryLevelText() {
        if (mBatteryIsCharging & mShowBatteryTextCharging) {
            mBatteryLevel.setText(getResources().getString(
                    R.string.battery_level_template_charging, mBatteryChargeLevel));
        } else {
            mBatteryLevel.setText(getResources().getString(
                    R.string.battery_level_template, mBatteryChargeLevel));
        }
    }

    public void updateBatteryLevel(int level, boolean charging) {
        mBatteryIsCharging = charging;
        mBatteryChargeLevel = level;
        loadShowBatteryTextSetting();
        updateBatteryLevelText();
    }

    @Override
    public void onBatteryLevelChanged(int level, boolean pluggedIn, boolean charging) {
        updateBatteryLevel(level, charging);
    }
}
