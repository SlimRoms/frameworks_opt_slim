package org.slim.utils;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.ConnectivityManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.android.internal.telephony.PhoneConstants;

import java.util.Iterator;
import java.util.List;

public class QSUtil {

    private static boolean sAvailableTilesFiltered;

    public static List<String> getAvailableTiles(Context context) {
        filterTiles(context);
        return (List<String>) QSConstants.TILES_AVAILABLE.clone();
    }

    private static void filterTiles(Context context) {
        if (!sAvailableTilesFiltered) {
            filterTiles(context, QSConstants.TILES_AVAILABLE);
            sAvailableTilesFiltered = true;
        }
    }

    private static void filterTiles(Context context, List<String> tiles) {
        boolean deviceSupportsMobile = deviceSupportsMobileData(context);

        // Tiles that need conditional filtering
        Iterator<String> iterator = tiles.iterator();
        while (iterator.hasNext()) {
            String tileKey = iterator.next();
            boolean removeTile = false;
            switch (tileKey) {
                case QSConstants.TILE_CELLULAR:
                case QSConstants.TILE_HOTSPOT:
                case QSConstants.TILE_DATA:
                case QSConstants.TILE_ROAMING:
                case QSConstants.TILE_APN:
                    removeTile = !deviceSupportsMobile;
                    break;
                case QSConstants.TILE_DDS:
                    removeTile = !deviceSupportsDdsSupported(context);
                    break;
                case QSConstants.TILE_FLASHLIGHT:
                    removeTile = !deviceSupportsFlashLight(context);
                    break;
                case QSConstants.TILE_BLUETOOTH:
                    removeTile = !deviceSupportsBluetooth();
                    break;
                case QSConstants.TILE_NFC:
                    removeTile = !deviceSupportsNfc(context);
                    break;
                case QSConstants.TILE_COMPASS:
                    removeTile = !deviceSupportsCompass(context);
                    break;
                case QSConstants.TILE_AMBIENT_DISPLAY:
                    removeTile = !deviceSupportsDoze(context);
                    break;
            }
            if (removeTile) {
                iterator.remove();
            }
        }
    }

    /*public static boolean deviceSupportsLte(Context ctx) {
        final TelephonyManager tm = (TelephonyManager)
                ctx.getSystemService(Context.TELEPHONY_SERVICE);
        return (tm.getLteOnCdmaMode() == PhoneConstants.LTE_ON_CDMA_TRUE)
                || tm.getLteOnGsmMode() != 0;
    }*/

    public static boolean deviceSupportsDdsSupported(Context context) {
        TelephonyManager tm = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.isMultiSimEnabled()
                && tm.getMultiSimConfiguration() == TelephonyManager.MultiSimVariants.DSDA;
    }

    public static boolean deviceSupportsMobileData(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        return cm.isNetworkSupported(ConnectivityManager.TYPE_MOBILE);
    }

    public static boolean deviceSupportsBluetooth() {
        return BluetoothAdapter.getDefaultAdapter() != null;
    }

    public static boolean deviceSupportsNfc(Context context) {
        PackageManager packageManager = context.getPackageManager();
        return packageManager.hasSystemFeature(PackageManager.FEATURE_NFC);
    }

    public static boolean deviceSupportsFlashLight(Context context) {
        CameraManager cameraManager = (CameraManager) context.getSystemService(
                Context.CAMERA_SERVICE);
        try {
            String[] ids = cameraManager.getCameraIdList();
            for (String id : ids) {
                CameraCharacteristics c = cameraManager.getCameraCharacteristics(id);
                Boolean flashAvailable = c.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                Integer lensFacing = c.get(CameraCharacteristics.LENS_FACING);
                if (flashAvailable != null
                        && flashAvailable
                        && lensFacing != null
                        && lensFacing == CameraCharacteristics.LENS_FACING_BACK) {
                    return true;
                }
            }
        } catch (CameraAccessException | AssertionError e) {
            // Ignore
        }
        return false;
    }

    public static boolean deviceSupportsCompass(Context context) {
        SensorManager sm = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        return sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null
                && sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null;
    }

    public static boolean deviceSupportsDoze(Context context) {
        String name = context.getResources().getString(
                com.android.internal.R.string.config_dozeComponent);
        return !TextUtils.isEmpty(name);
    }
}
