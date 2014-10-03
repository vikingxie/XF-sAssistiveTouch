package com.viking.xfat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class FloatService extends Service {
    static final int NOTIFY_ID = 0;
    private FloatButton fb = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Notification notification = new Notification();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        startForeground(NOTIFY_ID, notification);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null == fb) {
            fb = new FloatButton(getApplicationContext());
        }
        fb.show();
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).cancel(NOTIFY_ID);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        if (null != fb) {
            fb.hide();
        }
        super.onDestroy();
    }
}
