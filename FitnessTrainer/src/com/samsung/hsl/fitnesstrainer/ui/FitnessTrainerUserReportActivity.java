package com.samsung.hsl.fitnesstrainer.ui;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;

import com.samsung.hsl.fitnesstrainer.R;
import com.samsung.hsl.fitnesstrainer.service.FitnessBroadcastService.FitnessBroadcastListener;
import com.samsung.hsl.fitnesstrainer.service.FitnessPreference;
import com.samsung.hsl.fitnesstrainer.service.FitnessTrainerApplication;
import com.samsung.hsl.fitnesstrainer.sqlite.FitnessData;
import com.samsung.hsl.fitnesstrainer.sqlite.User;


public class FitnessTrainerUserReportActivity extends FitnessFontActivity implements OnClickListener {
	private static final String tag = FitnessTrainerUserReportActivity.class.getName();
	private static final int PAGER_GRAPH = 0x00;
	private static final int PAGER_CALENDAR = 0x01;
	
	FitnessBaseViewPager mViewPager;
	PagerAdapter mPagerAdapter;
	Fragment mGraphFragment;
	Fragment mCalendarFragment;
	
	ImageView mSubTopBarLeftImage;
	TextView mSubTopBarLeftText;
	ImageView mSubTopBarRightImage;
	TextView mSubTopBarRightText;
	User mUser;
	FitnessTrainerApplication mFitnessApplication;
	FitnessPreference mFitnessPreference;
	TextView mTime; 
	int strength;
	private TimerTask mSecond;
	private final Handler handler = new Handler();

	void timer() {
		mSecond = new TimerTask() {

				@Override
				public void run() {
					Update();
				}
			};
			Timer timer = new Timer();
			timer.schedule(mSecond, 0, 1000);
	}

	void Update() {
			Runnable updater = new Runnable() {
				public void run() {
					long starttime = mFitnessApplication.getFitnessService().getFitnessManager().getCurrentFitnessList().startTime;
					long curtime = System.currentTimeMillis();
					long runtime = (curtime - starttime)/1000;
					long hour = runtime/3600;
					long min = (runtime-(hour*3600))/60;
					long sec = runtime-(hour*3600)-(min*60);
					if(hour==0)
					{
						if(min==0)
						{
							mTime.setText("Runing time = "+sec+" sec");
						}
						else
						{
							mTime.setText("Runing time = "+min+" : "+sec);
						}
					}
					else
					{
						mTime.setText("Runing time = "+hour+" : "+min+" : "+sec);
					}
				}
			};
			handler.post(updater);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.layout_fitness_trainer_user_report);

		mFitnessApplication = ((FitnessTrainerApplication)getApplication());
		mUser = mFitnessApplication.getFitnessService().getCurrentUser();
		
		initialize();
		
		initUserBoard();
		timer();
	}
	void initialize(){
		TextView mTopBarTitleText = (TextView)findViewById(R.id.fitness_top_bar_title_textview); 
		mTopBarTitleText.setText(getResources().getString(R.string.string_fitness_main_title));
		
		TextView mSubBarLeftText = (TextView)findViewById(R.id.fitness_main_sub_top_bar_left_textview);
		mSubBarLeftText.setText(getResources().getString(R.string.string_fitness_trainer_user_report_graph));
		TextView mSubBarRightText = (TextView)findViewById(R.id.fitness_main_sub_top_bar_right_textview);
		mSubBarRightText.setText(getResources().getString(R.string.string_fitness_trainer_user_report_history));
		
		mViewPager = (FitnessBaseViewPager)findViewById(R.id.fitness_main_main_board_pager);
		mPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setPagingEnable(false);

		mGraphFragment = new FitnessTrainerUserReportLineGraphFragment();
		mCalendarFragment = new FitnessTrainerCalendarFragment();
		
		mSubTopBarLeftImage = (ImageView)findViewById(R.id.fitness_main_sub_top_bar_left_border_imageview);
		mSubTopBarRightImage = (ImageView)findViewById(R.id.fitness_main_sub_top_bar_right_border_imageview);
	}
	
	void initUserBoard(){
		ImageView picture = (ImageView)findViewById(R.id.fitness_trainer_user_board_photo);
		TextView name = (TextView)findViewById(R.id.fitness_trainer_user_board_name);
		TextView birth = (TextView)findViewById(R.id.fitness_trainer_user_board_birth_height_weight);
		mTime = (TextView)findViewById(R.id.fitness_trainer_user_board_time);
		// 사진
		if(mUser.picture!=null){
			Bitmap bitmap = BitmapFactory.decodeByteArray(mUser.picture, 0, mUser.picture.length);
			BitmapDrawable drawable = new BitmapDrawable(bitmap);
			picture.setImageDrawable(drawable);
		}
		
		name.setText(mUser.name+" "+getResources().getString(R.string.string_fitness_trainer_user_name_tail));
		String text = mUser.birthday.replace("-", ". ")+"\r\n"+mUser.height+"cm / "+mUser.weight+"kg";
		birth.setText(text);
	}
	
	private class MainPagerAdapter extends FragmentPagerAdapter{
		public MainPagerAdapter(FragmentManager fm) {
			super(fm);
			// TODO Auto-generated constructor stub
		}

		@Override
		public Fragment getItem(int index) {
			// TODO Auto-generated method stub
			Fragment fm = mGraphFragment;
			switch (index) {
			case PAGER_GRAPH:
				fm = mGraphFragment;
				break;
			case PAGER_CALENDAR:
				fm = mCalendarFragment;
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
			mViewPager.setCurrentItem(PAGER_GRAPH);
			mSubTopBarLeftImage.setVisibility(View.VISIBLE);
			mSubTopBarRightImage.setVisibility(View.INVISIBLE);
			break;

		case R.id.fitness_main_sub_top_bar_right_layout:
			mViewPager.setCurrentItem(PAGER_CALENDAR);
			mSubTopBarLeftImage.setVisibility(View.INVISIBLE);
			mSubTopBarRightImage.setVisibility(View.VISIBLE);
			break;
			
		}
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mFitnessApplication.mFitnessBroadcastService.addBroadcastObserver(mFitnessBroadcasetListener);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	
	FitnessBroadcastListener mFitnessBroadcasetListener = new FitnessBroadcastListener(){

		@Override
		public void onReceiveFitnessData(User user, FitnessData data) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onReceiveFitnessFall(User user) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onChangeAimStrength(User user, int strength) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onChangeUserState(User user, int state) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onChangeUserInfo(User user) {
			// TODO Auto-generated method stub
			if(mFitnessApplication.getFitnessService().getCurrentUser().equals(user)==false)return;
			
			Runnable runnable = new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					initUserBoard();
				}
				
			};
			
			runOnUiThread(runnable);
		}
		
	};
}
