package org.slim.action;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import org.slim.action.ActionConstants;
import org.slim.utils.DeviceUtils;

public class ActionsArray {

    private String[] values;
    private String[] entries;

    public ActionsArray(Context context) {
        this(context, true, false, null);
    }

    public ActionsArray(Context context, boolean showWake) {
        this(context, true, showWake, null);
    }

    public ActionsArray(Context context, boolean showNone, boolean showWake) {
        this(context, showNone, showWake, null);
    }

    public ActionsArray(Context context, boolean showNone, boolean showWake,
            ArrayList<String> actionsToExclude) {
        String[] initialValues = context.getResources().getStringArray(
                org.slim.framework.R.array.shortcut_action_values);
        String[] initialEntries = context.getResources().getStringArray(
                org.slim.framework.R.array.shortcut_action_entries);

        List<String> finalEntries = new ArrayList<>();
        List<String> finalValues = new ArrayList<>();

        for (int i = 0; i < initialValues.length; i++) {
            if (!showNone && ActionConstants.ACTION_NULL.equals(initialValues[i])
                    || !showWake && ActionConstants.ACTION_NULL.equals(initialValues[i])) {
                continue;
            } else if (actionsToExclude != null && actionsToExclude.contains(initialValues[i])) {
                continue;
            } else if (isSupported(context, initialValues[i])) {
                finalEntries.add(initialEntries[i]);
                finalValues.add(initialValues[i]);
            }
        }

        entries = finalEntries.toArray(new String[0]);
        values = finalValues.toArray(new String[0]);
    }

    public String[] getEntries() {
        return entries;
    }

    public String[] getValues() {
        return values;
    }

    private static boolean isSupported(Context context, String action) {
        if (action.equals(ActionConstants.ACTION_TORCH)
                        && !DeviceUtils.deviceSupportsTorch(context)
                || action.equals(ActionConstants.ACTION_VIB)
                        && !DeviceUtils.deviceSupportsVibrator(context)
                || action.equals(ActionConstants.ACTION_VIB_SILENT)
                        && !DeviceUtils.deviceSupportsVibrator(context)) {
            return false;
        }
        return true;
    }
}
