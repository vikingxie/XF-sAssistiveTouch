package com.viking.xfat;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;

public class FloatService extends Service {
    private static final int NOTIFY_ID = 0;
    private IFloatView fv = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (null == fv) {
            if (getSharedPreferences(getString(R.string.pref_name), MODE_MULTI_PROCESS).
                    getBoolean(DefaultPreference.SIDEBAR_ENABLED.getKey(), Boolean.parseBoolean(DefaultPreference.SIDEBAR_ENABLED.getDefaultValue()))) {
                fv = new SideBar(getApplicationContext());
            }
            else {
                fv = new FloatButton(getApplicationContext());
            }
        }

        Notification notification = new Notification();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        startForeground(NOTIFY_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        try {
            fv.hide();
        } catch (Exception e) {
            e.printStackTrace();
        }
        fv.show();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        if (null != fv) {
            fv.hide();
        }
        super.onDestroy();
        Process.killProcess(Process.myPid());
    }
}
