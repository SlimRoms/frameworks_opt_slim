/*
 * Copyright (C) 2016 SlimRoms Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.systemui.statusbar.slim;

import android.app.ActivityManagerNative;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.view.WindowManager;

import com.android.systemui.R;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.phone.PhoneStatusBarView;

import org.slim.provider.SlimSettings;

public class SlimStatusBar extends PhoneStatusBar {

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(SlimSettings.System.getUriFor(
                    SlimSettings.System.USE_SLIM_RECENTS), false, this,
                    UserHandle.USER_ALL);
            resolver.registerContentObserver(SlimSettings.System.getUriFor(
                    SlimSettings.System.RECENT_CARD_BG_COLOR), false, this,
                    UserHandle.USER_ALL);
            resolver.registerContentObserver(SlimSettings.System.getUriFor(
                    SlimSettings.System.RECENT_CARD_TEXT_COLOR), false, this,
                    UserHandle.USER_ALL);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);

            if (uri.equals(SlimSettings.System.getUriFor(
                    SlimSettings.System.USE_SLIM_RECENTS))) {
                updateRecents();
            } else if (uri.equals(SlimSettings.System.getUriFor(
                    SlimSettings.System.RECENT_CARD_BG_COLOR))
                    || uri.equals(SlimSettings.System.getUriFor(
                    SlimSettings.System.RECENT_CARD_TEXT_COLOR))) {
                rebuildRecentsScreen();
            }
        }
    }

    @Override
    public void start() {
        super.start();

        SettingsObserver observer = new SettingsObserver(mHandler);
        observer.observe();
    }

    @Override
    protected PhoneStatusBarView makeStatusBarView() {
        PhoneStatusBarView statusBarView = super.makeStatusBarView();

        SlimBatteryContainer container = (SlimBatteryContainer) statusBarView.findViewById(R.id.slim_battery_container);
        if (mBatteryController != null) {
            container.setBatteryController(mBatteryController);
        }

        return statusBarView;
    }
}
