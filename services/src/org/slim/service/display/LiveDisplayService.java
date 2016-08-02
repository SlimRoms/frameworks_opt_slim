/*
 * Copyright (C) 2016 The CyanogenMod Project
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
package org.slim.service.display;

import static org.slim.hardware.LiveDisplayManager.FEATURE_MANAGED_OUTDOOR_MODE;
import static org.slim.hardware.LiveDisplayManager.MODE_DAY;
import static org.slim.hardware.LiveDisplayManager.MODE_FIRST;
import static org.slim.hardware.LiveDisplayManager.MODE_LAST;
import static org.slim.hardware.LiveDisplayManager.MODE_OFF;
import static org.slim.hardware.LiveDisplayManager.MODE_OUTDOOR;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManagerInternal;
import android.os.Process;
import android.os.UserHandle;
import android.view.Display;

import com.android.server.LocalServices;
import com.android.server.ServiceThread;
import com.android.server.twilight.TwilightListener;
import com.android.server.twilight.TwilightManager;
import com.android.server.twilight.TwilightState;

import org.slim.service.SlimSystemService;
import org.slim.service.pm.UserContentObserver;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;

import org.slim.constants.SlimServiceConstants;
import org.slim.hardware.ILiveDisplayService;
import org.slim.hardware.LiveDisplayConfig;
import org.slim.provider.SlimSettings;

/**
 * LiveDisplay is an advanced set of features for improving
 * display quality under various ambient conditions.
 *
 * The service is constructed with a set of LiveDisplayFeatures
 * which provide capabilities such as outdoor mode, night mode,
 * and calibration. It interacts with CMHardwareService to relay
 * changes down to the lower layers.
 */
public class LiveDisplayService extends SlimSystemService {

    private static final String TAG = "LiveDisplay";

    private final Context mContext;
    private final Handler mHandler;
    private final ServiceThread mHandlerThread;

    private DisplayManager mDisplayManager;
    private ModeObserver mModeObserver;
    private TwilightManager mTwilightManager;

    private boolean mAwaitingNudge = true;
    private boolean mSunset = false;

    private final List<LiveDisplayFeature> mFeatures = new ArrayList<LiveDisplayFeature>();

    private ColorTemperatureController mCTC;
    private DisplayHardwareController mDHC;
    private OutdoorModeController mOMC;

    private LiveDisplayConfig mConfig;

    static int MODE_CHANGED = 1;
    static int DISPLAY_CHANGED = 2;
    static int TWILIGHT_CHANGED = 4;
    static int ALL_CHANGED = 255;

    static class State {
        public boolean mLowPowerMode = false;
        public boolean mScreenOn = false;
        public int mMode = -1;
        public TwilightState mTwilight = null;

        @Override
        public String toString() {
            return String.format(
                    "[mLowPowerMode=%b, mScreenOn=%b, mMode=%d, mTwilight=%s",
                    mLowPowerMode, mScreenOn, mMode,
                    (mTwilight == null ? "NULL" : mTwilight.toString()));
        }
    }

    private final State mState = new State();

    public LiveDisplayService(Context context) {
        super(context);

        mContext = context;

        mHandlerThread = new ServiceThread(TAG,
                Process.THREAD_PRIORITY_DEFAULT, false /*allowIo*/);
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }

    /*@Override
    public String getFeatureDeclaration() {
        return CMContextConstants.Features.LIVEDISPLAY;
    }*/

    /*@Override
    public boolean isCoreService() {
        return false;
    }*/

    @Override
    public void onStart() {
        publishBinderService(SlimServiceConstants.CM_LIVEDISPLAY_SERVICE, mBinder);
    }

