package com.viking.xfat;

import android.animation.TimeAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;

public class FloatButton extends ImageView {
    static final long animation_frame_delay = 50; // ms

    int status_bar_height = 0;
    float button_alpha = 0.25f;
    int button_height, button_width;
    float stick_distance_x, stick_distance_y;
    Point virtual_coordinate = new Point();
    Drawable button_image = null;
    WindowManager.LayoutParams layout_params = null;
    WindowManager window_manager = null;
    ValueAnimator fadeout_animation = null;
    TimeAnimator stick_animation = null;
    int stick_animation_speed = 0;
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
        stick_animation.start(); //fadeout_animation.start();
        orientation_listener.enable();
    }

    public void hide() {
        orientation_listener.disable();
        stick_animation.cancel();
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

        stick_animation_speed = Utility.DIP2PX(getContext(), 5);
        stick_animation = new TimeAnimator();
        stick_animation.setTimeListener(new TimeAnimator.TimeListener() {
            Point real = new Point();

            @Override
            public void onTimeUpdate(TimeAnimator animation, long totalTime, long deltaTime) {
                coordinateVirtualToReal(virtual_coordinate, real);
                if (stickStep(real)) {
                    move(real, 1.0f);
                } else {
                    coordinateRealStickEdge(real);
                    stick_animation.cancel();
                    if (fadeout_animation.isStarted()) {
                        fadeout_animation.cancel();
                    }
                    fadeout_animation.start();
                }
                coordinateRealToVirtual(real, virtual_coordinate);
                Log.i("XFAT", String.format("aa x: %d y:%d", real.x, real.y));
                coordinateVirtualToReal(virtual_coordinate, real);
                Log.i("XFAT", String.format("bb x: %d y:%d", real.x, real.y));
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

                //Log.i("XFAT", String.format("x:%d y:%d h:%d v:%d", real.x, real.y, h_speed, v_speed));

                real.x += h_speed;
                real.y += v_speed;

                return true;
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
            switch (event.getAction()) {
                case  MotionEvent.ACTION_UP:
                    fadeout_animation.cancel();
                    stick_animation.start();
                    break;

                case MotionEvent.ACTION_DOWN:
                    stick_animation.cancel();
                    fadeout_animation.cancel();
            }
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

    static final double VIRTUAL_SCREEN_DIMENSION = 1000000;

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
