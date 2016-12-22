/*
 * Copyright (C) 2013-2016 SlimRoms project
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
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.slim.settings.R;
import com.slim.settings.SettingsPreferenceFragment;

import org.slim.framework.internal.logging.SlimMetricsLogger;
import org.slim.preference.SlimSeekBarPreference;
import org.slim.provider.SlimSettings;

public class ProxAutoSpeakerFragment extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String PROX_AUTO_SPEAKER_OPTIONS = "prox_auto_speaker_options";
    private static final String PROX_AUTO_SPEAKER  = "prox_auto_speaker";
    private static final String PROX_AUTO_SPEAKER_DELAY  = "prox_auto_speaker_delay";
    private static final String PROX_AUTO_SPEAKER_INCALL_ONLY  = "prox_auto_speaker_incall_only";

    private static final String SYSTEMUI_METADATA_NAME = "com.android.systemui";

	private SwitchPreference mProxSpeaker;
    private SlimSeekBarPreference mProxSpeakerDelay;
    private SwitchPreference mProxSpeakerIncallOnly;


    @Override
    protected int getMetricsCategory() {
        return SlimMetricsLogger.PROX_AUTO_SPEAKER_SETTINGS;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Activity activity = getActivity();
        PreferenceScreen prefSet = getPreferenceScreen();
        Resources res = getResources();

        addPreferencesFromResource(R.xml.prox_auto_speaker);

		//delay
		mProxSpeaker = (SwitchPreference) findPreference(PROX_AUTO_SPEAKER);
        mProxSpeakerIncallOnly = (SwitchPreference) findPreference(PROX_AUTO_SPEAKER_INCALL_ONLY);
        mProxSpeakerDelay = (SlimSeekBarPreference) findPreference(PROX_AUTO_SPEAKER_DELAY);
        if (mProxSpeakerDelay != null) {
            mProxSpeakerDelay.setDefault(100);
            mProxSpeakerDelay.isMilliseconds(true);
            mProxSpeakerDelay.setInterval(1);
            mProxSpeakerDelay.minimumValue(100);
            mProxSpeakerDelay.multiplyValue(100);
            mProxSpeakerDelay.setOnPreferenceChangeListener(this);
        }
	}

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mProxSpeaker) {
            SlimSettings.System.putInt(getContentResolver(),
                    SlimSettings.System.PROXIMITY_AUTO_SPEAKER, mProxSpeaker.isChecked() ? 1 : 0);
        }
		if (preference == mProxSpeakerIncallOnly) {
            SlimSettings.System.putInt(getContentResolver(),
                    SlimSettings.System.PROXIMITY_AUTO_SPEAKER_INCALL_ONLY,
                    mProxSpeakerIncallOnly.isChecked() ? 1 : 0);
        }
        if (preference == mProxSpeakerDelay) {
            int delay = Integer.valueOf((String) newValue);
            SlimSettings.System.putInt(getContentResolver(),
                    SlimSettings.System.PROXIMITY_AUTO_SPEAKER_DELAY, delay);
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateState();
    }

    private void updateState() {
        final Activity activity = getActivity();

        final ContentResolver contentResolver = getContentResolver();
        
        PreferenceScreen prefSet = getPreferenceScreen();
        
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.prox_auto_speaker);

        boolean configEnabledSpeakerProx = getResources().getBoolean(org.slim.framework.internal.R.bool.config_enabled_speakerprox);

        if (mProxSpeaker != null) {
            PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
            if (pm.isWakeLockLevelSupported(
                    PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK)
                    && configEnabledSpeakerProx) {
                mProxSpeaker.setChecked(SlimSettings.System.getInt(contentResolver,
                        SlimSettings.System.PROXIMITY_AUTO_SPEAKER, 0) == 1);
                if (mProxSpeakerIncallOnly != null) {
                    mProxSpeakerIncallOnly.setChecked(SlimSettings.System.getInt(contentResolver,
                            SlimSettings.System.PROXIMITY_AUTO_SPEAKER_INCALL_ONLY, 0) == 1);
                }
                if (mProxSpeakerDelay != null) {
                    final int proxDelay = SlimSettings.System.getInt(getContentResolver(),
                            SlimSettings.System.PROXIMITY_AUTO_SPEAKER_DELAY, 100);
                    // minimum 100 is 1 interval of the 100 multiplier
                    mProxSpeakerDelay.setInitValue((proxDelay / 100) - 1);
                }
            } else {
                prefSet.removePreference(mProxSpeaker);
                mProxSpeaker = null;
                if (mProxSpeakerIncallOnly != null) {
                    prefSet.removePreference(mProxSpeakerIncallOnly);
                    mProxSpeakerIncallOnly = null;
                }
                if (mProxSpeakerDelay != null) {
                    prefSet.removePreference(mProxSpeakerDelay);
                    mProxSpeakerDelay = null;
                }
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

