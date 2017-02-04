package com.android.systemui.statusbar.slim;

import android.content.Context;

import com.android.systemui.qs.QSTile;
import com.android.systemui.statusbar.phone.QSTileHost;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.phone.StatusBarIconController;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.android.systemui.statusbar.phone.StatusBarWindowManager;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.BluetoothController;
import com.android.systemui.statusbar.policy.CastController;
import com.android.systemui.statusbar.policy.FlashlightController;
import com.android.systemui.statusbar.policy.HotspotController;
import com.android.systemui.statusbar.policy.KeyguardMonitor;
import com.android.systemui.statusbar.policy.LocationController;
import com.android.systemui.statusbar.policy.NetworkController;
import com.android.systemui.statusbar.policy.NextAlarmController;
import com.android.systemui.statusbar.policy.RotationLockController;
import com.android.systemui.statusbar.policy.SecurityController;
import com.android.systemui.statusbar.policy.UserInfoController;
import com.android.systemui.statusbar.policy.UserSwitcherController;
import com.android.systemui.statusbar.policy.ZenModeController;

public class SlimQSTileHost extends QSTileHost {

    public SlimQSTileHost(Context context, PhoneStatusBar statusBar,
            BluetoothController bluetooth, LocationController location,
            RotationLockController rotation, NetworkController network,
            ZenModeController zen, HotspotController hotspot,
            CastController cast, FlashlightController flashlight,
            UserSwitcherController userSwitcher, UserInfoController userInfo,
            KeyguardMonitor keyguard, SecurityController security,
            BatteryController battery, StatusBarIconController iconController,
            NextAlarmController nextAlarmController) {
        super(context, statusBar, bluetooth, location, rotation, network, zen, hotspot,
                cast, flashlight, userSwitcher, userInfo, keyguard, security, battery,
                iconController, nextAlarmController);
    }

    @Override
    public QSTile<?> createTile(String tileSpec) {
        // handle additional tiles here
        switch(tileSpec) {
            default:
                return super.createTile(tileSpec);
        }
    }
}
