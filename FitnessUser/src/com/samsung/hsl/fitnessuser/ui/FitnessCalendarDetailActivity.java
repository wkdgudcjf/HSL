package com.samsung.hsl.fitnessuser.ui;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.achartengine.GraphicalView;
import org.achartengine.tools.PanListener;

import com.samsung.hsl.fitnessuser.R;
import com.samsung.hsl.fitnessuser.service.FitnessFontUtils;
import com.samsung.hsl.fitnessuser.service.FitnessUserApplication;
import com.samsung.hsl.fitnessuser.service.FitnessUtils;
import com.samsung.hsl.fitnessuser.sqlite.FitnessData;
import com.samsung.hsl.fitnessuser.sqlite.User;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FitnessCalendarDetailActivity extends FitnessBaseActivity {
	private static final String tag = FitnessCalendarDetailActivity.class
			.getName();
	public static final String EXTRA_DATE = "date";
	
	Calendar curCalendar;
	TextView mTitleText;
	TextView mHeartrateText;
	TextView mSkinTemperatureText;
	TextView mCalorieText;
	TextView mAimText;

	ImageView mExerciseIcon;
	TextView mExerciseText;
	
	TextView mHighKarvonenText;
	TextView mCommonKarvonenText;
	TextView mFatBunningKarvonenText;
	TextView mWarmUpKarvonenText;
	
	FitnessUserApplication mFitnessApplication;
	ArrayList<FitnessData> mFitnessDataList;
	
	RelativeLayout mRelativeLayout;
	FitnessLineGraph mFitnessLineGraph = new FitnessLineGraph();
	GraphicalView mGraphicalView;
	String date;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState,
				R.layout.layout_fitness_calendar_detail);

		mTopBarTitleText.setText(getResources().getString(R.string.string_fitness_calendar_title));
		mTopBarLeftButton.setVisibility(View.VISIBLE);
		mTopBarRightButton.setVisibility(View.VISIBLE);

		date = getIntent().getStringExtra(EXTRA_DATE);
		try {
			curCalendar = Calendar.getInstance();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			curCalendar.setTime(format.parse(date));
			Log.i(tag, date);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String title = curCalendar.get(Calendar.YEAR)
				+getResources().getString(R.string.string_fitness_calendar_year)+" "
				+(curCalendar.get(Calendar.MONTH)+1)
				+getResources().getString(R.string.string_fitness_calendar_month)+" "
				+curCalendar.get(Calendar.DAY_OF_MONTH)
				+getResources().getString(R.string.string_fitness_calendar_day);
		
		mTitleText = (TextView) findViewById(R.id.fitness_calendar_detail_title_textview);
		mTitleText.setText(title);

		mHeartrateText = (TextView) findViewById(R.id.fitness_main_range_heartrate_value_textview);
		mSkinTemperatureText = (TextView) findViewById(R.id.fitness_main_range_temperature_value_textview);
		mCalorieText = (TextView) findViewById(R.id.fitness_main_range_calorie_value_textview);
		mAimText = (TextView) findViewById(R.id.fitness_main_range_aim_value_textview);
		mAimText.setTextSize(TypedValue.COMPLEX_UNIT_SP,12);
		
		// 운동 시간 아이콘 변경
		mExerciseIcon = (ImageView)((RelativeLayout) findViewById(R.id.fitness_main_range_aim_icon_layout)).getChildAt(0);
		mExerciseIcon.setImageResource(R.drawable.fitness_main_range_exercise_time_icon);	
		// 운동 시간 텍스트 변경
		mExerciseText = (TextView)findViewById(R.id.fitness_main_range_aim_text);
		mExerciseText.setText(getResources().getString(R.string.string_fitness_calendar_exercise_time));	
		
		mFitnessApplication = (FitnessUserApplication) getApplication();
		
		mRelativeLayout = (RelativeLayout) findViewById(R.id.fitness_main_graph_line_layout);
		mGraphicalView = mFitnessLineGraph.getView(this);
		mRelativeLayout.addView(mGraphicalView);
		mGraphicalView.setBackgroundColor(Color.TRANSPARENT);
		
		User user = mFitnessApplication.getFitnessService().getFitnessManager().getUser();
        mFitnessLineGraph.setKarvonen(
        		FitnessUtils.karvonen(user, FitnessUtils.STRENGTH_WARM_UP),
        		FitnessUtils.karvonen(user, FitnessUtils.STRENGTH_FAT_BUNNING), 
        		FitnessUtils.karvonen(user, FitnessUtils.STRENGTH_COMMON), 
        		FitnessUtils.karvonen(user, FitnessUtils.STRENGTH_HIGH));

		loadKarvonen();
        
        new SQLiteAsyncTask().execute();
	}

	public void onResume() {
		super.onResume();
		mGraphicalView.addPanListener(mPanListener);
	}
	
	public void onPause() {
		super.onStop();
		mGraphicalView.removePanListener(mPanListener);
	}
	
	private static final int PADDING_RIGHT = 20; 
	PanListener mPanListener = new PanListener(){

		@Override
		public void panApplied() {
			// TODO Auto-generated method stub
			if(mFitnessLineGraph.getXAxisMax()>(mFitnessDataList.size()-1+PADDING_RIGHT))mFitnessLineGraph.scrollTo(mFitnessDataList.size()-1+PADDING_RIGHT);
			else if(mFitnessLineGraph.getXAxisMax()<PADDING_RIGHT)mFitnessLineGraph.scrollTo(PADDING_RIGHT);
			updateStatus();
		}
	};
	
	void searchFitness(String date) {
		mFitnessDataList = mFitnessApplication.getSQLiteManager().selectFitnessData(
				mFitnessApplication.getFitnessService().getFitnessManager().getUser(), date + " 00:00:00",
				date + " 23:59:59");
	}

	void initGraphData(ArrayList<FitnessData> data) {

		if (data == null || data.size() == 0)
			return;

		for (int i = 0; i < data.size(); i++) {
			mFitnessLineGraph.addPoint(i, data.get(i).filterHeartrate);
		}
	}
	
	void updateStatus(){
		int index = mFitnessLineGraph.getXAxisMax()-PADDING_RIGHT;
		if(index>=mFitnessDataList.size())index = mFitnessDataList.size()-1;
		if(index<0)return;
		
		FitnessData data = mFitnessDataList.get(index);
		mHeartrateText.setText(String.valueOf(data.filterHeartrate));
		mSkinTemperatureText.setText(String.valueOf(data.skinTemperature));
		mCalorieText.setText(String.valueOf(data.consumeCalorie));
		mAimText.setText(data.datetime.substring(0, 10)+"\n"+data.datetime.substring(11));
	}
	
	void loadKarvonen(){
		mHighKarvonenText = (TextView)findViewById(R.id.fitness_main_graph_line_level4_range_textview);
		mCommonKarvonenText = (TextView)findViewById(R.id.fitness_main_graph_line_level3_range_textview);
		mFatBunningKarvonenText = (TextView)findViewById(R.id.fitness_main_graph_line_level2_range_textview);
		mWarmUpKarvonenText = (TextView)findViewById(R.id.fitness_main_graph_line_level1_range_textview);
		FitnessFontUtils.setFont(this, mHighKarvonenText);
        FitnessFontUtils.setFont(this, mCommonKarvonenText);
        FitnessFontUtils.setFont(this, mFatBunningKarvonenText);
        FitnessFontUtils.setFont(this, mWarmUpKarvonenText);
        
		User user = mFitnessApplication.getFitnessService().getFitnessManager().getUser();
		int high = FitnessUtils.karvonen(user, FitnessUtils.STRENGTH_HIGH);
		int common = FitnessUtils.karvonen(user, FitnessUtils.STRENGTH_COMMON);
		int fatBunning = FitnessUtils.karvonen(user, FitnessUtils.STRENGTH_FAT_BUNNING);
		int warmUp = FitnessUtils.karvonen(user, FitnessUtils.STRENGTH_WARM_UP);
		
		mHighKarvonenText.setText("85-100%\r\n("+common+" - "+high+")");
		mCommonKarvonenText.setText("65-85%\r\n("+fatBunning+" - "+common+")");
		mFatBunningKarvonenText.setText("50-65%\r\n("+warmUp+" - "+fatBunning+")");
		mWarmUpKarvonenText.setText("0-50%\r\n("+0+" - "+warmUp+")");
	}
	
	class SQLiteAsyncTask extends AsyncTask<Void,Void,Void>{

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			searchFitness(date);
			initGraphData(mFitnessDataList);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			mFitnessLineGraph.scrollTo(mFitnessDataList.size()-1+PADDING_RIGHT);
			mGraphicalView.repaint();
			updateStatus();
		}
	}
}