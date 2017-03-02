package com.viking.xfat;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.WindowManager;

/**
 * Created by Administrator on 2017/2/28.
 */
//public class SideButton extends ImageView implements IFloatView {
public class SideButton extends CFloatView {
    static final String TAG = "SideButton";
    private ISideButtonAction action = null;
    public SideButton(Context context, Drawable drawable, ISideButtonAction action) {
        super(context);
        setImageDrawable(drawable);
        this.action = action;
    }

    public void action() {
        action.action();
    }

    public boolean within(int x, int y) {
        int w = getDrawable().getMinimumWidth();
        int h = getDrawable().getMinimumHeight();
        WindowManager.LayoutParams para = getLayoutParams();

        return para.x <= x && x <= para.x + w && para.y <= y && y <= para.y + h;
    }
}
