package com.viking.xfat;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;

/**
 * Created by Administrator on 2017/2/28.
 */
public class CFloatView extends ImageView implements IFloatView {
    protected int status_bar_height = 0;
    protected LayoutParams layout_params = null;
    protected WindowManager window_manager = null;
    private boolean show = false;

    public CFloatView(Context context) {
        super(context);

        status_bar_height = Utility.GetStatusBarHeight(context);

        window_manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        layout_params = new LayoutParams();
        layout_params.type = LayoutParams.TYPE_SYSTEM_ALERT;
        layout_params.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL | LayoutParams.FLAG_NOT_FOCUSABLE;
        layout_params.format = PixelFormat.RGBA_8888;
        layout_params.gravity = Gravity.TOP | Gravity.START;
        layout_params.x = 0;
        layout_params.y = 0;
        layout_params.width = LayoutParams.WRAP_CONTENT;
        layout_params.height = LayoutParams.WRAP_CONTENT;
        layout_params.alpha = 1.0f;
    }

    public void show() {
        if (!show) {
            window_manager.addView(this, layout_params);
            show = true;
        } else {
            update();
        }
    }

    public void hide() {
        if (show) {
            window_manager.removeView(this);
            show = false;
        }
    }

    public void update() {
        window_manager.updateViewLayout(this, layout_params);
    }

    public LayoutParams getLayoutParams() {
        return layout_params;
    }

}
