package com.viking.xfat;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

public class HomeActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        initComponent();
    }

    private void initComponent() {
        ToggleButton startButton = (ToggleButton)findViewById(R.id.startButton);
        startButton.setChecked(Utility.IsFloatServiceRunning(HomeActivity.this));
        startButton.setBackground(getResources().getDrawable(startButton.isChecked() ? R.drawable.start_button_on_t : R.drawable.start_button_off_t));
        startButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                buttonView.setBackground(getResources().getDrawable(isChecked ? R.drawable.start_button_on_t : R.drawable.start_button_off_t));

                Utility.ToggleFloatService(HomeActivity.this);
            }
        });
        startButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEvent.ACTION_DOWN == event.getAction()) {
                    v.setBackground(getResources().getDrawable(((ToggleButton) v).isChecked() ? R.drawable.start_button_on_ts : R.drawable.start_button_off_ts));
                }
                return false;
            }
        });
    }
}