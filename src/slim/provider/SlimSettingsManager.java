package slim.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlimSettingsManager {

    private static SlimSettingsManager sInstance;
    private final ContentResolver mResolver;

    private final HashMap<OnSettingsChangedListener, List<Uri>> mListeners = new HashMap<>();

    private SlimSettingsManager(ContentResolver resolver) {
        mResolver = resolver;
    }

    public static synchronized SlimSettingsManager getInstance(ContentResolver resolver) {
        if (sInstance == null) {
            sInstance = new SlimSettingsManager(resolver);
        }
        return sInstance;
    }

    public interface OnSettingsChangedListener {
        public void onChanged(String name, String value);
    }

    private String getString(Uri uri) {
        List<String> segments = uri.getPathSegments();
        String type = segments.get(0);
        String key = segments.get(1);

        if (type.equals("system")) {
            return SlimSettings.System.getString(mResolver, key);
        } else if (type.equals("secure")) {
            return SlimSettings.Secure.getString(mResolver, key);
        } else if (type.equals("global")) {
            return SlimSettings.Global.getString(mResolver, key);
        }
        return null;
    }

    public void registerListener(OnSettingsChangedListener listener, Uri... settingsUris) {
        synchronized (mListeners) {
            List<Uri> uris = mListeners.get(listener);
            if (uris == null) {
                uris = new ArrayList<>();
                mListeners.put(listener, uris);
            }
            for (Uri uri : settingsUris) {
                uris.add(uri);
                listener.onChanged(uri.getPathSegments().get(1), getString(uri));
            }
        }
    }

    void onChange(String key, String value) {
        Log.d("TEST", "key=" + key + " : value=" + value);
        synchronized (mListeners) {
            final List<OnSettingsChangedListener> listeners = new ArrayList<>();
            for (Map.Entry<OnSettingsChangedListener, List<Uri>> entry : mListeners.entrySet()) {
                if (entry.getValue().contains(key)) {
                    listeners.add(entry.getKey());
                }
            }
            for (OnSettingsChangedListener listener : listeners) {
                listener.onChanged(key, value);
            }
        }
    }
}
