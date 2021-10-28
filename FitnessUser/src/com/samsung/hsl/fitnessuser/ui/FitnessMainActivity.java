package com.samsung.hsl.fitnessuser.ui;
import com.samsung.hsl.fitnessuser.R;
import com.samsung.hsl.fitnessuser.service.FitnessBroadcastService;
import com.samsung.hsl.fitnessuser.service.FitnessBroadcastService.FitnessBroadcastListener;
import com.samsung.hsl.fitnessuser.service.FitnessManager;
import com.samsung.hsl.fitnessuser.service.FitnessPreference;
import com.samsung.hsl.fitnessuser.service.FitnessUserApplication;
import com.samsung.hsl.fitnessuser.service.FitnessUtils;
import com.samsung.hsl.fitnessuser.sqlite.FitnessData;
import com.samsung.hsl.fitnessuser.sqlite.User;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;


public class FitnessMainActivity extends FitnessBaseActivity implements OnClickListener {
	private static final String tag = FitnessMainActivity.class.getName();
	private static final int PAGER_HEARTRATE = 0x00;
	private static final int PAGER_GRAPH = 0x01;
	
	FitnessBaseViewPager mViewPager;
	PagerAdapter mPagerAdapter;
	Fragment mHeartrateFragment;
	Fragment mGraphFragment;
	
	TextView mHeartrateValue;
	TextView mCalorieValue;
	TextView mSkinTemperatureValue;
	TextView mStrengthValue;
	RelativeLayout mStrengthLayout;
	
	ImageView mSubTopBarLeftImage;
	ImageView mSubTopBarRightImage;
	
	FitnessUserApplication mFitnessApplication;
	FitnessPreference mFitnessPreference;
	
