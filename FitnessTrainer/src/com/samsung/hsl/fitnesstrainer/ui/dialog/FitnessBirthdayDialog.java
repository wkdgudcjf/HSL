package com.samsung.hsl.fitnesstrainer.ui.dialog;

import com.samsung.hsl.fitnesstrainer.R;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;

public class FitnessBirthdayDialog extends FitnessFontDialog implements android.view.View.OnClickListener
{
	
	private static final String tag = FitnessWeightDialog.class.getName();
	
	Context context;
	OnDismissListener mListener = null;
	ImageButton mConfirm;
	ImageButton mCancel;
	ImageButton mUpper1;
	ImageButton mUpper2;
	ImageButton mUpper3;
	ImageButton mBelow1;
	ImageButton mBelow2;
	ImageButton mBelow3;
	EditText mYear; 
	EditText mMonth;
	EditText mDay;
	int year;
	int month;
	int day;
	
	public FitnessBirthdayDialog(Context context,OnDismissListener listener)
	{
		super(context);
		this.context = context;
		mListener = listener;
		// TODO Auto-generated constructor stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_fitness_birthday);
		getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		mConfirm = (ImageButton)findViewById(R.id.fitness_sign_up_popup_birth_confirm_btn);
		mCancel = (ImageButton)findViewById(R.id.fitness_sign_up_popup_birth_cancel_btn);
		mUpper1 = (ImageButton)findViewById(R.id.fitness_sign_up_popup_birth_1_triangle_upper);
		mUpper2 = (ImageButton)findViewById(R.id.fitness_sign_up_popup_birth_2_triangle_upper);
		mUpper3 = (ImageButton)findViewById(R.id.fitness_sign_up_popup_birth_3_triangle_upper);
		mBelow1 = (ImageButton)findViewById(R.id.fitness_sign_up_popup_birth_1_triangle_below);
		mBelow2 = (ImageButton)findViewById(R.id.fitness_sign_up_popup_birth_2_triangle_below);
		mBelow3 = (ImageButton)findViewById(R.id.fitness_sign_up_popup_birth_3_triangle_below);
		mYear = (EditText)findViewById(R.id.fitness_sign_up_popup_birth_1_text);
		mMonth = (EditText)findViewById(R.id.fitness_sign_up_popup_birth_2_text);
		mDay = (EditText)findViewById(R.id.fitness_sign_up_popup_birth_3_text);
		mConfirm.setOnClickListener(this);
		mCancel.setOnClickListener(this);
		mUpper1.setOnClickListener(this);
		mUpper2.setOnClickListener(this);
		mUpper3.setOnClickListener(this);
		mBelow1.setOnClickListener(this);
		mBelow2.setOnClickListener(this);
		mBelow3.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.fitness_sign_up_popup_birth_confirm_btn:
			year = Integer.parseInt(mYear.getText().toString());
			month = Integer.parseInt(mMonth.getText().toString());
			day = Integer.parseInt(mDay.getText().toString());
			if(mListener!=null){
				mListener.onDismiss(this);
			}
			dismiss();
			break;

		case R.id.fitness_sign_up_popup_birth_cancel_btn:
			dismiss();
			break;
		case R.id.fitness_sign_up_popup_birth_1_triangle_upper:
				mYear.setText(Integer.toString(Integer.parseInt(mYear.getText().toString())+1));
				year++;
			break;
		case R.id.fitness_sign_up_popup_birth_2_triangle_upper:
				mMonth.setText(Integer.toString(Integer.parseInt(mMonth.getText().toString())+1));
				month++;
			break;
		case R.id.fitness_sign_up_popup_birth_3_triangle_upper:
				mDay.setText(Integer.toString(Integer.parseInt(mDay.getText().toString())+1));
				day++;
			break;
		case R.id.fitness_sign_up_popup_birth_1_triangle_below:
				mYear.setText(Integer.toString(Integer.parseInt(mYear.getText().toString())-1));
				year--;
			break;
		case R.id.fitness_sign_up_popup_birth_2_triangle_below:
			mMonth.setText(Integer.toString(Integer.parseInt(mMonth.getText().toString())-1));
				month--;
			break;
		case R.id.fitness_sign_up_popup_birth_3_triangle_below:
			mDay.setText(Integer.toString(Integer.parseInt(mDay.getText().toString())-1));
				day--;
			break;
		}
	}

	public void setDate(int year,int month,int day)
	{
		this.year = year;
		this.month = month;
		this.day = day;
		mYear.setText(Integer.toString(year));
		mMonth.setText(Integer.toString(month));
		mDay.setText(Integer.toString(day));
	}
	public int getYear() {
		return year;
	}
	public int getMonth() {
		return month;
	}
	public int getDay() {
		return day;
	}
}
