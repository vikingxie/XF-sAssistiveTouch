package com.viking.xfat;

import android.animation.TimeAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.*;
import android.widget.ImageView;

import static android.content.Context.MODE_MULTI_PROCESS;

public class FloatButton extends CFloatView {
    private static final long animation_frame_delay = 50; // ms

    private float button_alpha;
    private int button_height, button_width;
    private float stick_distance_x, stick_distance_y;
    private boolean button_scrolled = false;
    private Point virtual_coordinate = new Point();
    private ValueAnimator fadeout_animation = null;
    private TimeAnimator stick_animation = null;
    private int stick_animation_speed = 0;
    private OrientationEventListener orientation_listener = null;
    private PreferenceListener preference_listener = null;
    private Context context;

    public FloatButton(Context context) {
        super(context);
        this.context = context;
        button_alpha = context.getSharedPreferences(context.getString(R.string.pref_name), MODE_MULTI_PROCESS).
                getFloat(DefaultPreference.BUTTON_TRANSPARENT.getKey(), Float.parseFloat(DefaultPreference.BUTTON_TRANSPARENT.getDefaultValue()));
        createDrawable();
        createLayoutParams();
        createAnimation();
        setOnTouchListener(new TouchListener(context));
        orientation_listener = new OrientationListener(context);
        preference_listener = new PreferenceListener();
    }

    public void show() {
        super.show();
        stick_animation.start(); //fadeout_animation.start();
        orientation_listener.enable();
        context.getSharedPreferences(context.getString(R.string.pref_name), MODE_MULTI_PROCESS).registerOnSharedPreferenceChangeListener(preference_listener);
    }

    public void hide() {
        context.getSharedPreferences(context.getString(R.string.pref_name), MODE_MULTI_PROCESS).unregisterOnSharedPreferenceChangeListener(preference_listener);
        orientation_listener.disable();
        stick_animation.cancel();
        fadeout_animation.cancel();
        super.hide();
    }

    public void updateLayoutParams(Point coordinate) {
        layout_params.x = coordinate.x - button_width / 2;
        layout_params.y = coordinate.y - button_height / 2;
    }

    public void updateLayoutParams(float alpha) {
        layout_params.alpha = alpha;
    }

    public void updateLayoutParams(Point coordinate, float alpha) {
        layout_params.x = coordinate.x - button_width / 2;
        layout_params.y = coordinate.y - button_height / 2;
        layout_params.alpha = alpha;
    }

    public void updateView(Point coordinate) {
        updateLayoutParams(coordinate);
        update();
    }

    public void updateView(Point coordinate, float alpha) {
        updateLayoutParams(coordinate, alpha);
        update();
    }

    public void updateView(float alpha) {
        updateLayoutParams(alpha);
        update();
    }

    private void createDrawable() {
        Drawable button_image = context.getResources().getDrawable(R.drawable.icon);
        setImageDrawable(button_image);
        button_height = button_image.getMinimumHeight();
        button_width = button_image.getMinimumWidth();
        stick_distance_x = button_width * 0.8f;
        stick_distance_y = button_height * 0.8f;
    }

    private void createLayoutParams() {
        layout_params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        layout_params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layout_params.format = PixelFormat.RGBA_8888;
        layout_params.gravity = Gravity.TOP | Gravity.START;
        layout_params.x = 0;
        layout_params.y = 0;
        layout_params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layout_params.height =WindowManager.LayoutParams.WRAP_CONTENT;
        layout_params.alpha = 1.0f;

        Point real = new Point();
        SharedPreferences pref = context.getSharedPreferences(context.getString(R.string.pref_name), MODE_MULTI_PROCESS);
        virtual_coordinate.x = pref.getInt(DefaultPreference.LAST_VIRTUAL_X.getKey(), Integer.parseInt(DefaultPreference.LAST_VIRTUAL_X.getDefaultValue()));
        virtual_coordinate.y = pref.getInt(DefaultPreference.LAST_VIRTUAL_Y.getKey(), Integer.parseInt(DefaultPreference.LAST_VIRTUAL_Y.getDefaultValue()));
        coordinateVirtualToReal(virtual_coordinate, real);
        updateLayoutParams(real);
    }

