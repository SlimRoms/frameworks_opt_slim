/*
* Copyright (C) 2016 SlimRoms Project
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

package org.slim.action;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.UserHandle;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import org.slim.provider.SlimSettings;
import org.slim.utils.ConfigSplitHelper;
import org.slim.utils.DeviceUtils;
import org.slim.utils.ImageHelper;

public class ActionHelper {

    private static final String SYSTEM_METADATA_NAME = "android";
    private static final String SLIM_FRAMEWORK_METADATA_NAME = "org.slim.framework";
    private static final String SYSTEMUI_METADATA_NAME = "com.android.systemui";
    private static final String SETTINGS_METADATA_NAME = "com.android.settings";

    // get and set the navbar configs from provider and return propper arraylist objects
    // @ActionConfig
    public static ArrayList<ActionConfig> getNavBarConfig(Context context) {
        return (ConfigSplitHelper.getActionConfigValues(context,
            getNavBarProvider(context), null, null, false));
    }

    // get @ActionConfig with description if needed and other then an app description
    public static ArrayList<ActionConfig> getNavBarConfigWithDescription(
            Context context, String values, String entries) {
        return (ConfigSplitHelper.getActionConfigValues(context,
            getNavBarProvider(context), values, entries, false));
    }

    private static String getNavBarProvider(Context context) {
        String config = SlimSettings.System.getStringForUser(
                    context.getContentResolver(),
                    SlimSettings.System.NAVIGATION_BAR_CONFIG,
                    UserHandle.USER_CURRENT);
        if (config == null) {
            config = ActionConstants.NAVIGATION_CONFIG_DEFAULT;
        }
        return config;
    }

    public static void setNavBarConfig(Context context,
            ArrayList<ActionConfig> actionConfig, boolean reset) {
        String config;
        if (reset) {
            config = ActionConstants.NAVIGATION_CONFIG_DEFAULT;
        } else {
            config = ConfigSplitHelper.setActionConfig(actionConfig, false);
        }
        SlimSettings.System.putString(context.getContentResolver(),
                    SlimSettings.System.NAVIGATION_BAR_CONFIG,
                    config);
    }

    // General methods to retrieve the correct icon for the respective action.
    public static Drawable getActionIconImage(Context context,
            String clickAction, String customIcon) {
        int resId = -1;
        Drawable d = null;
        PackageManager pm = context.getPackageManager();
        if (pm == null) {
            return null;
        }

        Resources resources;
        try {
            resources = pm.getResourcesForApplication(SLIM_FRAMEWORK_METADATA_NAME);
        } catch (Exception e) {
            Log.e("ActionHelper:", "can't access systemui resources",e);
            return null;
        }

        if (resources == null) {
            Log.d("TEST", "resources is null");
        }

        if (!clickAction.startsWith("**")) {
            try {
                String extraIconPath = clickAction.replaceAll(".*?hasExtraIcon=", "");
                if (extraIconPath != null && !extraIconPath.isEmpty()) {
                    File f = new File(Uri.parse(extraIconPath).getPath());
                    if (f.exists()) {
                        d = new BitmapDrawable(context.getResources(),
                                f.getAbsolutePath());
                    }
                }
                if (d == null) {
                    d = pm.getActivityIcon(Intent.parseUri(clickAction, 0));
                }
            } catch (NameNotFoundException e) {
                resId = resources.getIdentifier(
                    SLIM_FRAMEWORK_METADATA_NAME + ":drawable/ic_sysbar_null", null, null);
                if (resId > 0) {
                    d = resources.getDrawable(resId);
                    if (d == null) Log.d("TEST", "sysbar_null is null");
                    return d;
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }

        if (customIcon != null && customIcon.startsWith(ActionConstants.SYSTEM_ICON_IDENTIFIER)) {
            resId = resources.getIdentifier(customIcon.substring(
                        ActionConstants.SYSTEM_ICON_IDENTIFIER.length()),
                                            "drawable", "org.slim.framework");
            if (resId > 0) {
                return resources.getDrawable(resId);
            }
        } else if (customIcon != null && !customIcon.equals(ActionConstants.ICON_EMPTY)) {
            File f = new File(Uri.parse(customIcon).getPath());
            if (f.exists()) {
                return new BitmapDrawable(context.getResources(),
                    ImageHelper.getRoundedCornerBitmap(
                        new BitmapDrawable(context.getResources(),
                        f.getAbsolutePath()).getBitmap()));
            } else {
                Log.e("ActionHelper:", "can't access custom icon image");
                return null;
            }
        } else if (clickAction.startsWith("**")) {
            resId = getActionSystemIcon(resources, clickAction);

            if (resId > 0) {
                return resources.getDrawable(resId);
            }
        }
        return d;
    }

    private static int getActionSystemIcon(Resources resources, String clickAction) {
        int resId = -1;

        if (clickAction.equals(ActionConstants.ACTION_HOME)) {
            resId = resources.getIdentifier(
                        SLIM_FRAMEWORK_METADATA_NAME + ":drawable/ic_sysbar_home", null, null);
        } else if (clickAction.equals(ActionConstants.ACTION_BACK)) {
            resId = resources.getIdentifier(
                        SLIM_FRAMEWORK_METADATA_NAME + ":drawable/ic_sysbar_back", null, null);
        } else if (clickAction.equals(ActionConstants.ACTION_RECENTS)) {
            resId = resources.getIdentifier(
                        SLIM_FRAMEWORK_METADATA_NAME + ":drawable/ic_sysbar_recent", null, null);
        } else if (clickAction.equals(ActionConstants.ACTION_SEARCH)
                || clickAction.equals(ActionConstants.ACTION_ASSIST)
                || clickAction.equals(ActionConstants.ACTION_NOWONTAP)
                || clickAction.equals(ActionConstants.ACTION_VOICE_SEARCH)
                || clickAction.equals(ActionConstants.ACTION_KEYGUARD_SEARCH)) {
            resId = resources.getIdentifier(
                        SLIM_FRAMEWORK_METADATA_NAME + ":drawable/ic_sysbar_search", null, null);
        } else if (clickAction.equals(ActionConstants.ACTION_MENU)) {
            resId = resources.getIdentifier(
                        SLIM_FRAMEWORK_METADATA_NAME + ":drawable/ic_sysbar_menu_big", null, null);
        } else if (clickAction.equals(ActionConstants.ACTION_IME)) {
            resId = resources.getIdentifier(
                        SLIM_FRAMEWORK_METADATA_NAME + ":drawable/ic_sysbar_ime_switcher", null, null);
        } else if (clickAction.equals(ActionConstants.ACTION_POWER)) {
            resId = resources.getIdentifier(
                        SLIM_FRAMEWORK_METADATA_NAME + ":drawable/ic_sysbar_power", null, null);
        } else if (clickAction.equals(ActionConstants.ACTION_POWER_MENU)) {
            resId = resources.getIdentifier(
                        SLIM_FRAMEWORK_METADATA_NAME + ":drawable/ic_sysbar_power_menu", null, null);
        } else if (clickAction.equals(ActionConstants.ACTION_VIB)) {
            resId = resources.getIdentifier(
                        SLIM_FRAMEWORK_METADATA_NAME + ":drawable/ic_sysbar_vib", null, null);
        } else if (clickAction.equals(ActionConstants.ACTION_SILENT)) {
            resId = resources.getIdentifier(
                        SLIM_FRAMEWORK_METADATA_NAME + ":drawable/ic_sysbar_silent", null, null);
        } else if (clickAction.equals(ActionConstants.ACTION_VIB_SILENT)) {
            resId = resources.getIdentifier(
                        SLIM_FRAMEWORK_METADATA_NAME + ":drawable/ic_sysbar_ring_vib_silent", null, null);
        } else if (clickAction.equals(ActionConstants.ACTION_EXPANDED_DESKTOP)) {
            resId = resources.getIdentifier(
                        SLIM_FRAMEWORK_METADATA_NAME + ":drawable/ic_sysbar_expanded_desktop", null, null);
        } else if (clickAction.equals(ActionConstants.ACTION_KILL)) {
            resId = resources.getIdentifier(
                        SLIM_FRAMEWORK_METADATA_NAME + ":drawable/ic_sysbar_killtask", null, null);
        } else if (clickAction.equals(ActionConstants.ACTION_LAST_APP)) {
            resId = resources.getIdentifier(
                        SLIM_FRAMEWORK_METADATA_NAME + ":drawable/ic_sysbar_lastapp", null, null);
        } else if (clickAction.equals(ActionConstants.ACTION_NOTIFICATIONS)) {
            resId = resources.getIdentifier(
                        SLIM_FRAMEWORK_METADATA_NAME + ":drawable/ic_sysbar_notifications", null, null);
        } else if (clickAction.equals(ActionConstants.ACTION_SETTINGS_PANEL)) {
            resId = resources.getIdentifier(
                        SLIM_FRAMEWORK_METADATA_NAME + ":drawable/ic_sysbar_qs", null, null);
        } else if (clickAction.equals(ActionConstants.ACTION_SCREENSHOT)) {
            resId = resources.getIdentifier(
                        SLIM_FRAMEWORK_METADATA_NAME + ":drawable/ic_sysbar_screenshot", null, null);
        } else if (clickAction.equals(ActionConstants.ACTION_TORCH)) {
            resId = resources.getIdentifier(
                        SLIM_FRAMEWORK_METADATA_NAME + ":drawable/ic_sysbar_torch", null, null);
        } else if (clickAction.equals(ActionConstants.ACTION_CAMERA)) {
            resId = resources.getIdentifier(
                        SLIM_FRAMEWORK_METADATA_NAME + ":drawable/ic_sysbar_camera", null, null);
        }/* else if (clickAction.equals(ActionConstants.ACTION_POWER_OFF)) {
            resId = com.android.internal.R.drawable.ic_lock_power_off_alpha;
        } else if (clickAction.equals(ActionConstants.ACTION_REBOOT)) {
            resId = com.android.internal.R.drawable.ic_lock_reboot_alpha;
        } else if (clickAction.equals(ActionConstants.ACTION_AIRPLANE)) {
            resId = com.android.internal.R.drawable.ic_lock_airplane_mode_off_am_alpha;
        } else if (clickAction.equals(ActionConstants.ACTION_LOCKDOWN)) {
            resId = com.android.internal.R.drawable.ic_lock_lock_alpha;
        }*/ else {
            resId = resources.getIdentifier(
                        SLIM_FRAMEWORK_METADATA_NAME + ":drawable/ic_sysbar_null", null, null);
        }
        return resId;
    }

    public static Drawable getPowerMenuIconImage(Context context,
            String clickAction, String customIcon) {
        Drawable d = getActionIconImage(context, clickAction, customIcon);
        if (d != null) {
            d.mutate();
            d = ImageHelper.getColoredDrawable(d,
                    context.getResources().getColor(
                            org.slim.framework.internal.R.color.dslv_icon_dark));
        }
        return d;
    }

    public static String getActionDescription(Context context, String action) {
        Resources resources = getResources(context);
        ActionsArray actionsArray = new ActionsArray(context);

        int index = -1;
        for (int i = 0; i < actionsArray.getEntries().length; i++) {
            if (action.equals(actionsArray.getValues()[i])) {
                return actionsArray.getEntries()[i];
            }
        }
        return resources.getString(
                resources.getIdentifier(SLIM_FRAMEWORK_METADATA_NAME
                        + ":string/shortcut_action_none", null, null));
    }

    private static Resources getResources(Context context) {
        try {
            return context.getPackageManager()
                    .getResourcesForApplication(SLIM_FRAMEWORK_METADATA_NAME);
        } catch (Exception e) {
            return null;
        }
    }
}
