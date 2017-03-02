package com.viking.xfat;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.*;

import static android.content.Context.MODE_MULTI_PROCESS;

/**
 * Created by Administrator on 2017/2/28.
 */
public class SideBar extends CFloatView {
    private static final String TAG = "Sidebar";
    private static final int SIDE_BUTTON_COUNT = 3;
    private float sidebar_alpha = 0.0f;
    private int sidebar_height;
    private int sidebar_width;
    private OrientationEventListener orientation_listener = null;
    private SideButton[] side_buttons = new SideButton[SIDE_BUTTON_COUNT];

    public SideBar(Context context) {
        super(context);

        sidebar_alpha = context.getSharedPreferences(context.getString(R.string.pref_name), MODE_MULTI_PROCESS).
                getFloat(DefaultPreference.BUTTON_TRANSPARENT.getKey(), Float.parseFloat(DefaultPreference.BUTTON_TRANSPARENT.getDefaultValue()));
        createDrawable();
        createLayoutParams();
        createSideButton();
        setOnTouchListener(new TouchListener(context));
        orientation_listener = new SideBar.OrientationListener(context);
    }

    @Override
    public void show() {
        super.show();
        orientation_listener.enable();
    }

    @Override
    public void hide() {
        orientation_listener.disable();
        super.hide();
    }

    private void createDrawable() {
        Drawable image = getContext().getResources().getDrawable(R.drawable.rect);
        setImageDrawable(image);
        sidebar_height = image.getMinimumHeight();
        sidebar_width = image.getMinimumWidth();
    }

    private void createLayoutParams() {
        Point frame_size = new Point();
        window_manager.getDefaultDisplay().getSize(frame_size);

        layout_params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        layout_params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layout_params.format = PixelFormat.RGBA_8888;
        layout_params.gravity = Gravity.TOP | Gravity.START;
        layout_params.x = frame_size.x;
        layout_params.y = frame_size.y;
        layout_params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layout_params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        layout_params.alpha = sidebar_alpha;
    }

    private void createSideButton() {
        int[] drawable_id = {R.drawable.side_button_home, R.drawable.side_button_recents, R.drawable.side_button_lock};
        ISideButtonAction[] actions = {action_home, action_recents, action_lock};
        int size = Utility.DIP2PX(getContext(), 48);
        for (int i = 0; i< SIDE_BUTTON_COUNT; ++i) {
            try {
                Bitmap bitmap = Bitmap.createScaledBitmap(Utility.DrawableToBitmap(getContext().getResources().getDrawable(drawable_id[i])), size, size, true);
                side_buttons[i] = new SideButton(getContext(), new BitmapDrawable(getContext().getResources(), bitmap), actions[i]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private ISideButtonAction action_home = new ISideButtonAction() {
        @Override
        public void action() {
            Utility.GoHome(getContext());
        }
    };

    private ISideButtonAction action_recents = new ISideButtonAction() {
        @Override
        public void action() {
            Utility.ToggleRecentApps(getContext());
        }
    };

    private ISideButtonAction action_lock = new ISideButtonAction() {
        @Override
        public void action() {
            Context context = getContext();
            if (context.getSharedPreferences(context.getString(R.string.pref_name), MODE_MULTI_PROCESS).
                    getBoolean(DefaultPreference.HOME_BEFORE_LOCK.getKey(), Boolean.parseBoolean(DefaultPreference.HOME_BEFORE_LOCK.getDefaultValue()))) {
                Utility.GoHome(context);
            }
            Utility.LockScreen(context);
        }
    };

    private class TouchListener implements OnTouchListener {
        private GestureDetector gesture_detector = null;

        public TouchListener(Context context) {
            gesture_detector = new GestureDetector(context, new SideBar.GestureListener());
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case  MotionEvent.ACTION_UP:
                    int x = (int) (event.getRawX() + 0.5), y = (int) (event.getRawY() - status_bar_height + 0.5);
                    for (SideButton button : side_buttons) {
                        if (button.within(x, y)) {
                            button.action();
                            break;
                        }
                    }

                    for (int i=0; i<SIDE_BUTTON_COUNT; ++i) {
                        side_buttons[i].hide();
                    }
                    break;
            }
            return gesture_detector.onTouchEvent(event);
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private float ox, oy;
        private float step_r = 1.2f;
        private static final double ANGLE_CENTER = Math.PI / 4;
        private static final double ANGLE_STEP = Math.PI / 6;
        private double[] step_x = {-step_r * Math.cos(ANGLE_CENTER), -step_r * Math.cos(ANGLE_CENTER + ANGLE_STEP), -step_r * Math.cos(ANGLE_CENTER - ANGLE_STEP)};
        private double[] step_y = {-step_r * Math.sin(ANGLE_CENTER), -step_r * Math.sin(ANGLE_CENTER + ANGLE_STEP), -step_r * Math.sin(ANGLE_CENTER - ANGLE_STEP)};
        private float step_start = Utility.DIP2PX(getContext(), 24);
        private float step_stop = Utility.DIP2PX(getContext(), 120) / step_r;

        @Override
        public boolean onDown(MotionEvent e) {
            ox = e.getRawX();
            oy = e.getRawY();
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float dx = e2.getRawX() - ox, dy = e2.getRawY() - oy - status_bar_height;
            double dis_px = Math.sqrt(dx * dx + dy * dy);

            Log.d(TAG, "d " + dis_px);
            if (dis_px > step_stop) {
                dis_px = step_stop;
            }
            if (dis_px > step_start) {
                for (int i=0; i<SIDE_BUTTON_COUNT; ++i) {
                    int h = side_buttons[i].getDrawable().getMinimumHeight();
                    int w = side_buttons[i].getDrawable().getMinimumWidth();
                    WindowManager.LayoutParams para = side_buttons[i].getLayoutParams();
                    para.x = (int) (ox + dis_px * step_x[i] + 0.5f - w / 2);
                    para.y = (int) (oy + dis_px * step_y[i] + 0.5f - h / 2 - status_bar_height);
                    side_buttons[i].show();
                }

            }
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {
        }
    }

    private class OrientationListener extends OrientationEventListener {

        private int screen_rotation=Surface.ROTATION_0;

        public OrientationListener(Context context) {
            super(context);
            screen_rotation = window_manager.getDefaultDisplay().getRotation();
        }

        @Override
        public void onOrientationChanged(int orientation) {
            int rotation = window_manager.getDefaultDisplay().getRotation();
            if (screen_rotation != rotation) {
                Point frame_size = new Point();
                window_manager.getDefaultDisplay().getSize(frame_size);
                layout_params.x = frame_size.x;
                layout_params.y = frame_size.y;
                update();
                screen_rotation = rotation;
            }
        }
    }
}