	int strength;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState,R.layout.layout_fitness_main);
		
		mTopBarTitleText.setText(getResources().getString(R.string.string_fitness_main_title));
		mTopBarLeftButton.setVisibility(View.VISIBLE);
		mTopBarRightButton.setVisibility(View.VISIBLE);
		
		mViewPager = (FitnessBaseViewPager)findViewById(R.id.fitness_main_main_board_pager);
		mPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setPagingEnable(false);
		
		mHeartrateFragment = new FitnessMainDonutGraphFragment();
		mGraphFragment = new FitnessMainLineGraphFragment();
		
		// 값 객체
		mHeartrateValue = (TextView)findViewById(R.id.fitness_main_range_heartrate_value_textview);
		mCalorieValue = (TextView)findViewById(R.id.fitness_main_range_calorie_value_textview);
		mSkinTemperatureValue = (TextView)findViewById(R.id.fitness_main_range_temperature_value_textview);
		mStrengthValue = (TextView)findViewById(R.id.fitness_main_range_aim_value_textview);
		mStrengthLayout = (RelativeLayout)findViewById(R.id.fitness_main_range_aim_layout);
		mStrengthLayout.setOnClickListener(this);
		
		mSubTopBarLeftImage = (ImageView)findViewById(R.id.fitness_main_sub_top_bar_left_border_imageview);
		mSubTopBarRightImage = (ImageView)findViewById(R.id.fitness_main_sub_top_bar_right_border_imageview);
		
		mFitnessApplication = ((FitnessUserApplication)getApplication());
		mFitnessPreference = new FitnessPreference(this);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		strength = mFitnessPreference.getStrength(mFitnessApplication.getFitnessService().getCurrentUser(), FitnessUtils.STRENGTH_WARM_UP);
		setStrengthText(strength);
		mFitnessBroadcastService = ((FitnessUserApplication)getApplication()).mFitnessBroadcastService;
		mFitnessBroadcastService.addBroadcastObserver(mFitnessBroadcastListener);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mFitnessBroadcastService.removeBoradcastObserver(mFitnessBroadcastListener);
	}
	
	private class MainPagerAdapter extends FragmentPagerAdapter{
		public MainPagerAdapter(FragmentManager fm) {
			super(fm);
			// TODO Auto-generated constructor stub
		}

		@Override
		public Fragment getItem(int index) {
			// TODO Auto-generated method stub
			Fragment fm = mHeartrateFragment;
			switch (index) {
			case PAGER_HEARTRATE:
				fm = mHeartrateFragment;
				break;

			case PAGER_GRAPH:
				fm = mGraphFragment;
				break;
			}
			return fm;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 2;
		}
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent;
		switch (v.getId()) {
		case R.id.fitness_main_sub_top_bar_left_layout:
			mViewPager.setCurrentItem(PAGER_HEARTRATE);
			mSubTopBarLeftImage.setVisibility(View.VISIBLE);
			mSubTopBarRightImage.setVisibility(View.INVISIBLE);
			break;

		case R.id.fitness_main_sub_top_bar_right_layout:
			mViewPager.setCurrentItem(PAGER_GRAPH);
			mSubTopBarLeftImage.setVisibility(View.INVISIBLE);
			mSubTopBarRightImage.setVisibility(View.VISIBLE);
			break;
			
		case R.id.fitness_main_below_menu_aim_manager_imagebutton:
			intent = new Intent(this,FitnessAimManagerActivity.class);
			startActivity(intent);
			break;
			
		case R.id.fitness_main_below_menu_calendar_imagebutton:
			intent = new Intent(this,FitnessCalendarActivity.class);
			startActivity(intent);
			break;
			
		case R.id.fitness_main_below_menu_my_info_imagebutton:
			intent = new Intent(this,FitnessMyInfoActivity.class);
			startActivity(intent);
			break;
		case R.id.fitness_main_range_aim_layout:
			strength = (strength+1)%4;
			mFitnessPreference.putStrength(mFitnessApplication.getFitnessService().getCurrentUser(), strength);
			setStrengthText(strength);
			break;
		}
	}
	
	FitnessBroadcastService mFitnessBroadcastService;
	
	void setStrengthText(int strength){
		if(strength==FitnessUtils.STRENGTH_HIGH)mStrengthValue.setText(getResources().getString(R.string.string_fitness_main_range_aim_high));
		else if(strength==FitnessUtils.STRENGTH_COMMON)mStrengthValue.setText(getResources().getString(R.string.string_fitness_main_range_aim_common));
		else if(strength==FitnessUtils.STRENGTH_FAT_BUNNING)mStrengthValue.setText(getResources().getString(R.string.string_fitness_main_range_aim_start));
		else if(strength==FitnessUtils.STRENGTH_WARM_UP)mStrengthValue.setText(getResources().getString(R.string.string_fitness_main_range_aim_ready));
	}
	
	FitnessBroadcastListener mFitnessBroadcastListener = new FitnessBroadcastListener(){

		@Override
		public void onReceiveFitnessData(final User user, final FitnessData data) {
			if(mFitnessApplication.getFitnessService().getCurrentUser().equals(user)==false)return;
			
			// TODO Auto-generated method stub
			Runnable runnable = new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
//					if(mFitnessApplication.getFitnessService().getFitnessManager().isOldCheckStableHeartrate())return;
					
					mHeartrateValue.setText(String.valueOf(data.filterHeartrate));
					mCalorieValue.setText(String.valueOf(data.consumeCalorie));
					mSkinTemperatureValue.setText(String.valueOf(data.skinTemperature));
					
					if(data.filterHeartrate>FitnessUtils.karvonen(user, strength)){
						mHeartrateValue.setTextColor(getResources().getColor(R.color.range_color_high));
					}else if(data.filterHeartrate>FitnessUtils.karvonen(user, strength-1)){
						mHeartrateValue.setTextColor(getResources().getColor(R.color.range_color_aim));
					}else{
						mHeartrateValue.setTextColor(getResources().getColor(R.color.range_color_normal));
					}
				}
			};
			
			runOnUiThread(runnable);
		}

		@Override
		public void onReceiveFitnessFall(User user) {
			// TODO Auto-generated method stub
			if(mFitnessApplication.getFitnessService().getCurrentUser().equals(user)==false)return;
		}

		@Override
		public void onChangeAimStrength(User user, int strength) {
			// TODO Auto-generated method stub
			if(mFitnessApplication.getFitnessService().getCurrentUser().equals(user)==false)return;
		}

		@Override
		public void onChangeUserState(final User user, final int state) {
			// TODO Auto-generated method stub
			if(mFitnessApplication.getFitnessService().getCurrentUser().equals(user)==false)return;
			
			Runnable runnable = new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
//					if(mFitnessApplication.getFitnessService().getFitnessManager().isOldCheckStableHeartrate())return;
					
					int color = 0x000000;
					if(state==FitnessManager.STATE_WARM_UP_NEED || state==FitnessManager.STATE_WARM_UP_EXCEED_AIM || state==FitnessManager.STATE_WARM_UP_COMPLETE){
						color = getResources().getColor(R.color.range_color_normal);
					}else if(state==FitnessManager.STATE_SHORT_AIM){
						color = getResources().getColor(R.color.range_color_normal);
					}else if(state==FitnessManager.STATE_ARCHIEVE_AIM){
						color = getResources().getColor(R.color.range_color_aim);
					}else if(state==FitnessManager.STATE_EXCEED_AIM){
						color = getResources().getColor(R.color.range_color_high);
					}
					mHeartrateValue.setTextColor(color);
				}
			};
			
			runOnUiThread(runnable);
		}

		@Override
		public void onChangeUserInfo(User user) {
			// TODO Auto-generated method stub
			
		}
		
	};
	
	
}
