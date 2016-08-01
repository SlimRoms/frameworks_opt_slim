package com.android.systemui.statusbar.slim;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.util.Slog;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.systemui.R;
import com.android.systemui.recents.ScreenPinningRequest;
import com.android.systemui.slimrecent.RecentController;
import com.android.systemui.statusbar.phone.NavigationBarView;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.phone.PhoneStatusBarView;

import java.util.List;

import org.slim.constants.SlimServiceConstants;
import org.slim.framework.internal.statusbar.ISlimStatusBarService;
import org.slim.statusbar.SlimStatusBarManager;
import org.slim.provider.SlimSettings;

public class SlimStatusBar extends PhoneStatusBar implements
        SlimCommandQueue.Callbacks {

    private static final String TAG = "SlimStatusBar";

    protected static final int MSG_TOGGLE_LAST_APP = 11001;
    protected static final int MSG_TOGGLE_KILL_APP = 11002;
    protected static final int MSG_TOGGLE_SCREENSHOT = 11003;

    private PhoneStatusBarView mStatusBarView;
    private SlimNavigationBarView mSlimNavigationBarView;
    private RecentController mSlimRecents;
    private SlimCommandQueue mSlimCommandQueue;
    private Handler mHandler = new H();
    private Display mDisplay;

    private boolean mHasNavigationBar;
    private boolean mAttached;
    private boolean mUseSlimRecents = true;

    private long mLastLockToAppLongPress;

    private ScreenPinningRequest.ScreenPinningCallback mScreenPinningCallback =
            new ScreenPinningRequest.ScreenPinningCallback() {
        @Override
        public void onStartLockTask() {
            mSlimNavigationBarView.setOverrideMenuKeys(true);
        }
    };

    private SlimKeyButtonView.LongClickCallback mLongClickCallback =
            new SlimKeyButtonView.LongClickCallback() {
        @Override
        public boolean onLongClick(View v) {
            return handleLongPressBackRecents(v);
        }
    };

    class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(SlimSettings.System.getUriFor(
                    SlimSettings.System.NAVIGATION_BAR_BUTTON_TINT),
                    false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(SlimSettings.System.getUriFor(
                    SlimSettings.System.NAVIGATION_BAR_BUTTON_TINT_MODE),
                    false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(SlimSettings.System.getUriFor(
                    SlimSettings.System.NAVIGATION_BAR_GLOW_TINT),
                    false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(SlimSettings.System.getUriFor(
                    SlimSettings.System.NAVIGATION_BAR_SHOW),
                    false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(SlimSettings.System.getUriFor(
                    SlimSettings.System.NAVIGATION_BAR_CONFIG),
                    false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(SlimSettings.System.getUriFor(
                    SlimSettings.System.NAVIGATION_BAR_CAN_MOVE),
                    false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(SlimSettings.System.getUriFor(
                    SlimSettings.System.MENU_LOCATION),
                    false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(SlimSettings.System.getUriFor(
                    SlimSettings.System.MENU_VISIBILITY),
                    false, this, UserHandle.USER_ALL);
            resolver.registerContentObserver(SlimSettings.System.getUriFor(
                    SlimSettings.System.USE_SLIM_RECENTS), false, this,
                    UserHandle.USER_ALL);
            resolver.registerContentObserver(SlimSettings.System.getUriFor(
                    SlimSettings.System.RECENT_CARD_BG_COLOR), false, this,
                    UserHandle.USER_ALL);
            resolver.registerContentObserver(SlimSettings.System.getUriFor(
                    SlimSettings.System.RECENT_CARD_TEXT_COLOR), false, this,
                    UserHandle.USER_ALL);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);

            if (uri.equals(SlimSettings.System.getUriFor(
                    SlimSettings.System.NAVIGATION_BAR_BUTTON_TINT))
                || uri.equals(SlimSettings.System.getUriFor(
                    SlimSettings.System.NAVIGATION_BAR_BUTTON_TINT_MODE))
                || uri.equals(SlimSettings.System.getUriFor(
                    SlimSettings.System.NAVIGATION_BAR_CONFIG))
                || uri.equals(SlimSettings.System.getUriFor(
                    SlimSettings.System.NAVIGATION_BAR_GLOW_TINT))
                || uri.equals(SlimSettings.System.getUriFor(
                    SlimSettings.System.MENU_LOCATION))
                || uri.equals(SlimSettings.System.getUriFor(
                    SlimSettings.System.MENU_VISIBILITY))) {
                if (mSlimNavigationBarView != null) {
                    mSlimNavigationBarView.recreateNavigationBar();
                    prepareNavigationBarView();
                }
            } else if (uri.equals(SlimSettings.System.getUriFor(
                    SlimSettings.System.NAVIGATION_BAR_CAN_MOVE))) {
                prepareNavigationBarView();
            } else if (uri.equals(SlimSettings.System.getUriFor(
                    SlimSettings.System.NAVIGATION_BAR_SHOW))) {
                updateNavigationBarVisibility();
            } else if (uri.equals(SlimSettings.System.getUriFor(
                    SlimSettings.System.USE_SLIM_RECENTS))) {
                updateRecents();
            } else if (uri.equals(SlimSettings.System.getUriFor(
                    SlimSettings.System.RECENT_CARD_BG_COLOR))
                    || uri.equals(SlimSettings.System.getUriFor(
                    SlimSettings.System.RECENT_CARD_TEXT_COLOR))) {
                rebuildRecentsScreen();
            }
        }
    }

    @Override
    public void start() {
        super.start();

        mDisplay = ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE))
                .getDefaultDisplay();

        ISlimStatusBarService barService = SlimStatusBarManager.getService();

        mSlimCommandQueue = new SlimCommandQueue(this);
        try {
            if (barService != null) barService.registerSlimStatusBar(mSlimCommandQueue);
        } catch (RemoteException e) {
            // if the system process isn't there we're doomed anyway.
        }

        updateRecents();

        SettingsObserver observer = new SettingsObserver(mHandler);
        observer.observe();

        mScreenPinningRequest.setCallback(mScreenPinningCallback);
    }

    @Override
    protected PhoneStatusBarView makeStatusBarView() {
        mStatusBarView = super.makeStatusBarView();

        Log.d(TAG, "makeStatusBarView");

        if (mSlimNavigationBarView == null) {
            mSlimNavigationBarView = (SlimNavigationBarView)
                    View.inflate(mContext, R.layout.slim_navigation_bar, null);
        }

        mSlimNavigationBarView.setDisabledFlags(mDisabled1);
        mSlimNavigationBarView.setBar(this);
        mSlimNavigationBarView.setOnVerticalChangedListener(
                new NavigationBarView.OnVerticalChangedListener() {
            @Override
            public void onVerticalChanged(boolean isVertical) {
                if (mAssistManager != null) {
                    mAssistManager.onConfigurationChanged();
                }
                mNotificationPanel.setQsScrimEnabled(!isVertical);
            }
        });
        mSlimNavigationBarView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                checkUserAutohide(v, event);
                return false;
            }
        });

        if (!(mNavigationBarView instanceof SlimNavigationBarView) || mNavigationBarView != null) {
            if (mNavigationBarView.isAttachedToWindow()) {
                try {
                    mWindowManager.removeView(mNavigationBarView);
                } catch (Exception e) {}
            }
        }

        if (mNavigationBarView != mSlimNavigationBarView) {
            mNavigationBarView = mSlimNavigationBarView;
        }

        updateNavigationBarVisibility();

        return mStatusBarView;
    }

    @Override
    protected void hideRecents(boolean triggeredFromAltTab, boolean triggeredFromHomeKey) {
        if (mSlimRecents != null) {
            mSlimRecents.hideRecents(triggeredFromHomeKey);
        } else {
            super.hideRecents(triggeredFromAltTab, triggeredFromHomeKey);
        }
    }

    @Override
    protected void toggleRecents() {
        if (mSlimRecents != null) {
            sendCloseSystemWindows(mContext, SYSTEM_DIALOG_REASON_RECENT_APPS);
            mSlimRecents.toggleRecents(mDisplay, mLayoutDirection, getStatusBarView());
        } else {
            super.toggleRecents();
        }
    }

    @Override
    protected void preloadRecents() {
        if (mSlimRecents != null) {
            mSlimRecents.preloadRecentTasksList();
        } else {
            super.preloadRecents();
        }
    }

    @Override
    protected void cancelPreloadingRecents() {
        if (mSlimRecents != null) {
            mSlimRecents.cancelPreloadingRecentTasksList();
        } else {
            super.cancelPreloadingRecents();
        }
    }

    protected void rebuildRecentsScreen() {
        if (mSlimRecents != null) {
            mSlimRecents.rebuildRecentsScreen();
        }
    }

    protected void updateRecents() {
        boolean slimRecents = SlimSettings.System.getIntForUser(mContext.getContentResolver(),
                SlimSettings.System.USE_SLIM_RECENTS, 1, UserHandle.USER_CURRENT) == 1;

        if (slimRecents) {
            mSlimRecents = new RecentController(mContext, mLayoutDirection);
            mSlimRecents.setCallback(this);
            rebuildRecentsScreen();
        } else {
            mSlimRecents = null;
        }
    }

    private void updateNavigationBarVisibility() {
        final int showByDefault = mContext.getResources().getBoolean(
                    com.android.internal.R.bool.config_showNavigationBar) ? 1 : 0;
        mHasNavigationBar = SlimSettings.System.getIntForUser(mContext.getContentResolver(),
                    SlimSettings.System.NAVIGATION_BAR_SHOW, showByDefault,
                    UserHandle.USER_CURRENT) == 1;

        if (mHasNavigationBar) {
            addNavigationBar();
        } else {
            if (mAttached) {
                mWindowManager.removeView(mSlimNavigationBarView);
            }
        }
    }

    @Override
    protected void prepareNavigationBarView() {
        mSlimNavigationBarView.reorient();

        View home = mSlimNavigationBarView.getHomeButton();
        View recents = mSlimNavigationBarView.getRecentsButton();

        mSlimNavigationBarView.setPinningCallback(mLongClickCallback);

        /*if (recents != null) {
            recents.setOnClickListener(mRecentsClickListener);
            recents.setOnTouchListener(mRecentsPreloadOnTouchListener);
        }
        if (home != null) {
            home.setOnTouchListener(mHomeActionListener);
        }*/

        mAssistManager.onConfigurationChanged();
    }

    @Override
    protected void addNavigationBar() {
        if (DEBUG) Log.v(TAG, "addNavigationBar: about to add " + mSlimNavigationBarView);
        if (mSlimNavigationBarView == null) return;

        prepareNavigationBarView();

        if (!mAttached) {
            try {
                mWindowManager.addView(mSlimNavigationBarView, getNavigationBarLayoutParams());
            } catch (Exception e) {}
        }
    }

    @Override
    protected void repositionNavigationBar() {
        if (mSlimNavigationBarView == null
                || !mSlimNavigationBarView.isAttachedToWindow()) return;

        prepareNavigationBarView();

        mWindowManager.updateViewLayout(mSlimNavigationBarView, getNavigationBarLayoutParams());
    }

    private WindowManager.LayoutParams getNavigationBarLayoutParams() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_NAVIGATION_BAR,
                    0
                    | WindowManager.LayoutParams.FLAG_TOUCHABLE_WHEN_WAKING
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                    | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
                PixelFormat.TRANSLUCENT);
        // this will allow the navbar to run in an overlay on devices that support this
        if (ActivityManager.isHighEndGfx()) {
            lp.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        }

        lp.setTitle("NavigationBar");
        lp.windowAnimations = 0;
        return lp;
    }

    private boolean handleLongPressBackRecents(View v) {
        try {
            boolean sendBackLongPress = false;
            IActivityManager activityManager = ActivityManagerNative.getDefault();
            boolean isAccessiblityEnabled = mAccessibilityManager.isEnabled();
            if (activityManager.isInLockTaskMode() && !isAccessiblityEnabled) {
                // If we recently long-pressed the other button then they were
                // long-pressed 'together'
                if (mSlimNavigationBarView.getRightMenuButton().isPressed()
                        && mSlimNavigationBarView.getLeftMenuButton().isPressed()) {
                    activityManager.stopLockTaskModeOnCurrent();
                    // When exiting refresh disabled flags.
                    mSlimNavigationBarView.setDisabledFlags(mDisabled1, true);
                    mSlimNavigationBarView.setOverrideMenuKeys(false);
                } else if ((v.getId() == mSlimNavigationBarView.getLeftMenuButton().getId())
                        && !mSlimNavigationBarView.getRightMenuButton().isPressed()) {
                    // If we aren't pressing recents right now then they presses
                    // won't be together, so send the standard long-press action.
                    sendBackLongPress = true;
                }
            } else {
                // If this is back still need to handle sending the long-press event.
                long time = System.currentTimeMillis();
                if (( time - mLastLockToAppLongPress) < 2000) {
                    if (v.getId() == mSlimNavigationBarView.getLeftMenuButton().getId()
                        || v.getId() == mSlimNavigationBarView.getRightMenuButton().getId()) {
                        sendBackLongPress = true;
                    }
                } else if (isAccessiblityEnabled && activityManager.isInLockTaskMode()) {
                    // When in accessibility mode a long press that is recents (not back)
                    // should stop lock task.
                    activityManager.stopLockTaskModeOnCurrent();
                    // When exiting refresh disabled flags.
                    mSlimNavigationBarView.setDisabledFlags(mDisabled1, true);
                    mSlimNavigationBarView.setOverrideMenuKeys(false);
                }
                mLastLockToAppLongPress = time;
            }
            return sendBackLongPress;
        } catch (RemoteException e) {
            Log.d(TAG, "Unable to reach activity manager", e);
            return false;
        }
    }

    private static void sendCloseSystemWindows(Context context, String reason) {
        if (ActivityManagerNative.isSystemReady()) {
            try {
                ActivityManagerNative.getDefault().closeSystemDialogs(reason);
            } catch (RemoteException e) {
            }
        }
    }

    @Override // CommandQueue
    public void showCustomIntentAfterKeyguard(Intent intent) {
        startActivityDismissingKeyguard(intent, false, false);
    }

    @Override
    public void toggleLastApp() {
        int msg = MSG_TOGGLE_LAST_APP;
        mHandler.removeMessages(msg);
        mHandler.sendEmptyMessage(msg);
    }

    @Override
    public void toggleKillApp() {
        int msg = MSG_TOGGLE_KILL_APP;
        mHandler.removeMessages(msg);
        mHandler.sendEmptyMessage(msg);
    }

    @Override
    public void toggleScreenshot() {
        int msg = MSG_TOGGLE_SCREENSHOT;
        mHandler.removeMessages(msg);
        mHandler.sendEmptyMessage(msg);
    }

    protected class H extends Handler {
        public void handleMessage(Message m) {
            switch (m.what) {
             case MSG_TOGGLE_LAST_APP:
                  Slog.d(TAG, "toggle last app");
                  getLastApp();
                  break;
             case MSG_TOGGLE_KILL_APP:
                  Slog.d(TAG, "toggle kill app");
                  mHandler.post(mKillTask);
                  break;
             case MSG_TOGGLE_SCREENSHOT:
                  Slog.d(TAG, "toggle screenshot");
                  takeScreenshot();
                  break;
            }
        }
    }

    Runnable mKillTask = new Runnable() {
        public void run() {
            try {
                final Intent intent = new Intent(Intent.ACTION_MAIN);
                String defaultHomePackage = "com.android.launcher";
                intent.addCategory(Intent.CATEGORY_HOME);
                final ResolveInfo res = mContext.getPackageManager().resolveActivity(intent, 0);
                if (res.activityInfo != null && !res.activityInfo.packageName.equals("android")) {
                    defaultHomePackage = res.activityInfo.packageName;
                }
                boolean targetKilled = false;
                IActivityManager am = ActivityManagerNative.getDefault();
                List<RunningAppProcessInfo> apps = am.getRunningAppProcesses();
                for (RunningAppProcessInfo appInfo : apps) {
                    int uid = appInfo.uid;
                    // Make sure it's a foreground user application (not system,
                    // root, phone, etc.)
                    if (uid >= Process.FIRST_APPLICATION_UID && uid <= Process.LAST_APPLICATION_UID
                            && appInfo.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        if (appInfo.pkgList != null && (appInfo.pkgList.length > 0)) {
                            for (String pkg : appInfo.pkgList) {
                                if (!pkg.equals("com.android.systemui")
                                        && !pkg.equals(defaultHomePackage)) {
                                    am.forceStopPackage(pkg, UserHandle.USER_CURRENT);
                                    targetKilled = true;
                                    break;
                                }
                            }
                        } else {
                            Process.killProcess(appInfo.pid);
                            targetKilled = true;
                        }
                    }
                    if (targetKilled) {
                        Toast.makeText(mContext,
                                R.string.app_killed_message, Toast.LENGTH_SHORT).show();
                        break;
                    }
                }
            } catch (RemoteException e) {}
        }
    };

    final Runnable mScreenshotTimeout = new Runnable() {
        @Override
        public void run() {
            synchronized (mScreenshotLock) {
                if (mScreenshotConnection != null) {
                    mContext.unbindService(mScreenshotConnection);
                    mScreenshotConnection = null;
                }
            }
        }
    };

    private final Object mScreenshotLock = new Object();
    private ServiceConnection mScreenshotConnection = null;
    private Handler mHDL = new Handler();

    private void takeScreenshot() {
        synchronized (mScreenshotLock) {
            if (mScreenshotConnection != null) {
                return;
            }
            ComponentName cn = new ComponentName("com.android.systemui",
                    "com.android.systemui.screenshot.TakeScreenshotService");
            Intent intent = new Intent();
            intent.setComponent(cn);
            ServiceConnection conn = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    synchronized (mScreenshotLock) {
                        if (mScreenshotConnection != this) {
                            return;
                        }
                        Messenger messenger = new Messenger(service);
                        Message msg = Message.obtain(null, 1);
                        final ServiceConnection myConn = this;
                        Handler h = new Handler(mHDL.getLooper()) {
                            @Override
                            public void handleMessage(Message msg) {
                                synchronized (mScreenshotLock) {
                                    if (mScreenshotConnection == myConn) {
                                        mContext.unbindService(mScreenshotConnection);
                                        mScreenshotConnection = null;
                                        mHDL.removeCallbacks(mScreenshotTimeout);
                                    }
                                }
                            }
                        };
                        msg.replyTo = new Messenger(h);
                        msg.arg1 = msg.arg2 = 0;

                        /*
                         * remove for the time being if (mStatusBar != null &&
                         * mStatusBar.isVisibleLw()) msg.arg1 = 1; if
                         * (mNavigationBar != null &&
                         * mNavigationBar.isVisibleLw()) msg.arg2 = 1;
                         */

                        /* wait for the dialog box to close */
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ie) {
                        }

                        /* take the screenshot */
                        try {
                            messenger.send(msg);
                        } catch (RemoteException e) {
                        }
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                }
            };
            if (mContext.bindService(intent, conn, mContext.BIND_AUTO_CREATE)) {
                mScreenshotConnection = conn;
                mHDL.postDelayed(mScreenshotTimeout, 10000);
            }
        }
    }

    private void getLastApp() {
        int lastAppId = 0;
        int looper = 1;
        String packageName;
        final Intent intent = new Intent(Intent.ACTION_MAIN);
        final ActivityManager am = (ActivityManager) mContext
                .getSystemService(Activity.ACTIVITY_SERVICE);
        String defaultHomePackage = "com.android.launcher";
        intent.addCategory(Intent.CATEGORY_HOME);
        final ResolveInfo res = mContext.getPackageManager().resolveActivity(intent, 0);
        if (res.activityInfo != null && !res.activityInfo.packageName.equals("android")) {
            defaultHomePackage = res.activityInfo.packageName;
        }
        List <ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(5);
        // lets get enough tasks to find something to switch to
        // Note, we'll only get as many as the system currently has - up to 5
        while ((lastAppId == 0) && (looper < tasks.size())) {
            packageName = tasks.get(looper).topActivity.getPackageName();
            if (!packageName.equals(defaultHomePackage)
                    && !packageName.equals("com.android.systemui")) {
                lastAppId = tasks.get(looper).id;
            }
            looper++;
        }
        if (lastAppId != 0) {
            am.moveTaskToFront(lastAppId, am.MOVE_TASK_NO_USER_ACTION);
        }
    }
}
