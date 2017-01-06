package com.slim.settings.fragments;

import android.os.Bundle;

import com.slim.settings.SettingsPreferenceFragment;

import org.slim.framework.internal.logging.SlimMetricsLogger;

public class SlimPreferenceFragment extends SettingsPreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String screen = getArguments().getString("preference_xml", null);

        if (screen != null) {
            int id = getResources().getIdentifier(screen, "xml", "com.slim.settings");
            if (id > 0) {
                addPreferencesFromResource(id);
            }
        }
    }

    @Override
    public int getMetricsCategory() {
        String screen = getArguments().getString("preference_xml", null);
        return SlimMetricsLogger.SLIM_SETTINGS;
    }
}
