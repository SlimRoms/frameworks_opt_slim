/*
 * Copyright (C) 2016 The SlimRoms project
 * Copyright (C) 2015 The CyanogenMod Project
 * This code has been modified.  Portions copyright (C) 2010, T-Mobile USA, Inc.
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

package org.slim.content;

import android.Manifest;

/**
 * SlimRoms specific intent definition class.
 */
public class SlimIntent {

    /**
     * Broadcast action: perform any initialization required for CMHW services.
     * Runs when the service receives the signal the device has booted, but
     * should happen before {@link android.content.Intent#ACTION_BOOT_COMPLETED}.
     *
     * Requires {@link cyanogenmod.platform.Manifest.permission#HARDWARE_ABSTRACTION_ACCESS}.
     * @hide
     */
    public static final String ACTION_INITIALIZE_CM_HARDWARE =
            "cyanogenmod.intent.action.INITIALIZE_CM_HARDWARE";

    // Slim intents
    public static final String ACTION_SHORTCUT_PICKED = "slim.intent.action.SHORTCUT_PICKED";
}
