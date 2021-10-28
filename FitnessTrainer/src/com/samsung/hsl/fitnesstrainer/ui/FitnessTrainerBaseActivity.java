package com.samsung.hsl.fitnesstrainer.ui;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.samples.apps.iosched.ui.widget.ScrimInsetsFrameLayout;
import com.samsung.hsl.fitnesstrainer.R;
import com.samsung.hsl.fitnesstrainer.service.FitnessBroadcastService;
import com.samsung.hsl.fitnesstrainer.service.FitnessTrainerApplication;

public class FitnessTrainerBaseActivity extends FitnessBaseActivity {
	RelativeLayout mlogoutTouchLayout;
	RelativeLayout mUserListTouchLayout;
	RelativeLayout mStartTouchLayout;
	RelativeLayout mEndTouchLayout;
	@Override
	protected void onCreate(Bundle savedInstanceState,int layout) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState, layout);
	}
	
	@Override
	protected void initSlideMenu() {
		mDrawerLayout = (DrawerLayout) findViewById(R.id.layout_main);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mDrawerLayout.setDrawerListener(mDrawerListener);
        mDrawerLayout.setFitsSystemWindows(true);
        mDrawerLayout.setFocusableInTouchMode(false);
        
		mDrawerFragment = (ScrimInsetsFrameLayout) findViewById(R.id.navigation_framelayout);
		mlogoutTouchLayout = (RelativeLayout) findViewById(R.id.fitness_slide_menu_logout_layout);
		mUserListTouchLayout = (RelativeLayout) findViewById(R.id.fitness_slide_menu_user_list_layout);
		mStartTouchLayout = (RelativeLayout) findViewById(R.id.fitness_slide_menu_start_layout);
		mEndTouchLayout = (RelativeLayout) findViewById(R.id.fitness_slide_menu_end_layout);

		mlogoutTouchLayout.setOnClickListener(mSlideMenuClickListener);
		mUserListTouchLayout.setOnClickListener(mSlideMenuClickListener);
		mStartTouchLayout.setOnClickListener(mSlideMenuClickListener);
		mEndTouchLayout.setOnClickListener(mSlideMenuClickListener);
		
		mDrawerWelcomeText = (TextView) findViewById(R.id.fitness_slide_menu_bar_welcom_textview);
		mLogoutDialog = new LogoutDialog(this, mLogoutDismissListener);
	}
	
	/** @brief 종료 리스너 */
	OnDismissListener mLogoutDismissListener = new OnDismissListener() {

		@Override
		public void onDismiss(DialogInterface arg0) {
			// TODO Auto-generated method stub
			mDrawerLayout.closeDrawer(mDrawerFragment);
			finish();
		}

	};

	/** @brief 네비게이션 버튼 클릭 리스너 */
	OnClickListener mSlideMenuClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.fitness_slide_menu_logout_layout:
				mLogoutDialog.show();
				break;
			case R.id.fitness_slide_menu_start_layout:
				if(!((FitnessTrainerApplication) getApplication()).getFitnessService().mIsExerciseStart)
				{
					((FitnessTrainerApplication) getApplication()).getFitnessService().mIsExerciseStart = true;
					((FitnessTrainerApplication) getApplication()).getFitnessService().startExercise();
				}
				else
				{
					String str = getResources().getString(R.string.string_fitness_trainer_slide_menu_end_text);
					Toast.makeText(FitnessTrainerBaseActivity.this, str, Toast.LENGTH_SHORT).show(); 
				}
				mDrawerLayout.closeDrawer(mDrawerFragment);
				//운동 시작.
				break;
			case R.id.fitness_slide_menu_end_layout:
				if(((FitnessTrainerApplication) getApplication()).getFitnessService().mIsExerciseStart)
				{
					((FitnessTrainerApplication) getApplication()).getFitnessService().mIsExerciseStart = false;
					((FitnessTrainerApplication) getApplication()).getFitnessService().stopExercise();
				}
				else
				{
					String str = getResources().getString(R.string.string_fitness_trainer_slide_menu_start_text);
					Toast.makeText(FitnessTrainerBaseActivity.this, str, Toast.LENGTH_SHORT).show(); 
				}
				mDrawerLayout.closeDrawer(mDrawerFragment);
				//운동 종료.
				break;
			case R.id.fitness_slide_menu_user_list_layout:
				mDrawerLayout.closeDrawer(mDrawerFragment);
				Intent intent = new Intent(FitnessTrainerBaseActivity.this,	FitnessTrainerUserListActivity.class);
				startActivity(intent);
				break;
				// 목표관리
			}
		}
	};
	
	boolean mDrawerOpened = false;
	DrawerListener mDrawerListener = new DrawerListener(){

		@Override
		public void onDrawerClosed(View arg0)
		{
			// TODO Auto-generated method stub
			mDrawerOpened = false;
		}

		@Override
		public void onDrawerOpened(View arg0)
		{
			// TODO Auto-generated method stub
			mDrawerOpened = true;
		}

		@Override
		public void onDrawerSlide(View arg0, float arg1)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onDrawerStateChanged(int arg0)
		{
			// TODO Auto-generated method stub
		}
		
	};
}
