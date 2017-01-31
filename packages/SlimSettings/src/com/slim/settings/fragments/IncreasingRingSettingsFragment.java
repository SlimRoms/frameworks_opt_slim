/*
 * Copyright (C) 2013 Slimroms
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

package com.slim.settings.fragments;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.Vibrator;
import android.preference.SeekBarVolumizer;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.provider.SearchIndexableResource;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.TwoStatePreference;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedPreference;
//import com.android.settings.notification.SoundSettings;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import com.slim.settings.preference.IncreasingRingVolumePreference;
import com.slim.settings.R;
import com.slim.settings.SettingsPreferenceFragment;

import org.slim.framework.internal.logging.SlimMetricsLogger;
import slim.preference.SlimSeekBarPreferencev2;
import slim.provider.SlimSettings;

import static com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;

public class IncreasingRingSettingsFragment extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener {

    private static final String KEY_INCREASING_RING = "increasing_ring";
    private static final String KEY_INCREASING_RING_VOLUME = "increasing_ring_volume";
    public static final String INCREASING_RING_RAMP_UP_TIME = "increasing_ring_ramp_up_time";

    private static final String SYSTEMUI_METADATA_NAME = "com.android.systemui";

    private PackageManager mPM;
    private boolean mVoiceCapable;

    private TwoStatePreference mIncreasingRing;
    private IncreasingRingVolumePreference mIncreasingRingVolume;

    @Override
    protected int getMetricsCategory() {
        return SlimMetricsLogger.INCREASING_RING_SETTINGS;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceScreen prefSet = getPreferenceScreen();
        Resources res = getResources();

        addPreferencesFromResource(R.xml.increasing_ring_settings);

        initIncreasingRing();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mIncreasingRing) {
            boolean value = (Boolean) newValue;
            SlimSettings.System.putInt(getContentResolver(),
                    SlimSettings.System.INCREASING_RING, value ? 1 : 0);
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mIncreasingRingVolume != null) {
            mIncreasingRingVolume.onActivityResume();
        }
        updateState();
    }

    @Override
    public void onPause() {
        if (mIncreasingRingVolume != null) {
            mIncreasingRingVolume.stopSample();
        }
    
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mIncreasingRingVolume != null) {
            mIncreasingRingVolume.onActivityStop();
            mIncreasingRingVolume.stopSample();
        }
    }

    private void updateState() {
        final Activity activity = getActivity();

    }

    // === Increasing ringtone ===

    private void initIncreasingRing() {
        PreferenceScreen root = getPreferenceScreen();
        mIncreasingRing = (TwoStatePreference)
		        root.findPreference(Settings.System.INCREASING_RING);
        mIncreasingRingVolume = (IncreasingRingVolumePreference)
                root.findPreference(KEY_INCREASING_RING_VOLUME);

        if (mIncreasingRing == null || mIncreasingRingVolume == null || !mVoiceCapable) {
            if (mIncreasingRing != null) {
                root.removePreference(mIncreasingRing);
                mIncreasingRing = null;
            }
            if (mIncreasingRingVolume != null) {
                root.removePreference(mIncreasingRingVolume);
                mIncreasingRingVolume = null;
            }
        }
    }

    private static Boolean getConfigBoolean(Context context, String configBooleanName) {
        int resId = -1;
        Boolean b = true;
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            return null;
        }

        Resources systemUiResources;
        try {
            systemUiResources = pm.getResourcesForApplication(SYSTEMUI_METADATA_NAME);
        } catch (Exception e) {
            Log.e("DozeSettings:", "can't access systemui resources",e);
            return null;
        }

        resId = systemUiResources.getIdentifier(
            SYSTEMUI_METADATA_NAME + ":bool/" + configBooleanName, null, null);
        if (resId > 0) {
            b = systemUiResources.getBoolean(resId);
        }
        return b;
    }

    private static Integer getConfigInteger(Context context, String configIntegerName) {
        int resId = -1;
        Integer i = 1;
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            return null;
        }

        Resources systemUiResources;
        try {
            systemUiResources = pm.getResourcesForApplication(SYSTEMUI_METADATA_NAME);
        } catch (Exception e) {
            Log.e("DozeSettings:", "can't access systemui resources",e);
            return null;
        }

        resId = systemUiResources.getIdentifier(
            SYSTEMUI_METADATA_NAME + ":integer/" + configIntegerName, null, null);
        if (resId > 0) {
            i = systemUiResources.getInteger(resId);
        }
        return i;
    }
}
