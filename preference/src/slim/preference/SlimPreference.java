package slim.preference;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;

import slim.utils.AttributeHelper;

import java.util.List;

public class SlimPreference extends Preference {

    private boolean mRemovePreference = false;

    public SlimPreference(Context context, AttributeSet attrs, int defStyleAttr,
            int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    public SlimPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public SlimPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public SlimPreference(Context context) {
        this(context, null);
    }

    private void init(Context context, AttributeSet attrs) {
        AttributeHelper a = new AttributeHelper(context, attrs,
            slim.R.styleable.SlimPreference);

        boolean hidePreference = a.getBoolean(slim.R.styleable.SlimPreference_hidePreference, false);
        int hidePreferenceInt = a.getInt(slim.R.styleable.SlimPreference_hidePreferenceInt, -1);
        int intDep = a.getInt(slim.R.styleable.SlimPreference_hidePreferenceIntDependency, 0);
        boolean removePreference = a.getBoolean(slim.R.styleable.SlimPreference_removePreference, false);
        boolean removePreferenceIntent = a.getBoolean(slim.R.styleable.SlimPreference_removePreferenceIntent, false);
        if (hidePreference || hidePreferenceInt == intDep) {
            if (removePreference) {
                mRemovePreference = true;
            } else {
                setVisible(false);
            }
        }

        //check intent if it exists
        Intent intent = getIntent();
        android.util.Log.d("TEST", "intent=" + intent);
        if (intent != null) {
            if (!intentExists(context, intent)) {
                mRemovePreference = true;
            }
        } else if (intent == null && removePreferenceIntent) {
            mRemovePreference = true;
        }
    }

    @Override
    public void onAttached() {
        super.onAttached();
        if (mRemovePreference) {
            getPreferenceManager().getPreferenceScreen().removePreference(this);
        }
    }

    private boolean intentExists(Context context, Intent intent) {
        PackageManager pm = context.getPackageManager();
        List<ResolveInfo> list = pm.queryIntentActivities(intent, PackageManager.GET_META_DATA);
        return (list.size() > 0);
    }
}
