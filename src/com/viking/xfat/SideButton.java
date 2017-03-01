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
    public SideButton(Context context, Drawable drawable) {
        super(context);
        setImageDrawable(drawable);
    }
}
