package com.viking.xfat;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.*;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Utility {
    static final String TAG = "UTILITY";

    public static boolean IsFloatServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (FloatService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static void ShowInstruction(Context context) {
        Dialog alertDialog = new AlertDialog.Builder(context).
            setTitle(R.string.instruction).
            setMessage(R.string.instruction_content).
            setIcon(R.drawable.icon).
            setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).create();
        alertDialog.show();
    }

    public static int GetStatusBarHeight(Context context) {
        int status_bar_height=0;

        try {
            Class<?> c = Class.forName("com.android.internal.R$dimen");
            Object o = c.newInstance();
            Field field = c.getField("status_bar_height");
            int x = Integer.parseInt(field.get(o).toString());
            status_bar_height = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return status_bar_height;
    }

    public static int DIP2PX(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int PX2DIP(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static void GoHome(Context context) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        context.startActivity(intent);
    }

    public static void LockScreen(Context context) {
        ApplicationInfo info = context.getApplicationInfo();
        if (null != info) {
            ComponentName admin_component = new ComponentName(context.getApplicationInfo().packageName, DeviceAdminReceiver.class.getName());
            DevicePolicyManager device_policy_manager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            if (device_policy_manager.isAdminActive(admin_component)) {
                device_policy_manager.lockNow();
            }
        }
    }

    private static final String[][] pcs = {
            {"com.android.systemui", "com.android.systemui.recents.RecentsActivity"},
            {"com.htc.taskmanager", "com.htc.taskmanager.MainActivity"},
    };

    public static void OpenRecentActivity(Context context) {
        for (String[] pc : pcs) {
            try {
                Intent intent = new Intent("/");
                intent.setComponent(new ComponentName(pc[0], pc[1]));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                context.startActivity(intent);
                return;
            } catch (ActivityNotFoundException | SecurityException e) {
                e.printStackTrace();
            }

        }

        //todo: Show customize recent app view
    }

    public static void ToggleRecentApps(Context context) {
        Class serviceManagerClass;
        try {
            serviceManagerClass = Class.forName("android.os.ServiceManager");
            Method getService = serviceManagerClass.getMethod("getService", String.class);
            IBinder retbinder = (IBinder) getService.invoke(serviceManagerClass, "statusbar");
            Class statusBarClass = Class.forName(retbinder.getInterfaceDescriptor());
            Object statusBarObject = statusBarClass.getClasses()[0].getMethod(
                    "asInterface", IBinder.class).invoke(null,
                    new Object[] { retbinder });
            Method clearAll = statusBarClass.getMethod("toggleRecentApps");
            clearAll.setAccessible(true);
            clearAll.invoke(statusBarObject);
            return;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException | IllegalAccessException | RemoteException e) {
            e.printStackTrace();
        }

        if (Build.VERSION.SDK_INT >= 19) {
            if (isAccessibilitySettingsOn(context, AccService.class)) {
                Intent service = new Intent(context, AccService.class);
                service.putExtra("EXTRA_ACTION", AccService.ACTION_RECENTS);
                context.startService(service);
                return;
            } else {
                context.startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            }
        }

        OpenRecentActivity(context);
    }

    public static void OpenAllApp(Context context) {
        Intent intent = new Intent(Intent.ACTION_ALL_APPS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_HISTORY);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void OpenSetting(Context context) {
        Intent intent = new Intent(Settings.ACTION_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        context.startActivity(intent);
    }

    public static boolean isAccessibilitySettingsOn(Context mContext, Class accessibilitySeriveClass) {
        int accessibilityEnabled = 0;
        final String service = mContext.getPackageName() + "/" + accessibilitySeriveClass.getCanonicalName();
        try {
            accessibilityEnabled = Settings.Secure.getInt(
                    mContext.getApplicationContext().getContentResolver(),
                    android.provider.Settings.Secure.ACCESSIBILITY_ENABLED);
            Log.v(TAG, "accessibilityEnabled = " + accessibilityEnabled);
        } catch (Settings.SettingNotFoundException e) {
            Log.e(TAG, "Error finding setting, default accessibility to not found: "
                    + e.getMessage());
        }
        TextUtils.SimpleStringSplitter mStringColonSplitter = new TextUtils.SimpleStringSplitter(':');

        if (accessibilityEnabled == 1) {
            Log.v(TAG, "***ACCESSIBILITY IS ENABLED*** -----------------");
            String settingValue = Settings.Secure.getString(
                    mContext.getApplicationContext().getContentResolver(),
                    Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
            if (settingValue != null) {
                mStringColonSplitter.setString(settingValue);
                while (mStringColonSplitter.hasNext()) {
                    String accessibilityService = mStringColonSplitter.next();

                    Log.v(TAG, "-------------- > accessibilityService :: " + accessibilityService + " " + service);
                    if (accessibilityService.equalsIgnoreCase(service)) {
                        Log.v(TAG, "We've found the correct setting - accessibility is switched on!");
                        return true;
                    }
                }
            }
        } else {
            Log.v(TAG, "***ACCESSIBILITY IS DISABLED***");
        }

        return false;
    }

    public static int dip2px(Context context, double dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2dip(Context context, double pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }
}
