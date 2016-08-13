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
 * limitations under the License
 */

package org.slim.utils;

import java.util.ArrayList;

public class QSConstants {
    private QSConstants() {}

    public static final String TILE_WIFI = "wifi";
    public static final String TILE_BLUETOOTH = "bt";
    public static final String TILE_INVERSION = "inversion";
    public static final String TILE_CELLULAR = "cell";
    public static final String TILE_AIRPLANE = "airplane";
    public static final String TILE_ROTATION = "rotation";
    public static final String TILE_FLASHLIGHT = "flashlight";
    public static final String TILE_LOCATION = "location";
    public static final String TILE_CAST = "cast";
    public static final String TILE_HOTSPOT = "hotspot";
    public static final String TILE_NOTIFICATIONS = "notifications";
    public static final String TILE_DATA = "data";
    public static final String TILE_ROAMING = "roaming";
    public static final String TILE_DDS = "dds";
    public static final String TILE_APN = "apn";
    public static final String TILE_NFC = "nfc";
    public static final String TILE_COMPASS = "compass";
    public static final String TILE_LOCKSCREEN = "lockscreen";
    public static final String TILE_LTE = "lte";
    public static final String TILE_VISUALIZER = "visualizer";
    public static final String TILE_VOLUME = "volume";
    public static final String TILE_SCREEN_TIMEOUT = "timeout";
    public static final String TILE_USB_TETHER = "usb_tether";
    public static final String TILE_HEADS_UP = "heads_up";
    public static final String TILE_AMBIENT_DISPLAY = "ambient_display";
    public static final String TILE_SYNC = "sync";
    public static final String TILE_BATTERY_SAVER = "battery_saver";
    public static final String TILE_CAFFEINE = "caffeine";
    public static final String TILE_DND = "dnd";
    public static final String TILE_SCREENSHOT = "screenshot";
    public static final String TILE_SCREENOFF = "screenoff";
    public static final String TILE_BRIGHTNESS = "brightness";
    public static final String TILE_MUSIC = "music";
    public static final String TILE_REBOOT = "reboot";
    public static final String TILE_IME = "ime";
    public static final String TILE_SOUND = "sound";

    public static final String DYNAMIC_TILE_NEXT_ALARM = "next_alarm";
    public static final String DYNAMIC_TILE_IME_SELECTOR = "ime_selector";

    protected static final ArrayList<String> TILES_AVAILABLE = new ArrayList<String>();

    static {
        TILES_AVAILABLE.add(TILE_WIFI);
        TILES_AVAILABLE.add(TILE_BLUETOOTH);
        TILES_AVAILABLE.add(TILE_CELLULAR);
        TILES_AVAILABLE.add(TILE_AIRPLANE);
        TILES_AVAILABLE.add(TILE_ROTATION);
        TILES_AVAILABLE.add(TILE_FLASHLIGHT);
        TILES_AVAILABLE.add(TILE_LOCATION);
        TILES_AVAILABLE.add(TILE_CAST);
        TILES_AVAILABLE.add(TILE_HOTSPOT);
        TILES_AVAILABLE.add(TILE_INVERSION);
        TILES_AVAILABLE.add(TILE_DND);
        TILES_AVAILABLE.add(TILE_NFC);
        TILES_AVAILABLE.add(TILE_COMPASS);
        TILES_AVAILABLE.add(TILE_LOCKSCREEN);
        TILES_AVAILABLE.add(TILE_VOLUME);
        TILES_AVAILABLE.add(TILE_SCREEN_TIMEOUT);
        TILES_AVAILABLE.add(TILE_USB_TETHER);
        TILES_AVAILABLE.add(TILE_HEADS_UP);
        TILES_AVAILABLE.add(TILE_AMBIENT_DISPLAY);
        TILES_AVAILABLE.add(TILE_SYNC);
        TILES_AVAILABLE.add(TILE_BATTERY_SAVER);
        TILES_AVAILABLE.add(TILE_CAFFEINE);
        TILES_AVAILABLE.add(TILE_SCREENSHOT);
        TILES_AVAILABLE.add(TILE_SCREENOFF);
        TILES_AVAILABLE.add(TILE_BRIGHTNESS);
        TILES_AVAILABLE.add(TILE_MUSIC);
        TILES_AVAILABLE.add(TILE_REBOOT);
        TILES_AVAILABLE.add(TILE_IME);
        TILES_AVAILABLE.add(TILE_SOUND);
    }
}
