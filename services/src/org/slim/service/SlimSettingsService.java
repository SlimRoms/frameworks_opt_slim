package org.slim.service;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import slim.constants.SlimServiceConstants;
import slim.provider.ISettingsChangedListener;
import slim.provider.ISlimSettingsService;
import slim.provider.SlimSettings;

public class SlimSettingsService extends SlimSystemService {

    private Context mContext;

    private final HashMap<ISettingsChangedListener, List<Uri>> mListeners = new HashMap<>();

    private final IBinder mService = new ISlimSettingsService.Stub() {
        @Override
        public void registerListener(ISettingsChangedListener listener, Uri[] settingsUris) {
            synchronized (mListeners) {
                List<Uri> uris = mListeners.get(listener);
                if (uris == null) {
                    uris = new ArrayList<>();
                    mListeners.put(listener, uris);
                }
                for (Uri uri : settingsUris) {
                    uris.add(uri);
                    try {
                        listener.onChanged(uri.getPathSegments().get(1), getString(uri));
                    } catch (RemoteException e) {}
                }
            }
        }

        @Override
        public void onChange(Uri uri, String value) {
            Log.d("TEST", "uri=" + uri.toString() + " : value=" + value);
            Log.d("TEST", "authority=" + uri.getAuthority());
            String key= uri.getPathSegments().get(1);
            Log.d("TEST", "key=" + key);
            synchronized (mListeners) {
                final List<ISettingsChangedListener> listeners = new ArrayList<>();
                Log.d("TEST", "mListener.size=" + mListeners.size());
                for (Map.Entry<ISettingsChangedListener, List<Uri>> entry : mListeners.entrySet()) {
                    if (entry.getValue().contains(uri)) {
                        listeners.add(entry.getKey());
                    }
                }
                for (ISettingsChangedListener listener : listeners) {
                    try {
                        listener.onChanged(key, value);
                    } catch (RemoteException e) {}
                }
        }
        }

        @Override
        public void unregisterListener(ISettingsChangedListener listener) {
        }
    };

    public SlimSettingsService(Context context) {
        super(context);

        mContext = context;
    }

    @Override
    public void onStart() {
        publishBinderService(SlimServiceConstants.SLIM_SETTINGS_SERVICE, mService);
    }

    private String getString(Uri uri) {
        List<String> segments = uri.getPathSegments();
        String type = segments.get(0);
        String key = segments.get(1);

        ContentResolver resolver = mContext.getContentResolver();

        if (type.equals("system")) {
            return SlimSettings.System.getString(resolver, key);
        } else if (type.equals("secure")) {
            return SlimSettings.Secure.getString(resolver, key);
        } else if (type.equals("global")) {
            return SlimSettings.Global.getString(resolver, key);
        }
        return null;
    }
}
