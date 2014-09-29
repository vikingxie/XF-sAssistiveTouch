package com.viking.xfat;

import android.content.Context;
import android.graphics.drawable.ScaleDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ToggleButton;

public class StartButton extends ToggleButton {
    public StartButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public StartButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StartButton(Context context) {
        super(context);
    }

    @Override
    public void setChecked(boolean checked) {
        setBackground(getResources().getDrawable(checked ? R.drawable.start_button_on : R.drawable.start_button_off));
        super.setChecked(checked);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (MotionEvent.ACTION_DOWN == event.getAction()) {
            ScaleDrawable bg = (ScaleDrawable) getResources().getDrawable(isChecked() ? R.drawable.start_button_on_s : R.drawable.start_button_off_s);
            bg.setLevel(1);
            setBackground(bg);
        }

        return super.onTouchEvent(event);
    }
}
