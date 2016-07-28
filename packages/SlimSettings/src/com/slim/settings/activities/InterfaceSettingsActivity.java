package com.slim.settings.activities;

import android.app.Fragment;

import com.android.settings.slim.InterfaceSettings;
import com.slim.settings.SettingsActivity;

public class InterfaceSettingsActivity extends SettingsActivity {

    @Override
    public Fragment getFragment() {
        return new InterfaceSettings();
    }
}
