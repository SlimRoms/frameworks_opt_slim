/*
 * Copyright (C) 2016 The SlimRoms Project
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

package org.slim.provider;

public final class SlimSettingsKeys {

    public interface System {

        /**
         * Custom navigation bar intent and action configuration
         * @hide
         */
        public static final String NAVIGATION_BAR_CONFIG = "navigation_bar_config";

         /**
         * Timeout for ambient display notification
         * @hide
         */
        public static final String DOZE_TIMEOUT = "doze_timeout";

        /**
         * Use pick up gesture sensor as doze pulse trigger
         * @hide
         */
        public static final String DOZE_TRIGGER_PICKUP = "doze_trigger_pickup";

        /**
         * Use significant motion sensor as doze pulse trigger
         * @hide
         */
        public static final String DOZE_TRIGGER_SIGMOTION = "doze_trigger_sigmotion";

        /**
         * Use notifications as doze pulse triggers
         * @hide
         */
        public static final String DOZE_TRIGGER_NOTIFICATION = "doze_trigger_notification";

        /**
         * Follow pre-configured doze pulse repeat schedule
         * @hide
         */
        public static final String DOZE_SCHEDULE = "doze_schedule";

        /**
         * Doze pulse screen brightness level
         * @hide
         */
        public static final String DOZE_BRIGHTNESS = "doze_brightness";

        /**
         * Require double tap instead of simple tap to wake from Doze pulse screen
         * @hide
         */
        public static final String DOZE_WAKEUP_DOUBLETAP = "doze_wakeup_doubletap";

        /**
         * Check the proximity sensor during wakeup
         * @hide
         */
        public static final String PROXIMITY_ON_WAKE = "proximity_on_wake";

        /**
         * wake up when plugged or unplugged
         *
         * @hide
         */
        public static final String WAKEUP_WHEN_PLUGGED_UNPLUGGED = "wakeup_when_plugged_unplugged";

        /**
         * Whether the proximity sensor will adjust call to speaker
         * @hide
         */
        public static final String PROXIMITY_AUTO_SPEAKER = "proximity_auto_speaker";

        /**
         * Time delay to activate speaker after proximity sensor triggered
         * @hide
         */
        public static final String PROXIMITY_AUTO_SPEAKER_DELAY = "proximity_auto_speaker_delay";

        /**
         * Whether the proximity sensor will adjust call to speaker,
         * only while in call (not while ringing on outgoing call)
         * @hide
         */
        public static final String PROXIMITY_AUTO_SPEAKER_INCALL_ONLY =
                "proximity_auto_speaker_incall_only";

        /**
         * Navigation bar button color
         * @hide
         */
        public static final String NAVIGATION_BAR_BUTTON_TINT = "navigation_bar_button_tint";

        /**
         * Option To Colorize Navigation bar buttons in different modes
         * 0 = all, 1 = system icons, 2 = system icons + custom user icons
         * @hide
         */
        public static final String NAVIGATION_BAR_BUTTON_TINT_MODE = "navigation_bar_button_tint_mode";

        /**
         * Navigation bar glow color
         * @hide
         */
        public static final String NAVIGATION_BAR_GLOW_TINT = "navigation_bar_glow_tint";

        /**
         * Wether navigation bar is enabled or not
         * @hide
         */
        public static final String NAVIGATION_BAR_SHOW = "navigation_bar_show";

        /**
         * Wether navigation bar is on landscape on the bottom or on the right
         * @hide
         */
        public static final String NAVIGATION_BAR_CAN_MOVE = "navigation_bar_can_move";

        /**
         * Navigation bar height when it is on protrait
         * @hide
         */
        public static final String NAVIGATION_BAR_HEIGHT = "navigation_bar_height";

        /**
         * Navigation bar height when it is on landscape at the bottom
         * @hide
         */
        public static final String NAVIGATION_BAR_HEIGHT_LANDSCAPE = "navigation_bar_height_landscape";

        /**
         * Navigation bar height when it is on landscape at the right
         * @hide
         */
        public static final String NAVIGATION_BAR_WIDTH = "navigation_bar_width";

        /**
         * Wether the navbar menu button is on the left/right/both
         * @hide
         */
        public static final String MENU_LOCATION = "menu_location";

        /**
         * Wether the navbar menu button should show or not
         * @hide
         */
        public static final String MENU_VISIBILITY = "menu_visibility";

        /**
         * Display style of the status bar battery information
         * 0: Display the battery an icon in portrait mode
         * 2: Display the battery as a full circle
         * 3: Display the battery as a dotted circle
         * 4: Hide the battery status information
         * 5: Display the battery an icon in landscape mode
         * 6: Display the battery as plain text
         * default: 0
         * @hide
         */
        public static final String STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style";

        /**
         * Status bar battery %
         * 0: Hide the battery percentage
         * 1: Display the battery percentage inside the icon
         * 2: Display the battery percentage next to the icon
         * @hide
         */
        public static final String STATUS_BAR_BATTERY_PERCENT = "status_bar_battery_percent";

    }

    public interface Secure {
        /**
         * Whether to include options in power menu for rebooting into recovery and bootloader
         * @hide
         */
        public static final String ADVANCED_REBOOT = "advanced_reboot";
    }

    public interface Global {
    }

}
