package com.slim.settings.activities;

import android.app.Fragment;

import com.android.settings.slim.NavigationSettings;
import com.slim.settings.SettingsActivity;

public class NavigationSettingsActivity extends SettingsActivity {

    @Override
    public Fragment getFragment() {
        return new NavigationSettings();
    }
}
