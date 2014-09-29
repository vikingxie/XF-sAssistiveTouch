package com.viking.xfat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.*;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

public class HomeActivity extends Activity {
    boolean firstRun = true;
    static final String PREF_NAME = "user";
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

        firstRun = getSharedPreferences(PREF_NAME, MODE_PRIVATE).getBoolean(KEY_FIRST_RUN, true);
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
    protected void onStart() {
        super.onStart();
        showInstruction();
    }

    private void showInstruction() {
        if (!firstRun) {
            return;
        }

        firstRun = false;
        SharedPreferences.Editor editor = getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(KEY_FIRST_RUN, firstRun);
        editor.commit();

        View layout = getLayoutInflater().inflate(R.layout.instruction, (ViewGroup)findViewById(R.id.instruction));
        Dialog alertDialog = new AlertDialog.Builder(this).
                setTitle(R.string.instruction).
                setView(layout).
                setIcon(R.drawable.icon).
                setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        alertDialog.show();
    }
}