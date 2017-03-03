package slim.preference;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.ListPreference;
import android.text.TextUtils;
import android.util.AttributeSet;

import slim.action.ActionsArray;
import slim.action.ActionConstants;
import slim.utils.AttributeHelper;
import slim.utils.ShortcutPickerHelper;

import java.util.Arrays;

public class SlimActionsPreference extends ListPreference {

    private SlimPreferenceManager mSlimPreferenceManager = SlimPreferenceManager.getInstance();

    private int mSettingType;
    private String mListDependency;
    private String[] mListDependencyValues;

    private Context mContext;
    private ShortcutReceiver mReceiver;
    private IntentFilter mFilter;
    private ActionsArray mActionsArray;

    private boolean mPending = false;

    public SlimActionsPreference(Context context, AttributeSet attrs) {
       super(context, attrs);

       mContext = context;

       AttributeHelper a = new AttributeHelper(context, attrs,
               slim.R.styleable.SlimActionsPreference);

        boolean allowWake = a.getBoolean(slim.R.styleable.SlimActionsPreference_showWake, false);
        boolean allowNone = a.getBoolean(slim.R.styleable.SlimActionsPreference_showNone, true);
        String[] removeActions = a.getString(slim.R.styleable.SlimActionsPreference_removeActions,
            "").split("\\|");

        mActionsArray = new ActionsArray(context, allowNone, allowWake, Arrays.asList(removeActions));

        a = new AttributeHelper(context, attrs, slim.R.styleable.SlimPreference);

        mSettingType = SlimPreferenceManager.getSettingType(a);

        String list = a.getString(slim.R.styleable.SlimPreference_listDependency);
        if (!TextUtils.isEmpty(list)) {
            String[] listParts = list.split(":");
            mListDependency = listParts[0];
            mListDependencyValues = listParts[1].split("\\|");
        }

        boolean hidePreference =
                a.getBoolean(slim.R.styleable.SlimPreference_hidePreference, false);
        int hidePreferenceInt = a.getInt(slim.R.styleable.SlimPreference_hidePreferenceInt, -1);
        int intDep = a.getInt(slim.R.styleable.SlimPreference_hidePreferenceIntDependency, 0);
        if (hidePreference || hidePreferenceInt == intDep) {
            setVisible(false);
        }
    }

    @Override
    public void onAttached() {
        super.onAttached();
        if (mListDependency != null) {
            mSlimPreferenceManager.registerListDependent(
                    this, mListDependency, mListDependencyValues);
        }
        if (mFilter == null) {
            mFilter = new IntentFilter(ShortcutPickerHelper.ACTION_SHORTCUT_PICKED);
        }
        if (mReceiver == null) {
            mReceiver = new ShortcutReceiver();
        }
        mContext.registerReceiver(mReceiver, mFilter);
        setEntries(mActionsArray.getEntries());
        setEntryValues(mActionsArray.getValues());
    }

    @Override
    public void onDetached() {
        super.onDetached();
        if (mListDependency != null) {
            mSlimPreferenceManager.unregisterListDependent(this, mListDependency);
        }
        mContext.unregisterReceiver(mReceiver);
    }

    private class ShortcutReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent data) {
            String action = data.getStringExtra(ShortcutPickerHelper.EXTRA_SHORTCUT_ACTION);
            if (action != null) {
                callChangeListener(action);
            }
        }
    }

    public void shortcutPicked(String action, String description) {
        mPending = false;
        if (action == null) {
            return;
        }

        SlimPreferenceManager.putStringInSlimSettings(getContext(),
                mSettingType, getKey(), action);
    }

    @Override
    public boolean callChangeListener(final Object newValue) {
        String action = String.valueOf(newValue);
        if (action.equals(ActionConstants.ACTION_APP)) {
            mPending = true;
            ShortcutPickerHelper.pickShortcut(mContext);
            return false;
        }
        return super.callChangeListener(newValue);
    }

    @Override
    public boolean persistString(String value) {
        if (shouldPersist()) {
            if (TextUtils.equals(value, getPersistedString(null))) {
                return true;
            }
            if (value.equals(ActionConstants.ACTION_APP)) {
                mPending = true;
                ShortcutPickerHelper.pickShortcut(mContext);
            } else {
                SlimPreferenceManager.putStringInSlimSettings(getContext(),
                        mSettingType, getKey(), value);
            }
            return true;
        }
        return false;
    }

    @Override
    protected String getPersistedString(String defaultReturnValue) {
        if (!shouldPersist()) {
            return defaultReturnValue;
        }
        return SlimPreferenceManager.getStringFromSlimSettings(getContext(), mSettingType, getKey(),
                defaultReturnValue);
    }

    @Override
    protected boolean isPersisted() {
        return SlimPreferenceManager.settingExists(getContext(), mSettingType, getKey());
    }
}
