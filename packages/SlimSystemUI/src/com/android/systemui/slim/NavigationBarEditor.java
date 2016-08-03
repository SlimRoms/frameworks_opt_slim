package com.android.systemui.statusbar.slim;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DisplayInfo;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.internal.util.ArrayUtils;
import com.android.systemui.editor.ActionItem;
import com.android.systemui.editor.IconPackActivity;
import com.android.systemui.editor.IconPickerGallery;
import com.android.systemui.editor.QuickAction;
import org.slim.action.ActionConfig;
import org.slim.action.ActionConstants;
import org.slim.action.ActionHelper;
import org.slim.provider.SlimSettings;
import org.slim.utils.DeviceUtils;
import org.slim.utils.DeviceUtils.FilteredDeviceFeaturesArray;
import org.slim.utils.ImageHelper;

import com.android.systemui.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class NavigationBarEditor implements View.OnTouchListener {

    public static final String NAVBAR_EDIT_ACTION = "android.intent.action.NAVBAR_EDIT";

    private static final String SETTINGS_METADATA_NAME = "com.android.settings";

    private static final int MENU_SINGLE_TAP = 1;
    private static final int MENU_DOUBLE_TAP = 2;
    private static final int MENU_LONG_PRESS = 3;
    private static final int MENU_PICK_ICON  = 4;

    private static final int MENU_VISIBILITY_ALWAYS  = 5;
    private static final int MENU_VISIBILITY_REQUEST = 6;
    private static final int MENU_VISIBILITY_NEVER   = 7;

    private static final int IME_VISIBILITY_NEVER = 8;
    private static final int IME_VISIBILITY_REQUEST = 9;

    private static final int ADD_BUTTON_ID = View.generateViewId();
    private static final int DELETE_BUTTON_ID = View.generateViewId();

    private Context mContext;
    private WindowManager mWindowManager;
    private SlimNavigationBarView mNavBar;

    private FilteredDeviceFeaturesArray mActionsArray;
    private ArrayList<SlimKeyButtonView> mButtons = new ArrayList<>();

    private SlimKeyButtonView mAddButton;
    private SlimKeyButtonView mDeleteButton;
    private SlimKeyButtonView mButtonToEdit;

    private boolean mEditing;
    private boolean mDeleting = false;
    private boolean mLongPressed;

    private QuickAction mPopup;
    private ArrayList<ActionItem> mButtonItems = new ArrayList<>();
    private ArrayList<ActionItem> mMenuButtonItems = new ArrayList<>();
    private ArrayList<ActionItem> mImeButtonItems = new ArrayList<>();

    private QuickAction.OnActionItemClickListener mQuickClickListener =
            new QuickAction.OnActionItemClickListener() {
        SlimKeyButtonView mButton;
        @Override
        public void onItemClick(QuickAction action, int pos, int actionId) {
            if (mButton == mButtonToEdit) return;
            mButton = mButtonToEdit;
            switch (actionId) {
                case MENU_SINGLE_TAP:
                    editSingleTap(mButtonToEdit);
                    break;
                case MENU_DOUBLE_TAP:
                    editDoubleTap(mButtonToEdit);
                    break;
                case MENU_LONG_PRESS:
                    editLongpress(mButtonToEdit);
                    break;
                case MENU_PICK_ICON:
                    selectIcon();
                    break;
                case MENU_VISIBILITY_ALWAYS:
                case MENU_VISIBILITY_REQUEST:
                case MENU_VISIBILITY_NEVER:
                    updateMenuButtonVisibility(actionId);
                    break;
                case IME_VISIBILITY_REQUEST:
                case IME_VISIBILITY_NEVER:
                    updateImeButtonVisibility(actionId);
                    break;
            }
        }
    };

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ImageHelper.ACTION_IMAGE_PICKED.equals(action)) {
                int result = intent.getIntExtra("result", Activity.RESULT_CANCELED);
                if (result == Activity.RESULT_OK) {
                    String uri = intent.getStringExtra("uri");
                    imagePicked(uri);
                }
            }
        }
    };

    private FrameLayout mEditContainer;
    private View mHidden;

    private View.OnTouchListener mPopupTouchWrapper = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
                mPopup.dismiss();
                return true;
            }
            return false;
        }
    };

    public static final int[] SMALL_BUTTON_IDS = { R.id.menu, R.id.menu_left, R.id.ime_switcher,
        ADD_BUTTON_ID, DELETE_BUTTON_ID };

     // start point of the current drag operation
    private float mDragOrigin;

    // just to avoid reallocations
    private static final int[] sLocation = new int[2];

    // Button chooser dialog
    private AlertDialog mDialog;

    private AlertDialog mActionDialog;

    /**
     * Longpress runnable to assign buttons in edit mode
     */
    private Runnable mCheckLongPress = new Runnable() {
        public void run() {
            if (mEditing) {
                mLongPressed = true;
                mNavBar.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                setDeleting(true);
            }
        }
    };

    public NavigationBarEditor(SlimNavigationBarView navBar) {
        mContext = navBar.getContext();
        mWindowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        mNavBar = navBar;
        createAddButton();
        createDeleteButton();
        initActionsArray();

        IntentFilter filter = new IntentFilter(ImageHelper.ACTION_IMAGE_PICKED);
        mContext.registerReceiver(mReceiver, filter);
    }

    private void createAddButton() {
        mAddButton = mNavBar.generateKey(false, "", "", "", "");
        mAddButton.setId(ADD_BUTTON_ID);
        Drawable d = mContext.getResources().getDrawable(R.drawable.ic_action_add);
        mAddButton.setImageDrawable(d);
        mAddButton.setOnTouchListener(this);
    }

    private void createDeleteButton() {
        mDeleteButton = mNavBar.generateKey(false, "", "", "", "");
        Drawable d = mContext.getResources().getDrawable(R.drawable.ic_action_delete);
        mDeleteButton.setImageDrawable(d);
    }

    private void initActionsArray() {
        PackageManager pm = mContext.getPackageManager();
        Resources res = mContext.getResources();
        mActionsArray = new FilteredDeviceFeaturesArray();
        mActionsArray = DeviceUtils.filterUnsupportedDeviceFeatures(mContext,
            res.getStringArray(res.getIdentifier(
                    "shortcut_action_values", "array", "org.slim.framework")),
            res.getStringArray(res.getIdentifier(
                    "shortcut_action_entries", "array", "org.slim.framework")));
    }

    private interface DialogClickListener {
        void onClick(int which);
    }

    private void showActionDialog(final DialogClickListener clickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                .setTitle(mContext.getString(R.string.navbar_dialog_title))
                .setItems(mActionsArray.entries, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        clickListener.onClick(which);
                        dialog.cancel();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null);
        mDialog = builder.create();
        mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
    }

    private void editSingleTap(final SlimKeyButtonView v) {
        showActionDialog(new DialogClickListener() {
            @Override
            public void onClick(int which) {
                String action = mActionsArray.values[which];
                String description = mActionsArray.entries[which];
                ActionConfig config = v.getConfig();
                config.setClickAction(action);
                config.setClickActionDescription(description);
                updateKey(v, config);
            }
        });
    }

    private void editDoubleTap(final SlimKeyButtonView v) {
        showActionDialog(new DialogClickListener() {
            @Override
            public void onClick(int which) {
                String action = mActionsArray.values[which];
                String description = mActionsArray.entries[which];
                ActionConfig config = v.getConfig();
                config.setDoubleTapAction(action);
                config.setDoubleTapActionDescription(description);
                v.setConfig(config);
            }
        });
    }

    private void editLongpress(final SlimKeyButtonView v) {
        showActionDialog(new DialogClickListener() {
            @Override
            public void onClick(int which) {
                String action = mActionsArray.values[which];
                String description = mActionsArray.entries[which];
                ActionConfig config = v.getConfig();
                config.setLongpressAction(action);
                config.setLongpressActionDescription(description);
                v.setConfig(config);
            }
        });
    }

    private void selectIcon() {
        String[] items = { "Default", "Gallery", "Icon Pack" };

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
            .setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            resetIcon();
                            break;
                        case 1:
                            selectIconFromGallery();
                            break;
                        case 2:
                            selectIconFromIconPack();
                            break;
                    }
                    dialog.cancel();
                }
            })
            .setNegativeButton(android.R.string.cancel, null);
        mDialog = builder.create();
        mDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_DIALOG);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
    }

    private void resetIcon() {
        ActionConfig config = mButtonToEdit.getConfig();
        config.setIcon(ActionConstants.ICON_EMPTY);
        updateKey(mButtonToEdit, config);
        mButtonToEdit = null;
    }

    private void selectIconFromGallery() {
        Intent intent = new Intent(mContext, IconPickerGallery.class);
        //intent.setAction(Intent.ACTION_MAIN);
        mContext.startActivityAsUser(intent, UserHandle.CURRENT);
    }

    private void selectIconFromIconPack() {
        Intent intent = new Intent(mContext, IconPackActivity.class);
        //intent.setAction(Intent.ACTION_MAIN);
        mContext.startActivityAsUser(intent, UserHandle.CURRENT);
    }

    private void imagePicked(String uri) {
        ActionConfig config = mButtonToEdit.getConfig();
        config.setIcon(uri);
        updateKey(mButtonToEdit, config);
        mButtonToEdit = null;
    }

    private void updateKey(SlimKeyButtonView button, ActionConfig config) {
        ActionConfig oldConfig = button.getConfig();
        button.setConfig(config);
        mNavBar.setButtonIcon(button, false, config.getClickAction(), config.getIcon());
    }

    private void updateButton(SlimKeyButtonView v) {
        v.setEditing(mEditing);
        v.setOnTouchListener(mEditing ? this : null);
        v.setOnClickListener(null);
        v.setOnLongClickListener(null);
        v.setVisibility(View.VISIBLE);
        v.updateFromConfig();
    }

    private void updateMenuButtonVisibility(int menuId) {
        String key;
        int vis = 0;
        if (mButtonToEdit.getId() == R.id.menu) {
            key = SlimSettings.System.MENU_VISIBILITY_RIGHT;
        } else {
            key = SlimSettings.System.MENU_VISIBILITY_LEFT;
        }
        switch (menuId) {
            case MENU_VISIBILITY_ALWAYS:
                vis = SlimNavigationBarView.MENU_VISIBILITY_ALWAYS;
                break;
            case MENU_VISIBILITY_REQUEST:
                vis = SlimNavigationBarView.MENU_VISIBILITY_SYSTEM;
                break;
            case MENU_VISIBILITY_NEVER:
                vis = SlimNavigationBarView.MENU_VISIBILITY_NEVER;
                break;
        }
        SlimSettings.System.putIntForUser(mContext.getContentResolver(),
                key, vis, UserHandle.USER_CURRENT);
    }

    private void updateImeButtonVisibility(int menuId) {
        int vis;
        if (menuId == IME_VISIBILITY_NEVER) {
            vis = 1;
        } else {
            vis = 2;
        }
        SlimSettings.System.putIntForUser(mContext.getContentResolver(),
                SlimSettings.System.IME_BUTTON_VISIBILITY, vis, UserHandle.USER_CURRENT);
    }

    private void setDeleting(boolean d) {
        if (mDeleting == d) return;
        mDeleting = d;
        if (d) {
            mNavBar.removeButton(mAddButton);
            mNavBar.addButton(mDeleteButton);
        } else {
            mNavBar.removeButton(mDeleteButton);
            mNavBar.addButton(mAddButton);
        }
    }

    public void setEditing(boolean editing) {
        mEditing = editing;
        if (mEditing) {
            mButtons.addAll(mNavBar.getButtons());
            createPopupContainer();
            mNavBar.addButton(mAddButton);
        } else {
            removePopupContainer();
            mNavBar.removeButton(mAddButton);
            save();
            mButtons.clear();
        }
        for (SlimKeyButtonView key : mButtons) {
            updateButton(key);
        }
    }

    @Override
    public boolean onTouch(final View view, MotionEvent event) {
        if (!mEditing || (mDialog != null && mDialog.isShowing())) {
            return false;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (mPopup != null) {
                mPopup.dismiss();
            }
            view.setPressed(true);
            view.getLocationOnScreen(sLocation);
            mDragOrigin = sLocation[mNavBar.isVertical() ? 1 : 0];
            prepareToShowPopup(view);
            view.postDelayed(mCheckLongPress, ViewConfiguration.getLongPressTimeout());
        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            view.setPressed(false);

            if (!mLongPressed || ArrayUtils.contains(SMALL_BUTTON_IDS, view.getId())
                    || view == mAddButton) {
                return false;
            }

            ViewGroup viewParent = (ViewGroup) view.getParent();
            float pos = mNavBar.isVertical() ? event.getRawY() : event.getRawX();
            float buttonSize = mNavBar.isVertical() ? view.getHeight() : view.getWidth();
            float min = mNavBar.isVertical() ? viewParent.getTop() : (viewParent.getLeft() - buttonSize / 2);
            float max = mNavBar.isVertical() ? (viewParent.getTop() + viewParent.getHeight())
                    : (viewParent.getLeft() + viewParent.getWidth());

            // Prevents user from dragging view outside of bounds
            if (pos < min || pos > max) {
                //return false;
            }
            if (true) {
                view.setX(pos - viewParent.getLeft() - buttonSize / 2);
            } else {
                view.setY(pos - viewParent.getTop() - buttonSize / 2);
            }
            View affectedView = findInterceptingView(pos, view);
            if (affectedView == null) {
                return false;
            } else if (affectedView == mDeleteButton) {
                return true;
            }
            if (mPopup != null) {
                mPopup.dismiss();
            }
            moveButton(affectedView, view);
        } else if (event.getAction() == MotionEvent.ACTION_UP
                || event.getAction() == MotionEvent.ACTION_CANCEL) {
            view.setPressed(false);
            view.removeCallbacks(mCheckLongPress);

            float pos = mNavBar.isVertical() ? event.getRawY() : event.getRawX();
            View affectedView = findInterceptingView(pos, view);
            setDeleting(false);

            Log.d("TEST", "ACTION_UP");

            if (!mLongPressed) {
                Log.d("TEST", "!mLongPressed");
                if (view == mAddButton) {
                    Log.d("TEST","mAddButton");
                    showActionDialog(new DialogClickListener() {
                        @Override
                        public void onClick(int which) {
                            String action = mActionsArray.values[which];
                            String description = mActionsArray.entries[which];
                            addButton(action, description);
                        }
                    });
                } else {
                    editAction((SlimKeyButtonView) view);
                }
            } else {
                if (affectedView == mDeleteButton) {
                    deleteButton(view);
                } else {
                    // Reset the dragged view to its original location
                    ViewGroup parent = (ViewGroup) view.getParent();

                    if (!mNavBar.isVertical()) {
                        view.setX(mDragOrigin - parent.getLeft());
                    } else {
                        view.setY(mDragOrigin - parent.getTop());
                    }
                }
            }
            if (mPopup == null || !mPopup.mWindow.isShowing()) {
                mEditContainer.setVisibility(View.GONE);
            }
            mLongPressed = false;
        }
        return true;
    }

    private void moveButton(View targetView, View view) {
        ViewGroup parent = (ViewGroup) view.getParent();

        targetView.getLocationOnScreen(sLocation);
        if (true) {
            targetView.setX(mDragOrigin - parent.getLeft());
            mDragOrigin = sLocation[0];
        } else {
            targetView.setY(mDragOrigin - parent.getTop());
            mDragOrigin = sLocation[1];
        }
        Collections.swap(mButtons, mButtons.indexOf(view), mButtons.indexOf(targetView));
    }

    private void deleteButton(View view) {
       mNavBar.removeButton(view);
       mButtons.remove(view);
    }

    public boolean isEditing() {
        return mEditing;
    }

    /**
     * Find intersecting view in mButtonViews
     * @param pos - pointer location
     * @param v - view being dragged
     * @return intersecting view or null
     */
    private View findInterceptingView(float pos, View v) {
        for (SlimKeyButtonView otherView : mNavBar.getButtons()) {
            if (otherView == v) {
                continue;
            }

            if (ArrayUtils.contains(SMALL_BUTTON_IDS, otherView.getId())
                    || otherView == mAddButton) {
                continue;
            }

            otherView.getLocationOnScreen(sLocation);
            float otherPos = sLocation[mNavBar.isVertical() ? 1 : 0];
            float otherDimension = mNavBar.isVertical() ? v.getHeight() : v.getWidth();

            if (pos > (otherPos + otherDimension / 4) && pos < (otherPos + otherDimension)) {
                return otherView;
            }
        }
        return null;
    }

    public View getAddButton() {
        return mAddButton;
    }

    private void addButton(String action, String description) {
        ActionConfig actionConfig = new ActionConfig(
                action, description,
                ActionConstants.ACTION_NULL,
                mContext.getResources().getString(
                        org.slim.framework.internal.R.string.shortcut_action_none),
                ActionConstants.ACTION_NULL,
                mContext.getResources().getString(
                        org.slim.framework.internal.R.string.shortcut_action_none),
                ActionConstants.ICON_EMPTY);

        SlimKeyButtonView v = mNavBar.generateKey(mNavBar.isVertical(),
                actionConfig.getClickAction(),
                actionConfig.getLongpressAction(),
                actionConfig.getDoubleTapAction(),
                actionConfig.getIcon());
        v.setConfig(actionConfig);

        updateButton(v);

        mNavBar.addButton(v, mNavBar.getNavButtons().indexOfChild(mAddButton));
        mButtons.add(v);
    }

    private Resources getSettingsResources() {
        try {
            return mContext.getPackageManager().getResourcesForApplication(SETTINGS_METADATA_NAME);
        } catch (Exception e) {
            return null;
        }
    }

    public void save() {

       ArrayList<ActionConfig> buttons = new ArrayList<>();

        for (View v : mButtons) {
            if (v instanceof SlimKeyButtonView) {
                SlimKeyButtonView key = (SlimKeyButtonView) v;

                if (ArrayUtils.contains(SMALL_BUTTON_IDS, v.getId()) || mAddButton == v
                        || mDeleteButton == v) {
                    continue;
                }

                ActionConfig config = key.getConfig();
                if (config != null) {
                    buttons.add(config);
                }
            }
        }

        ActionHelper.setNavBarConfig(mContext, buttons, false);
    }

    private void prepareToShowPopup(View editView) {
        ViewGroup parent = (ViewGroup) editView.getParent();
        mEditContainer.setVisibility(View.VISIBLE);
        mHidden.setTag(editView.getTag());
        mHidden.getLayoutParams().width = editView.getWidth();
        mHidden.getLayoutParams().height = editView.getHeight();
        mHidden.setLayoutParams(mHidden.getLayoutParams());
        mHidden.setX(sLocation[0] - parent.getLeft());
        mHidden.setY(sLocation[1] - parent.getTop());
    }

    private void createPopupContainer() {
        removePopupContainer();
        loadMenu();
        mEditContainer = new FrameLayout(mContext);
        mHidden = new View(mContext);
        mEditContainer.setOnTouchListener(mEditorWindowTouchListener);
        mHidden.setLayoutParams(new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT));
        mHidden.setVisibility(View.INVISIBLE);
        mEditContainer.addView(mHidden);
        mEditContainer.setVisibility(View.GONE);
        mWindowManager.addView(mEditContainer, getEditorParams());
    }

    private void removePopupContainer() {
        if (mPopup != null) {
            mPopup.dismiss();
            mPopup = null;
        }
        if (mEditContainer != null && mEditContainer.isAttachedToWindow()) {
            mEditContainer.removeAllViews();
            mEditContainer.setVisibility(View.GONE);
            mWindowManager.removeViewImmediate(mEditContainer);
        }
    }

    private View.OnTouchListener mEditorWindowTouchListener = new View.OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            return mPopupTouchWrapper.onTouch(v, event);
        }
    };

    private WindowManager.LayoutParams getEditorParams() {
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_NAVIGATION_BAR_PANEL,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                        | WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR
                        | WindowManager.LayoutParams.FLAG_SPLIT_TOUCH,
                PixelFormat.TRANSLUCENT);
        lp.flags |= WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        lp.gravity = Gravity.BOTTOM;
        lp.setTitle("SmartBar Editor");
        lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_UNCHANGED
                | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING;
        return lp;
    }

    private void editAction(SlimKeyButtonView key) {
        mButtonToEdit = key;
        QuickAction popup = new QuickAction(mContext, QuickAction.VERTICAL);
        ArrayList<ActionItem> items;
        if (key.getId() == R.id.menu || key.getId() == R.id.menu_left) {
            items = mMenuButtonItems;
        } else if (key.getId() == R.id.ime_switcher) {
            items = mImeButtonItems;
        } else {
            items = mButtonItems;
        }
        for (ActionItem item : items) {
            popup.addActionItem(item);
        }
        popup.setOnActionItemClickListener(mQuickClickListener);
        popup.mWindow.setOnDismissListener(mPopupDismissListener);
        popup.mWindow.setTouchInterceptor(mPopupTouchWrapper);
        popup.mWindow.setFocusable(true);
        popup.show(mHidden);
        mPopup = popup;
    }

    private final Runnable mHidePopupContainer = new Runnable() {
        @Override
        public void run() {
            mEditContainer.setVisibility(View.GONE);
        }
    };

    private PopupWindow.OnDismissListener mPopupDismissListener = new PopupWindow.OnDismissListener() {
        @Override
        public void onDismiss() {
            mHidePopupContainer.run();
        }
    };

    private void loadMenu() {
        mButtonItems.clear();
        mButtonItems.add(new ActionItem(MENU_SINGLE_TAP, "Single tap", null));
        mButtonItems.add(new ActionItem(MENU_DOUBLE_TAP, "Double tap", null));
        mButtonItems.add(new ActionItem(MENU_LONG_PRESS, "Longpress", null));
        mButtonItems.add(new ActionItem(MENU_PICK_ICON, "Icon", null));

        mMenuButtonItems.clear();
        mMenuButtonItems.add(new ActionItem(MENU_VISIBILITY_ALWAYS, "Always show", null));
        mMenuButtonItems.add(new ActionItem(MENU_VISIBILITY_REQUEST, "Show on request (default)", null));
        mMenuButtonItems.add(new ActionItem(MENU_VISIBILITY_NEVER, "Never show", null));

        mImeButtonItems.clear();
        mImeButtonItems.add(new ActionItem(IME_VISIBILITY_REQUEST, "Show on request (default)", null));
        mImeButtonItems.add(new ActionItem(IME_VISIBILITY_NEVER, "Never show", null));
    }
}
