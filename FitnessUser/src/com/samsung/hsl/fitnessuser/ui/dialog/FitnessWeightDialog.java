package com.samsung.hsl.fitnessuser.ui.dialog;

import com.samsung.hsl.fitnessuser.R;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;


public class FitnessWeightDialog extends FitnessFontDialog implements android.view.View.OnClickListener
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
	EditText mWeight1; 
	EditText mWeight2;
	float weight;
	public FitnessWeightDialog(Context context,OnDismissListener listener)
	{
		super(context);
		this.context = context;
		mListener = listener;
		// TODO Auto-generated constructor stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_fitness_weight);
		getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		mConfirm = (ImageButton)findViewById(R.id.fitness_sign_up_popup_weight_confirm_btn);
		mCancel = (ImageButton)findViewById(R.id.fitness_sign_up_popup_weight_cancel_btn);
		mUpper1 = (ImageButton)findViewById(R.id.fitness_sign_up_popup_weight_1_triangle_upper);
		mUpper2 = (ImageButton)findViewById(R.id.fitness_sign_up_popup_weight_2_triangle_upper);
		mBelow1 = (ImageButton)findViewById(R.id.fitness_sign_up_popup_weight_1_triangle_below);
		mBelow2 = (ImageButton)findViewById(R.id.fitness_sign_up_popup_weight_2_triangle_below);
		mWeight1 = (EditText)findViewById(R.id.fitness_sign_up_popup_weight_1_text);
		mWeight2 = (EditText)findViewById(R.id.fitness_sign_up_popup_weight_2_text);
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
		case R.id.fitness_sign_up_popup_weight_confirm_btn:
			int weight1 = Integer.parseInt(mWeight1.getText().toString());
			int weight2 = Integer.parseInt(mWeight2.getText().toString());
			weight = (float) ((float)weight1 + (float)weight2/10.0);
			if(mListener!=null){
				mListener.onDismiss(this);
			}
			dismiss();
			break;

		case R.id.fitness_sign_up_popup_weight_cancel_btn:
			dismiss();
			break;
		case R.id.fitness_sign_up_popup_weight_1_triangle_upper:
			if(Integer.parseInt(mWeight1.getText().toString())<=999){
				mWeight1.setText(Integer.toString(Integer.parseInt(mWeight1.getText().toString())+1));
				weight+=1;
			}
			break;
		case R.id.fitness_sign_up_popup_weight_2_triangle_upper:
			if(Integer.parseInt(mWeight2.getText().toString())<=9){
				mWeight2.setText(Integer.toString(Integer.parseInt(mWeight2.getText().toString())+1));
				weight+=0.1;
			}
			break;
		case R.id.fitness_sign_up_popup_weight_1_triangle_below:
			if(Integer.parseInt(mWeight1.getText().toString())>0){
				mWeight1.setText(Integer.toString(Integer.parseInt(mWeight1.getText().toString())-1));
				weight-=1;
			}
			break;
		case R.id.fitness_sign_up_popup_weight_2_triangle_below:
			if(Integer.parseInt(mWeight2.getText().toString())>0){
				mWeight2.setText(Integer.toString(Integer.parseInt(mWeight2.getText().toString())-1));
				weight-=0.1;
			}
			break;
		}
	}

	public void setWeight(float weight)
	{
		this.weight = weight;
		int weight1 = (int)weight;
		int weight2 = (int)((weight - (float)weight1)*10.0);
		mWeight1.setText(Integer.toString(weight1));
		mWeight2.setText(Integer.toString(weight2));
	}
	
	public float getWeight()
	{
		return weight;
	}
}
