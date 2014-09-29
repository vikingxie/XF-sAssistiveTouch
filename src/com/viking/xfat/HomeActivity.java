package com.viking.xfat;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ScaleDrawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

public class HomeActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        initComponent();
    }

    private void initComponent() {
        final Context context = HomeActivity.this;
        boolean service_running = Utility.IsFloatServiceRunning(context);
        ToggleButton startButton = (ToggleButton)findViewById(R.id.startButton);

        startButton.setChecked(service_running);
        startButton.setBackground(getResources().getDrawable(service_running ? R.drawable.start_button_on : R.drawable.start_button_off));
        startButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonView.setBackground(getResources().getDrawable(isChecked ? R.drawable.start_button_on : R.drawable.start_button_off));

                Utility.ToggleFloatService(context);
            }
        });
        startButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_DOWN == event.getAction()) {
                    ScaleDrawable bg = (ScaleDrawable)getResources().getDrawable(((ToggleButton) v).isChecked() ? R.drawable.start_button_on_s : R.drawable.start_button_off_s);
                    bg.setLevel(1);
                    v.setBackground(bg);
                }
                return false;
            }
        });
    }

    static final int RESULT_FOR_ADD_DEVICE_ADMIN = 1;

    private void addAdminComp() {
        DevicePolicyManager device_policy_manager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName admin_component = new ComponentName(this.getPackageName(), DeviceAdminReceiver.class.getName());
        if (!device_policy_manager.isAdminActive(admin_component)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, admin_component);
            startActivityForResult(intent, RESULT_FOR_ADD_DEVICE_ADMIN);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (RESULT_FOR_ADD_DEVICE_ADMIN == requestCode) {
            if (RESULT_OK != resultCode) {
                finish();
            }
        }
    }

    private void removeAdminComp() {
        DevicePolicyManager device_policy_manager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName admin_component = new ComponentName(this.getPackageName(), DeviceAdminReceiver.class.getName());
        device_policy_manager.removeActiveAdmin(admin_component);
    }
}