package com.samsung.hsl.fitnessuser.ui;
import com.samsung.hsl.fitnessuser.R;
import com.samsung.hsl.fitnessuser.service.FitnessPreference;
import com.samsung.hsl.fitnessuser.service.FitnessUserApplication;
import com.samsung.hsl.fitnessuser.sqlite.User;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class FitnessIntroLoginActivity extends FitnessFontActivity implements OnClickListener{
	private static final String tag = FitnessIntroLoginActivity.class.getName();
	TextView mUserPassword;
	ImageView mSavePassword;
	FitnessPreference mFitnessPreference;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.layout_fitness_intro_login);
		
		mUserPassword = (TextView)findViewById(R.id.fitness_intro_login_password_box_edittext);
		mSavePassword = (ImageView)findViewById(R.id.fitness_intro_login_auto_login_imagebutton);
		
		mFitnessPreference = new FitnessPreference(this);
		
		if(mFitnessPreference.getValue(FitnessPreference.PREFERENCE_SAVE_PASSWORD, false)){
			mUserPassword.setText(mFitnessPreference.getValue(FitnessPreference.PREFERENCE_PASSWORD, ""));
			mSavePassword.setSelected(true);
		}

		mUserPassword.requestFocus();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId())
		{
		case R.id.fitness_intro_login_sign_in_imagebutton:
			if(checkPassword()){
				savePassword();
				goToMain();
			}
			break;
		case R.id.fitness_intro_login_sign_up_imagebutton:
			goToSignUp();
			break;
		case R.id.fitness_intro_login_for_get_password_imagebutton:
			goToForgetPassword();
			break;
		case R.id.fitness_intro_login_keep_login_imageview:
		case R.id.fitness_intro_login_auto_login_imagebutton:
			mSavePassword.setSelected(!mSavePassword.isSelected());
			break;
		}
	}
	
	boolean checkPassword(){
		User user = new User();
		user.password = mUserPassword.getText().toString();
		((FitnessUserApplication)getApplication()).getFitnessService().getFitnessSQLiteManager().select(user);
		
		if(user.id==0){
			Toast.makeText(this, getResources().getString(R.string.string_fitness_intro_login_wrong_password), Toast.LENGTH_SHORT).show();
			return false;
		}else {
			((FitnessUserApplication)getApplication()).getFitnessService().setCurrentUser(user);
			return true;
		}
	}
	
	void savePassword(){
		User user = ((FitnessUserApplication)getApplication()).getFitnessService().getFitnessManager().getUser();
		if(mSavePassword.isSelected()){
			mFitnessPreference.put(FitnessPreference.PREFERENCE_SAVE_PASSWORD, true);
			mFitnessPreference.put(FitnessPreference.PREFERENCE_PASSWORD, user.password);
		}else {
			mFitnessPreference.put(FitnessPreference.PREFERENCE_SAVE_PASSWORD, false);
			mFitnessPreference.put(FitnessPreference.PREFERENCE_PASSWORD, "");
		}
	}
	
	void goToMain(){
		Intent intent = new Intent(this,FitnessMainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
	}
	
	void goToSignUp(){
		Intent intent = new Intent(this,FitnessSignUpActivity.class);
		startActivity(intent);
	}
	
	void goToForgetPassword(){
		Intent intent = new Intent(this,FitnessFindPasswordActivity.class);
		startActivity(intent);
	}
}
