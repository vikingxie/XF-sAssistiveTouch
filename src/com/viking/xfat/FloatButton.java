package com.viking.xfat;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.view.*;
import android.widget.ImageView;

public class FloatButton extends ImageView {
    int status_bar_height = 0;
    float button_alpha = 0.25f;
    int button_height, button_width;
    float stick_distance_x, stick_distance_y;
    Point virtual_coordinate = new Point();
    Drawable button_image = null;
    WindowManager.LayoutParams layout_params = null;
    WindowManager window_manager = null;
    long animation_frame_delay = 50;
    ValueAnimator fadeout_animation = null;
    OrientationEventListener orientation_listener = null;

    public FloatButton(Context context) {
        super(context);
        status_bar_height = Utility.GetStatusBarHeight(context);
        button_alpha = PreferenceManager.getDefaultSharedPreferences(context).getFloat("button_alpha", 0.25f);
        window_manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        createDrawable();
        createLayoutParams();
        createAnimation();
        setOnTouchListener(new TouchListener(context));
        orientation_listener = new OrientationListener(context);
    }

    public void show() {
        window_manager.addView(this, layout_params);
        fadeout_animation.start();
        orientation_listener.enable();
    }

    public void hide() {
        orientation_listener.disable();
        fadeout_animation.cancel();
        window_manager.removeView(this);
    }

    public void move(Point coordinate) {
        layout_params.x = coordinate.x - button_width / 2;
        layout_params.y = coordinate.y - button_height / 2;
        window_manager.updateViewLayout(FloatButton.this, layout_params);
    }

    public void move(Point coordinate, float alpha) {
        layout_params.x = coordinate.x - button_width / 2;
        layout_params.y = coordinate.y - button_height / 2;
        layout_params.alpha = alpha;
        window_manager.updateViewLayout(FloatButton.this, layout_params);
    }

    private void createDrawable() {
        button_image = getContext().getResources().getDrawable(R.drawable.icon);
        setImageDrawable(button_image);
        button_height = button_image.getMinimumHeight();
        button_width = button_image.getMinimumWidth();
        stick_distance_x = button_width * 0.8f;
        stick_distance_y = button_height * 0.8f;
    }

    private void createLayoutParams() {
        layout_params = new WindowManager.LayoutParams();
        layout_params.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        layout_params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layout_params.format = PixelFormat.RGBA_8888;
        layout_params.gravity = Gravity.TOP | Gravity.LEFT;
        layout_params.x = 0;
        layout_params.y = 0;
        layout_params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layout_params.height =WindowManager.LayoutParams.WRAP_CONTENT;
        layout_params.alpha = 1.0f;
    }

    private void createAnimation() {
        ValueAnimator.setFrameDelay(animation_frame_delay);
        fadeout_animation = ValueAnimator.ofFloat(1.0f, button_alpha);
        fadeout_animation.setDuration(1500);
        fadeout_animation.setStartDelay(800);
        fadeout_animation.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                layout_params.alpha = (Float) animation.getAnimatedValue();
                window_manager.updateViewLayout(FloatButton.this, layout_params);
            }
        });
    }

    private class TouchListener implements OnTouchListener {
        private GestureDetector gesture_detector=null;

        public TouchListener(Context context) {
            gesture_detector = new GestureDetector(context, new GestureListener());
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            return gesture_detector.onTouchEvent(event);
        }
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        private float dx, dy; // x,y relative to view

        @Override
        public boolean onDown(MotionEvent e) {
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
            move(real, 1.0f);
            coordinateRealToVirtual(real, virtual_coordinate);

            if (fadeout_animation.isStarted()) {
                fadeout_animation.cancel();
            }
            fadeout_animation.start();

            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            Context context = getContext();
            Utility.GoHome(context);
            Utility.LockScreen(context);
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            Utility.GoHome(getContext());
            return true;
        }

        @Override
        public void onLongPress(MotionEvent e) {
            Utility.OpenRecentActivity(getContext());
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
                move(real);
                screen_rotation = rotation;
            }
        }
    }

    static final int VIRTUAL_SCREEN_DIMENSION = 100000;

    private void coordinateRealToVirtual(Point real, Point virtual) {
        int rotation = window_manager.getDefaultDisplay().getRotation();
        Point frame_size = new Point();
        window_manager.getDefaultDisplay().getSize(frame_size);
        frame_size.y -= status_bar_height;

        switch (rotation) {
            case Surface.ROTATION_90:
                virtual.x = VIRTUAL_SCREEN_DIMENSION * (frame_size.y - real.y) / frame_size.y;
                virtual.y = VIRTUAL_SCREEN_DIMENSION * real.x / frame_size.x;
                break;
            case Surface.ROTATION_180:
                virtual.x = VIRTUAL_SCREEN_DIMENSION * (frame_size.x - real.x) / frame_size.x;
                virtual.y = VIRTUAL_SCREEN_DIMENSION * (frame_size.y - real.y) / frame_size.y;
                break;
            case Surface.ROTATION_270:
                virtual.x = VIRTUAL_SCREEN_DIMENSION * real.y / frame_size.y;
                virtual.y = VIRTUAL_SCREEN_DIMENSION * (frame_size.x - real.x) / frame_size.x;
                break;
            default:
                virtual.x = VIRTUAL_SCREEN_DIMENSION * real.x / frame_size.x;
                virtual.y = VIRTUAL_SCREEN_DIMENSION * real.y / frame_size.y;
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
                real.x = (frame_size.x * virtual.y / VIRTUAL_SCREEN_DIMENSION);
                real.y = (frame_size.y - frame_size.y * virtual.x / VIRTUAL_SCREEN_DIMENSION);
                break;
            case Surface.ROTATION_180:
                real.x = (frame_size.x - frame_size.x * virtual.x / VIRTUAL_SCREEN_DIMENSION);
                real.y = (frame_size.y - frame_size.y * virtual.y / VIRTUAL_SCREEN_DIMENSION);
                break;
            case Surface.ROTATION_270:
                real.x = (frame_size.x - frame_size.x * virtual.y / VIRTUAL_SCREEN_DIMENSION);
                real.y = (frame_size.y * virtual.x / VIRTUAL_SCREEN_DIMENSION);
                break;
            default:
                real.x = (frame_size.x * virtual.x / VIRTUAL_SCREEN_DIMENSION);
                real.y = frame_size.y * virtual.y / VIRTUAL_SCREEN_DIMENSION;
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
