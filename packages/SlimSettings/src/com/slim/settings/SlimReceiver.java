package com.slim.settings;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.settings.slim.HardwareKeysSettings;

public class SlimReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context ctx, Intent intent) {
        HardwareKeysSettings.restore(ctx);
    }
}
