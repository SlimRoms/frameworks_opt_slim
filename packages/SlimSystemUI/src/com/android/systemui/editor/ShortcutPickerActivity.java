package com.android.systemui.editor;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.UserHandle;

import org.slim.content.SlimIntent;
import org.slim.utils.ShortcutPickerHelper;
import org.slim.utils.ShortcutPickerHelper.OnPickListener;

public class ShortcutPickerActivity extends Activity implements OnPickListener {

    private ShortcutPickerHelper mPicker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPicker = new ShortcutPickerHelper(this, this);
        mPicker.pickShortcut(0);
    }

    @Override
    public void shortcutPicked(String action, String description, Bitmap b, boolean isApplication) {
        sendShortcut(action);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == ShortcutPickerHelper.REQUEST_PICK_SHORTCUT
                    || requestCode == ShortcutPickerHelper.REQUEST_PICK_APPLICATION
                    || requestCode == ShortcutPickerHelper.REQUEST_CREATE_SHORTCUT) {
                mPicker.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private void sendShortcut(String action) {
        Intent intent = new Intent(SlimIntent.ACTION_SHORTCUT_PICKED);
        intent.putExtra("result", Activity.RESULT_OK);
        intent.putExtra("shortcut", action);
        sendBroadcastAsUser(intent, UserHandle.CURRENT);
        finish();
    }
}
