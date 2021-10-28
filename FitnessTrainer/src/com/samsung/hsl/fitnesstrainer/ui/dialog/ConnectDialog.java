package com.samsung.hsl.fitnesstrainer.ui.dialog;

import com.samsung.hsl.fitnesstrainer.R;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;

public class ConnectDialog extends Dialog implements android.view.View.OnClickListener
{
	private static final String tag = ConnectDialog.class.getName();
	
	Context context;
	OnDismissListener mListener = null;
	ImageButton mBle;
	ImageButton mCancel;
	ImageButton mWifi;
	int result; //1 = ble , 2 = wifi , 3 = cancel
	public ConnectDialog(Context context,OnDismissListener listener)
	{
		super(context);
		this.context = context;
		mListener = listener;
		// TODO Auto-generated constructor stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_connect);
		getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		mBle = (ImageButton)findViewById(R.id.fitness_popup_ble_icon);
		mWifi = (ImageButton)findViewById(R.id.fitness_popup_connect_btn);
		mCancel = (ImageButton)findViewById(R.id.fitness_popup_wifi_icon);
		mBle.setOnClickListener(this);
		mWifi.setOnClickListener(this);
		mCancel.setOnClickListener(this);
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.fitness_popup_ble_icon:
			result = 1;
			if(mListener!=null){
				mListener.onDismiss(this);
			}
			dismiss();
			break;

		case R.id.fitness_popup_wifi_icon:
			result = 2;
			if(mListener!=null){
				mListener.onDismiss(this);
			}
			dismiss();
			break;
		case R.id.fitness_popup_connect_btn:
			result = 3;
			if(mListener!=null){
				mListener.onDismiss(this);
			}
			dismiss();
			break;
		}
	}
	
	public int getResult()
	{
		return result;
	}
}
