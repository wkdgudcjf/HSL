package com.samsung.hsl.fitnessuser.ui;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.achartengine.GraphicalView;
import org.achartengine.tools.PanListener;

import com.samsung.hsl.fitnessuser.R;
import com.samsung.hsl.fitnessuser.service.FitnessBroadcastService;
import com.samsung.hsl.fitnessuser.service.FitnessBroadcastService.FitnessBroadcastListener;
import com.samsung.hsl.fitnessuser.service.FitnessFontUtils;
import com.samsung.hsl.fitnessuser.service.FitnessManager;
import com.samsung.hsl.fitnessuser.service.FitnessPreference;
import com.samsung.hsl.fitnessuser.service.FitnessUserApplication;
import com.samsung.hsl.fitnessuser.service.FitnessUtils;
import com.samsung.hsl.fitnessuser.sqlite.FitnessData;
import com.samsung.hsl.fitnessuser.sqlite.User;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FitnessMainLineGraphFragment extends Fragment implements OnClickListener {
	private static final String tag = FitnessMainLineGraphFragment.class.getName();
	private static final int TIME_WAIT_SCROLL = 2000;
	
	ArrayList<FitnessData> mFitnessDataList = new ArrayList<FitnessData>();
	
	int state = FitnessManager.STATE_WARM_UP_NEED;
	
	RelativeLayout mRelativeLayout;
	FitnessLineGraph mFitnessLineGraph;
	GraphicalView mGraphicalView;
	ImageView mControlBar;
	TextView mMessage;
	FitnessUserApplication mFitnessApplication;
	FitnessPreference mFitnessPreference;
	TextView mTime;
	TextView mHighKarvonenText;
	TextView mCommonKarvonenText;
	TextView mFatBunningKarvonenText;
	TextView mWarmUpKarvonenText;
	
	public FitnessMainLineGraphFragment() {
		// Empty constructor required for fragment subclasses
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.layout_fitness_main_graph_line_includer, container, false);
		
		mFitnessApplication = (FitnessUserApplication)getActivity().getApplication();
		
		mControlBar = (ImageView)view.findViewById(R.id.fitness_main_graph_red_control_bar);
		mMessage = (TextView)view.findViewById(R.id.fitness_main_graph_message_textview);
		mTime = (TextView)view.findViewById(R.id.fitness_main_graph_time_textview);
		mRelativeLayout = (RelativeLayout)view.findViewById(R.id.fitness_main_graph_line_layout);
		mFitnessLineGraph = new FitnessLineGraph();
		mGraphicalView = mFitnessLineGraph.getView(getActivity());
		mRelativeLayout.addView(mGraphicalView);
		mGraphicalView.setBackgroundColor(Color.TRANSPARENT);
		
		startPixelControlBar = FitnessUtils.convertDpToPixels(START_X_DP_CONTROL_BAR,getActivity());
		
		mFitnessPreference = new FitnessPreference(getActivity());
		
        User user = mFitnessApplication.getFitnessService().getFitnessManager().getUser();
        mFitnessLineGraph.setKarvonen(
        		FitnessUtils.karvonen(user, FitnessUtils.STRENGTH_WARM_UP),
        		FitnessUtils.karvonen(user, FitnessUtils.STRENGTH_FAT_BUNNING), 
        		FitnessUtils.karvonen(user, FitnessUtils.STRENGTH_COMMON), 
        		FitnessUtils.karvonen(user, FitnessUtils.STRENGTH_HIGH));
        
        FitnessFontUtils.setViewGroupFont((ViewGroup)view);
        loadKarvonen(view);
        return view;
	}
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
					if(!mFitnessApplication.getFitnessService().isCommAvail())
					{
						mTime.setText("");
					}
					else
					{
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
				}
			};
			handler.post(updater);
	}
	void setNoStableHeartrate(){
    	mMessage.setText(getResources().getString(R.string.string_fitness_main_message_no_stable_heartrate));
    	mMessage.setTextColor(getResources().getColor(R.color.donut_progress_not_used));
    	mControlBar.setVisibility(View.INVISIBLE);
        mMessage.setOnClickListener(this);
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

	FitnessBroadcastService mFitnessBroadcastService;
	
	@Override
	public void onResume() {
		super.onResume();
		
		//안정심박수가 없을대 설정
        if(mFitnessApplication.getFitnessService().getFitnessManager().isOldCheckStableHeartrate())
        {
        	setNoStableHeartrate();
        }
        else 
        {
        	mFitnessDataList = mFitnessApplication.getFitnessService().getFitnessManager().getFitnessDataList();
    		initGraphData(mFitnessDataList);
    		setControlBarPositon();
        }
        
		mFitnessBroadcastService = mFitnessApplication.mFitnessBroadcastService;
		mFitnessBroadcastService.addBroadcastObserver(mFitnessBroadcastListener);
		
		User user = mFitnessApplication.getFitnessService().getCurrentUser();
		int state = mFitnessApplication.getFitnessService().getFitnessManager(user).getState();
		int strength = mFitnessApplication.getFitnessService().getFitnessManager(user).getStrength();
		mFitnessBroadcastListener.onChangeUserState(user, state);
		mFitnessBroadcastListener.onChangeAimStrength(user, strength);
		mGraphicalView.addPanListener(mPanListener);
		timer();
	}
	
	public void onPause()
	{
		super.onStop();
		mFitnessBroadcastService.removeBoradcastObserver(mFitnessBroadcastListener);
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

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId()) 
		{
		case R.id.fitness_main_graph_message_textview:
			Intent intent = new Intent(getActivity(),FitnessAimManagerActivity.class);
			intent.putExtra(FitnessAimManagerActivity.AIM_MANAGER_START_PAGER, FitnessAimManagerActivity.PAGER_STABLE_HEARTRATE);
			startActivity(intent);
			break;

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
	
	FitnessBroadcastListener mFitnessBroadcastListener = new FitnessBroadcastListener(){

		@Override
		public void onReceiveFitnessData(User user, final FitnessData data) {
			// TODO Auto-generated method stub
			if(mFitnessApplication.getFitnessService().getCurrentUser().equals(user)==false)return;
			if(mFitnessApplication.getFitnessService().getFitnessManager().isOldCheckStableHeartrate())return;
			
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
			
			Runnable runnable = new Runnable() {
				
				@Override
				public void run() {
					if(mFitnessApplication.getFitnessService().getFitnessManager().isOldCheckStableHeartrate())return;
					if(checkTime!=null)return;
					
					checkTime = new TimerTask() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							this.cancel();
							skipMessage = false;
							checkTime = null;
							int state = mFitnessApplication.getFitnessService().getFitnessManager(user).getState();
							mFitnessBroadcastListener.onChangeUserState(user,state);
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
			if(mFitnessApplication.getFitnessService().getFitnessManager().isOldCheckStableHeartrate())return;
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
}