    private void createAnimation() {
        ValueAnimator.setFrameDelay(animation_frame_delay);
        fadeout_animation = ValueAnimator.ofFloat(1.0f, button_alpha);
        fadeout_animation.setDuration(1500);
        fadeout_animation.setStartDelay(800);
        fadeout_animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                updateView((Float) animation.getAnimatedValue());
            }
        });

        stick_animation_speed = Utility.DIP2PX(context, 10);
        stick_animation = new TimeAnimator();
        stick_animation.setTimeListener(new TimeAnimator.TimeListener() {
            Point real = new Point();

            @Override
            public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
                boolean is_animation_end = false;
                coordinateVirtualToReal(virtual_coordinate, real);
                if (stickStep(real)) {
                    updateView(real, 1.0f);
                } else {
                    coordinateRealStickEdge(real);
                    stick_animation.cancel();
                    if (fadeout_animation.isStarted()) {
                        fadeout_animation.cancel();
                    }
                    fadeout_animation.start();
                    is_animation_end = true;
                }
                coordinateRealToVirtual(real, virtual_coordinate);
                if (is_animation_end) {
                    context.getSharedPreferences(context.getString(R.string.pref_name), MODE_MULTI_PROCESS).
                            edit().
                            putInt(DefaultPreference.LAST_VIRTUAL_X.getKey(), virtual_coordinate.x).
                            putInt(DefaultPreference.LAST_VIRTUAL_Y.getKey(), virtual_coordinate.y).
                            commit();
                }
            }

            private boolean stickStep(Point real) {
                Point frame_size = new Point();
                window_manager.getDefaultDisplay().getSize(frame_size);
                frame_size.y -= status_bar_height;

                if (real.x < button_width / 2 || real.x > frame_size.x - button_width / 2 ||
                        real.y < button_height / 2 || real.y > frame_size.y - button_height / 2)
                {
                    return false;
                }

                int to_left, to_right, to_top, to_bottom;
                int h_speed, v_speed;
                int h_dis, v_dis;

                to_left = real.x;
                to_right = frame_size.x - real.x;
                to_top = real.y;
                to_bottom = frame_size.y - real.y;

                if (to_left < to_right) {
                    h_speed = -stick_animation_speed;
                    h_dis = to_left;
                } else {
                    h_speed = stick_animation_speed;
                    h_dis = to_right;
                }

                if (to_top < to_bottom) {
                    v_speed = -stick_animation_speed;
                    v_dis = to_top;
                } else {
                    v_speed = stick_animation_speed;
                    v_dis = to_bottom;
                }

                if (h_dis <= v_dis) {
                    v_speed = 0;
                } else {
                    h_speed = 0;
                }

                real.x += h_speed;
                real.y += v_speed;

                return true;
            }
        });
    }

    private class TouchListener implements OnTouchListener {
        private GestureDetector gesture_detector = null;

        public TouchListener(Context context) {
            gesture_detector = new GestureDetector(context, new GestureListener());
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case  MotionEvent.ACTION_UP:
                    if (button_scrolled) {
                        button_scrolled = false;
                        stick_animation.start();
                    }
                    break;
            }
            return gesture_detector.onTouchEvent(event);
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private float dx, dy; // x,y relative to view
        private boolean double_taped = false;

        @Override
        public boolean onDown(MotionEvent e) {
            stick_animation.cancel();
            fadeout_animation.cancel();
            if (!double_taped) {
                updateView(1.0f);
            }
            double_taped = false;
            dx = e.getX();
            dy = e.getY();
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            float x, y;

            x = e2.getRawX() - dx + button_width / 2;
            y = e2.getRawY() - dy - status_bar_height + button_height / 2;

            Point real = new Point((int)x, (int)y);
            coordinateRealStickEdge(real);
            updateView(real, 1.0f);
            coordinateRealToVirtual(real, virtual_coordinate);
            button_scrolled = true;
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            updateView(button_alpha);
            double_taped = true;
            if (context.getSharedPreferences(context.getString(R.string.pref_name), MODE_MULTI_PROCESS).
                    getBoolean(DefaultPreference.HOME_BEFORE_LOCK.getKey(), Boolean.parseBoolean(DefaultPreference.HOME_BEFORE_LOCK.getDefaultValue()))) {
                Utility.GoHome(context);
            }
            Utility.LockScreen(context);
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            fadeout_animation.start();
            Utility.GoHome(FloatButton.this.context);
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            fadeout_animation.start();
            Utility.ToggleRecentApps(FloatButton.this.context);
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
                Point real = new Point();
                coordinateVirtualToReal(virtual_coordinate, real);
                coordinateRealStickEdge(real);
                updateView(real);
                screen_rotation = rotation;
            }
        }
    }

    private class PreferenceListener implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(DefaultPreference.BUTTON_TRANSPARENT.getKey())) {
                button_alpha = sharedPreferences.getFloat(key, Float.parseFloat(DefaultPreference.BUTTON_TRANSPARENT.getDefaultValue()));

                fadeout_animation.cancel();
                fadeout_animation.setFloatValues(1.0f, button_alpha);

                layout_params.alpha = button_alpha;
                window_manager.updateViewLayout(FloatButton.this, layout_params);
            }
        }
    }

    private static final double VIRTUAL_SCREEN_DIMENSION = 1000000;

    private void coordinateRealToVirtual(Point real, Point virtual) {
        int rotation = window_manager.getDefaultDisplay().getRotation();
        Point frame_size = new Point();
        window_manager.getDefaultDisplay().getSize(frame_size);
        frame_size.y -= status_bar_height;

        switch (rotation) {
            case Surface.ROTATION_90:
                virtual.x = (int) ((VIRTUAL_SCREEN_DIMENSION * (frame_size.y - real.y) / frame_size.y) + 0.5f);
                virtual.y = (int) ((VIRTUAL_SCREEN_DIMENSION * real.x / frame_size.x) + 0.5f);
                break;
            case Surface.ROTATION_180:
                virtual.x = (int) ((VIRTUAL_SCREEN_DIMENSION * (frame_size.x - real.x) / frame_size.x) + 0.5f);
                virtual.y = (int) ((VIRTUAL_SCREEN_DIMENSION * (frame_size.y - real.y) / frame_size.y) + 0.5f);
                break;
            case Surface.ROTATION_270:
                virtual.x = (int) ((VIRTUAL_SCREEN_DIMENSION * real.y / frame_size.y) + 0.5f);
                virtual.y = (int) ((VIRTUAL_SCREEN_DIMENSION * (frame_size.x - real.x) / frame_size.x) + 0.5f);
                break;
            default:
                virtual.x = (int) ((VIRTUAL_SCREEN_DIMENSION * real.x / frame_size.x) + 0.5f);
                virtual.y = (int) ((VIRTUAL_SCREEN_DIMENSION * real.y / frame_size.y) + 0.5f);
                break;
        }
    }

    private void coordinateVirtualToReal(Point virtual, Point real) {
        int rotation = window_manager.getDefaultDisplay().getRotation();
        Point frame_size = new Point();
        window_manager.getDefaultDisplay().getSize(frame_size);
        frame_size.y -= status_bar_height;

        switch (rotation) {
            case Surface.ROTATION_90:
                real.x = (int) ((frame_size.x * virtual.y / VIRTUAL_SCREEN_DIMENSION) + 0.5f);
                real.y = (int) ((frame_size.y - frame_size.y * virtual.x / VIRTUAL_SCREEN_DIMENSION) + 0.5f);
                break;
            case Surface.ROTATION_180:
                real.x = (int) ((frame_size.x - frame_size.x * virtual.x / VIRTUAL_SCREEN_DIMENSION) + 0.5f);
                real.y = (int) ((frame_size.y - frame_size.y * virtual.y / VIRTUAL_SCREEN_DIMENSION) + 0.5f);
                break;
            case Surface.ROTATION_270:
                real.x = (int) ((frame_size.x - frame_size.x * virtual.y / VIRTUAL_SCREEN_DIMENSION) + 0.5f);
                real.y = (int) ((frame_size.y * virtual.x / VIRTUAL_SCREEN_DIMENSION) + 0.5f);
                break;
            default:
                real.x = (int) ((frame_size.x * virtual.x / VIRTUAL_SCREEN_DIMENSION) + 0.5f);
                real.y = (int) ((frame_size.y * virtual.y / VIRTUAL_SCREEN_DIMENSION) + 0.5f);
                break;
        }
    }

    private void coordinateRealStickEdge(Point real) {
        Point screen_size = new Point();
        window_manager.getDefaultDisplay().getSize(screen_size);
        if (real.x <= stick_distance_x) {
            real.x = button_width / 2;
        } else if (real.x >= screen_size.x - stick_distance_x) {
            real.x = screen_size.x - button_width / 2;
        }
        if (real.y <= stick_distance_y) {
            real.y = button_height / 2;
        } else if (real.y >= screen_size.y - status_bar_height - stick_distance_y) {
            real.y = screen_size.y - status_bar_height - button_height / 2;
        }
    }
}
