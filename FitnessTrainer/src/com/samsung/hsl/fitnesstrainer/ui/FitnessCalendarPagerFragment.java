package com.samsung.hsl.fitnesstrainer.ui;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import com.samsung.hsl.fitnesstrainer.R;
import com.samsung.hsl.fitnesstrainer.service.FitnessFontUtils;
import com.samsung.hsl.fitnesstrainer.service.FitnessTrainerApplication;
import com.samsung.hsl.fitnesstrainer.sqlite.FitnessList;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FitnessCalendarPagerFragment extends Fragment implements OnClickListener {
	private static final String tag = FitnessCalendarPagerFragment.class.getName();
	
	private static final int NUM_OF_WEEK = 6;
	private static final int NUM_OF_DAYS_OF_WEEK = 7;
	private static final int NUM_OF_DAY_TEXT_VIEW = 0x00;
	private static final int NUM_OF_INFO_TEXT_VIEW = 0x01;

	Calendar[][] mDateList = new Calendar[NUM_OF_WEEK][NUM_OF_DAYS_OF_WEEK];
	TextView[][] mDayTextView = new TextView[NUM_OF_WEEK][NUM_OF_DAYS_OF_WEEK];
	TextView[][] mInfoTextView = new TextView[NUM_OF_WEEK][NUM_OF_DAYS_OF_WEEK];

	protected FitnessTrainerApplication mFitnessApplication;
	ArrayList<FitnessList> mFitnessList;
	Calendar curMonth;
	protected int curMonthColor;
	protected int otherMonthColor;
	
	int startIndex;
	
	public FitnessCalendarPagerFragment(Calendar calendar) {
		// Empty constructor required for fragment subclasses
		this.curMonth = calendar;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.layout_fitness_calendar_pager, container,	false);

		mFitnessApplication = (FitnessTrainerApplication)getActivity().getApplication();

		curMonthColor = getResources().getColor(R.color.day_of_cur_month);
		otherMonthColor = getResources().getColor(R.color.day_of_other_month);

		initTextView(view);
		
		new SQLiteAsyncTask().execute();
		
		FitnessFontUtils.setViewGroupFont((ViewGroup)view);
		return view;
	}

	protected void initTextView(View view) {
		for (int week = 0; week < NUM_OF_WEEK; week++) {
			for (int dayOfWeek = 0; dayOfWeek < NUM_OF_DAYS_OF_WEEK; dayOfWeek++) {
				String fieldName = "calendar_day" + week + "" + dayOfWeek;
				try {
					int id = R.id.class.getDeclaredField(fieldName)
							.getInt(null);
					RelativeLayout parent = (RelativeLayout) view.findViewById(id);
					parent.setOnClickListener(this);
					mDayTextView[week][dayOfWeek] = (TextView) parent
							.getChildAt(NUM_OF_DAY_TEXT_VIEW);
					mInfoTextView[week][dayOfWeek] = (TextView) parent
							.getChildAt(NUM_OF_INFO_TEXT_VIEW);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	void initCalendar(Calendar calendar) {
		calendar.set(Calendar.DAY_OF_MONTH, 1);
		
		startIndex = calendar.get(Calendar.DAY_OF_WEEK) - 1;
		mDateList[0][startIndex] = calendar;
		for (int i = startIndex - 1; i >= 0; i--) {
			calendar = (Calendar) calendar.clone();
			calendar.add(Calendar.DAY_OF_MONTH, -1);
			mDateList[0][i] = calendar;
		}

		calendar = mDateList[0][0];

		for (int week = 0; week < NUM_OF_WEEK; week++) {
			for (int dayOfWeek = 0; dayOfWeek < NUM_OF_DAYS_OF_WEEK; dayOfWeek++) {
				mDateList[week][dayOfWeek] = calendar;
				calendar = (Calendar) calendar.clone();
				calendar.add(Calendar.DAY_OF_MONTH, +1);
			}
		}
	}

	void setDateText() {
		Calendar today = Calendar.getInstance();
		for (int week = 0; week < NUM_OF_WEEK; week++) {
			for (int dayOfWeek = 0; dayOfWeek < NUM_OF_DAYS_OF_WEEK; dayOfWeek++) {
				String date = String.valueOf(mDateList[week][dayOfWeek]
						.get(Calendar.DAY_OF_MONTH));
				mDayTextView[week][dayOfWeek].setText(date);
				//오늘 날짜에 표시
				if(isSameDay(mDateList[week][dayOfWeek],today)){
					mDayTextView[week][dayOfWeek].setBackgroundResource(R.drawable.fitness_calendar_selected_date_btn);
				}
				//날짜 색상 구분
				if (mDateList[week][dayOfWeek].get(Calendar.MONTH) != curMonth
						.get(Calendar.MONTH)) {
					mDayTextView[week][dayOfWeek].setTextColor(otherMonthColor);
				} else {
					mDayTextView[week][dayOfWeek].setTextColor(curMonthColor);
				}
			}
		}
	}

	void searchFitnessList(){
		String from = getStringFormat(mDateList[0][0])+" 00:00:00";
		String to = getStringFormat(mDateList[NUM_OF_WEEK - 1][NUM_OF_DAYS_OF_WEEK - 1])+" 23:59:59";
		mFitnessList = mFitnessApplication.getSQLiteManager()
				.selectFitnessList(mFitnessApplication.getFitnessService().getFitnessManager().getUser(),from, to);
	}
	
	void setInfoText() {
		Calendar calendar = Calendar.getInstance();

		for (int i = 0; i < mFitnessList.size(); i++) {
			try {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				calendar.setTime(format.parse(mFitnessList.get(i).datetime));

				for (int week = 0; week < NUM_OF_WEEK; week++) {
					for (int dayOfWeek = 0; dayOfWeek < NUM_OF_DAYS_OF_WEEK; dayOfWeek++) {
						if (isSameDay(calendar, mDateList[week][dayOfWeek])) {
							int min = mFitnessList.get(i).exerciseTime / 60;
							int second = mFitnessList.get(i).exerciseTime % 60;
							String info = mFitnessList.get(i).consumeCalorie + "\r\n";
							info += min + "'" + second + "\"";
							mInfoTextView[week][dayOfWeek].setText(info);
						}
					}
				}
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	public boolean isSameDay(Calendar cal1, Calendar cal2) {
		if (cal1 == null || cal2 == null)
			return false;
		return (cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
				&& cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1
					.get(Calendar.DAY_OF_MONTH) == cal2
				.get(Calendar.DAY_OF_MONTH));
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.fitness_calendar_left_imagebutton) {
			curMonth.add(Calendar.MONTH, -1);
			initCalendar(curMonth);
		} else if (v.getId() == R.id.fitness_calendar_right_imagebutton) {
			curMonth.add(Calendar.MONTH, 1);
			initCalendar(curMonth);
		} else {
			for (int week = 0; week < NUM_OF_WEEK; week++) {
				for (int dayOfWeek = 0; dayOfWeek < NUM_OF_DAYS_OF_WEEK; dayOfWeek++) {
					String fieldName = "calendar_day" + week + "" + dayOfWeek;
					try {
						int id = R.id.class.getDeclaredField(fieldName).getInt(null);
						if (v.getId() == id) {
							if(mInfoTextView[week][dayOfWeek].getText().toString().length()==0)continue;
							goToCalendarDetail(mDateList[week][dayOfWeek]);
							return;
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	protected void goToCalendarDetail(Calendar calendar) {
	
	}

	protected String getStringFormat(Calendar calendar) {
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		return format.format(calendar.getTime());
	}
	
	public class SQLiteAsyncTask extends AsyncTask<Void,Void,Void>{

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			Log.i(tag, "doInBackground");
			initCalendar(curMonth);
			searchFitnessList();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			Log.i(tag, "onPostExecute");
			setDateText();
			setInfoText();
		}
	}
}