/*
 * Copyright (C) 2016 The SlimRoms Project
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

import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import org.slim.framework.internal.statusbar.ISlimStatusBar;

public class SlimCommandQueue extends ISlimStatusBar.Stub {

    private static final int MSG_SHIFT = 61;
    private static final int MSG_MASK  = 0xffff << MSG_SHIFT;

    private static final int MSG_START_CUSTOM_INTENT_AFTER_KEYGUARD = 1 << MSG_SHIFT;
    private static final int MSG_TOGGLE_LAST_APP                    = 2 << MSG_SHIFT;
    private static final int MSG_TOGGLE_KILL_APP                    = 3 << MSG_SHIFT;
    private static final int MSG_TOGGLE_SCREENSHOT                  = 4 << MSG_SHIFT;

    private Callbacks mCallbacks;
    private Handler mHandler = new H();

    public interface Callbacks {
        public void showCustomIntentAfterKeyguard(Intent intent);
        public void toggleLastApp();
        public void toggleKillApp();
        public void toggleScreenshot();
    }

    public SlimCommandQueue(Callbacks callbacks) {
        mCallbacks = callbacks;
    }

    @Override
    public void showCustomIntentAfterKeyguard(Intent intent) {
        mHandler.removeMessages(MSG_START_CUSTOM_INTENT_AFTER_KEYGUARD);
        Message m = mHandler.obtainMessage(MSG_START_CUSTOM_INTENT_AFTER_KEYGUARD, 0, 0, intent);
        m.sendToTarget();
    }

    @Override
    public void toggleLastApp() {
        synchronized (mHandler) {
            mHandler.removeMessages(MSG_TOGGLE_LAST_APP);
            mHandler.obtainMessage(MSG_TOGGLE_LAST_APP, 0, 0, null).sendToTarget();
       }
    }

    @Override
    public void toggleKillApp() {
        synchronized (mHandler) {
            mHandler.removeMessages(MSG_TOGGLE_KILL_APP);
            mHandler.obtainMessage(MSG_TOGGLE_KILL_APP, 0, 0, null).sendToTarget();
        }
    }

    @Override
    public void toggleScreenshot() {
        synchronized (mHandler) {
            mHandler.removeMessages(MSG_TOGGLE_SCREENSHOT);
            mHandler.obtainMessage(MSG_TOGGLE_SCREENSHOT, 0, 0, null).sendToTarget();
        }
    }

    private final class H extends Handler {
        public void handleMessage(Message msg) {
            final int what = msg.what & MSG_MASK;
            switch (what) {
                case MSG_START_CUSTOM_INTENT_AFTER_KEYGUARD:
                    mCallbacks.showCustomIntentAfterKeyguard((Intent) msg.obj);
                    break;
                case MSG_TOGGLE_LAST_APP:
                    mCallbacks.toggleLastApp();
                    break;
                case MSG_TOGGLE_KILL_APP:
                    mCallbacks.toggleKillApp();
                    break;
                case MSG_TOGGLE_SCREENSHOT:
                    mCallbacks.toggleScreenshot();
                    break;
            }
        }
    }
}
