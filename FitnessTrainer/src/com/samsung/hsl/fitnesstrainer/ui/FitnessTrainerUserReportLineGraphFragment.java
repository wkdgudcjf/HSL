package com.samsung.hsl.fitnesstrainer.ui;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.achartengine.GraphicalView;
import org.achartengine.tools.PanListener;

import com.samsung.hsl.fitnesstrainer.R;
import com.samsung.hsl.fitnesstrainer.service.FitnessBroadcastService;
import com.samsung.hsl.fitnesstrainer.service.FitnessBroadcastService.FitnessBroadcastListener;
import com.samsung.hsl.fitnesstrainer.service.FitnessFontUtils;
import com.samsung.hsl.fitnesstrainer.service.FitnessManager;
import com.samsung.hsl.fitnesstrainer.service.FitnessPreference;
import com.samsung.hsl.fitnesstrainer.service.FitnessTrainerApplication;
import com.samsung.hsl.fitnesstrainer.service.FitnessUtils;
import com.samsung.hsl.fitnesstrainer.sqlite.FitnessData;
import com.samsung.hsl.fitnesstrainer.sqlite.User;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FitnessTrainerUserReportLineGraphFragment extends Fragment implements OnClickListener{
	private static final String tag = FitnessTrainerUserReportLineGraphFragment.class.getName();
	private static final int TIME_WAIT_SCROLL = 2000;
	
	ArrayList<FitnessData> mFitnessDataList = new ArrayList<FitnessData>();
	
	int state = FitnessManager.STATE_WARM_UP_NEED;
	
	RelativeLayout mRelativeLayout;
	FitnessLineGraph mFitnessLineGraph;
	GraphicalView mGraphicalView;
	ImageView mControlBar;
	TextView mMessage;
	FitnessTrainerApplication mFitnessApplication;
	FitnessPreference mFitnessPreference;

	TextView mHighKarvonenText;
	TextView mCommonKarvonenText;
	TextView mFatBunningKarvonenText;
	TextView mWarmUpKarvonenText;
	
	TextView mHeartrateValue;
	TextView mCalorieValue;
	TextView mSkinTemperatureValue;
	TextView mStrengthValue;
	RelativeLayout mStrengthLayout;
	
	int strength;
	FitnessBroadcastService mFitnessBroadcastService;
	
	public FitnessTrainerUserReportLineGraphFragment() {
		// Empty constructor required for fragment subclasses
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.layout_fitness_trainer_user_report_graph, container, false);
		
		initStatsLayout(view);
		initGraphLayout(view);
		
		mFitnessApplication = (FitnessTrainerApplication)getActivity().getApplication();
		mFitnessPreference = new FitnessPreference(getActivity());
		
        User user = mFitnessApplication.getFitnessService().getFitnessManager().getUser();
        mFitnessLineGraph.setKarvonen(
        		FitnessUtils.karvonen(user, FitnessUtils.STRENGTH_WARM_UP),
        		FitnessUtils.karvonen(user, FitnessUtils.STRENGTH_FAT_BUNNING), 
        		FitnessUtils.karvonen(user, FitnessUtils.STRENGTH_COMMON), 
        		FitnessUtils.karvonen(user, FitnessUtils.STRENGTH_HIGH));
        
        FitnessFontUtils.setFont(getActivity(), mMessage);
        loadKarvonen(view);
     
     		
        return view;
	}

	void initGraphLayout(View view){
		mControlBar = (ImageView)view.findViewById(R.id.fitness_main_graph_red_control_bar);
		mMessage = (TextView)view.findViewById(R.id.fitness_main_graph_message_textview);
		
		mRelativeLayout = (RelativeLayout)view.findViewById(R.id.fitness_main_graph_line_layout);
		mFitnessLineGraph = new FitnessLineGraph();
		mGraphicalView = mFitnessLineGraph.getView(getActivity());
		mRelativeLayout.addView(mGraphicalView);
		mGraphicalView.setBackgroundColor(Color.TRANSPARENT);
		
		startPixelControlBar = FitnessUtils.convertDpToPixels(START_X_DP_CONTROL_BAR,getActivity());
		
		
	}
	
	void initStatsLayout(View view){
 		mHeartrateValue = (TextView)view.findViewById(R.id.fitness_main_range_heartrate_value_textview);
 		mCalorieValue = (TextView)view.findViewById(R.id.fitness_main_range_calorie_value_textview);
 		mSkinTemperatureValue = (TextView)view.findViewById(R.id.fitness_main_range_temperature_value_textview);
 		mStrengthValue = (TextView)view.findViewById(R.id.fitness_main_range_aim_value_textview);
 		mStrengthLayout = (RelativeLayout)view.findViewById(R.id.fitness_main_range_aim_layout);
 		mStrengthLayout.setOnClickListener(this);
	}
	
	void initGraphData(ArrayList<FitnessData> data){
		if(data==null || data.size()==0)return;
		mFitnessLineGraph.clear();
		
		for(int i=0;i<data.size();i++){
			mFitnessLineGraph.addPoint(i, data.get(i).filterHeartrate);
		}
	}
	
	long scrollTime = 0;
	boolean isScrolling(){
		return (System.currentTimeMillis() - scrollTime - TIME_WAIT_SCROLL) < 0 ? true : false;
	}

	
	
	@Override
	public void onResume() {
		super.onResume();
		
		//안정심박수가 없을대 설정
        if(mFitnessApplication.getFitnessService().getFitnessManager().isOldCheckStableHeartrate())
        {
        	setNoStableHeartrate();
        }
        else {
        	mFitnessDataList = mFitnessApplication.getFitnessService().getFitnessManager().getFitnessDataList();
    		initGraphData(mFitnessDataList);
    		setControlBarPositon();
        }
    	
        
		mFitnessBroadcastService = mFitnessApplication.mFitnessBroadcastService;
		mFitnessBroadcastService.addBroadcastObserver(mFitnessBroadcastGraphListener);
		mFitnessBroadcastService.addBroadcastObserver(mFitnessBroadcastStateListener);
		
		User user = mFitnessApplication.getFitnessService().getCurrentUser();
		int state = mFitnessApplication.getFitnessService().getFitnessManager(user).getState();
		int strength = mFitnessApplication.getFitnessService().getFitnessManager(user).getStrength();
		setStrengthText(strength);
		mFitnessBroadcastGraphListener.onChangeUserState(user, state);
		mFitnessBroadcastGraphListener.onChangeAimStrength(user, strength);
		mFitnessBroadcastStateListener.onChangeUserState(user, state);
		mFitnessBroadcastStateListener.onChangeAimStrength(user, strength);
		
		mGraphicalView.addPanListener(mPanListener);
	}
	
	void setNoStableHeartrate(){
    	mMessage.setText(getResources().getString(R.string.string_fitness_main_message_no_stable_heartrate));
    	mMessage.setTextColor(getResources().getColor(R.color.donut_progress_not_used));
    	mControlBar.setVisibility(View.INVISIBLE);
        mMessage.setOnClickListener(this);
    }
	
	public void onPause()
	{
		super.onPause();
		mFitnessBroadcastService.removeBoradcastObserver(mFitnessBroadcastGraphListener);
		mFitnessBroadcastService.removeBoradcastObserver(mFitnessBroadcastStateListener);
		mGraphicalView.removePanListener(mPanListener);
	}

	private static final int START_X_DP_CONTROL_BAR = 22;
	float startPixelControlBar = 0;
	private static final int PADDING_RIGHT = 20;
	PanListener mPanListener = new PanListener()
	{
		@Override
		public void panApplied()
		{
			// TODO Auto-generated method stub
			
			if(mFitnessLineGraph.getXAxisMin()<0)mFitnessLineGraph.scrollTo(FitnessLineGraph.COUNT_OF_X_AXIS);
			else if(mFitnessDataList.size()>FitnessLineGraph.COUNT_OF_X_AXIS-PADDING_RIGHT
					&& mFitnessLineGraph.getXAxisMax()>mFitnessDataList.size()-1+PADDING_RIGHT){
				mFitnessLineGraph.scrollTo(mFitnessDataList.size()-1+PADDING_RIGHT);
			}else if(mFitnessDataList.size()<FitnessLineGraph.COUNT_OF_X_AXIS-PADDING_RIGHT
					&& mFitnessLineGraph.getXAxisMax()>FitnessLineGraph.COUNT_OF_X_AXIS){
				mFitnessLineGraph.scrollTo(FitnessLineGraph.COUNT_OF_X_AXIS);
			}
			else 
				scrollTime = System.currentTimeMillis();
			
			// 컨트롤 바 위치 이동
			setControlBarPositon();
		}
	};
	
	void setControlBarPositon()
	{
		int position = mFitnessDataList.size()-1;
		if(mFitnessLineGraph.getXAxisMax()<position || mFitnessLineGraph.getXAxisMin()>position){
			mControlBar.setVisibility(View.INVISIBLE);
		}
		else {
			mControlBar.setVisibility(View.VISIBLE);
			float xWidth = (float)mGraphicalView.getWidth()/FitnessLineGraph.COUNT_OF_X_AXIS;
			float xPosition = xWidth * (position - mFitnessLineGraph.getXAxisMin());
			mControlBar.setX(startPixelControlBar+xPosition);
		}
	}

	void loadKarvonen(View view)
	{
		mHighKarvonenText = (TextView)view.findViewById(R.id.fitness_main_graph_line_level4_range_textview);
		mCommonKarvonenText = (TextView)view.findViewById(R.id.fitness_main_graph_line_level3_range_textview);
		mFatBunningKarvonenText = (TextView)view.findViewById(R.id.fitness_main_graph_line_level2_range_textview);
		mWarmUpKarvonenText = (TextView)view.findViewById(R.id.fitness_main_graph_line_level1_range_textview);
		FitnessFontUtils.setFont(getActivity(), mHighKarvonenText);
        FitnessFontUtils.setFont(getActivity(), mCommonKarvonenText);
        FitnessFontUtils.setFont(getActivity(), mFatBunningKarvonenText);
        FitnessFontUtils.setFont(getActivity(), mWarmUpKarvonenText);
        
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
	
	boolean skipMessage = false;
	private static final int SKIP_MESSAGE_TIME = 5000;
	TimerTask checkTime;
	
	FitnessBroadcastListener mFitnessBroadcastGraphListener = new FitnessBroadcastListener(){

		@Override
		public void onReceiveFitnessData(User user, final FitnessData data) {
			// TODO Auto-generated method stub
			if(mFitnessApplication.getFitnessService().getCurrentUser().equals(user)==false)return;
			//if(mFitnessApplication.getFitnessService().getFitnessManager().isOldCheckStableHeartrate())return;
			
			Runnable runnable = new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					mFitnessLineGraph.addPoint(mFitnessDataList.size()-1, data.filterHeartrate);
					if(!isScrolling()){
						if(mFitnessDataList.size()>FitnessLineGraph.COUNT_OF_X_AXIS - PADDING_RIGHT){
							mFitnessLineGraph.scrollTo(mFitnessDataList.size()-1+PADDING_RIGHT);
						}
					}
					mGraphicalView.repaint();
					setControlBarPositon();
				}
				
			};
			
			getActivity().runOnUiThread(runnable);
		}

		@Override
		public void onReceiveFitnessFall(final User user) {
			// TODO Auto-generated method stub
			if(mFitnessApplication.getFitnessService().getCurrentUser().equals(user)==false)return;
			//if(mFitnessApplication.getFitnessService().getFitnessManager().isOldCheckStableHeartrate())return;
			if(checkTime!=null)return;
			
			Runnable runnable = new Runnable() {
				
				@Override
				public void run() {
					
					checkTime = new TimerTask() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							this.cancel();
							skipMessage = false;
							checkTime = null;
							int state = mFitnessApplication.getFitnessService().getFitnessManager(user).getState();
							mFitnessBroadcastGraphListener.onChangeUserState(user,state);
						}
					};
					Timer timer = new Timer();
					timer.schedule(checkTime, SKIP_MESSAGE_TIME, SKIP_MESSAGE_TIME);
					
					skipMessage = true;
					String message = getResources().getString(R.string.string_fitness_main_message_fall);
					int color = getResources().getColor(R.color.range_color_high);
					mMessage.setText(message);
					mMessage.setTextColor(color);
					
				}
			};
			
			getActivity().runOnUiThread(runnable);
		}

		@Override
		public void onChangeAimStrength(User user, final int strength) {
			// TODO Auto-generated method stub
			if(mFitnessApplication.getFitnessService().getCurrentUser().equals(user)==false)return;
			
			Runnable runnable = new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					mFitnessLineGraph.setAimStrength(strength);
					mGraphicalView.repaint();
				}
				
			};
			
			getActivity().runOnUiThread(runnable);
		}

		@Override
		public void onChangeUserState(User user, final int state) {
			// TODO Auto-generated method stub
			if(mFitnessApplication.getFitnessService().getCurrentUser().equals(user)==false)return;
			//if(mFitnessApplication.getFitnessService().getFitnessManager().isOldCheckStableHeartrate())return;
			if(skipMessage)return;
			
			Runnable runnable = new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					int color = 0x000000;
					String message = "";
					if(state==FitnessManager.STATE_STOP){
						message = getResources().getString(R.string.string_fitness_main_message_none);
						color = getResources().getColor(R.color.range_color_normal);
					}
					else if(state==FitnessManager.STATE_WARM_UP_NEED){
						message = getResources().getString(R.string.string_fitness_main_message_warm_up_need);
						color = getResources().getColor(R.color.range_color_normal);
					}
					else if(state==FitnessManager.STATE_WARM_UP_EXCEED_AIM){
						message = getResources().getString(R.string.string_fitness_main_message_warm_up_exeed_aim);
						color = getResources().getColor(R.color.range_color_high);
					}
					else if(state==FitnessManager.STATE_WARM_UP_COMPLETE){
						message = getResources().getString(R.string.string_fitness_main_message_warm_up_complete);
						color = getResources().getColor(R.color.range_color_aim);
					}
					else if(state==FitnessManager.STATE_SHORT_AIM){
						message = getResources().getString(R.string.string_fitness_main_message_short_aim);
						color = getResources().getColor(R.color.range_color_normal);
					}
					else if(state==FitnessManager.STATE_ARCHIEVE_AIM){
						message = getResources().getString(R.string.string_fitness_main_message_achieve_aim);
						color = getResources().getColor(R.color.range_color_aim);
					}
					else if(state==FitnessManager.STATE_EXCEED_AIM){
						message = getResources().getString(R.string.string_fitness_main_message_exceed_aim);
						color = getResources().getColor(R.color.range_color_high);
					}
					
					mMessage.setText(message);
					mMessage.setTextColor(color);
					
				}
				
			};
			
			getActivity().runOnUiThread(runnable);
		}

		@Override
		public void onChangeUserInfo(User user) {
			// TODO Auto-generated method stub
			
		}
		
	};

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Intent intent;
		switch (v.getId()) {
		case R.id.fitness_main_range_aim_layout:
			strength = (strength+1)%4;
			mFitnessPreference.putStrength(mFitnessApplication.getFitnessService().getCurrentUser(), strength);
			setStrengthText(strength);
			break;
		}
	}
	
	void setStrengthText(int strength){
		if(strength==FitnessUtils.STRENGTH_HIGH)mStrengthValue.setText(getResources().getString(R.string.string_fitness_main_range_aim_high));
		else if(strength==FitnessUtils.STRENGTH_COMMON)mStrengthValue.setText(getResources().getString(R.string.string_fitness_main_range_aim_common));
		else if(strength==FitnessUtils.STRENGTH_FAT_BUNNING)mStrengthValue.setText(getResources().getString(R.string.string_fitness_main_range_aim_start));
		else if(strength==FitnessUtils.STRENGTH_WARM_UP)mStrengthValue.setText(getResources().getString(R.string.string_fitness_main_range_aim_ready));
	}
	
	FitnessBroadcastListener mFitnessBroadcastStateListener = new FitnessBroadcastListener(){

		@Override
		public void onReceiveFitnessData(final User user, final FitnessData data) {
			if(mFitnessApplication.getFitnessService().getCurrentUser().equals(user)==false)return;
			//if(mFitnessApplication.getFitnessService().getFitnessManager().isOldCheckStableHeartrate())return;
			
			// TODO Auto-generated method stub
			Runnable runnable = new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					
					
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
			
			getActivity().runOnUiThread(runnable);
		}

		@Override
		public void onReceiveFitnessFall(User user) {
			// TODO Auto-generated method stub
			if(mFitnessApplication.getFitnessService().getCurrentUser().equals(user)==false)return;
		}

		@Override
		public void onChangeAimStrength(User user, final int strength) {
			// TODO Auto-generated method stub
			if(mFitnessApplication.getFitnessService().getCurrentUser().equals(user)==false)return;
			
			FitnessTrainerUserReportLineGraphFragment.this.strength = strength;
			
			Runnable runnable = new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					
					setStrengthText(strength);
				}
				
			};
			
			getActivity().runOnUiThread(runnable);
			
			
		}

		@Override
		public void onChangeUserState(final User user, final int state) {
			// TODO Auto-generated method stub
			if(mFitnessApplication.getFitnessService().getCurrentUser().equals(user)==false)return;
			//if(mFitnessApplication.getFitnessService().getFitnessManager().isOldCheckStableHeartrate())return;
			
			Runnable runnable = new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					
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
			
			getActivity().runOnUiThread(runnable);
		}

		@Override
		public void onChangeUserInfo(User user) {
			// TODO Auto-generated method stub
			
		}
		
	};
}
