/**
 * Copyright (C) 2016 The DirtyUnicorns Project
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
 *
 */

package com.android.systemui.editor;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.UserHandle;
import android.provider.MediaStore;
import android.widget.Toast;

import com.android.systemui.R;

import java.io.IOException;

import org.slim.utils.ImageHelper;

/**
 * So we can capture image selection in DUSystemReceiver
 */
public class IconPickerGallery extends Activity {
    public static String TAG = IconPickerGallery.class.getSimpleName();

    private File mImageTmp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mImageTmp = new File(ImageHelper.getIconFolder() + File.separator + "shortcut.tmp");

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT, null);
        intent.setType("image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("scale", true);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.PNG.toString());
        intent.putExtra("aspectX", 100);
        intent.putExtra("aspectY", 100);
        intent.putExtra("outputX", 100);
        intent.putExtra("outputY", 100);
        try {
            mImageTmp.createNewFile();
            mImageTmp.setWritable(true, false);
            intent.putExtra(MediaStore.EXTRA_OUTPUT,
                    Uri.fromFile(mImageTmp));
            startActivityForResult(intent, 69);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == 69) {
            if (mImageTmp.length() == 0 || !mImageTmp.exists()) {
                //Toast.makeText(this, getResources().getString(
                  //      R.string.shortcut_image_not_valid), Toast.LENGTH_LONG).show();
                sendCancelResultAndFinish();
            } else if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                Uri image = ImageHelper.saveImageFile(mImageTmp);
                Intent resultIntent = new Intent(ImageHelper.ACTION_IMAGE_PICKED);
                resultIntent.putExtra("result", Activity.RESULT_OK);
                resultIntent.putExtra("uri", image.toString());
                android.util.Log.d("IconPickerGallery", "sending icon : " + image.toString());
                sendBroadcastAsUser(resultIntent, UserHandle.CURRENT);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                sendCancelResultAndFinish();
            }
        } else {
            sendCancelResultAndFinish();
        }
    }

    private void sendCancelResultAndFinish() {
        Intent intent = new Intent(ImageHelper.ACTION_IMAGE_PICKED);
        intent.putExtra("result", Activity.RESULT_CANCELED);
        sendBroadcastAsUser(intent, UserHandle.CURRENT);
        setResult(Activity.RESULT_CANCELED);
        finish();
    }
}
