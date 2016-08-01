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
package org.slim.statusbar;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;

import org.slim.constants.SlimServiceConstants;
import org.slim.framework.internal.statusbar.ISlimStatusBarService;

public class SlimStatusBarManager {

    private final Context mContext;

    private static SlimStatusBarManager sInstance;
    private static ISlimStatusBarService sService;

    private SlimStatusBarManager(Context context) {
        Context appContext = context.getApplicationContext();
        if (appContext != null) {
            mContext = appContext;
        } else {
            mContext = context;
        }

        sService = getService();
    }

    public synchronized static SlimStatusBarManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SlimStatusBarManager(context);
        }
        return sInstance;
    }

    public static ISlimStatusBarService getService() {
        if (sService != null) {
            return sService;
        }
        IBinder b = ServiceManager.getService(SlimServiceConstants.SLIM_STATUS_BAR_SERVICE);
        if (b != null) {
            sService = ISlimStatusBarService.Stub.asInterface(b);
            return sService;
        }
        return null;
    }
}
