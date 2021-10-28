package com.samsung.hsl.fitnessuser.ui;

import com.samsung.hsl.fitnessuser.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

public class FitnessAimManagerActivity extends FitnessBaseActivity implements OnClickListener {
	private static final String tag = FitnessAimManagerActivity.class.getName();

	public static final String AIM_MANAGER_START_PAGER = "pager";
	public static final int PAGER_STRENGTH = 0x00;
	public static final int PAGER_STABLE_HEARTRATE = 0x01;
	
	FitnessBaseViewPager mViewPager;
	PagerAdapter mPagerAdapter;
	Fragment mStableHeartrateFragment;
	Fragment mStrengthFragment;
	TextView mStableHeartrate;
	TextView mSettingStrength;
	

	ImageView mSubTopBarLeftImage;
	ImageView mSubTopBarRightImage;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState,R.layout.layout_fitness_aim_manager);
		
		mTopBarTitleText.setText(getResources().getString(R.string.string_fitness_aim_manager_title));
		mTopBarLeftButton.setVisibility(View.VISIBLE);
		mTopBarRightButton.setVisibility(View.VISIBLE);
		
		mSettingStrength = (TextView)findViewById(R.id.fitness_main_sub_top_bar_left_textview);
		mSettingStrength.setText(getResources().getString(R.string.string_fitness_aim_manager_subtitle_aim));
		
		mStableHeartrate = (TextView)findViewById(R.id.fitness_main_sub_top_bar_right_textview);
		mStableHeartrate.setText(getResources().getString(R.string.string_fitness_aim_manager_subtitle_heartrate));
				
		mViewPager = (FitnessBaseViewPager)findViewById(R.id.fitness_aim_manager_viewpager);
		mPagerAdapter = new ManagementPagerAdapter(getSupportFragmentManager());
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setPagingEnable(false);
		
		mStableHeartrateFragment = new FitnessAimManagerStableHeartrateFragment();
		mStrengthFragment = new FitnessAimManagerStrengthFragment();
		
		mSubTopBarLeftImage = (ImageView)findViewById(R.id.fitness_main_sub_top_bar_left_border_imageview);
		mSubTopBarRightImage = (ImageView)findViewById(R.id.fitness_main_sub_top_bar_right_border_imageview);
		
		if(getIntent().getIntExtra(AIM_MANAGER_START_PAGER, PAGER_STRENGTH)==PAGER_STABLE_HEARTRATE){
			onClick(findViewById(R.id.fitness_main_sub_top_bar_right_layout));
		}
		
	}
	
	private class ManagementPagerAdapter extends FragmentPagerAdapter{
		public ManagementPagerAdapter(FragmentManager fm) {
			super(fm);
			// TODO Auto-generated constructor stub
		}

		@Override
		public Fragment getItem(int index) {
			// TODO Auto-generated method stub
			Fragment fm = mStrengthFragment;
			switch (index) {
			case PAGER_STRENGTH:
				fm = mStrengthFragment;
				break;
			case PAGER_STABLE_HEARTRATE:
				fm = mStableHeartrateFragment;
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
		switch (v.getId()) {
		case R.id.fitness_main_sub_top_bar_left_layout:
			mViewPager.setCurrentItem(PAGER_STRENGTH);
			mSubTopBarLeftImage.setVisibility(View.VISIBLE);
			mSubTopBarRightImage.setVisibility(View.INVISIBLE);
			break;

		case R.id.fitness_main_sub_top_bar_right_layout:
			mViewPager.setCurrentItem(PAGER_STABLE_HEARTRATE);
			mSubTopBarLeftImage.setVisibility(View.INVISIBLE);
			mSubTopBarRightImage.setVisibility(View.VISIBLE);
			break;
		}
	}
}