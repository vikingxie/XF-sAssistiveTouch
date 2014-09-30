package com.viking.xfat;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class FloatService extends Service {
    private FloatButton fb = null;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (null == fb) {
            fb = new FloatButton(getApplicationContext());
            fb.show();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        if (null != fb) {
            fb.hide();
        }
        super.onDestroy();
    }
}
