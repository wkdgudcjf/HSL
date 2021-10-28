package com.samsung.hsl.fitnessuser.ui;
import com.samsung.hsl.fitnessuser.R;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

public class FitnessCalendarLoadingActivity extends Activity{
	private static final String tag = FitnessCalendarLoadingActivity.class.getName();
	
	Handler handler = new Handler();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.layout_fitness_calendar_loading);
		
		AnimationDrawable animation = (AnimationDrawable)((ImageView)findViewById(R.id.fitness_calendar_loading_imageview)).getBackground();
		animation.start();
		
		handler.postDelayed(new Runnable() {
            @Override
            public void run() {
            	Intent intent = new Intent(FitnessCalendarLoadingActivity.this,FitnessCalendarActivity.class);
    			startActivity(intent);
                finish(); 
            }
        }, 100);	
	}
}
