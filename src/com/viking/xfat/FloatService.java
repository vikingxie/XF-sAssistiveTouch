package com.viking.xfat;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;

public class FloatService extends Service {
    private static final int NOTIFY_ID = 0;
    private FloatButton fb = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (null == fb) {
            fb = new FloatButton(getApplicationContext());
        }

        Notification notification = new Notification();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        startForeground(NOTIFY_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        try {
            fb.hide();
        } catch (Exception e) {
            e.printStackTrace();
        }
        fb.show();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        if (null != fb) {
            fb.hide();
        }
        super.onDestroy();
        Process.killProcess(Process.myPid());
    }
}
