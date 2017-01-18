/*
 * Copyright (C) 2014-2017 SlimRoms Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package slim.utils;

import android.content.Context;
import android.os.UserHandle;
import android.provider.Settings;

import slim.action.ActionConstants;
import slim.provider.SlimSettings;
import slim.R;

public class HwKeyHelper {

    // These need to match the documentation/constant in
    // core/res/res/values/config.xml
    static final int LONG_PRESS_HOME_NOTHING = 0;
    static final int LONG_PRESS_HOME_RECENT_SYSTEM_UI = 1;
    static final int LONG_PRESS_HOME_ASSIST = 2;

    static final int DOUBLE_TAP_HOME_NOTHING = 0;
    static final int DOUBLE_TAP_HOME_RECENT_SYSTEM_UI = 1;

    public static String getPressOnHomeBehavior(Context context, boolean getDefault) {
        String defaultValue = getDefaultAction(context, R.string.key_home_action_default);
        if (getDefault) {
            return defaultValue;
        }
        String value = SlimSettings.System.getStringForUser(
                context.getContentResolver(),
                SlimSettings.System.KEY_HOME_ACTION,
                UserHandle.USER_CURRENT);
        return value == null ? defaultValue : value;
    }

    public static String getLongPressOnHomeBehavior(Context context, boolean getDefault) {
        String defaultValue = getDefaultAction(context,
                R.string.key_home_long_press_action_default);
        if (getDefault) {
            return defaultValue;
        }
        String value = SlimSettings.System.getStringForUser(
                context.getContentResolver(),
                SlimSettings.System.KEY_HOME_LONG_PRESS_ACTION,
                UserHandle.USER_CURRENT);
        return value == null ? defaultValue : value;
    }

    public static String getDoubleTapOnHomeBehavior(Context context, boolean getDefault) {
        String defaultValue = getDefaultAction(context,
                R.string.key_home_double_tap_action_default);
        if (getDefault) {
            return defaultValue;
        }
        String value = SlimSettings.System.getStringForUser(
                context.getContentResolver(),
                SlimSettings.System.KEY_HOME_DOUBLE_TAP_ACTION,
                UserHandle.USER_CURRENT);
        return value == null ? defaultValue : value;
    }

    public static String getPressOnMenuBehavior(Context context, boolean getDefault) {
        String defaultValue = getDefaultAction(context, R.string.key_menu_action_default);
        if (getDefault) {
            return defaultValue;
        }
        String value = SlimSettings.System.getStringForUser(
                context.getContentResolver(),
                SlimSettings.System.KEY_MENU_ACTION,
                UserHandle.USER_CURRENT);
        return value == null ? defaultValue : value;
    }

    public static String getLongPressOnMenuBehavior(Context context, boolean getDefault) {
        String defaultValue = getDefaultAction(context,
                R.string.key_menu_long_press_action_default);
        if (getDefault) {
            return defaultValue;
        }
        String value = SlimSettings.System.getStringForUser(
                context.getContentResolver(),
                SlimSettings.System.KEY_MENU_LONG_PRESS_ACTION,
                UserHandle.USER_CURRENT);
        return value == null ? defaultValue : value;
    }

    public static String getDoubleTapOnMenuBehavior(Context context, boolean getDefault) {
        String defaultValue = getDefaultAction(context,
                R.string.key_menu_double_tap_action_default);
        if (getDefault) {
            return defaultValue;
        }
        String value = SlimSettings.System.getStringForUser(
                context.getContentResolver(),
                SlimSettings.System.KEY_MENU_DOUBLE_TAP_ACTION,
                UserHandle.USER_CURRENT);
        return value == null ? defaultValue : value;
    }

    public static String getPressOnBackBehavior(Context context, boolean getDefault) {
        String defaultValue = getDefaultAction(context, R.string.key_back_action_default);
        if (getDefault) {
            return defaultValue;
        }
        String value = SlimSettings.System.getStringForUser(
                context.getContentResolver(),
                SlimSettings.System.KEY_BACK_ACTION,
                UserHandle.USER_CURRENT);
        return value == null ? defaultValue : value;
    }

    public static String getLongPressOnBackBehavior(Context context, boolean getDefault) {
        String defaultValue = getDefaultAction(context,
                R.string.key_back_long_press_action_default);
        if (getDefault) {
            return defaultValue;
        }
        String value = SlimSettings.System.getStringForUser(
                context.getContentResolver(),
                SlimSettings.System.KEY_BACK_LONG_PRESS_ACTION,
                UserHandle.USER_CURRENT);
        return value == null ? defaultValue : value;
    }

    public static String getDoubleTapOnBackBehavior(Context context, boolean getDefault) {
        String defaultValue = getDefaultAction(context,
                R.string.key_back_double_tap_action_default);
        if (getDefault) {
            return defaultValue;
        }
        String value = SlimSettings.System.getStringForUser(
                context.getContentResolver(),
                SlimSettings.System.KEY_BACK_DOUBLE_TAP_ACTION,
                UserHandle.USER_CURRENT);
        return value == null ? defaultValue : value;
    }

    public static String getPressOnAssistBehavior(Context context, boolean getDefault) {
        String defaultValue = getDefaultAction(context, R.string.key_assist_action_default);
        if (getDefault) {
            return defaultValue;
        }
        String value = SlimSettings.System.getStringForUser(
                context.getContentResolver(),
                SlimSettings.System.KEY_ASSIST_ACTION,
                UserHandle.USER_CURRENT);
        return value == null ? defaultValue : value;
    }

    public static String getLongPressOnAssistBehavior(Context context, boolean getDefault) {
        String defaultValue = getDefaultAction(context,
                R.string.key_assist_long_press_action_default);
        if (getDefault) {
            return defaultValue;
        }
        String value = SlimSettings.System.getStringForUser(
                context.getContentResolver(),
                SlimSettings.System.KEY_ASSIST_LONG_PRESS_ACTION,
                UserHandle.USER_CURRENT);
        return value == null ? defaultValue : value;
    }

    public static String getDoubleTapOnAssistBehavior(Context context, boolean getDefault) {
        String defaultValue = getDefaultAction(context,
                R.string.key_assist_double_tap_action_default);
        if (getDefault) {
            return defaultValue;
        }
        String value = SlimSettings.System.getStringForUser(
                context.getContentResolver(),
                SlimSettings.System.KEY_ASSIST_DOUBLE_TAP_ACTION,
                UserHandle.USER_CURRENT);
        return value == null ? defaultValue : value;
    }

    public static String getPressOnAppSwitchBehavior(Context context, boolean getDefault) {
        String defaultValue = getDefaultAction(context, R.string.key_app_switch_action_default);
        if (getDefault) {
            return defaultValue;
        }
        String value = SlimSettings.System.getStringForUser(
                context.getContentResolver(),
                SlimSettings.System.KEY_APP_SWITCH_ACTION,
                UserHandle.USER_CURRENT);
        return value == null ? defaultValue : value;
    }

    public static String getLongPressOnAppSwitchBehavior(Context context, boolean getDefault) {
        String defaultValue = getDefaultAction(context,
                R.string.key_app_switch_long_press_action_default);
        if (getDefault) {
            return defaultValue;
        }
        String value = SlimSettings.System.getStringForUser(
                context.getContentResolver(),
                SlimSettings.System.KEY_APP_SWITCH_LONG_PRESS_ACTION,
                UserHandle.USER_CURRENT);
        return value == null ? defaultValue : value;
    }

    public static String getDoubleTapOnAppSwitchBehavior(Context context, boolean getDefault) {
        String defaultValue = getDefaultAction(context,
                R.string.key_app_switch_double_tap_action_default);
        if (getDefault) {
            return defaultValue;
        }
        String value = SlimSettings.System.getStringForUser(
                context.getContentResolver(),
                SlimSettings.System.KEY_APP_SWITCH_DOUBLE_TAP_ACTION,
                UserHandle.USER_CURRENT);
        return value == null ? defaultValue : value;
    }

    public static String getPressOnCameraBehavior(Context context, boolean getDefault) {
        String defaultValue = getDefaultAction(context, R.string.key_camera_action_default);
        if (getDefault) {
            return defaultValue;
        }
        String value = SlimSettings.System.getStringForUser(
                context.getContentResolver(),
                SlimSettings.System.KEY_CAMERA_ACTION,
                UserHandle.USER_CURRENT);
        return value == null ? defaultValue : value;
    }

    public static String getLongPressOnCameraBehavior(Context context, boolean getDefault) {
        String defaultValue = getDefaultAction(context,
                R.string.key_camera_long_press_action_default);
        if (getDefault) {
            return defaultValue;
        }
        String value = SlimSettings.System.getStringForUser(
                context.getContentResolver(),
                SlimSettings.System.KEY_CAMERA_LONG_PRESS_ACTION,
                UserHandle.USER_CURRENT);
        return value == null ? defaultValue : value;
    }

    public static String getDoubleTapOnCameraBehavior(Context context, boolean getDefault) {
        String defaultValue = getDefaultAction(context,
                R.string.key_camera_double_tap_action_default);
        if (getDefault) {
            return defaultValue;
        }
        String value = SlimSettings.System.getStringForUser(
                context.getContentResolver(),
                SlimSettings.System.KEY_CAMERA_DOUBLE_TAP_ACTION,
                UserHandle.USER_CURRENT);
        return value == null ? defaultValue : value;
    }

    private static String getDefaultAction(Context context, int resId) {
        return context.getResources().getString(resId);
    }
}

