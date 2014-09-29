package com.viking.xfat;

import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class Utility {

    public static boolean IsFloatServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (FloatService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
