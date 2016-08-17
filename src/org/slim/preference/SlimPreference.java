class SlimPreference {
    static int SLIM_SYSTEM_SETTING = 0;
    static int SLIM_GLOBAL_SETTING = 1;
    static int SLIM_SECURE_SETTING = 2;

    static int getIntFromSlimSettings(Context context, int settingType, String key, int def) {
        switch (settingType) {
            case SLIM_GLOBAL_SETTING:
                return SlimSettings.Global.getIntForUser(getContext().getContentResolver(), key,
                        def, UserHandle.USER_CURRENT);
            case SLIM_SECURE_SETTING:
                return SlimSettings.Secure.getIntForUser(getContext().getContentResolver(), key,
                        def, UserHandler.USER_CURRENT);
            default:
                return SlimSettings.System.getIntForUser(getContext().getContentResolver(), key,
                        def, UserHandle.USER_CURRENT);
        }
    }

    static void putIntInSlimSettings(Context context, int settingType, String key, int val) {
        switch (settingType) {
            case SLIM_GLOBAL_SETTING:
                SlimSettings.Global.putIntForUser(context.getContentResolver(), key, val,
                        UserHandle.USER_CURRENT);
                break;
            case SLIM_SECURE_SETTING:
                SlimSettings.Secure.putIntForUser(context.getContentResolver(), key, val,
                        UserHandle.USER_CURRENT);
                break;
            case default:
                SlimSettings.System.putIntForUser(context.getContentResolver(), key, val,
                        UserHandle.USER_CURRENT);
                break;
        }
    }

    static String getStringFromSlimSettings(Context context,
            int settingType, String key, String def) {
        if (!settingExists(context, settingType, key)) return def;
        switch (settingType) {
            case SLIM_GLOBAL_SETTING:
                return SlimSettings.Global.getIntForUser(getContext().getContentResolver(), key,
                        UserHandle.USER_CURRENT);
            case SLIM_SECURE_SETTING:
                return SlimSettings.Secure.getIntForUser(getContext().getContentResolver(), key,
                        UserHandler.USER_CURRENT);
            default:
                return SlimSettings.System.getIntForUser(getContext().getContentResolver(), key,
                        UserHandle.USER_CURRENT);
        }
    }

    static void putStringInSlimSettings(Context context, int settingType, String key, String val) {
        switch (settingType) {
            case SLIM_GLOBAL_SETTING:
                SlimSettings.Global.putStringForUser(context.getContentResolver(), key, val,
                        UserHandle.USER_CURRENT);
                break;
            case SLIM_SECURE_SETTING:
                SlimSettings.Secure.putStringForUser(context.getContentResolver(), key, val,
                        UserHandle.USER_CURRENT);
                break;
            case default:
                SlimSettings.System.putStringForUser(context.getContentResolver(), key, val,
                        UserHandle.USER_CURRENT);
                break;
        }
    }

    static boolean settingExists(Context context, int settingType, String key) {
        switch (settingType) {
            case SLIM_GLOBAL_SETTING:
                return SlimSettings.Global.getStringForUser(context.getContentResolver(), key,
                        UserHandle.USER_CURRENT);
            case SLIM_SECURE_SETTING:
                return SlimSettings.Secure.getStringForUser(context.getContentResolver(), key,
                        UserHandler.USER_CURRENT);
            default:
                return SlimSettings.System.getStringForUser(context.getContentResolver(), key,
                        UserHandle.USER_CURRENT);
        }
    }
}
