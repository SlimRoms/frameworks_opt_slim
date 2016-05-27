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

        /** @hide */
        public static final Validator DOZE_TIMEOUT_VALIDATOR = sNonNegativeIntegerValidator;

        /**
         * Use pick up gesture sensor as doze pulse trigger
         * @hide
         */
        public static final String DOZE_TRIGGER_PICKUP = "doze_trigger_pickup";

        /** @hide */
        public static final Validator DOZE_TRIGGER_PICKUP_VALIDATOR = sBooleanValidator;

        /**
         * Use significant motion sensor as doze pulse trigger
         * @hide
         */
        public static final String DOZE_TRIGGER_SIGMOTION = "doze_trigger_sigmotion";

        /** @hide */
        public static final Validator DOZE_TRIGGER_SIGMOTION_VALIDATOR = sBooleanValidator;

        /**
         * Use notifications as doze pulse triggers
         * @hide
         */
        public static final String DOZE_TRIGGER_NOTIFICATION = "doze_trigger_notification";

        /** @hide */
        public static final Validator DOZE_TRIGGER_NOTIFICATION_VALIDATOR = sBooleanValidator;

        /**
         * Follow pre-configured doze pulse repeat schedule
         * @hide
         */
        public static final String DOZE_SCHEDULE = "doze_schedule";

        /** @hide */
        public static final Validator DOZE_SCHEDULE_VALIDATOR = sBooleanValidator;

        /**
         * Doze pulse screen brightness level
         * @hide
         */
        public static final String DOZE_BRIGHTNESS = "doze_brightness";

        /** @hide */
        public static final Validator DOZE_BRIGHTNESS_VALIDATOR =
                new InclusiveFloatRangeValidator(0, 1);

    }

    public interface Secure {
    }

    public interface Global {
    }

}
