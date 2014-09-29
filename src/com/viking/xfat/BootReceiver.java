package com.viking.xfat;

import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        DevicePolicyManager device_policy_manager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName admin_component = new ComponentName(context.getPackageName(), DeviceAdminReceiver.class.getName());
        if (device_policy_manager.isAdminActive(admin_component)) {
            context.startService(new Intent(context, FloatService.class));
        }
    }
}
