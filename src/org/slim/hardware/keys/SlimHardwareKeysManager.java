package org.slim.hardware.keys;

import android.content.Context;
import android.os.IBinder;
import android.os.ServiceManager;

import org.slim.constants.SlimServiceConstants;
import org.slim.hardware.keys.ISlimHardwareKeysService;

public class SlimHardwareKeysManager {

    private final Context mContext;

    private static SlimHardwareKeysManager sInstance;
    private static ISlimHardwareKeysService sService;

    private SlimHardwareKeysManager(Context context) {
        Context appContext = context.getApplicationContext();
        if (appContext != null) {
            mContext = appContext;
        } else {
            mContext = context;
        }
        sService = getService();
    }

    public synchronized static SlimHardwareKeysManager getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new SlimHardwareKeysManager(context);
        }
        return sInstance;
    }

    public static ISlimHardwareKeysService getService() {
        android.util.Log.d("TEST", "sService == " + (sService == null));
        if (sService != null) {
            return sService;
        }
        IBinder b = ServiceManager.getService(SlimServiceConstants.SLIM_HARDWARE_KEYS_SERVICE);
        if (b != null) {
            sService = ISlimHardwareKeysService.Stub.asInterface(b);
            return sService;
        }
        return null;
    }
}