    @Override
    public void onBootPhase(int phase) {
        if (phase == PHASE_BOOT_COMPLETED) {

            mAwaitingNudge = getSunsetCounter() < 1;

            mDHC = new DisplayHardwareController(mContext, mHandler);
            mFeatures.add(mDHC);

            mCTC = new ColorTemperatureController(mContext, mHandler, mDHC);
            mFeatures.add(mCTC);

            mOMC = new OutdoorModeController(mContext, mHandler);
            mFeatures.add(mOMC);

            // Get capabilities, throw out any unused features
            final BitSet capabilities = new BitSet();
            for (Iterator<LiveDisplayFeature> it = mFeatures.iterator(); it.hasNext();) {
                final LiveDisplayFeature feature = it.next();
                if (!feature.getCapabilities(capabilities)) {
                    it.remove();
                }
            }

            // static config
            int defaultMode = mContext.getResources().getInteger(
                    org.slim.framework.internal.R.integer.config_defaultLiveDisplayMode);

            mConfig = new LiveDisplayConfig(capabilities, defaultMode,
                    mCTC.getDefaultDayTemperature(), mCTC.getDefaultNightTemperature(),
                    mOMC.getDefaultAutoOutdoorMode(), mDHC.getDefaultAutoContrast(),
                    mDHC.getDefaultCABC(), mDHC.getDefaultColorEnhancement(),
                    mCTC.getColorTemperatureRange(), mCTC.getColorBalanceRange());

            // listeners
            mDisplayManager = (DisplayManager) getContext().getSystemService(
                    Context.DISPLAY_SERVICE);
            mDisplayManager.registerDisplayListener(mDisplayListener, null);
            mState.mScreenOn = mDisplayManager.getDisplay(
                    Display.DEFAULT_DISPLAY).getState() == Display.STATE_ON;

            PowerManagerInternal pmi = LocalServices.getService(PowerManagerInternal.class);
            pmi.registerLowPowerModeObserver(mLowPowerModeListener);
            mState.mLowPowerMode = pmi.getLowPowerModeEnabled();

            mTwilightManager = LocalServices.getService(TwilightManager.class);
            if (mTwilightManager != null) {
                mTwilightManager.registerListener(mTwilightListener, mHandler);
                mState.mTwilight = mTwilightManager.getCurrentState();
            }

            if (mConfig.hasModeSupport()) {
                mModeObserver = new ModeObserver(mHandler);
                mState.mMode = mModeObserver.getMode();
            }

            // start and update all features
            for (int i = 0; i < mFeatures.size(); i++) {
                mFeatures.get(i).start();
            }

            updateFeatures(ALL_CHANGED);
        }
    }

