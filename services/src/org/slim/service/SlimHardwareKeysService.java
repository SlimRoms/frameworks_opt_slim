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

package org.slim.service;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Slog;
import android.view.KeyEvent;

import org.slim.constants.SlimServiceConstants;
import org.slim.hardware.keys.ISlimHardwareKeysService;
import org.slim.hardware.keys.ISlimHardwareKeysListener;

public class SlimHardwareKeysService extends SlimSystemService {

    private final Context mContext;

    private final IBinder mService = new ISlimHardwareKeysService.Stub() {
        private ISlimHardwareKeysListener mListener;

        @Override
        public void registerListener(ISlimHardwareKeysListener listener) {
            mListener = listener;
        }

        @Override
        public void unregisterListener(ISlimHardwareKeysListener listener) {
            mListener = null;
        }

        @Override
        public boolean sendKeyEventToListener(KeyEvent event) {
            if (mListener != null) {
                try {
                    return mListener.onHardwareKeyEvent(event);
                } catch (Exception e) {}
            }
            return false;
        }
    };

    public SlimHardwareKeysService(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    public void onStart() {
        publishBinderService(SlimServiceConstants.SLIM_HARDWARE_KEYS_SERVICE, mService);
    }
}
