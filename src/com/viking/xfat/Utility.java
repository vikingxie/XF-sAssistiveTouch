package com.viking.xfat;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.*;
import android.content.pm.ApplicationInfo;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.Settings;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
            {"com.android.systemui", "com.android.systemui.recent.RecentsActivity"},
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
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
        }

        //todo: Show customize recent app view
    }

    public static void ToggleRecentApps(Context context) {
        try {
            Class sm = Class.forName("android.os.ServiceManager");
            IBinder statusbar_binder = (IBinder)sm.getMethod("getService", String.class).invoke(sm, "statusbar");
            Class statusbar_class = Class.forName(statusbar_binder.getInterfaceDescriptor());
            Object statusbar_object = statusbar_class.getClasses()[0].getMethod("asInterface", IBinder.class).invoke(null, new Object[] { statusbar_binder });
            Method statusbar_method = statusbar_class.getMethod("toggleRecentApps");
            statusbar_method.setAccessible(true);
            statusbar_method.invoke(statusbar_object);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
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
}