    private void updateFeatures(final int flags) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < mFeatures.size(); i++) {
                    mFeatures.get(i).update(flags, mState);
                }
            }
        });
    }
    private final IBinder mBinder = new ILiveDisplayService.Stub() {

        @Override
        public LiveDisplayConfig getConfig() {
            return mConfig;
        }

        @Override
        public int getMode() {
            if (mConfig.hasModeSupport()) {
                return mModeObserver.getMode();
            } else {
                return MODE_OFF;
            }
        }

        @Override
        public boolean setMode(int mode) {
            enforceLiveDisplayPermission(mContext);
            if (!mConfig.hasModeSupport()) {
                return false;
            }
            return mModeObserver.setMode(mode);
        }

        @Override
        public float[] getColorAdjustment() {
            return mDHC.getColorAdjustment();
        }

        @Override
        public boolean setColorAdjustment(float[] adj) {
            enforceLiveDisplayPermission(mContext);
            return mDHC.setColorAdjustment(adj);
        }

        @Override
        public boolean isAutoContrastEnabled() {
            return mDHC.isAutoContrastEnabled();
        }

        @Override
        public  boolean setAutoContrastEnabled(boolean enabled) {
            enforceLiveDisplayPermission(mContext);
            return mDHC.setAutoContrastEnabled(enabled);
        }

        @Override
        public boolean isCABCEnabled() {
            return mDHC.isCABCEnabled();
        }

        @Override
        public boolean setCABCEnabled(boolean enabled) {
            enforceLiveDisplayPermission(mContext);
            return mDHC.setCABCEnabled(enabled);
        }

        @Override
        public boolean isColorEnhancementEnabled() {
            return mDHC.isColorEnhancementEnabled();
        }

        @Override
        public boolean setColorEnhancementEnabled(boolean enabled) {
            enforceLiveDisplayPermission(mContext);
            return mDHC.setColorEnhancementEnabled(enabled);
        }

        @Override
        public boolean isAutomaticOutdoorModeEnabled() {
            return mOMC.isAutomaticOutdoorModeEnabled();
        }

        @Override
        public boolean setAutomaticOutdoorModeEnabled(boolean enabled) {
            enforceLiveDisplayPermission(mContext);
            return mOMC.setAutomaticOutdoorModeEnabled(enabled);
        }

        @Override
        public int getDayColorTemperature() {
            return mCTC.getDayColorTemperature();
        }

        @Override
        public boolean setDayColorTemperature(int temperature) {
            enforceLiveDisplayPermission(mContext);
            mCTC.setDayColorTemperature(temperature);
            return true;
        }

        @Override
        public int getNightColorTemperature() {
            return mCTC.getNightColorTemperature();
        }

        @Override
        public boolean setNightColorTemperature(int temperature) {
            enforceLiveDisplayPermission(mContext);
            mCTC.setNightColorTemperature(temperature);
            return true;
        }

        @Override
        public int getColorTemperature() {
            return mCTC.getColorTemperature();
        }

        @Override
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            mContext.enforceCallingOrSelfPermission(android.Manifest.permission.DUMP, TAG);

            pw.println();
            pw.println("LiveDisplay Service State:");
            pw.println("  mState=" + mState.toString());
            pw.println("  mConfig=" + mConfig.toString());
            pw.println("  mAwaitingNudge=" + mAwaitingNudge);

            for (int i = 0; i < mFeatures.size(); i++) {
                mFeatures.get(i).dump(pw);
            }
        }
    };

    // Listener for screen on/off events
    private final DisplayManager.DisplayListener mDisplayListener =
            new DisplayManager.DisplayListener() {
        @Override
        public void onDisplayAdded(int displayId) {
        }

        @Override
        public void onDisplayRemoved(int displayId) {
        }

        @Override
        public void onDisplayChanged(int displayId) {
            if (displayId == Display.DEFAULT_DISPLAY) {
                boolean screenOn = isScreenOn();
                if (screenOn != mState.mScreenOn) {
                    mState.mScreenOn = screenOn;
                    updateFeatures(DISPLAY_CHANGED);
                }
            }
        }
    };


    // Display postprocessing can have power impact.
    private PowerManagerInternal.LowPowerModeListener mLowPowerModeListener =
            new PowerManagerInternal.LowPowerModeListener() {
        @Override
        public void onLowPowerModeChanged(boolean lowPowerMode) {
            if (lowPowerMode != mState.mLowPowerMode) {
                mState.mLowPowerMode = lowPowerMode;
                updateFeatures(MODE_CHANGED);
            }
         }
    };

    // Watch for mode changes
    private final class ModeObserver extends UserContentObserver {

        private final Uri MODE_SETTING =
                SlimSettings.System.getUriFor(SlimSettings.System.DISPLAY_TEMPERATURE_MODE);

        ModeObserver(Handler handler) {
            super(handler);

            final ContentResolver cr = mContext.getContentResolver();
            cr.registerContentObserver(MODE_SETTING, false, this, UserHandle.USER_ALL);

            observe();
        }

        @Override
        protected void update() {
            int mode = getMode();
            if (mode != mState.mMode) {
                mState.mMode = mode;

                updateFeatures(MODE_CHANGED);
            }
        }

        int getMode() {
            return getInt(SlimSettings.System.DISPLAY_TEMPERATURE_MODE,
                    mConfig.getDefaultMode());
        }

        boolean setMode(int mode) {
            if (mConfig.hasFeature(mode) && mode >= MODE_FIRST && mode <= MODE_LAST) {
                putInt(SlimSettings.System.DISPLAY_TEMPERATURE_MODE, mode);
                if (mode != mConfig.getDefaultMode()) {
                    stopNudgingMe();
                }
                return true;
            }
            return false;
        }
    }

    // Night watchman
    private final TwilightListener mTwilightListener = new TwilightListener() {
        @Override
        public void onTwilightStateChanged() {
            mState.mTwilight = mTwilightManager.getCurrentState();
            updateFeatures(TWILIGHT_CHANGED);
            nudge();
        }
    };

    private boolean isScreenOn() {
        return mDisplayManager.getDisplay(
                Display.DEFAULT_DISPLAY).getState() == Display.STATE_ON;
    }

    private int getSunsetCounter() {
        // Counter used to determine when we should tell the user about this feature.
        // If it's not used after 3 sunsets, we'll show the hint once.
        return SlimSettings.System.getIntForUser(mContext.getContentResolver(),
                SlimSettings.System.LIVE_DISPLAY_HINTED,
                -3,
                UserHandle.USER_CURRENT);
    }


    private void updateSunsetCounter(int count) {
        SlimSettings.System.putIntForUser(mContext.getContentResolver(),
                SlimSettings.System.LIVE_DISPLAY_HINTED,
                count,
                UserHandle.USER_CURRENT);
        mAwaitingNudge = count > 0;
    }

    private void stopNudgingMe() {
        if (mAwaitingNudge) {
            updateSunsetCounter(1);
        }
    }

    /**
     * Show a friendly notification to the user about the potential benefits of decreasing
     * blue light at night. Do this only once if the feature has not been used after
     * three sunsets. It would be great to enable this by default, but we don't want
     * the change of screen color to be considered a "bug" by a user who doesn't
     * understand what's happening.
     *
     * @param state
     */
    private void nudge() {
        final TwilightState twilight = mTwilightManager.getCurrentState();
        if (!mAwaitingNudge || twilight == null) {
            return;
        }

        int counter = getSunsetCounter();

        // check if we should send the hint only once after sunset
        boolean transition = twilight.isNight() && !mSunset;
        mSunset = twilight.isNight();
        if (!transition) {
            return;
        }

        if (counter <= 0) {
            counter++;
            updateSunsetCounter(counter);
        }
        if (counter == 0) {
            //show the notification and don't come back here
            final Intent intent = new Intent(SlimSettings.ACTION_LIVEDISPLAY_SETTINGS);
            PendingIntent result = PendingIntent.getActivity(
                    mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            Notification.Builder builder = new Notification.Builder(mContext)
                    .setContentTitle(mContext.getResources().getString(
                            org.slim.framework.internal.R.string.live_display_title))
                    .setContentText(mContext.getResources().getString(
                            org.slim.framework.internal.R.string.live_display_hint))
                    .setSmallIcon(org.slim.framework.internal.R.drawable.ic_livedisplay_notif)
                    .setStyle(new Notification.BigTextStyle().bigText(mContext.getResources()
                             .getString(
                                     org.slim.framework.internal.R.string.live_display_hint)))
                    .setContentIntent(result)
                    .setAutoCancel(true);

            NotificationManager nm =
                    (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.notifyAsUser(null, 1, builder.build(), UserHandle.CURRENT);

            updateSunsetCounter(1);
        }
    }

    private int getInt(String setting, int defValue) {
        return SlimSettings.System.getIntForUser(mContext.getContentResolver(),
                setting, defValue, UserHandle.USER_CURRENT);
    }

    private void putInt(String setting, int value) {
        SlimSettings.System.putIntForUser(mContext.getContentResolver(),
                setting, value, UserHandle.USER_CURRENT);
    }

    private static void enforceLiveDisplayPermission(Context context) {
        context.enforceCallingOrSelfPermission(
                    org.slim.framework.Manifest.permission.MANAGE_LIVEDISPLAY, null);
    }
}
