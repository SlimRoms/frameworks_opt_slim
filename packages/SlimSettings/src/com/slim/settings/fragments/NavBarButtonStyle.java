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

package com.slim.settings.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.slim.settings.SettingsPreferenceFragment;
import com.slim.settings.R;

import org.slim.framework.internal.logging.SlimMetricsLogger;
import slim.preference.colorpicker.ColorPickerPreference;
import slim.provider.SlimSettings;

public class NavBarButtonStyle extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "NavBarButtonStyle";
    private static final String PREF_NAV_BUTTON_COLOR_MODE = "nav_button_color_mode";

    private static final int MENU_RESET = Menu.FIRST;

    private boolean mCheckPreferences;

    ListPreference mNavigationBarButtonColorMode;

    @Override
    protected int getMetricsCategory() {
        return SlimMetricsLogger.NAV_BAR_STYLE_SETTINGS;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        refreshSettings();
    }

    private PreferenceScreen refreshSettings() {
        mCheckPreferences = false;
        PreferenceScreen prefs = getPreferenceScreen();
        if (prefs != null) {
            prefs.removeAll();
        }

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.navbar_button_style);

        prefs = getPreferenceScreen();

        mNavigationBarButtonColorMode =
            (ListPreference) prefs.findPreference(PREF_NAV_BUTTON_COLOR_MODE);
        int navigationBarButtonColorMode = SlimSettings.System.getInt(getContentResolver(),
                SlimSettings.System.NAVIGATION_BAR_BUTTON_TINT_MODE, 0);
        mNavigationBarButtonColorMode.setValue(String.valueOf(navigationBarButtonColorMode));
        mNavigationBarButtonColorMode.setSummary(mNavigationBarButtonColorMode.getEntry());
        mNavigationBarButtonColorMode.setOnPreferenceChangeListener(this);

        updateColorPreference();

        setHasOptionsMenu(true);
        mCheckPreferences = true;
        return prefs;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, org.slim.framework.internal.R.string.reset)
                // use the reset settings icon
                .setIcon(org.slim.framework.internal.R.drawable.ic_settings_reset)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                resetToDefault();
                return true;
             default:
                return super.onContextItemSelected(item);
        }
    }

    private void resetToDefault() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());
        alertDialog.setTitle(org.slim.framework.internal.R.string.reset);
        alertDialog.setMessage(R.string.navbar_button_style_reset_message);
        alertDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                SlimSettings.System.putInt(getActivity().getContentResolver(),
                        SlimSettings.System.NAVIGATION_BAR_BUTTON_TINT, -2);
                SlimSettings.System.putInt(getActivity().getContentResolver(),
                       SlimSettings.System.NAVIGATION_BAR_BUTTON_TINT_MODE, 0);
                SlimSettings.System.putInt(getActivity().getContentResolver(),
                        SlimSettings.System.NAVIGATION_BAR_GLOW_TINT, -2);
                refreshSettings();
            }
        });
        alertDialog.setNegativeButton(android.R.string.cancel, null);
        alertDialog.create().show();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (!mCheckPreferences) {
            return false;
        }
        if (preference == mNavigationBarButtonColorMode) {
            int index = mNavigationBarButtonColorMode.findIndexOfValue((String) newValue);
            int value = Integer.valueOf((String) newValue);
            SlimSettings.System.putInt(getContentResolver(),
                    SlimSettings.System.NAVIGATION_BAR_BUTTON_TINT_MODE,
                    value);
            mNavigationBarButtonColorMode.setSummary(
                mNavigationBarButtonColorMode.getEntries()[index]);
            updateColorPreference();
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void updateColorPreference() {
        int navigationBarButtonColorMode = SlimSettings.System.getInt(getContentResolver(),
                SlimSettings.System.NAVIGATION_BAR_BUTTON_TINT_MODE, 0);
        //mNavigationBarButtonColor.setEnabled(navigationBarButtonColorMode != 3);
    }
}