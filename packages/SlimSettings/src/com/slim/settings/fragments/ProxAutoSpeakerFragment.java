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
import org.slim.preference.SlimSwitchPreference;
import org.slim.provider.SlimSettings;

public class ProxAutoSpeakerFragment extends SettingsPreferenceFragment {

    @Override
    protected int getMetricsCategory() {
        return SlimMetricsLogger.PROXIMITY_AUTO_SPEAKER_SETTINGS;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.prox_auto_speaker);

    }
}

