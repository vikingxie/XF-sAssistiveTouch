package com.viking.xfat;

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
    float button_x = 0, button_y = 0;
    Drawable button_image = null;
    WindowManager.LayoutParams layout_params = null;
    WindowManager window_manager = null;
    OrientationEventListener orientation_listener = null;

    public FloatButton(Context context) {
        super(context);
        status_bar_height = Utility.GetStatusBarHeight(context);
        button_alpha = PreferenceManager.getDefaultSharedPreferences(context).getFloat("button_alpha", 0.25f);
        window_manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        createDrawable();
        createLayoutParams();
        setOnTouchListener(new TouchListener(context));
        orientation_listener = new OrientationListener(context);
    }

    public void show() {
        window_manager.addView(this, layout_params);
        orientation_listener.enable();
    }

    public void hide() {
        orientation_listener.disable();
        window_manager.removeView(this);
    }

    private void createDrawable() {
        button_image = getContext().getResources().getDrawable(R.drawable.icon);
        setImageDrawable(button_image);
        button_height = button_image.getMinimumHeight();
        button_width = button_image.getMinimumWidth();
        button_x = button_width / 2;
        button_y = button_height / 2;
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

            // Stick to edge
            /*Point screen_size = new Point();
            window_manager.getDefaultDisplay().getSize(screen_size);
            if (x <= stick_distance) {
                x = button_width / 2;
            } else if (x >= screen_size.x - stick_distance) {
                x = screen_size.x - button_width / 2;
            }
            if (y <= stick_distance) {
                y = button_height / 2;
            } else if (y >= screen_size.y - status_bar_height - stick_distance) {
                y = screen_size.y - status_bar_height - button_height / 2;
            }*/

            moveView((int)x, (int)y);

            /*if (view_fadeout.isStarted()) {
                view_fadeout.cancel();
            }
            view_fadeout.start();*/

            translateViewLocation(x, y);

            return true;
        }

        private void moveView(int x, int y) {
            layout_params.x = x - button_width / 2;
            layout_params.y = y - button_height / 2;
            layout_params.alpha = 1.0f;
            window_manager.updateViewLayout(FloatButton.this, layout_params);
        }

        private  void translateViewLocation(float x, float y) {
            int rotation = window_manager.getDefaultDisplay().getRotation();
            Point screen_size = new Point();
            window_manager.getDefaultDisplay().getSize(screen_size);
            float frame_x = screen_size.x;
            float frame_y = screen_size.y - status_bar_height;

            switch (rotation) {
                case Surface.ROTATION_90:
                    button_x = (frame_y - y) / frame_y;
                    button_y = x / frame_x;
                    break;
                case Surface.ROTATION_180:
                    button_x = (frame_x - x) / frame_x;
                    button_y = (frame_y - y) / frame_y;
                    break;
                case Surface.ROTATION_270:
                    button_x = y / frame_y;
                    button_y = (frame_x - x) / frame_x;
                    break;
                default:
                    button_x = x / frame_x;
                    button_y = y / frame_y;
                    break;
            }
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
            Point screen_size = new Point();
            window_manager.getDefaultDisplay().getSize(screen_size);
            int rotation = window_manager.getDefaultDisplay().getRotation();
            if (screen_rotation != rotation) {
                int frame_x = screen_size.x;
                int frame_y = screen_size.y - status_bar_height;
                int x, y;

                switch (rotation) {
                    case Surface.ROTATION_90:
                        x = (int) (frame_x * button_x);
                        y = (int) (frame_y * (1.0f - button_y));
                        break;
                    case Surface.ROTATION_180:
                        x = (int) (frame_x * (1.0f - button_y));
                        y = (int) (frame_y * (1.0f - button_x));
                        break;
                    case Surface.ROTATION_270:
                        x = (int) (frame_x * (1.0f - button_x));
                        y = (int) (frame_y * button_y);
                        break;
                    default:
                        x = (int) (frame_x * button_y);
                        y = (int) (frame_y * button_x);
                        break;
                }

                // Stick to edge
                /*if (x <= stick_distance) {
                    x = view_width / 2;
                } else if (x >= screen_size.x - stick_distance) {
                    x = screen_size.x - view_width / 2;
                }
                if (y <= stick_distance) {
                    y = view_height / 2;
                } else if (y >= screen_size.y - status_bar_height - stick_distance) {
                    y = screen_size.y - status_bar_height - view_height / 2;
                }*/

                relocateView(x, y);
                screen_rotation = rotation;
            }
        }

        private void relocateView(int x, int y) {
            layout_params.x = x - button_width / 2;
            layout_params.y = y - button_height / 2;
            window_manager.updateViewLayout(FloatButton.this, layout_params);
        }
    }
}
