/**
 * Copyright (c) 2016, The SlimRoms Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.slim.framework.internal.logging;

import com.android.internal.logging.MetricsLogger;

/**
 * Serves as a central location for logging constants that is android release agnostic.
 */
public class SlimMetricsLogger extends MetricsLogger {

    private static final int BASE = -Integer.MAX_VALUE;

    public static final int INTERFACE_SETTINGS = BASE + 2;
    public static final int STATUS_BAR_SETTINGS = BASE + 3;
    public static final int NAVIGATION_SETTINGS = BASE + 4;
    public static final int HARDWAREKEYS_SETTINGS = BASE + 5;
    public static final int NAV_BAR_STYLE_SETTINGS = BASE + 6;
    public static final int NAV_BAR_SETTINGS = BASE + 7;
    public static final int NAV_BAR_DIMEN_SETTINGS = BASE + 8;
    public static final int RECENT_PANEL_SETTINGS = BASE + 9;
    public static final int CLOCK_SETTINGS = BASE + 10;
    public static final int ADVANCED_SETTINGS = BASE + 11;
    public static final int NAVBAR_BUTTON_SETTINGS = BASE + 12;
    public static final int DOZE_SETTINGS = BASE + 13;
    public static final int PROXIMITY_AUTO_SPEAKER_SETTINGS = BASE + 14;
}
