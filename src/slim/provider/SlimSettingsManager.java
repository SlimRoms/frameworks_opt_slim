package slim.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.os.IBinder;
import android.os.ServiceManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import slim.constants.SlimServiceConstants;

public class SlimSettingsManager {

    private static SlimSettingsManager sInstance;
    private static ISlimSettingsService sService;


    private SlimSettingsManager() {
    }

    public static synchronized SlimSettingsManager getInstance() {
        if (sInstance == null) {
            sInstance = new SlimSettingsManager();
        }
        return sInstance;
    }

    public abstract class OnSettingsChangedListener extends ISettingsChangedListener.Stub {
    }

    public void registerListener(OnSettingsChangedListener listener, Uri... settingsUris) {
        try {
            getService().registerListener(listener, settingsUris);
        } catch (Exception e) {}
    }

    void onChange(Uri uri, String value) {
        try {
            getService().onChange(uri, value);
        } catch (Exception e) {}
    }

    private ISlimSettingsService getService() {
        if (sService != null) {
            return sService;
        }
        IBinder b = ServiceManager.getService(SlimServiceConstants.SLIM_SETTINGS_SERVICE);
        if (b != null) {
            sService = ISlimSettingsService.Stub.asInterface(b);
            return sService;
        }
        return null;
    }
}
