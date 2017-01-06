package com.slim.settings.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
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

import java.lang.reflect.Field;

public class SlimPreferenceFragment extends SettingsPreferenceFragment {

    private static final int MENU_RESET = Menu.FIRST;
    private static final int DLG_RESET = 0;

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
                showDialogInner(DLG_RESET);
                return true;
             default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected int getMetricsCategory() {
        String screen = getArguments().getString("preference_xml", null);
        try {
            Class<?> clazz = Class.forName("org.slim.framework.internal.logging.SlimMetricsLogger");
            Field field = clazz.getField(screen.toUpperCase());
            return field.getInt(clazz);
        } catch (ClassNotFoundException|NoSuchFieldException|IllegalAccessException e) {
            e.printStackTrace();
            return SlimMetricsLogger.SLIM_SETTINGS;
        }
    }

    private void showDialogInner(int id) {
        DialogFragment newFragment = MyAlertDialogFragment.newInstance(id);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "dialog " + id);
    }

    public static class MyAlertDialogFragment extends DialogFragment {

        public static MyAlertDialogFragment newInstance(int id) {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("id", id);
            frag.setArguments(args);
            return frag;
        }

        SlimPreferenceFragment getOwner() {
            return (SlimPreferenceFragment) getTargetFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt("id");
            switch (id) {
                case DLG_RESET:
                    return new AlertDialog.Builder(getActivity())
                    .setTitle(org.slim.framework.internal.R.string.reset)
                    .setMessage(com.slim.settings.R.string.status_bar_clock_style_reset_message)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            PreferenceScreen prefScreen = getOwner().getPreferenceScreen();
                            for (int i = 0; i < prefScreen.getPreferenceCount(); i++) {
                                Preference pref = prefScreen.getPreference(i);
                                if (pref instanceof ColorPickerPreference) {
                                    ColorPickerPreference cpp = (ColorPickerPreference) pref;
                                    cpp.onColorChanged(cpp.getDefaultColor());
                                }
                            }
                        }
                    })
                    .create();
            }
            throw new IllegalArgumentException("unknown id " + id);
        }

        @Override
        public void onCancel(DialogInterface dialog) {

        }
    }
}
