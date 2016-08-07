package com.android.systemui.utils;

import android.view.View;
import android.view.ViewGroup;

public final class ViewUtils {

    public static View replaceView(View view, View newView, boolean moveChildren) {
        ViewGroup vg = (ViewGroup) view.getParent();
        if (vg == null) return view;

        moveChildren = 
                moveChildren && (view instanceof ViewGroup) && (newView instanceof ViewGroup);

        if (moveChildren) {
            ViewGroup oldV = (ViewGroup) view;
            ViewGroup newV = (ViewGroup) newView;
            for (int i = 0; i < oldV.getChildCount(); i++) {
                View v = oldV.getChildAt(i);
                if (v != null) {
                    oldV.removeView(v);
                    newV.addView(v);
                }
            }
        }

        int index = vg.indexOfChild(view);
        newView.setLayoutParams(view.getLayoutParams());
        vg.removeView(view);
        vg.addView(newView, index);

        return newView;
    }
}
