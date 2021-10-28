package com.samsung.hsl.fitnesstrainer.ui;
import com.samsung.hsl.fitnesstrainer.service.FitnessFontUtils;

import android.support.v4.app.FragmentActivity;
import android.view.ViewGroup;

public class FitnessFontActivity extends FragmentActivity {
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		FitnessFontUtils.setViewGroupFont((ViewGroup)findViewById(android.R.id.content));
	}
}
