package slim.provider;

import android.net.Uri;

import slim.provider.ISettingsChangedListener;

interface ISlimSettingsService {

    void registerListener(ISettingsChangedListener listener, in Uri[] uri);
    void unregisterListener(ISettingsChangedListener listener);

    void onChange(in Uri uri, String value);
}
