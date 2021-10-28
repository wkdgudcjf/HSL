package com.samsung.hsl.fitnessuser.ui;
import com.samsung.hsl.fitnessuser.R;
import com.samsung.hsl.fitnessuser.service.FitnessUserApplication;
import com.samsung.hsl.fitnessuser.sqlite.FitnessSQLiteManager;
import com.samsung.hsl.fitnessuser.sqlite.User;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.View;

public class FitnessMyInfoActivity extends FitnessSignUpActivity {
	private static final String tag = FitnessSignUpActivity.class.getName();
	User mUser;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);

		mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
		mTopBarTitleText.setText(getResources().getString(R.string.string_fitness_my_info_title));
		mTopBarLeftButton.setVisibility(View.VISIBLE);
		mTopBarRightButton.setVisibility(View.VISIBLE);
		mConfirm.setText(getResources().getString(R.string.string_fitness_my_info_button));
		mUser = ((FitnessUserApplication)getApplication()).getFitnessService().getFitnessManager().getUser();
		// 회원가입 버튼 바까줘야함
		loadUserInfo();
	}
	
	void loadUserInfo(){
		
		mUserEmail.setText(mUser.email);
		mUserEmail.setEnabled(false);
		mUserPassword.setText(mUser.password);
		mUserPasswordConfirm.setText(mUser.password);
		mUserName.setText(mUser.name);
		
		//생년월일
		mBirth.setText(mUser.birthday);
		
		//성별
		if(mUser.gender.equals(getResources().getString(R.string.string_fitness_sign_up_man))){
			mUserManGender.setSelected(true);
			mUserWomenGender.setSelected(false);
		}else{
			mUserManGender.setSelected(false);
			mUserWomenGender.setSelected(true);
		}
		
		// 키
		mHeight.setText(String.valueOf(mUser.height));
		
		// 몸무게
		mWeight.setText(String.valueOf(mUser.weight));
		
		// 사진
		mPictureData = mUser.picture;
		if(mPictureData!=null){
			Bitmap bitmap = BitmapFactory.decodeByteArray(mPictureData, 0, mPictureData.length);
			BitmapDrawable drawable = new BitmapDrawable(bitmap);
			mPicture.setImageDrawable(drawable);
		}
		
	}
	
	@Override
	void signUp() {
		// TODO Auto-generated method stub
		FitnessSQLiteManager db = ((FitnessUserApplication) getApplication()).getSQLiteManager();

		mUser.password = mUserPassword.getText().toString();
		mUser.name = mUserName.getText().toString();
		mUser.birthday = mBirth.getText().toString();
		if(mUserManGender.isSelected()){
			mUser.gender = getResources().getString(R.string.string_fitness_sign_up_man);
		}else{
			mUser.gender = getResources().getString(R.string.string_fitness_sign_up_women);
		}
		try{
			mUser.weight = Float.valueOf(mWeight.getText().toString());
			mUser.height = Float.valueOf(mHeight.getText().toString());
		}catch(Exception e){
			mUser.weight = 0;
			mUser.height = 0;
		}
		mUser.picture = mPictureData;
		db.update(mUser);
		((FitnessUserApplication)getApplication()).getFitnessService().modifyUsermFitnessUserData(mUser);
		goToMain();
	}
	
	void goToMain(){
		Intent intent = new Intent(this,FitnessMainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
	}
}
