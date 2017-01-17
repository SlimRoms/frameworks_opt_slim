/*
 * Copyright (C) 2011 Sergey Margaritov
 * Copyright (C) 2013-2017 SlimRoms Project
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

package slim.preference.colorpicker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.slim.framework.internal.R;

import slim.preference.SlimPreferenceManager;
import slim.utils.AttributeHelper;

/**
 * A preference type that allows a user to choose a time
 *
 * @author Sergey Margaritov
 */
public class ColorPickerPreference extends Preference implements
        Preference.OnPreferenceClickListener, ColorPickerDialog.OnColorChangedListener {

    View mView;
    ColorPickerDialog mDialog;
    LinearLayout widgetFrameView;
    private int mValue = Color.BLACK;
    private float mDensity = 0;
    private boolean mAlphaSliderEnabled = false;
    private int mDefaultColor = Color.WHITE;
    private int mSettingType;

    private EditText mEditText;

    private SlimPreferenceManager mSlimPreferenceManager = SlimPreferenceManager.get();
    private String mListDependency;
    private String[] mListDependencyValues;

    public ColorPickerPreference(Context context) {
        super(context);
        init(context, null);
    }

    public ColorPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ColorPickerPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, Color.BLACK);
    }

    public int getDefaultColor() {
        return mDefaultColor;
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        onColorChanged(restoreValue ? getPersistedInt(mValue) : (Integer) defaultValue);
    }

    private void init(Context context, AttributeSet attrs) {
        mDensity = getContext().getResources().getDisplayMetrics().density;
        setOnPreferenceClickListener(this);
        if (attrs != null) {
            AttributeHelper a =
                    new AttributeHelper(context, attrs, slim.R.styleable.ColorPickerPreference);

            mDefaultColor = a.getInt(slim.R.styleable.ColorPickerPreference_defaultColor,
                    Color.WHITE);
            mAlphaSliderEnabled = a.getBoolean(
                    slim.R.styleable.ColorPickerPreference_alphaSliderEnabled, false);

            a = new AttributeHelper(context, attrs, slim.R.styleable.SlimPreference);

            mSettingType = SlimPreferenceManager.getSettingType(a);

            String list = a.getString(slim.R.styleable.SlimPreference_listDependency);
            if (!TextUtils.isEmpty(list)) {
                String[] listParts = list.split(":");
                if (listParts.length == 2) {
                    mListDependency = listParts[0];
                    mListDependencyValues = listParts[1].split("\\|");
                }
           }

           boolean hidePreference =
                    a.getBoolean(slim.R.styleable.SlimPreference_hidePreference, false);
            int hidePreferenceInt = a.getInt(slim.R.styleable.SlimPreference_hidePreferenceInt, -1);
            int intDep = a.getInt(slim.R.styleable.SlimPreference_hidePreferenceIntDependency, 0);
            if (hidePreference || hidePreferenceInt == intDep) {
                setVisible(false);
            }
        }
    }

    @Override
    public void onAttached() {
        super.onAttached();
        if (mListDependency != null) {
            mSlimPreferenceManager.registerListDependent(
                    this, mListDependency, mListDependencyValues);
        }
    }

    @Override
    public void onDetached() {
        super.onDetached();
        if (mListDependency != null) {
            mSlimPreferenceManager.unregisterListDependent(this, mListDependency);
        }
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        mView = holder.itemView;
        super.onBindViewHolder(holder);

        widgetFrameView = (LinearLayout) holder.findViewById(android.R.id.widget_frame);

        setPreviewColor();
    }

    private void setPreviewColor() {
        if (mView == null)
            return;

        ImageView iView = new ImageView(getContext());
        LinearLayout widgetFrameView = ((LinearLayout) mView
                .findViewById(android.R.id.widget_frame));
        if (widgetFrameView == null)
            return;

        widgetFrameView.setVisibility(View.VISIBLE);
        widgetFrameView.setPadding(
                widgetFrameView.getPaddingLeft(),
                widgetFrameView.getPaddingTop(),
                (int) (mDensity * 8),
                widgetFrameView.getPaddingBottom()
                );
        // remove already create preview image
        int count = widgetFrameView.getChildCount();
        if (count > 0) {
            widgetFrameView.removeViews(0, count);
        }
        widgetFrameView.addView(iView);
        widgetFrameView.setMinimumWidth(0);
        iView.setBackgroundDrawable(new AlphaPatternDrawable((int) (5 * mDensity)));
        iView.setImageBitmap(getPreviewBitmap());
    }

    private Bitmap getPreviewBitmap() {
        int d = (int) (mDensity * 31); // 30dip
        int color = mValue;
        Bitmap bm = Bitmap.createBitmap(d, d, Config.ARGB_8888);
        int w = bm.getWidth();
        int h = bm.getHeight();
        int c = color;
        for (int i = 0; i < w; i++) {
            for (int j = i; j < h; j++) {
                c = (i <= 1 || j <= 1 || i >= w - 2 || j >= h - 2) ? Color.GRAY : color;
                bm.setPixel(i, j, c);
                if (i != j) {
                    bm.setPixel(j, i, c);
                }
            }
        }

        return bm;
    }

    @Override
    public void onColorChanged(int color) {
        if (isPersistent()) {
            persistInt(color);
        }
        mValue = color;
        setPreviewColor();
        try {
            getOnPreferenceChangeListener().onPreferenceChange(this, color);
        } catch (NullPointerException e) {
        }
        try {
            mEditText.setText(Integer.toString(color, 16));
        } catch (NullPointerException e) {
        }
        handleSetSummary();
    }

    public void handleSetSummary() {
        if (mValue == mDefaultColor || mValue == -2) {
            setSummary(getContext().getResources()
                    .getString(org.slim.framework.internal.R.string.default_string));
        } else {
            setSummary(String.format("#%08x", (0xffffffff & mValue)));
        }
    }

    public boolean onPreferenceClick(Preference preference) {
        showDialog(null);
        return false;
    }

    protected void showDialog(Bundle state) {
        mDialog = new ColorPickerDialog(getContext(), mValue);
        mDialog.setOnColorChangedListener(this);
        if (mAlphaSliderEnabled) {
            mDialog.setAlphaSliderVisible(true);
        }
        if (state != null) {
            mDialog.onRestoreInstanceState(state);
        }
        mDialog.show();
    }


    /**
     * Toggle Alpha Slider visibility (by default it's disabled)
     *
     * @param enable
     */
    public void setAlphaSliderEnabled(boolean enable) {
        mAlphaSliderEnabled = enable;
    }

    /**
     * For custom purposes. Not used by ColorPickerPreferrence
     *
     * set color preview value from outside
     * @author kufikugel
     */
    public void setNewPreviewColor(int color) {
        onColorChanged(color);
    }

    /**
     * For custom purposes. Not used by ColorPickerPreferrence
     *
     * @param color
     * @author Unknown
     */
    public static String convertToARGB(int color) {
        String alpha = Integer.toHexString(Color.alpha(color));
        String red = Integer.toHexString(Color.red(color));
        String green = Integer.toHexString(Color.green(color));
        String blue = Integer.toHexString(Color.blue(color));

        if (alpha.length() == 1) {
            alpha = "0" + alpha;
        }

        if (red.length() == 1) {
            red = "0" + red;
        }

        if (green.length() == 1) {
            green = "0" + green;
        }

        if (blue.length() == 1) {
            blue = "0" + blue;
        }

        return "#" + alpha + red + green + blue;
    }

    /**
     * For custom purposes. Not used by ColorPickerPreferrence
     *
     * @param argb
     * @throws NumberFormatException
     * @author Unknown
     */
    public static int convertToColorInt(String argb) throws NumberFormatException {

        if (argb.startsWith("#")) {
            argb = argb.replace("#", "");
        }

        int alpha = -1, red = -1, green = -1, blue = -1;

        if (argb.length() == 8) {
            alpha = Integer.parseInt(argb.substring(0, 2), 16);
            red = Integer.parseInt(argb.substring(2, 4), 16);
            green = Integer.parseInt(argb.substring(4, 6), 16);
            blue = Integer.parseInt(argb.substring(6, 8), 16);
        }
        else if (argb.length() == 6) {
            alpha = 255;
            red = Integer.parseInt(argb.substring(0, 2), 16);
            green = Integer.parseInt(argb.substring(2, 4), 16);
            blue = Integer.parseInt(argb.substring(4, 6), 16);
        }

        return Color.argb(alpha, red, green, blue);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (mDialog == null || !mDialog.isShowing()) {
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.dialogBundle = mDialog.onSaveInstanceState();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !(state instanceof SavedState)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        showDialog(myState.dialogBundle);
    }

    private static class SavedState extends BaseSavedState {
        Bundle dialogBundle;

        public SavedState(Parcel source) {
            super(source);
            dialogBundle = source.readBundle();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeBundle(dialogBundle);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

    @Override
    protected boolean persistInt(int value) {
        if (shouldPersist()) {
            if (value == getPersistedInt(Integer.MIN_VALUE)) {
                return true;
            }
            SlimPreferenceManager.putIntInSlimSettings(getContext(),
                    mSettingType, getKey(), value);
            return true;
        }
        return false;
    }

    @Override
    protected int getPersistedInt(int defaultReturnValue) {
        if (!shouldPersist()) {
            return defaultReturnValue;
        }
        return SlimPreferenceManager.getIntFromSlimSettings(getContext(), mSettingType,
                getKey(), defaultReturnValue);
    }

    @Override
    protected boolean isPersisted() {
        return SlimPreferenceManager.settingExists(getContext(), mSettingType, getKey());
    }
}
