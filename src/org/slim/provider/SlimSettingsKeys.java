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

    }

    public interface Secure {
    }

    public interface Global {
    }

}
