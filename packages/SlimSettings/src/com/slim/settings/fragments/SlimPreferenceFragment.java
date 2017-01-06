package com.slim.settings.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.slim.settings.SettingsActivity;
import com.slim.settings.SettingsPreferenceFragment;

import slim.preference.colorpicker.ColorPickerPreference;

import org.slim.framework.internal.logging.SlimMetricsLogger;

public class SlimPreferenceFragment extends SettingsPreferenceFragment {

    private static final int MENU_RESET = Menu.FIRST;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String screen = getArguments().getString("preference_xml", null);
        android.util.Log.d("TEST", "screen=" + screen);


        if (screen != null) {
            int id = getResources().getIdentifier(screen, "xml", "com.slim.settings");
            if (id > 0) {
                addPreferencesFromResource(id);
            }
        }

        PreferenceScreen prefScreen = getPreferenceScreen();
        if (prefScreen != null && prefScreen.getPreferenceCount() == 1) {
            Preference pref = prefScreen.getPreference(0);
            if (pref instanceof PreferenceScreen) {
                if (getActivity() instanceof SettingsActivity) {
                    SettingsActivity sa = (SettingsActivity) getActivity();
                    sa.startPreferencePanel(pref.getFragment(), pref.getExtras(), pref.getTitle(),
                            null, 0, true);
                    sa.finish();
                }
            }
        }

        for (int i = 0; i < prefScreen.getPreferenceCount(); i++) {
            Preference pref = prefScreen.getPreference(i);
            if (pref instanceof ColorPickerPreference) {
                setHasOptionsMenu(true);
                break;
            }
        }
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
        alertDialog.setMessage(com.slim.settings.R.string.navbar_button_style_reset_message);
        alertDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                PreferenceScreen prefScreen = getPreferenceScreen();
                for (int i = 0; i < prefScreen.getPreferenceCount(); i++) {
                    Preference pref = prefScreen.getPreference(i);
                    if (pref instanceof ColorPickerPreference) {
                        ColorPickerPreference cpp = (ColorPickerPreference) pref;
                        cpp.onColorChanged(cpp.getDefaultColor());
                    }
                }
            }
        });
        alertDialog.setNegativeButton(android.R.string.cancel, null);
        alertDialog.create().show();
    }

    @Override
    public int getMetricsCategory() {
        String screen = getArguments().getString("preference_xml", null);
        return SlimMetricsLogger.SLIM_SETTINGS;
    }
}
