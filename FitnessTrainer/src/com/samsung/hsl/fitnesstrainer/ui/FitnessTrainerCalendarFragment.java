package com.samsung.hsl.fitnesstrainer.ui;

import java.util.Calendar;

import com.samsung.hsl.fitnesstrainer.R;
import com.samsung.hsl.fitnesstrainer.service.FitnessFontUtils;
import com.samsung.hsl.fitnesstrainer.service.FitnessTrainerApplication;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FitnessTrainerCalendarFragment extends Fragment implements
		OnClickListener, OnPageChangeListener {
	private static final String tag = FitnessTrainerCalendarFragment.class.getName();
	private static final int CENTER_POSITION = Integer.MAX_VALUE / 2;

	FitnessTrainerApplication mFitnessApplication;
	ImageButton mLeftButton, mRightButton;
	TextView mYearMonthTextView;

	Calendar curMonth;

	ViewPager mViewPager;
	CalendarPagerAdapter mPagerAdapter;
	ProgressDialog mProgressDialog;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.i(tag, "onCreateView");
		
		View view = inflater.inflate(R.layout.layout_fitness_trainer_calendar,container, false);

		RelativeLayout topBar = (RelativeLayout) view.findViewById(R.id.fitness_top_bar_layout); 
		topBar.setVisibility(View.GONE);
		
		mLeftButton = (ImageButton) view.findViewById(R.id.fitness_calendar_left_imagebutton);
		mRightButton = (ImageButton) view.findViewById(R.id.fitness_calendar_right_imagebutton);
		mLeftButton.setOnClickListener(FitnessTrainerCalendarFragment.this);
		mRightButton.setOnClickListener(FitnessTrainerCalendarFragment.this);
		mYearMonthTextView = (TextView) view.findViewById(R.id.fitness_calendar_year_month_textview);

		mViewPager = (ViewPager) view.findViewById(R.id.fitness_calendar_viewpager);
		mViewPager.setOnPageChangeListener(FitnessTrainerCalendarFragment.this);
		
		mPagerAdapter = new CalendarPagerAdapter(getChildFragmentManager());
		mViewPager.setAdapter(mPagerAdapter);
		mViewPager.setCurrentItem(CENTER_POSITION);
		
		FitnessFontUtils.setViewGroupFont((ViewGroup)view);
		
		return view;

	}

	void initTextView() {
		String str = String.valueOf(curMonth.get(Calendar.YEAR))+getResources().getString(R.string.string_fitness_calendar_year)
				+"  "+String.format("%02d",curMonth.get(Calendar.MONTH) + 1)+getResources().getString(R.string.string_fitness_calendar_month);
		mYearMonthTextView.setText(str);
	}

	private class CalendarPagerAdapter extends FragmentPagerAdapter {

		public CalendarPagerAdapter(FragmentManager fm) {
			super(fm);
			// TODO Auto-generated constructor stub
		}

		@Override
		public Fragment getItem(int index) {
			// TODO Auto-generated method stub

			Calendar calendar = Calendar.getInstance();
			if (index != CENTER_POSITION) {
				index = index - CENTER_POSITION;
				calendar.add(Calendar.MONTH, index);
			}

			FitnessTrainerCalendarPagerFragment fm = new FitnessTrainerCalendarPagerFragment(calendar);
			
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
			mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1, true);
			break;

		case R.id.fitness_calendar_right_imagebutton:
			mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
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
		int index = position - Integer.MAX_VALUE / 2;
		curMonth = Calendar.getInstance();
		curMonth.add(Calendar.MONTH, index);
		initTextView();
	}

	class LoadingAsyncTask extends AsyncTask<Void, Void, Void> {

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
