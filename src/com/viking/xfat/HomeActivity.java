package com.viking.xfat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.*;
import android.widget.CompoundButton;
import android.widget.SeekBar;

import java.lang.reflect.Field;

public class HomeActivity extends Activity {
    static final String KEY_FIRST_RUN = "first_run";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setOverflowShowingAlways();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_preference:
                showPreferenceDialog();
                break;
            case R.id.menu_instruction:
                Utility.ShowInstruction(HomeActivity.this);
                break;

        }
        return super.onMenuItemSelected(featureId, item);
    }

    private void setOverflowShowingAlways() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            menuKeyField.setAccessible(true);
            menuKeyField.setBoolean(config, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showPreferenceDialog() {
        final float transparent_value = PreferenceManager.getDefaultSharedPreferences(HomeActivity.this).
                getFloat(DefaultPreference.BUTTON_TRANSPARENT.getKey(), Float.parseFloat(DefaultPreference.BUTTON_TRANSPARENT.getDefaultValue()));

        View layout = getLayoutInflater().inflate(R.layout.preference, (ViewGroup) findViewById(R.id.layout_preference));
        SeekBar transparent = ((SeekBar) layout.findViewById(R.id.preference_transparent));
        transparent.setProgress((int) (transparent_value * transparent.getMax()));
        transparent.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                PreferenceManager.getDefaultSharedPreferences(HomeActivity.this).edit().
                        putFloat(DefaultPreference.BUTTON_TRANSPARENT.getKey(), (float)progress / seekBar.getMax()).commit();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        new AlertDialog.Builder(HomeActivity.this).setTitle(R.string.preference).setIcon(R.drawable.preference).
                setView(layout).
                setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //todo: Save preference
                    }
                }).
                setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Revert preference
                        PreferenceManager.getDefaultSharedPreferences(HomeActivity.this).edit().
                                putFloat(DefaultPreference.BUTTON_TRANSPARENT.getKey(), transparent_value).commit();
                    }
                }).create().show();
    }
}