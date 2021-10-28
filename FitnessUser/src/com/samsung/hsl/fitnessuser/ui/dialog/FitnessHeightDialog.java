package com.samsung.hsl.fitnessuser.ui.dialog;

import com.samsung.hsl.fitnessuser.R;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;

public class FitnessHeightDialog extends FitnessFontDialog implements android.view.View.OnClickListener
{
	private static final String tag = FitnessWeightDialog.class.getName();
	
	Context context;
	OnDismissListener mListener = null;
	ImageButton mConfirm;
	ImageButton mCancel;
	ImageButton mUpper1;
	ImageButton mUpper2;
	ImageButton mBelow1;
	ImageButton mBelow2;
	EditText mHeight1; 
	EditText mHeight2;
	float height;
	public FitnessHeightDialog(Context context,OnDismissListener listener)
	{
		super(context);
		this.context = context;
		mListener = listener;
		// TODO Auto-generated constructor stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_fitness_height);
		getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		mConfirm = (ImageButton)findViewById(R.id.fitness_sign_up_popup_height_confirm_btn);
		mCancel = (ImageButton)findViewById(R.id.fitness_sign_up_popup_height_cancel_btn);
		mUpper1 = (ImageButton)findViewById(R.id.fitness_sign_up_popup_height_1_triangle_upper);
		mUpper2 = (ImageButton)findViewById(R.id.fitness_sign_up_popup_height_2_triangle_upper);
		mBelow1 = (ImageButton)findViewById(R.id.fitness_sign_up_popup_height_1_triangle_below);
		mBelow2 = (ImageButton)findViewById(R.id.fitness_sign_up_popup_height_2_triangle_below);
		mHeight1 = (EditText)findViewById(R.id.fitness_sign_up_popup_height_1_text);
		mHeight2 = (EditText)findViewById(R.id.fitness_sign_up_popup_height_2_text);
		mConfirm.setOnClickListener(this);
		mCancel.setOnClickListener(this);
		mUpper1.setOnClickListener(this);
		mUpper2.setOnClickListener(this);
		mBelow1.setOnClickListener(this);
		mBelow2.setOnClickListener(this);
	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.fitness_sign_up_popup_height_confirm_btn:
			int height1 = Integer.parseInt(mHeight1.getText().toString());
			int height2 = Integer.parseInt(mHeight2.getText().toString());
			height = (float) ((float)height1 + (float)height2/10.0);
			if(mListener!=null){
				mListener.onDismiss(this);
			}
			dismiss();
			break;

		case R.id.fitness_sign_up_popup_height_cancel_btn:
			dismiss();
			break;
		case R.id.fitness_sign_up_popup_height_1_triangle_upper:
			if(Integer.parseInt(mHeight1.getText().toString())<=999){
				mHeight1.setText(Integer.toString(Integer.parseInt(mHeight1.getText().toString())+1));
				height+=1;
			}
			break;
		case R.id.fitness_sign_up_popup_height_2_triangle_upper:
			if(Integer.parseInt(mHeight2.getText().toString())<=9){
				mHeight2.setText(Integer.toString(Integer.parseInt(mHeight2.getText().toString())+1));
				height+=0.1;
			}
			break;
		case R.id.fitness_sign_up_popup_height_1_triangle_below:
			if(Integer.parseInt(mHeight1.getText().toString())>0){
				mHeight1.setText(Integer.toString(Integer.parseInt(mHeight1.getText().toString())-1));
				height-=1;
			}
			break;
		case R.id.fitness_sign_up_popup_height_2_triangle_below:
			if(Integer.parseInt(mHeight2.getText().toString())>0){
				mHeight2.setText(Integer.toString(Integer.parseInt(mHeight2.getText().toString())-1));
				height-=0.1;
			}
			break;
		}
	}

	public void setHeight(float height)
	{
		this.height = height;
		int height1 = (int)height;
		int height2 = (int)((height - (float)height1)*10.0);
		mHeight1.setText(Integer.toString(height1));
		mHeight2.setText(Integer.toString(height2));
	}
	
	public float getHeight()
	{
		return height;
	}
}
