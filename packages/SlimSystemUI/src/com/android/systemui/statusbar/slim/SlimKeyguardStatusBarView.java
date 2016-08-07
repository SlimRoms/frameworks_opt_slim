package com.android.systemui.statusbar.slim;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.android.systemui.R;
import com.android.systemui.statusbar.phone.KeyguardStatusBarView;
import com.android.systemui.statusbar.policy.BatteryController;

public class SlimKeyguardStatusBarView extends KeyguardStatusBarView {

    public SlimKeyguardStatusBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        View batteryLevel = findViewById(R.id.battery_level);
        ((ViewGroup) batteryLevel.getParent()).removeView(batteryLevel);
    }

    @Override
    public void setBatteryController(BatteryController controller) {
        super.setBatteryController(controller);
        ((SlimBatteryContainer) findViewById(R.id.slim_battery_container))
                .setBatteryController(controller);
    }
}
