/*
 * Copyright (C) 2014-2016 SlimRoms Project
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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.SwitchPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.android.settings.slim.ButtonBacklightBrightness;
import com.android.settings.SettingsPreferenceFragment;
import com.slim.settings.R;

import org.slim.framework.internal.logging.SlimMetricsLogger;
import org.slim.action.ActionConstants;

import org.slim.action.ActionHelper;
import org.slim.constants.SlimServiceConstants;
import org.slim.hardware.CMHardwareManager;
import org.slim.hardware.keys.ISlimHardwareKeysListener;
import org.slim.hardware.keys.SlimHardwareKeysManager;
import org.slim.provider.SlimSettings;
import org.slim.utils.AppHelper;
import org.slim.utils.DeviceUtils;
import org.slim.utils.DeviceUtils.FilteredDeviceFeaturesArray;
import org.slim.utils.HwKeyHelper;
import org.slim.utils.ShortcutPickerHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HardwareKeysSettings extends PreferenceActivity implements
        OnPreferenceChangeListener, OnPreferenceClickListener,
        ShortcutPickerHelper.OnPickListener {

    private static final String TAG = "HardwareKeys";

    private static final String CATEGORY_KEYS = "button_keys";
    private static final String CATEGORY_BACK = "button_keys_back";
    private static final String CATEGORY_CAMERA = "button_keys_camera";
    private static final String CATEGORY_HOME = "button_keys_home";
    private static final String CATEGORY_MENU = "button_keys_menu";
    private static final String CATEGORY_ASSIST = "button_keys_assist";
    private static final String CATEGORY_APPSWITCH = "button_keys_appSwitch";

    private static final String KEYS_CATEGORY_BINDINGS = "keys_bindings";
    private static final String KEYS_ENABLE_CUSTOM = "enable_hardware_rebind";
    private static final String KEYS_BACK_PRESS = "keys_back_press";
    private static final String KEYS_BACK_LONG_PRESS = "keys_back_long_press";
    private static final String KEYS_BACK_DOUBLE_TAP = "keys_back_double_tap";
    private static final String KEYS_CAMERA_PRESS = "keys_camera_press";
    private static final String KEYS_CAMERA_LONG_PRESS = "keys_camera_long_press";
    private static final String KEYS_CAMERA_DOUBLE_TAP = "keys_camera_double_tap";
    private static final String KEYS_HOME_PRESS = "keys_home_press";
    private static final String KEYS_HOME_LONG_PRESS = "keys_home_long_press";
    private static final String KEYS_HOME_DOUBLE_TAP = "keys_home_double_tap";
    private static final String KEYS_MENU_PRESS = "keys_menu_press";
    private static final String KEYS_MENU_LONG_PRESS = "keys_menu_long_press";
    private static final String KEYS_MENU_DOUBLE_TAP = "keys_menu_double_tap";
    private static final String KEYS_ASSIST_PRESS = "keys_assist_press";
    private static final String KEYS_ASSIST_LONG_PRESS = "keys_assist_long_press";
    private static final String KEYS_ASSIST_DOUBLE_TAP = "keys_assist_double_tap";
    private static final String KEYS_APP_SWITCH_PRESS = "keys_app_switch_press";
    private static final String KEYS_APP_SWITCH_LONG_PRESS = "keys_app_switch_long_press";
    private static final String KEYS_APP_SWITCH_DOUBLE_TAP = "keys_app_switch_double_tap";

    private static final int SINGLE_TAP_ACTION = 0;
    private static final int DOUBLE_TAP_ACTION = 1;
    private static final int LONG_PRESS_ACTION = 2;

    private static final int DLG_SHOW_WARNING_DIALOG       = 0;
    private static final int DLG_SHOW_ACTION_SELECT_DIALOG = 1;
    private static final int DLG_SHOW_ACTION_DIALOG        = 2;
    private static final int DLG_RESET_TO_DEFAULT          = 3;

    private static final int MENU_RESET = Menu.FIRST;

    // Masks for checking presence of hardware keys.
    // Must match values in frameworks/base/core/res/res/values/config.xml
    private static final int KEY_MASK_HOME       = 0x01;
    private static final int KEY_MASK_BACK       = 0x02;
    private static final int KEY_MASK_MENU       = 0x04;
    private static final int KEY_MASK_ASSIST     = 0x08;
    private static final int KEY_MASK_APP_SWITCH = 0x10;
    private static final int KEY_MASK_CAMERA     = 0x20;

    private SwitchPreference mEnableCustomBindings;
    private Preference mBackPressAction;
    private Preference mBackLongPressAction;
    private Preference mBackDoubleTapAction;
    private Preference mCameraPressAction;
    private Preference mCameraLongPressAction;
    private Preference mCameraDoubleTapAction;
    private Preference mHomePressAction;
    private Preference mHomeLongPressAction;
    private Preference mHomeDoubleTapAction;
    private Preference mMenuPressAction;
    private Preference mMenuLongPressAction;
    private Preference mMenuDoubleTapAction;
    private Preference mAssistPressAction;
    private Preference mAssistLongPressAction;
    private Preference mAssistDoubleTapAction;
    private Preference mAppSwitchPressAction;
    private Preference mAppSwitchLongPressAction;
    private Preference mAppSwitchDoubleTapAction;

    private boolean mCheckPreferences;
    private static Map<String, String> mKeySettings = new HashMap<String, String>();

    private ShortcutPickerHelper mPicker;
    private String mPendingSettingsKey;
    private static FilteredDeviceFeaturesArray sFinalActionDialogArray;

    private Preference mEditor;
    private boolean mEditing = false;

    private ISlimHardwareKeysListener.Stub mKeyListener = new  ISlimHardwareKeysListener.Stub() {

        @Override
        public boolean onHardwareKeyEvent(KeyEvent event) {
            final int flags = event.getFlags();
            final boolean longpress = (flags & KeyEvent.FLAG_LONG_PRESS) != 0;
            if (longpress || event.getAction() != KeyEvent.ACTION_UP) {
                return true;
            } else if (!mEditing) {
                return false;
            }
            int keyCode = event.getKeyCode();
            int key = -1;
            if (keyCode == KeyEvent.KEYCODE_HOME) {
                key = R.string.keys_home_press_title;
            } else if (keyCode == KeyEvent.KEYCODE_MENU) {
                key = R.string.keys_menu_press_title;
            } else if (keyCode == KeyEvent.KEYCODE_BACK) {
                key = R.string.keys_back_press_title;
            } else if (keyCode == KeyEvent.KEYCODE_ASSIST) {
                key = R.string.keys_assist_press_title;
            } else if (keyCode == KeyEvent.KEYCODE_APP_SWITCH) {
                key = R.string.keys_app_switch_press_title;
            } else if (keyCode == KeyEvent.KEYCODE_CAMERA) {
                key = R.string.keys_camera_press_title;
            }
            if (key > 0) {
                showDialogInner(DLG_SHOW_ACTION_SELECT_DIALOG, "", key, keyCode);
                return true;
            }
            return false;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPicker = new ShortcutPickerHelper(this, this);

        // Before we start filter out unsupported options on the
        // ListPreference values and entries
        Resources res = getResources();
        sFinalActionDialogArray = new FilteredDeviceFeaturesArray();
        sFinalActionDialogArray = DeviceUtils.filterUnsupportedDeviceFeatures(this,
            res.getStringArray(res.getIdentifier(
                    "shortcut_action_hwkey_values", "array", "org.slim.framework")),
            res.getStringArray(res.getIdentifier(
                    "shortcut_action_hwkey_entries", "array", "org.slim.framework")));

        getActionBar().setDisplayHomeAsUpEnabled(true);

        // Attach final settings screen.
        reloadSettings();
    }

    private PreferenceScreen reloadSettings() {
        mCheckPreferences = false;
        PreferenceScreen prefs = getPreferenceScreen();
        if (prefs != null) {
            prefs.removeAll();
        }

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.hardwarekeys_settings);
        prefs = getPreferenceScreen();

        int deviceKeys = getResources().getInteger(
                org.slim.framework.internal.R.integer.config_deviceHardwareKeys);

        boolean hasHomeKey = (deviceKeys & KEY_MASK_HOME) != 0;

        mEnableHwKeys = (SwitchPreference) findPreference(KEY_ENABLE_HW_KEYS);
        mEnableHwKeys.setOnPreferenceClickListener(this);

        final ButtonBacklightBrightness backlight = (ButtonBacklightBrightness)
                findPreference(KEY_BUTTON_BACKLIGHT);
        if (!backlight.isButtonSupported()) {
            getPreferenceScreen().removePreference(backlight);
        }

        updateKeyMap(this);

        mEditor = findPreference("editor");
        mEditor.setOnPreferenceClickListener(this);

        // Handle warning dialog.
        SharedPreferences preferences =
                getSharedPreferences("hw_key_settings", Activity.MODE_PRIVATE);
        if (hasHomeKey && !hasHomeKey() && !preferences.getBoolean("no_home_action", false)) {
            preferences.edit()
                    .putBoolean("no_home_action", true).commit();
            showDialogInner(DLG_SHOW_WARNING_DIALOG, null, 0, -1);
        } else if (hasHomeKey()) {
            preferences.edit()
                    .putBoolean("no_home_action", false).commit();
        }

        mCheckPreferences = true;
        return prefs;
    }

    private static void updateKeyMap(Context context) {
        int deviceKeys = context.getResources().getInteger(
                org.slim.framework.internal.R.integer.config_deviceHardwareKeys);

        boolean hasBackKey = (deviceKeys & KEY_MASK_BACK) != 0;
        boolean hasHomeKey = (deviceKeys & KEY_MASK_HOME) != 0;
        boolean hasMenuKey = (deviceKeys & KEY_MASK_MENU) != 0;
        boolean hasAssistKey = (deviceKeys & KEY_MASK_ASSIST) != 0;
        boolean hasAppSwitchKey = (deviceKeys & KEY_MASK_APP_SWITCH) != 0;
        boolean hasCameraKey = (deviceKeys & KEY_MASK_CAMERA) != 0;

        mKeySettings.clear();

        mKeySettings.put(SlimSettings.System.KEY_HOME_ACTION,
                HwKeyHelper.getPressOnHomeBehavior(context, false));
        mKeySettings.put(SlimSettings.System.KEY_HOME_DOUBLE_TAP_ACTION,
                HwKeyHelper.getDoubleTapOnHomeBehavior(context, false));
        mKeySettings.put(SlimSettings.System.KEY_HOME_LONG_PRESS_ACTION,
                HwKeyHelper.getLongPressOnHomeBehavior(context, false));

        mKeySettings.put(SlimSettings.System.KEY_BACK_ACTION,
                HwKeyHelper.getPressOnBackBehavior(context, false));
        mKeySettings.put(SlimSettings.System.KEY_BACK_DOUBLE_TAP_ACTION,
                HwKeyHelper.getDoubleTapOnBackBehavior(context, false));
        mKeySettings.put(SlimSettings.System.KEY_BACK_LONG_PRESS_ACTION,
                HwKeyHelper.getLongPressOnBackBehavior(context, false));

        mKeySettings.put(SlimSettings.System.KEY_CAMERA_ACTION,
                HwKeyHelper.getPressOnCameraBehavior(context, false));
        mKeySettings.put(SlimSettings.System.KEY_CAMERA_DOUBLE_TAP_ACTION,
                HwKeyHelper.getDoubleTapOnCameraBehavior(context, false));
        mKeySettings.put(SlimSettings.System.KEY_CAMERA_LONG_PRESS_ACTION,
                HwKeyHelper.getLongPressOnCameraBehavior(context, false));

        mKeySettings.put(SlimSettings.System.KEY_MENU_ACTION,
                HwKeyHelper.getPressOnMenuBehavior(context, false));
        mKeySettings.put(SlimSettings.System.KEY_MENU_DOUBLE_TAP_ACTION,
                HwKeyHelper.getDoubleTapOnMenuBehavior(context, false));
        mKeySettings.put(SlimSettings.System.KEY_MENU_LONG_PRESS_ACTION,
                HwKeyHelper.getLongPressOnMenuBehavior(context, false, hasAssistKey));

        mKeySettings.put(SlimSettings.System.KEY_ASSIST_ACTION,
                HwKeyHelper.getPressOnAssistBehavior(context, false));
        mKeySettings.put(SlimSettings.System.KEY_ASSIST_DOUBLE_TAP_ACTION,
                HwKeyHelper.getDoubleTapOnAssistBehavior(context, false));
        mKeySettings.put(SlimSettings.System.KEY_ASSIST_LONG_PRESS_ACTION,
                HwKeyHelper.getLongPressOnAssistBehavior(context, false));

        mKeySettings.put(SlimSettings.System.KEY_APP_SWITCH_ACTION,
                HwKeyHelper.getPressOnAppSwitchBehavior(context, false));
        mKeySettings.put(SlimSettings.System.KEY_APP_SWITCH_DOUBLE_TAP_ACTION,
                HwKeyHelper.getDoubleTapOnAppSwitchBehavior(context, false));
        mKeySettings.put(SlimSettings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION,
                HwKeyHelper.getLongPressOnAppSwitchBehavior(context, false));
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference == mEnableHwKeys) {
            writeDisableHwKeys(this, !mEnableHwKeys.isChecked());
            return true;
        } else if (preference == mEditor) {
            setEditing(!mEditing);
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (!mCheckPreferences) {
            return false;
        }
        if (preference == mEnableCustomBindings) {
            boolean value = (Boolean) newValue;
            SlimSettings.System.putInt(getContentResolver(),
                                       SlimSettings.System.HARDWARE_KEY_REBINDING, value ? 1 : 0);
            return true;
        }
        return false;
    }

    private boolean hasHomeKey() {
        Iterator<String> nextAction = mKeySettings.values().iterator();
        while (nextAction.hasNext()){
            String action = nextAction.next();
            if (action != null && action.equals(ActionConstants.ACTION_HOME)) {
                return true;
            }
        }
        return false;
    }

    private void resetToDefault() {
        for (String settingsKey : mKeySettings.keySet()) {
            if (settingsKey != null) {
                Settings.System.putString(getContentResolver(),
                settingsKey, null);
            }
        }
        SlimSettings.System.putInt(getContentResolver(),
                SlimSettings.System.HARDWARE_KEY_REBINDING, 1);
        updateKeyMap(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        setEditing(false);
    }

    public void setEditing(boolean editing) {
        mEditing = editing;
        if (editing) {
            try {
                SlimHardwareKeysManager.getService().registerListener(mKeyListener);
            } catch (RemoteException e) {}
        } else {
            try {
                SlimHardwareKeysManager.getService().unregisterListener(mKeyListener);
            } catch (RemoteException e) {}
        }
    }

    private static void writeDisableHwKeys(Context context, boolean enabled) {
        SlimSettings.System.putInt(context.getContentResolver(),
                SlimSettings.System.DISABLE_HW_KEYS, enabled ? 0 : 1);

        CMHardwareManager cmHardwareManager = CMHardwareManager.getInstance(context);
        cmHardwareManager.set(CMHardwareManager.FEATURE_KEY_DISABLE, enabled);

        if (enabled) {
            Settings.System.putInt(context.getContentResolver(),
                    Settings.System.BUTTON_BRIGHTNESS, 0);
        } else {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            final int defaultBrightness = context.getResources().getInteger(
                    com.android.internal.R.integer.config_buttonBrightnessSettingDefault);
            int oldBright = prefs.getInt(ButtonBacklightBrightness.KEY_BUTTON_BACKLIGHT,
                    defaultBrightness);
            Settings.System.putInt(context.getContentResolver(),
                    Settings.System.BUTTON_BRIGHTNESS, oldBright);
        }
    }

    public static void restore(Context context) {
        CMHardwareManager cmHardwareManager = CMHardwareManager.getInstance(context);
        if (cmHardwareManager.isSupported(CMHardwareManager.FEATURE_KEY_DISABLE)) {

            boolean enabled = SlimSettings.System.getInt(context.getContentResolver(),
                    SlimSettings.System.DISABLE_HW_KEYS, 1) == 0;
            cmHardwareManager.set(CMHardwareManager.FEATURE_KEY_DISABLE, enabled);
        }
    }

    @Override
    public void shortcutPicked(String action,
                String description, Bitmap b, boolean isApplication) {
        if (mPendingSettingsKey == null || action == null) {
            return;
        }
        SlimSettings.System.putString(getContentResolver(), mPendingSettingsKey, action);
        updateKeyMap(this);
        mPendingSettingsKey = null;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ShortcutPickerHelper.REQUEST_PICK_SHORTCUT
                    || requestCode == ShortcutPickerHelper.REQUEST_PICK_APPLICATION
                    || requestCode == ShortcutPickerHelper.REQUEST_CREATE_SHORTCUT) {
                mPicker.onActivityResult(requestCode, resultCode, data);

            }
        } else {
            mPendingSettingsKey = null;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void selectAction(int key, int action) {
        String settingsKey = "key_";
        String dialogTitle = "keys_";

        if (key == KeyEvent.KEYCODE_HOME) {
            settingsKey += "home_";
            dialogTitle += "home_";
        } else if (key == KeyEvent.KEYCODE_MENU) {
            settingsKey += "menu_";
            dialogTitle += "menu_";
        } else if (key == KeyEvent.KEYCODE_BACK) {
            settingsKey += "back_";
            dialogTitle += "back_";
        } else if (key == KeyEvent.KEYCODE_ASSIST) {
            settingsKey += "assist_";
            dialogTitle += "assist_";
        } else if (key == KeyEvent.KEYCODE_APP_SWITCH) {
            settingsKey += "app_switch_";
            dialogTitle += "app_switch_";
        } else if (key == KeyEvent.KEYCODE_CAMERA) {
            settingsKey += "camera_";
            dialogTitle += "camera_";
        }

        if (action == SINGLE_TAP_ACTION) {
            dialogTitle += "press_";
        } else if (action == DOUBLE_TAP_ACTION) {
            settingsKey += "double_tap_";
            dialogTitle += "double_tap_";
        } else if (action == LONG_PRESS_ACTION) {
            settingsKey += "long_press_";
            dialogTitle += "long_press_";
        }
        settingsKey += "action";
        dialogTitle += "title";

        int dialogTitleResId = getResources().getIdentifier(
                dialogTitle, "string", "com.slim.settings");
        if (dialogTitleResId > 0) {
            showDialogInner(DLG_SHOW_ACTION_DIALOG, settingsKey, dialogTitleResId, key);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case MENU_RESET:
                    showDialogInner(DLG_RESET_TO_DEFAULT, null, 0, -1);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, MENU_RESET, 0, org.slim.framework.internal.R.string.reset)
                // Use the reset icon
                .setIcon(org.slim.framework.internal.R.drawable.ic_settings_reset)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        return true;
    }

    private void showDialogInner(int id, String settingsKey, int dialogTitle, int key) {
        DialogFragment newFragment =
                MyAlertDialogFragment.newInstance(id, settingsKey, dialogTitle, key);
        //newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "dialog " + id);
    }

    public static class MyAlertDialogFragment extends DialogFragment {

        public static MyAlertDialogFragment newInstance(
                int id, String settingsKey, int dialogTitle, int key) {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("id", id);
            args.putString("settingsKey", settingsKey);
            args.putInt("dialogTitle", dialogTitle);
            args.putInt("key", key);
            frag.setArguments(args);
            return frag;
        }

        HardwareKeysSettings getOwner() {
            return (HardwareKeysSettings) getActivity();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt("id");
            final String settingsKey = getArguments().getString("settingsKey");
            int dialogTitle = getArguments().getInt("dialogTitle");
            switch (id) {
                case DLG_SHOW_ACTION_SELECT_DIALOG:
                    final int key = getArguments().getInt("key");
                    return new AlertDialog.Builder(getActivity())
                        .setTitle(dialogTitle)
                        .setAdapter(new HardwareKeysSettings.KeyItemAdapter(getActivity(), key),
                                new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                getOwner().selectAction(key, i);
                            }
                        })
                        .setPositiveButton(android.R.string.ok, null)
                        .create();
                case DLG_SHOW_WARNING_DIALOG:
                    return new AlertDialog.Builder(getActivity())
                    .setTitle(org.slim.framework.internal.R.string.attention)
                    .setMessage(org.slim.framework.internal.R.string.no_home_key)
                    .setPositiveButton(android.R.string.ok, null)
                    .create();
                case DLG_SHOW_ACTION_DIALOG:
                    if (sFinalActionDialogArray == null) {
                        return null;
                    }
                    return new AlertDialog.Builder(getActivity())
                    .setTitle(dialogTitle)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setItems(getOwner().sFinalActionDialogArray.entries,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            if (getOwner().sFinalActionDialogArray.values[item]
                                    .equals(ActionConstants.ACTION_APP)) {
                                if (getOwner().mPicker != null) {
                                    getOwner().mPendingSettingsKey = settingsKey;
                                    getOwner().mPicker.pickShortcut(0);
                                }
                            } else {
                                SlimSettings.System.putString(getActivity().getContentResolver(),
                                        settingsKey,
                                        getOwner().sFinalActionDialogArray.values[item]);
                                updateKeyMap(getActivity());
                            }
                        }
                    })
                    .create();
                case DLG_RESET_TO_DEFAULT:
                    return new AlertDialog.Builder(getActivity())
                    .setTitle(org.slim.framework.internal.R.string.shortcut_action_reset)
                    .setMessage(org.slim.framework.internal.R.string.reset_message)
                    .setNegativeButton(android.R.string.cancel, null)
                    .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            getOwner().resetToDefault();
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

    /*@Override
    protected int getMetricsCategory() {
        return SlimMetricsLogger.HARDWAREKEYS_SETTINGS;
    }*/

    public static class KeyItemAdapter extends BaseAdapter {

        Context mContext;
        int mKey;
        ArrayList<String> mActions = new ArrayList<>();

        public KeyItemAdapter(Context context, int key) {
            mContext = context;
            mKey = key;
            mActions.add(SINGLE_TAP_ACTION,
                    context.getResources().getString(R.string.keys_action_normal));
            mActions.add(DOUBLE_TAP_ACTION,
                    context.getResources().getString(R.string.keys_action_double));
            mActions.add(LONG_PRESS_ACTION,
                    context.getResources().getString(R.string.keys_action_long));
        }

        @Override
        public int getCount() {
            return mActions.size();
        }

        @Override
        public Object getItem(int i) {
            return mActions.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = View.inflate(mContext, R.layout.action_item, null);
            }
            TextView title = (TextView) view.findViewById(R.id.action_title);
            title.setText(mActions.get(i));

            TextView action = (TextView) view.findViewById(R.id.action);
            action.setText(getAction(mKey, i));

            return view;
        }

        private String getAction(int key, int action) {
            String settingsKey = "key_";

            if (key == KeyEvent.KEYCODE_HOME) {
                settingsKey += "home_";
            } else if (key == KeyEvent.KEYCODE_MENU) {
                settingsKey += "menu_";
            } else if (key == KeyEvent.KEYCODE_BACK) {
                settingsKey += "back_";
            } else if (key == KeyEvent.KEYCODE_ASSIST) {
                settingsKey += "assist_";
            } else if (key == KeyEvent.KEYCODE_APP_SWITCH) {
                settingsKey += "app_switch_";
            } else if (key == KeyEvent.KEYCODE_CAMERA) {
                settingsKey += "camera_";
            }
            if (action == DOUBLE_TAP_ACTION) {
                settingsKey += "double_tap_";
            } else if (action == LONG_PRESS_ACTION) {
                settingsKey += "long_press_";
            }
            settingsKey += "action";

            String actionString = mKeySettings.get(settingsKey);

            Log.d("TEST", "settingsKey=" + settingsKey + " : actionString=" + actionString);
            if (!TextUtils.isEmpty(actionString)) {
                return ActionHelper.getActionDescription(mContext, actionString);
            }
            return "None";
        }
    }
}
