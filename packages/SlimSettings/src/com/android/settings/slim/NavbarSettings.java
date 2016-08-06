/*
 * Copyright (C) 2012-2016 SlimRoms Project
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

package com.android.settings.slim;

import android.os.Bundle;
import android.content.Intent;
import android.preference.SwitchPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;

import com.android.settings.SettingsPreferenceFragment;
import com.slim.settings.R;

import org.slim.framework.internal.logging.SlimMetricsLogger;
import org.slim.provider.SlimSettings;
import org.slim.utils.DeviceUtils;

public class NavbarSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "NavBar";
    private static final String PREF_MENU_LOCATION = "pref_navbar_menu_location";
    private static final String PREF_NAVBAR_MENU_DISPLAY = "pref_navbar_menu_display";
    private static final String ENABLE_NAVIGATION_BAR = "enable_nav_bar";
    private static final String PREF_BUTTON = "navbar_button_settings";
    private static final String PREF_BUTTON_STYLE = "nav_bar_button_style";
    private static final String PREF_STYLE_DIMEN = "navbar_style_dimen_settings";
    private static final String PREF_NAVIGATION_BAR_CAN_MOVE = "navbar_can_move";

    private int mNavBarMenuDisplayValue;

    SwitchPreference mEnableNavigationBar;
    SwitchPreference mNavigationBarCanMove;
    PreferenceScreen mButtonStylePreference;
    PreferenceScreen mStyleDimenPreference;

    private final static Intent mIntent = new Intent("android.intent.action.NAVBAR_EDIT");

    @Override
    protected int getMetricsCategory() {
        return SlimMetricsLogger.NAV_BAR_SETTINGS;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.navbar_settings);

        PreferenceScreen prefs = getPreferenceScreen();

        mButtonStylePreference = (PreferenceScreen) findPreference(PREF_BUTTON_STYLE);
        mStyleDimenPreference = (PreferenceScreen) findPreference(PREF_STYLE_DIMEN);

        final int showByDefault = getContext().getResources().getBoolean(
                com.android.internal.R.bool.config_showNavigationBar) ? 1 : 0;
        boolean enableNavigationBar = SlimSettings.System.getInt(getContentResolver(),
                SlimSettings.System.NAVIGATION_BAR_SHOW, showByDefault) == 1;
        mEnableNavigationBar = (SwitchPreference) findPreference(ENABLE_NAVIGATION_BAR);
        // disable switch until we have other navigation options
        if (showByDefault == 1) {
            prefs.removePreference(mEnableNavigationBar);
        } else {
            mEnableNavigationBar.setOnPreferenceChangeListener(this);
        }

        mNavigationBarCanMove = (SwitchPreference) findPreference(PREF_NAVIGATION_BAR_CAN_MOVE);
        mNavigationBarCanMove.setChecked(SlimSettings.System.getInt(getContentResolver(),
                SlimSettings.System.NAVIGATION_BAR_CAN_MOVE,
                DeviceUtils.isPhone(getActivity()) ? 1 : 0) == 0);
        mNavigationBarCanMove.setOnPreferenceChangeListener(this);

        findPreference("navbar_editor").setOnPreferenceClickListener(new OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                mIntent.removeExtra("edit");
                getActivity().sendBroadcast(mIntent);
                return true;
           }
        });

        updateNavbarPreferences(enableNavigationBar);
    }

    @Override
    public void onPause() {
        super.onPause();
        mIntent.putExtra("edit", false);
        getActivity().sendBroadcast(mIntent);
    }

    private void updateNavbarPreferences(boolean show) {
        mButtonStylePreference.setEnabled(show);
        mStyleDimenPreference.setEnabled(show);
        mNavigationBarCanMove.setEnabled(show);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mEnableNavigationBar) {
            SlimSettings.System.putInt(getActivity().getContentResolver(),
                    SlimSettings.System.NAVIGATION_BAR_SHOW,
                    ((Boolean) newValue) ? 1 : 0);
            updateNavbarPreferences((Boolean) newValue);
            return true;
        } else if (preference == mNavigationBarCanMove) {
            SlimSettings.System.putInt(getActivity().getContentResolver(),
                    SlimSettings.System.NAVIGATION_BAR_CAN_MOVE,
                    ((Boolean) newValue) ? 0 : 1);
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

}
