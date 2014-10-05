package com.viking.xfat;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;

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
        if (null == fb) {
            fb = new FloatButton(getApplicationContext());
        }
        fb.show();

        Notification notification = new Notification();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        startForeground(NOTIFY_ID, notification);
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        if (null != fb) {
            fb.hide();
        }
        super.onDestroy();
        //Process.killProcess(Process.myPid());
    }
}
