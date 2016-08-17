package com.slim.packageinstaller;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import com.android.packageinstaller.PackageInstallerActivity;

public class HandleDefaultReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        setSlimPackageInstallerAsDefault(context);
    }

    private void setSlimPackageInstallerAsDefault(Context context) {
        IntentFilter filter = new IntentFilter(Intent.ACTION_VIEW);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filter.addDataType("application/vnd.android.package-archive");
        } catch (Exception e) {}
        ComponentName[] set = new ComponentName[1];
        set[0] = new ComponentName(context, PackageInstallerActivity.class);

        PackageManager pm = context.getPackageManager();
        pm.addPreferredActivity(filter, 0, set, set[0]);
    }
}
