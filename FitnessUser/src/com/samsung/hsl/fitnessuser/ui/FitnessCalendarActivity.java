package com.samsung.hsl.fitnessuser.ui;

import java.util.Calendar;

import com.samsung.hsl.fitnessuser.R;
import com.samsung.hsl.fitnessuser.service.FitnessUserApplication;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class FitnessCalendarActivity extends FitnessBaseActivity implements
		OnClickListener,OnPageChangeListener {
	private static final String tag = FitnessCalendarActivity.class.getName();
	private static final int CENTER_POSITION = Integer.MAX_VALUE/2; 
	
	FitnessUserApplication mFitnessApplication;
	ImageButton mLeftButton, mRightButton;
	TextView mYearMonthTextView;

	Calendar curMonth;
	
	ViewPager mViewPager;
	CalendarPagerAdapter mPagerAdapter;
	ProgressDialog mProgressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState, R.layout.layout_fitness_calendar);
		mFitnessApplication = (FitnessUserApplication) getApplication();

		mTopBarTitleText.setText(getResources().getString(R.string.string_fitness_calendar_title));
		mTopBarLeftButton.setVisibility(View.VISIBLE);
		mTopBarRightButton.setVisibility(View.VISIBLE);

		mLeftButton = (ImageButton) findViewById(R.id.fitness_calendar_left_imagebutton);
		mRightButton = (ImageButton) findViewById(R.id.fitness_calendar_right_imagebutton);
		mLeftButton.setOnClickListener(FitnessCalendarActivity.this);
		mRightButton.setOnClickListener(FitnessCalendarActivity.this);
		mYearMonthTextView = (TextView) findViewById(R.id.fitness_calendar_year_month_textview);

		mViewPager = (ViewPager)findViewById(R.id.fitness_calendar_viewpager);
		mViewPager.setOnPageChangeListener(FitnessCalendarActivity.this);
		mPagerAdapter = new CalendarPagerAdapter(getSupportFragmentManager());
		
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setCurrentItem(CENTER_POSITION);
		
	}
	
	void initTextView(){
		String str = String.valueOf(curMonth.get(Calendar.YEAR))+getResources().getString(R.string.string_fitness_calendar_year)
				+"  "+String.format("%02d",curMonth.get(Calendar.MONTH) + 1)+getResources().getString(R.string.string_fitness_calendar_month);
		mYearMonthTextView.setText(str);
	}
	
	
	private class CalendarPagerAdapter extends FragmentPagerAdapter{
		
		public CalendarPagerAdapter(FragmentManager fm) {
			super(fm);
			// TODO Auto-generated constructor stub
		}

		@Override
		public Fragment getItem(int index) {
			// TODO Auto-generated method stub
		    
			Calendar calendar = Calendar.getInstance();
			if(index!=CENTER_POSITION){
				index = index - CENTER_POSITION;
				calendar.add(Calendar.MONTH, index);
			}
			
			FitnessCalendarPagerFragment fm = new FitnessCalendarPagerFragment(calendar);

			return fm;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return Integer.MAX_VALUE;
		}
		
	}


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.fitness_calendar_left_imagebutton:
			mViewPager.setCurrentItem(mViewPager.getCurrentItem()-1,true);
			break;

		case R.id.fitness_calendar_right_imagebutton:
			mViewPager.setCurrentItem(mViewPager.getCurrentItem()+1,true);
			break;
		}
		
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPageSelected(int position) {
		// TODO Auto-generated method stub
		int index = position - Integer.MAX_VALUE/2;
		curMonth = Calendar.getInstance();
		curMonth.add(Calendar.MONTH, index);
		initTextView();
	}
	
	class LoadingAsyncTask extends AsyncTask<Void,Void,Void>{

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
		}
	}
}
