package com.samsung.hsl.fitnessuser.ui.dialog;

import com.samsung.hsl.fitnessuser.service.FitnessFontUtils;

import android.app.Dialog;
import android.content.Context;
import android.view.ViewGroup;

 public class FitnessFontDialog extends Dialog{

	public FitnessFontDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		FitnessFontUtils.setViewGroupFont((ViewGroup)findViewById(android.R.id.content));
	}
} 
