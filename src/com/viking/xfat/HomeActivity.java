package com.viking.xfat;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.CompoundButton;

public class HomeActivity extends Activity {
    static final String KEY_FIRST_RUN = "first_run";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        initComponent();
        addAdminComp();
    }

    @Override
    protected void onDestroy() {
        if (!Utility.IsFloatServiceRunning(HomeActivity.this)) {
            removeAdminComp();
        }
        super.onDestroy();
    }

    private void initComponent() {
        StartButton startButton = (StartButton)findViewById(R.id.startButton);

        startButton.setChecked(Utility.IsFloatServiceRunning(HomeActivity.this));
        startButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                toggleFloatService(isChecked);
            }
        });
    }

    private void toggleFloatService(boolean start) {
        Intent intent = new Intent(HomeActivity.this, FloatService.class);

        if (!start) {
            stopService(intent);
            removeAdminComp();
            finish();
        } else {
            if (!Utility.IsFloatServiceRunning(HomeActivity.this)) {
                startService(intent);
                finish();
            }
        }
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

    @Override
    protected void onResume() {
        super.onResume();
        if (PreferenceManager.getDefaultSharedPreferences(HomeActivity.this).getBoolean(KEY_FIRST_RUN, true)) {
            PreferenceManager.getDefaultSharedPreferences(HomeActivity.this).edit().putBoolean(KEY_FIRST_RUN, false).commit();
            Utility.ShowInstruction(HomeActivity.this);
        }
    }
}