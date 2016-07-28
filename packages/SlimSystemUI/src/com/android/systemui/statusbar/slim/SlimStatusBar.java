package com.android.systemui.statusbar.slim;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.app.StatusBarManager;
import android.content.ContentResolver;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.android.systemui.BatteryMeterView;
import com.android.systemui.FontSizeUtils;
import com.android.systemui.recents.ScreenPinningRequest;
import com.android.systemui.statusbar.phone.NavigationBarView;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.phone.PhoneStatusBarView;
import com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback;
import com.android.systemui.statusbar.policy.KeyButtonView;
import com.android.systemui.statusbar.stack.StackStateAnimator;

import com.android.systemui.R;

import org.slim.action.ActionConfig;
import org.slim.action.ActionConstants;
import org.slim.action.ActionHelper;
import org.slim.provider.SlimSettings;
import org.slim.utils.DeviceUtils;

public class SlimStatusBar extends PhoneStatusBar {

    static final String TAG = "SlimStatusBar";

    private BatteryMeterView mBatteryView;
    private TextView mBatteryLevel;

    private SlimNavigationBarView mSlimNavigationBarView;

    private boolean mHasNavigationBar = false;
    boolean mDisableHomeLongpress;

    private long mLastLockToAppLongPress;

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
            resolver.registerContentObserver(SlimSettings.System.getUriFor(
                    SlimSettings.System.NAVIGATION_BAR_BUTTON_TINT),
                    false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(SlimSettings.System.getUriFor(
                    SlimSettings.System.NAVIGATION_BAR_BUTTON_TINT_MODE),
                    false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(SlimSettings.System.getUriFor(
                    SlimSettings.System.NAVIGATION_BAR_GLOW_TINT),
                    false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(SlimSettings.System.getUriFor(
                    SlimSettings.System.NAVIGATION_BAR_SHOW),
                    false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(SlimSettings.System.getUriFor(
                    SlimSettings.System.NAVIGATION_BAR_CONFIG),
                    false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(SlimSettings.System.getUriFor(
                    SlimSettings.System.NAVIGATION_BAR_CAN_MOVE),
                    false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(SlimSettings.System.getUriFor(
                    SlimSettings.System.MENU_LOCATION),
                    false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(SlimSettings.System.getUriFor(
                    SlimSettings.System.MENU_VISIBILITY),
                    false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(SlimSettings.Secure.getUriFor(
                    SlimSettings.Secure.STATUS_BAR_BATTERY_PERCENT),
                    false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(SlimSettings.Secure.getUriFor(
                    SlimSettings.Secure.STATUS_BAR_BATTERY_STYLE),
                    false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(SlimSettings.System.getUriFor(
                    SlimSettings.System.USE_SLIM_RECENTS), false, this,
                    UserHandle.USER_ALL);
            resolver.registerContentObserver(SlimSettings.System.getUriFor(
                    SlimSettings.System.RECENT_CARD_BG_COLOR), false, this,
                    UserHandle.USER_ALL);
            resolver.registerContentObserver(SlimSettings.System.getUriFor(
                    SlimSettings.System.RECENT_CARD_TEXT_COLOR), false, this,
                    UserHandle.USER_ALL);
            update();
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);

            update();

            if (uri.equals(SlimSettings.System.getUriFor(
                    SlimSettings.System.NAVIGATION_BAR_BUTTON_TINT))
                || uri.equals(SlimSettings.System.getUriFor(
                    SlimSettings.System.NAVIGATION_BAR_BUTTON_TINT_MODE))
                || uri.equals(SlimSettings.System.getUriFor(
                    SlimSettings.System.NAVIGATION_BAR_CONFIG))
                || uri.equals(SlimSettings.System.getUriFor(
                    SlimSettings.System.NAVIGATION_BAR_GLOW_TINT))
                || uri.equals(SlimSettings.System.getUriFor(
                    SlimSettings.System.MENU_LOCATION))
                || uri.equals(SlimSettings.System.getUriFor(
                    SlimSettings.System.MENU_VISIBILITY))) {
                if (mSlimNavigationBarView != null) {
                    mSlimNavigationBarView.recreateNavigationBar();
                    prepareNavigationBarView();
                }
            } else if (uri.equals(SlimSettings.System.getUriFor(
                    SlimSettings.System.NAVIGATION_BAR_CAN_MOVE))) {
                prepareNavigationBarView();
            } else if (uri.equals(SlimSettings.System.getUriFor(
                    SlimSettings.System.NAVIGATION_BAR_SHOW))) {
                updateNavigationBarVisibility();
            } else if (uri.equals(SlimSettings.Secure.getUriFor(
                            SlimSettings.Secure.STATUS_BAR_BATTERY_PERCENT)) ||
                            uri.equals(SlimSettings.Secure.getUriFor(
                            SlimSettings.Secure.STATUS_BAR_BATTERY_STYLE))) {
                        mBatteryView.updateBatteryIconSettings();
                        mHeader.updateBatteryIconSettings();
                        mKeyguardStatusBar.updateBatteryIconSettings();
            } else if (uri.equals(SlimSettings.System.getUriFor(
                    SlimSettings.System.USE_SLIM_RECENTS))) {
                updateRecents();
            } else if (uri.equals(SlimSettings.System.getUriFor(
                    SlimSettings.System.RECENT_CARD_BG_COLOR))
                    || uri.equals(SlimSettings.System.getUriFor(
                    SlimSettings.System.RECENT_CARD_TEXT_COLOR))) {
                rebuildRecentsScreen();
            }
            Log.d(TAG, "uri=" + uri);
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
    public void start() {
        super.start();

        updateNavigationBarVisibility();

        SettingsObserver observer = new SettingsObserver(mHandler);
        observer.observe();

        mScreenPinningRequest.setCallback(mScreenPinningCallback);
    }

    @Override
    protected PhoneStatusBarView makeStatusBarView() {
        PhoneStatusBarView statusBarView = super.makeStatusBarView();

        Log.d(TAG, "makeStatusBarView");

        if (mNavigationBarView != null) {
            if (mNavigationBarView.isAttachedToWindow()) {
                try {
                    mWindowManager.removeView(mNavigationBarView);
                } catch (Exception e) {}
            }
            mNavigationBarView = null;
        }

        if (mSlimNavigationBarView == null) {
            mSlimNavigationBarView = (SlimNavigationBarView)
                    View.inflate(mContext, R.layout.slim_navigation_bar, null);
        }

        mSlimNavigationBarView.setDisabledFlags(mDisabled1);
        mSlimNavigationBarView.setBar(this);
        mSlimNavigationBarView.setOnVerticalChangedListener(
                new NavigationBarView.OnVerticalChangedListener() {
            @Override
            public void onVerticalChanged(boolean isVertical) {
                if (mAssistManager != null) {
                    mAssistManager.onConfigurationChanged();
                }
                mNotificationPanel.setQsScrimEnabled(!isVertical);
            }
        });
        mSlimNavigationBarView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                checkUserAutohide(v, event);
                return false;
            }
        });

        mNavigationBarView = mSlimNavigationBarView;

        mBatteryLevel = (TextView) statusBarView.findViewById(R.id.battery_level_text);
        mBatteryView = (BatteryMeterView) statusBarView.findViewById(R.id.battery);
        mBatteryView.setBatteryController(mBatteryController);

        if (mBatteryController != null) {
            mBatteryController.addStateChangedCallback(new BatteryStateChangeCallback() {
                @Override
                public void onPowerSaveChanged() {
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
                /*@Override
                public void onBatteryStyleChanged(int style, int percentMode, int percentLowOnly) {
                    // noop
                    //TOFIX 
                } No battery styles here! */
            });
        }
        return statusBarView;
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        FontSizeUtils.updateFontSize(mBatteryLevel, R.dimen.battery_level_text_size);
    }

    private KeyButtonView.LongClickCallback mLongClickCallback =
            new KeyButtonView.LongClickCallback() {
        @Override
        public boolean onLongClick(View v) {
            return handleLongPressBackRecents(v);
        }
    };

    private ScreenPinningRequest.ScreenPinningCallback mScreenPinningCallback =
            new ScreenPinningRequest.ScreenPinningCallback() {
        @Override
        public void onStartLockTask() {
            mSlimNavigationBarView.setOverrideMenuKeys(true);
        }
    };

    private void updateNavigationBarVisibility() {
        final int showByDefault = mContext.getResources().getBoolean(
                    com.android.internal.R.bool.config_showNavigationBar) ? 1 : 0;
        mHasNavigationBar = SlimSettings.System.getIntForUser(mContext.getContentResolver(),
                    SlimSettings.System.NAVIGATION_BAR_SHOW, showByDefault,
                    UserHandle.USER_CURRENT) == 1;

        if (mHasNavigationBar) {
            addNavigationBar();
        } else {
            if (mSlimNavigationBarView.isAttachedToWindow()) {
                mWindowManager.removeView(mSlimNavigationBarView);
            }
        }
    }

    @Override
    protected void prepareNavigationBarView() {
        mSlimNavigationBarView.reorient();

        View home = mSlimNavigationBarView.getHomeButton();
        View recents = mSlimNavigationBarView.getRecentsButton();

        mSlimNavigationBarView.setPinningCallback(mLongClickCallback);

        /*if (recents != null) {
            recents.setOnClickListener(mRecentsClickListener);
            recents.setOnTouchListener(mRecentsPreloadOnTouchListener);
        }
        if (home != null) {
            home.setOnTouchListener(mHomeActionListener);
        }*/

        mAssistManager.onConfigurationChanged();
    }

    protected void addNavigationBar() {
        if (DEBUG) Log.v(TAG, "addNavigationBar: about to add " + mSlimNavigationBarView);
        if (mSlimNavigationBarView == null) return;

        prepareNavigationBarView();

        if (!mSlimNavigationBarView.isAttachedToWindow()) {
            try {
                mWindowManager.addView(mSlimNavigationBarView, getNavigationBarLayoutParams());
            } catch (Exception e) {}
        }
    }

    private boolean handleLongPressBackRecents(View v) {
        try {
            boolean sendBackLongPress = false;
            IActivityManager activityManager = ActivityManagerNative.getDefault();
            boolean isAccessiblityEnabled = mAccessibilityManager.isEnabled();
            if (activityManager.isInLockTaskMode() && !isAccessiblityEnabled) {
                // If we recently long-pressed the other button then they were
                // long-pressed 'together'
                if (mSlimNavigationBarView.getRightMenuButton().isPressed()
                        && mSlimNavigationBarView.getLeftMenuButton().isPressed()) {
                    activityManager.stopLockTaskModeOnCurrent();
                    // When exiting refresh disabled flags.
                    mSlimNavigationBarView.setDisabledFlags(mDisabled1, true);
                    mSlimNavigationBarView.setOverrideMenuKeys(false);
                } else if ((v.getId() == mSlimNavigationBarView.getLeftMenuButton().getId())
                        && !mSlimNavigationBarView.getRightMenuButton().isPressed()) {
                    // If we aren't pressing recents right now then they presses
                    // won't be together, so send the standard long-press action.
                    sendBackLongPress = true;
                }
            } else {
                // If this is back still need to handle sending the long-press event.
                long time = System.currentTimeMillis();
                if (( time - mLastLockToAppLongPress) < 2000) {
                    if (v.getId() == mSlimNavigationBarView.getLeftMenuButton().getId()
                        || v.getId() == mSlimNavigationBarView.getRightMenuButton().getId()) {
                        sendBackLongPress = true;
                    }
                } else if (isAccessiblityEnabled && activityManager.isInLockTaskMode()) {
                    // When in accessibility mode a long press that is recents (not back)
                    // should stop lock task.
                    activityManager.stopLockTaskModeOnCurrent();
                    // When exiting refresh disabled flags.
                    mSlimNavigationBarView.setDisabledFlags(mDisabled1, true);
                    mSlimNavigationBarView.setOverrideMenuKeys(false);
                }
                mLastLockToAppLongPress = time;
            }
            return sendBackLongPress;
        } catch (RemoteException e) {
            Log.d(TAG, "Unable to reach activity manager", e);
            return false;
        }
    }  
}
