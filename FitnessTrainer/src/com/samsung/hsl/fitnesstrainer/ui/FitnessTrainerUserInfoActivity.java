package com.samsung.hsl.fitnesstrainer.ui;

import com.samsung.hsl.fitnesstrainer.R;
import com.samsung.hsl.fitnesstrainer.service.FitnessPreference;
import com.samsung.hsl.fitnesstrainer.service.FitnessTrainerApplication;
import com.samsung.hsl.fitnesstrainer.sqlite.User;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;


public class FitnessTrainerUserInfoActivity extends FitnessFontActivity implements OnClickListener {
	private static final String tag = FitnessTrainerUserInfoActivity.class.getName();
	private static final int PAGER_CALENDAR = 0x00;
	private static final int PAGER_USER_INFO = 0x01;
	private static final int PAGER_STABLE_HEART = 0x02;
	private static final int PAGER_AIM = 0x03;
	
	FitnessBaseViewPager mViewPager;
	PagerAdapter mPagerAdapter;
	Fragment mCalendarFragment;
	Fragment mUserEditFragment;
	Fragment mStableFragment;
	Fragment mAimFragment;
	
	ImageView mSubTopBarLeftImagetop;
	ImageView mSubTopBarRightImagetop;
	ImageView mSubTopBarLeftImagebottom;
	ImageView mSubTopBarRightImagebottom;
	
	User user;
	FitnessTrainerApplication mFitnessApplication;
	FitnessPreference mFitnessPreference;
	
	int strength;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.layout_fitness_trainer_user_info);

		mFitnessApplication = ((FitnessTrainerApplication)getApplication());
		user = mFitnessApplication.getFitnessService().getCurrentUser();
		
		TextView mTopBarTitleText = (TextView)findViewById(R.id.fitness_top_bar_title_textview); 
		mTopBarTitleText.setText(user.name+" "+getResources().getString(R.string.string_fitness_trainer_user_name_tail));
		
		TextView mSubBarLeftTexttop = (TextView)findViewById(R.id.fitness_main_sub_top_bar_left_textview_top);
		mSubBarLeftTexttop.setText(getResources().getString(R.string.string_fitness_calendar_title));
		TextView mSubBarRightTexttop = (TextView)findViewById(R.id.fitness_main_sub_top_bar_right_textview_top);
		mSubBarRightTexttop.setText(getResources().getString(R.string.string_fitness_trainer_user_edit_title));
		
		TextView mSubBarLeftTextbottom = (TextView)findViewById(R.id.fitness_main_sub_top_bar_left_textview_bottom);
		mSubBarLeftTextbottom.setText(getResources().getString(R.string.string_fitness_aim_manager_subtitle_heartrate));
		TextView mSubBarRightTextbottom = (TextView)findViewById(R.id.fitness_main_sub_top_bar_right_textview_bottom);
		mSubBarRightTextbottom.setText(getResources().getString(R.string.string_fitness_aim_manager_subtitle_aim));
		
		mViewPager = (FitnessBaseViewPager)findViewById(R.id.fitness_main_main_board_pager);
		mPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setPagingEnable(false);
		
		mCalendarFragment = new FitnessTrainerCalendarFragment();
		mUserEditFragment = new FitnessTrainerUserEditFragment();
		mAimFragment = new FitnessAimManagerStrengthFragment();
		mStableFragment = new FitnessAimManagerStableHeartrateFragment();
		
		mSubTopBarLeftImagetop = (ImageView)findViewById(R.id.fitness_main_sub_top_bar_left_border_imageview_top);
		mSubTopBarRightImagetop = (ImageView)findViewById(R.id.fitness_main_sub_top_bar_right_border_imageview_top);
		mSubTopBarLeftImagebottom = (ImageView)findViewById(R.id.fitness_main_sub_top_bar_left_border_imageview_bottom);
		mSubTopBarRightImagebottom = (ImageView)findViewById(R.id.fitness_main_sub_top_bar_right_border_imageview_bottom);
		
	}
	
	private class MainPagerAdapter extends FragmentPagerAdapter{
		public MainPagerAdapter(FragmentManager fm) {
			super(fm);
			// TODO Auto-generated constructor stub
		}

		@Override
		public Fragment getItem(int index) {
			// TODO Auto-generated method stub
			Fragment fm = mCalendarFragment;
			switch (index) {
			case PAGER_CALENDAR:
				fm = mCalendarFragment;
				break;
			case PAGER_USER_INFO:
				fm = mUserEditFragment;
				break;
			case PAGER_STABLE_HEART:
				fm = mStableFragment;
				break;
			case PAGER_AIM:
				fm = mAimFragment;
				break;
			}
			return fm;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return 4;
		}
		
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent;
		switch (v.getId()) {
		case R.id.fitness_main_sub_top_bar_left_layout_top:
			mViewPager.setCurrentItem(PAGER_CALENDAR);
			mSubTopBarLeftImagetop.setVisibility(View.VISIBLE);
			mSubTopBarRightImagetop.setVisibility(View.INVISIBLE);
			mSubTopBarLeftImagebottom.setVisibility(View.INVISIBLE);
			mSubTopBarRightImagebottom.setVisibility(View.INVISIBLE);
			break;

		case R.id.fitness_main_sub_top_bar_right_layout_top:
			mViewPager.setCurrentItem(PAGER_USER_INFO);
			mSubTopBarLeftImagetop.setVisibility(View.INVISIBLE);
			mSubTopBarRightImagetop.setVisibility(View.VISIBLE);
			mSubTopBarLeftImagebottom.setVisibility(View.INVISIBLE);
			mSubTopBarRightImagebottom.setVisibility(View.INVISIBLE);
			break;
			
		case R.id.fitness_main_sub_top_bar_left_layout_bottom:
			mViewPager.setCurrentItem(PAGER_STABLE_HEART);
			mSubTopBarLeftImagetop.setVisibility(View.INVISIBLE);
			mSubTopBarRightImagetop.setVisibility(View.INVISIBLE);
			mSubTopBarLeftImagebottom.setVisibility(View.VISIBLE);
			mSubTopBarRightImagebottom.setVisibility(View.INVISIBLE);
			break;
			
		case R.id.fitness_main_sub_top_bar_right_layout_bottom:
			mViewPager.setCurrentItem(PAGER_AIM);
			mSubTopBarLeftImagetop.setVisibility(View.INVISIBLE);
			mSubTopBarRightImagetop.setVisibility(View.INVISIBLE);
			mSubTopBarLeftImagebottom.setVisibility(View.INVISIBLE);
			mSubTopBarRightImagebottom.setVisibility(View.VISIBLE);
			break;
		}
	}
	
}
