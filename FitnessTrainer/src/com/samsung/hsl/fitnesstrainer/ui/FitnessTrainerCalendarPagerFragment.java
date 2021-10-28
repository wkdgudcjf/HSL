package com.samsung.hsl.fitnesstrainer.ui;

import java.util.Calendar;

import com.samsung.hsl.fitnesstrainer.R;
import com.samsung.hsl.fitnesstrainer.service.FitnessFontUtils;
import com.samsung.hsl.fitnesstrainer.service.FitnessTrainerApplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FitnessTrainerCalendarPagerFragment extends FitnessCalendarPagerFragment {
	private static final String tag = FitnessTrainerCalendarPagerFragment.class.getName();
	
	public FitnessTrainerCalendarPagerFragment(Calendar calendar) {
		super(calendar);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		Log.i(tag, "onCreateView");
		
		View view = inflater.inflate(R.layout.layout_fitness_trainer_calendar_pager, container,	false);

		mFitnessApplication = (FitnessTrainerApplication)getActivity().getApplication();

		curMonthColor = getResources().getColor(R.color.day_of_cur_month);
		otherMonthColor = getResources().getColor(R.color.day_of_other_month);

		initTextView(view);
		
		new SQLiteAsyncTask().execute();
		
		FitnessFontUtils.setViewGroupFont((ViewGroup)view);
		return view;
	}
	
	@Override
	protected void goToCalendarDetail(Calendar calendar) {
		String date = getStringFormat(calendar);
		Intent intent = new Intent(getActivity(), FitnessTrainerCalendarDetailActivity.class);
		intent.putExtra(FitnessTrainerCalendarDetailActivity.EXTRA_DATE, date);
		startActivity(intent);
	}
	
}