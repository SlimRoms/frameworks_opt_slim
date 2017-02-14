package slim.provider;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SlimSettingsManager {

    private static SlimSettingsManager sInstance;

    private final HashMap<OnSettingsChangedListener, List<Uri>> mListeners = new HashMap<>();

    private SlimSettingsManager() {
    }

    public static synchronized SlimSettingsManager getInstance() {
        if (sInstance == null) {
            sInstance = new SlimSettingsManager();
        }
        return sInstance;
    }

    public interface OnSettingsChangedListener {
        public void onChanged(Uri uri);
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
                listener.onChanged(uri);
            }
        }
    }

    void onChange(Uri uri) {
        Log.d("TEST", "uri=" + uri.toString());
        synchronized (mListeners) {
            final List<OnSettingsChangedListener> listeners = new ArrayList<>();
            for (Map.Entry<OnSettingsChangedListener, List<Uri>> entry : mListeners.entrySet()) {
                if (entry.getValue().contains(uri)) {
                    listeners.add(entry.getKey());
                }
            }
            for (OnSettingsChangedListener listener : listeners) {
                listener.onChanged(uri);
            }
        }
    }
}
