package com.viking.xfat;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;


public class AccService extends AccessibilityService {
    public static final int ACTION_HOME = 0x1;
    public static final int ACTION_RECENTS = 0x2;
    public static final String EXTRA_ACTION = "EXTRA_ACTION";
    private static final String TAG = "AccService";


    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Log.d(TAG, "onAccessibilityEvent");
    }

    @Override
    public void onInterrupt() {
        Log.d(TAG, "onInterrupt");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        int action = intent.getIntExtra(EXTRA_ACTION, 0x0);
        performAction(action);
        stopSelf();
        return super.onStartCommand(intent, flags, startId);
    }

    private void performAction(int action) {
        if(Build.VERSION.SDK_INT >= 19) {
            switch(action) {
                case ACTION_HOME:
                {
                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_HOME);
                    return;
                }
                case ACTION_RECENTS:
                {
                    performGlobalAction(AccessibilityService.GLOBAL_ACTION_RECENTS);
                    return;
                }
            }
        }
    }
}
