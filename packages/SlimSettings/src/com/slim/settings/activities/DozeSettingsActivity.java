package com.slim.settings.activities;

import android.app.Fragment;

import com.android.settings.slim.fragments.DozeSettingsFragment;
import com.slim.settings.SettingsActivity;

public class DozeSettingsActivity extends SettingsActivity {

    @Override
    public Fragment getFragment() {
        return new DozeSettingsFragment();
    }
}
