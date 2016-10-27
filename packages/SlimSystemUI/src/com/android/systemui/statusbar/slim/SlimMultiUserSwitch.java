/*
* Copyright (C) 2016 SlimRoms Project
* Copyright (C) 2013-14 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.android.systemui.statusbar.slim;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.android.systemui.R;
import com.android.systemui.statusbar.phone.MultiUserSwitch;

public class SlimMultiUserSwitch extends MultiUserSwitch implements View.OnLongClickListener {

    Intent notificationStationIntent, collapsePanelIntent;

    public SlimMultiUserSwitch(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setOnLongClickListener(this);
        notificationStationIntent = new Intent(Intent.ACTION_MAIN);
        collapsePanelIntent = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
    }

    @Override
    public boolean onLongClick(View v) {
        Context context = getContext();
        notificationStationIntent.setClassName("com.android.settings",
                "com.android.settings.Settings$NotificationStationActivity");
        notificationStationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivityAsUser(notificationStationIntent, new UserHandle(UserHandle.USER_CURRENT));
        context.sendBroadcast(collapsePanelIntent);
        return true;
    }

}
