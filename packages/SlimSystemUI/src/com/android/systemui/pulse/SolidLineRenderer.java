/**
 * Copyright (C) 2016 The DirtyUnicorns Project
 * Copyright (C) 2015 The CyanogenMod Project
 * 
 * @author: Randall Rushing <randall.rushing@gmail.com>
 *
 * Contributions from The CyanogenMod Project
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

package com.android.systemui.pulse;

import android.animation.ValueAnimator;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings;

import com.android.systemui.pulse.PulseController.PulseObserver;
import com.android.systemui.utils.ColorAnimator;

import org.slim.provider.SlimSettings;

public class SolidLineRenderer extends Renderer implements ColorAnimator.ColorAnimationListener {
    private Paint mPaint;
    private ValueAnimator[] mValueAnimators;
    private float[] mFFTPoints;
    private int mColor;

    private byte rfk, ifk;
    private int dbValue;
    private float magnitude;
    private float mDbFuzzFactor;
    private boolean mVertical;
    private boolean mLeftInLandscape;
    private int mWidth, mHeight;

    private boolean mIsValidStream;
    private boolean mLavaLampEnabled;
    private CMRendererObserver mObserver;
    private ColorAnimator mLavaLamp;

    public SolidLineRenderer(Context context, Handler handler, PulseObserver callback) {
        super(context, handler, callback);
        mColor = Color.TRANSPARENT;
        mLavaLamp = new ColorAnimator();
        mLavaLamp.setColorAnimatorListener(this);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(mColor);
        mDbFuzzFactor = 5f;
        mFFTPoints = new float[128];
        loadValueAnimators();
        mObserver = new CMRendererObserver(handler);
        mObserver.updateSettings();
    }

    @Override
    public void setLeftInLandscape(boolean leftInLandscape) {
        if (mLeftInLandscape != leftInLandscape) {
            mLeftInLandscape = leftInLandscape;
            onSizeChanged(0, 0, 0, 0);
        }
    }

    private void loadValueAnimators() {
        if (mValueAnimators != null) {
            for (int i = 0; i < 32; i++) {
                mValueAnimators[i].cancel();
            }
        }
        mValueAnimators = new ValueAnimator[32];
        final boolean isVertical = mVertical;
        for (int i = 0; i < 32; i++) {
            final int j;
            if (isVertical) {
                j = i * 4;
            } else {
                j = i * 4 + 1;
            }
            mValueAnimators[i] = new ValueAnimator();
            mValueAnimators[i].setDuration(128);
            mValueAnimators[i].addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    mFFTPoints[j] = (float) animation.getAnimatedValue();
                    postInvalidate();
                }
            });
        }
    }

    private void setPortraitPoints() {
        float barUnit = mWidth / 32f;
        float barWidth = barUnit * 8f / 9f;
        barUnit = barWidth + (barUnit - barWidth) * 32f / 31f;
        mPaint.setStrokeWidth(barWidth);
        for (int i = 0; i < 32; i++) {
            mFFTPoints[i * 4] = mFFTPoints[i * 4 + 2] = i * barUnit + (barWidth / 2);
            mFFTPoints[i * 4 + 1] = mHeight;
            mFFTPoints[i * 4 + 3] = mHeight;
        }
    }

    private void setVerticalPoints() {
        float barUnit = mHeight / 32f;
        float barHeight = barUnit * 8f / 9f;
        barUnit = barHeight + (barUnit - barHeight) * 32f / 31f;
        mPaint.setStrokeWidth(barHeight);
        for (int i = 0; i < 32; i++) {
            mFFTPoints[i * 4 + 1] = mFFTPoints[i * 4 + 3] = i * barUnit + (barHeight / 2);
            mFFTPoints[i * 4] = mLeftInLandscape ? 0 : mWidth;
            mFFTPoints[i * 4 + 2] = mLeftInLandscape ? 0 : mWidth;
        }
    }

    @Override
    public void onSizeChanged(int w, int h, int oldw, int oldh) {
        if (mCallback.getWidth() > 0 && mCallback.getHeight() > 0) {
            mWidth = mCallback.getWidth();
            mHeight = mCallback.getHeight();
            mVertical = mHeight > mWidth;
            loadValueAnimators();
            if (mVertical) {
                setVerticalPoints();
            } else {
                setPortraitPoints();
            }
        }
    }

    @Override
    public void onStreamAnalyzed(boolean isValid) {
        mIsValidStream = isValid;
        if (isValid) {
            onSizeChanged(0, 0, 0, 0);
            if (mLavaLampEnabled) {
                mLavaLamp.start();
            }
        }
    }

    @Override
    public void onFFTUpdate(byte[] fft) {
        for (int i = 0; i < 32; i++) {
            mValueAnimators[i].cancel();
            rfk = fft[i * 2 + 2];
            ifk = fft[i * 2 + 3];
            magnitude = rfk * rfk + ifk * ifk;
            dbValue = magnitude > 0 ? (int) (10 * Math.log10(magnitude)) : 0;
            if (mVertical) {
                if (mLeftInLandscape) {
                    mValueAnimators[i].setFloatValues(mFFTPoints[i * 4],
                            dbValue * mDbFuzzFactor);
                } else {
                    mValueAnimators[i].setFloatValues(mFFTPoints[i * 4],
                            mFFTPoints[2] - (dbValue * mDbFuzzFactor));
                }
            } else {
                mValueAnimators[i].setFloatValues(mFFTPoints[i * 4 + 1],
                        mFFTPoints[3] - (dbValue * mDbFuzzFactor));
            }
            mValueAnimators[i].start();
        }
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawLines(mFFTPoints, mPaint);
    }

    @Override
    public void destroy() {
        mContext.getContentResolver().unregisterContentObserver(mObserver);
        mLavaLamp.stop();
    }

    @Override
    public void onVisualizerLinkChanged(boolean linked) {
        if (!linked) {
            mLavaLamp.stop();
        }
    }

    @Override
    public void onColorChanged(ColorAnimator colorAnimator, int color) {
        mPaint.setColor(color);
    }

    @Override
    public void onStartAnimation(ColorAnimator colorAnimator, int firstColor) {
    }

    @Override
    public void onStopAnimation(ColorAnimator colorAnimator, int lastColor) {
        mPaint.setColor(mColor);
    }

    private class CMRendererObserver extends ContentObserver {
        public CMRendererObserver(Handler handler) {
            super(handler);
            register();
        }

        void register() {
            ContentResolver resolver = mContext.getContentResolver();
            resolver.registerContentObserver(
                    SlimSettings.Secure.getUriFor(SlimSettings.Secure.FLING_PULSE_COLOR), false, this,
                    UserHandle.USER_ALL);
            resolver.registerContentObserver(
                    SlimSettings.Secure.getUriFor(SlimSettings.Secure.FLING_PULSE_LAVALAMP_ENABLED), false,
                    this,
                    UserHandle.USER_ALL);
            resolver.registerContentObserver(
                    SlimSettings.Secure.getUriFor(SlimSettings.Secure.PULSE_SOLID_FUDGE_FACTOR), false, this,
                    UserHandle.USER_ALL);
            resolver.registerContentObserver(
                    SlimSettings.Secure.getUriFor(SlimSettings.Secure.PULSE_LAVALAMP_SOLID_SPEED), false, this,
                    UserHandle.USER_ALL);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            updateSettings();
        }

        public void updateSettings() {
            ContentResolver resolver = mContext.getContentResolver();
            mLavaLampEnabled = SlimSettings.Secure.getIntForUser(resolver,
                    SlimSettings.Secure.FLING_PULSE_LAVALAMP_ENABLED, 1, UserHandle.USER_CURRENT) == 1;
            mColor = SlimSettings.Secure.getIntForUser(resolver,
                    SlimSettings.Secure.FLING_PULSE_COLOR,
                    Color.WHITE,
                    UserHandle.USER_CURRENT);
            if (!mLavaLampEnabled) {
                mPaint.setColor(mColor);
            }
            int lavaLampSpeed = SlimSettings.Secure.getIntForUser(resolver,
                    SlimSettings.Secure.PULSE_LAVALAMP_SOLID_SPEED, 10 * 1000,
                    UserHandle.USER_CURRENT);
            mLavaLamp.setAnimationTime(lavaLampSpeed);
            if (mLavaLampEnabled && mIsValidStream) {
                mLavaLamp.start();
            } else {
                mLavaLamp.stop();
            }
            // putFloat, getFloat is better. catch it next time
            mDbFuzzFactor = Float.valueOf(SlimSettings.Secure.getIntForUser(
                    resolver, SlimSettings.Secure.PULSE_SOLID_FUDGE_FACTOR, 4,
                    UserHandle.USER_CURRENT)) + 1f;
        }
    }
}
