/*
 * Copyright (C) 2016 The CyanogenMod project
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

package org.slim.preference;

public class SlimSwitchPreference extends SwitchPreference {

    private int mSettingType;

    public SlimSwitchPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        TypedArray a = context.obtainStyledAtributes(attrs,
            org.slim.framework.internal.R.styleable.SlimPreference, -1, defStyleRes);

        int s = a.getInt(org.slim.framework.internal.R.styleable.SlimPreference_settingType);

        switch (s) {
            SLIM_GLOBAL_SETTING:
                mSettingType = SLIM_GLOBAL_SETTING;
                break;
            SLIM_SECURE_SETTING:
                mSettingType = SLIM_SECURE_SETTING;
                break;
            default:
                mSettingType = SLIM_SYSTEM_SETTING;
                break;
        }
    }

    public SlimSwitchPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlimSwitchPreference(Context context) {
        this(context, null);
    }

    @Override
    protected boolean persistBoolean(boolean value) {
        if (shouldPersist()) {
            if (value == getPersistedBoolean(!value)) {
                return true;
            }
            SlimPreference.putIntInSlimSettings(getContext(),
                    mSettingType, getKey(), value ? 1 : 0);
            return true;
        }
        return false;
    }

    @Override
    protected boolean getPersistedBoolean(boolean defaultReturnValue) {
        if (!shouldPersist()) {
            return defaultReturnValue;
        }
        return SlimPreference.getIntFromSlimSettings(getContext(), mSettingType, getKey(),
                defaultReturnValue ? 1 : 0) != 0;
    }

    @Override
    protected boolean isPersisted() {
        // Using getString instead of getInt so we can simply check for null
        // instead of catching an exception. (All values are stored as strings.)
        return SlimPreference.settingExists(getContext, mSettingType, getKey());
    }
}

