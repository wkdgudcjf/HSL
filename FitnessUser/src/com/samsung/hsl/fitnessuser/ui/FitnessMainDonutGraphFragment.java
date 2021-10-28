package com.samsung.hsl.fitnessuser.ui;
import java.util.Timer;
import java.util.TimerTask;

import com.github.lzyzsd.circleprogress.DonutProgress;
import com.samsung.hsl.fitnessuser.R;
import com.samsung.hsl.fitnessuser.service.FitnessBroadcastService;
import com.samsung.hsl.fitnessuser.service.FitnessBroadcastService.FitnessBroadcastListener;
import com.samsung.hsl.fitnessuser.service.FitnessFontUtils;
import com.samsung.hsl.fitnessuser.service.FitnessManager;
import com.samsung.hsl.fitnessuser.service.FitnessPreference;
import com.samsung.hsl.fitnessuser.service.FitnessUserApplication;
import com.samsung.hsl.fitnessuser.sqlite.FitnessData;
import com.samsung.hsl.fitnessuser.sqlite.User;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class FitnessMainDonutGraphFragment extends Fragment implements OnClickListener {
	private static final String tag = FitnessMainDonutGraphFragment.class.getName();
	
	int state = FitnessManager.STATE_WARM_UP_NEED;
	FitnessUserApplication mFitnessApplication;
	DonutProgress mHeartrateProgress;
	ImageView mHeartrateIcon;
	TextView mHeartrateProgressValue;
	TextView mMessage;
	TextView mTime;
	FitnessPreference mFitnessPreference;
	Handler mHandler = new Handler();
    public FitnessMainDonutGraphFragment() {
        // Empty constructor required for fragment subclasses
    	
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_fitness_main_graph_donutprogress, container, false);
        
        mHeartrateProgress = (DonutProgress)view.findViewById(R.id.fitness_main_graph_donutprogress);
        mHeartrateIcon = (ImageView)view.findViewById(R.id.fitness_main_graph_heart_imageview);
        mHeartrateProgressValue = (TextView)view.findViewById(R.id.fitness_main_graph_heart_value_textview);
        mFitnessApplication = ((FitnessUserApplication)getActivity().getApplication());
        mMessage = (TextView)view.findViewById(R.id.fitness_main_graph_message_textview);
        mTime = (TextView)view.findViewById(R.id.fitness_main_graph_time_textview);
        mFitnessPreference = new FitnessPreference(getActivity());
        
        //안정심박수가 없을대 설정
        if(mFitnessApplication.getFitnessService().getFitnessManager().isOldCheckStableHeartrate()){
        	setNoStableHeartrate();
        }
        
        FitnessFontUtils.setViewGroupFont((ViewGroup)view);
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
    	mHeartrateIcon.setImageResource(R.drawable.fitness_main_no_heart_rate_grey_heart);
    	mMessage.setText(getResources().getString(R.string.string_fitness_main_message_no_stable_heartrate));
    	mMessage.setTextColor(getResources().getColor(R.color.donut_progress_not_used));
    	mHeartrateProgress.setFinishedStrokeColor(getResources().getColor(R.color.donut_progress_not_used));
    	mHeartrateProgress.setUnfinishedStrokeColor(getResources().getColor(R.color.donut_progress_not_used));
    	mHeartrateProgress.setProgress(0);
        mMessage.setOnClickListener(this);
    }
    
    @Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mFitnessBroadcastService = ((FitnessUserApplication)getActivity().getApplication()).mFitnessBroadcastService;
		mFitnessBroadcastService.addBroadcastObserver(mFitnessBroadcastListener);
		User user = mFitnessApplication.getFitnessService().getCurrentUser();
		int state = mFitnessApplication.getFitnessService().getFitnessManager(user).getState();
		mFitnessBroadcastListener.onChangeUserState(user,state);
		timer();
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mFitnessBroadcastService.removeBoradcastObserver(mFitnessBroadcastListener);
	}
	
	Handler mHanlder2 = new Handler() {
		 @Override
	        public void handleMessage(Message msg)
		 	{
	            super.handleMessage(msg);
	            mHeartrateIcon.setVisibility(View.INVISIBLE);
	        }
	};
	
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.fitness_main_graph_message_textview:
			Intent intent = new Intent(getActivity(),FitnessAimManagerActivity.class);
			intent.putExtra(FitnessAimManagerActivity.AIM_MANAGER_START_PAGER, FitnessAimManagerActivity.PAGER_STABLE_HEARTRATE);
			startActivity(intent);
			break;

		}
	}
	
	boolean skipMessage = false;
	private static final int SKIP_MESSAGE_TIME = 5000;
	TimerTask checkTime;
	FitnessBroadcastService mFitnessBroadcastService;
	FitnessBroadcastListener mFitnessBroadcastListener = new FitnessBroadcastListener(){

		@Override
		public void onReceiveFitnessData(User user, final FitnessData data) {
			// TODO Auto-generated method stub
			if(mFitnessApplication.getFitnessService().getCurrentUser().equals(user)==false)return;
			
			mHanlder2.sendEmptyMessage(0);
			mHandler.postDelayed(new Runnable() {
			      @SuppressLint("NewApi")
			      @Override
			      public void run()
			      {
			    	  mHeartrateIcon.setVisibility(View.VISIBLE);
			   	  }
			  }, 500);
			
			if(mFitnessApplication.getFitnessService().getFitnessManager(user).isOldCheckStableHeartrate())return;
			
			Runnable runnable = new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					mHeartrateProgress.setProgress(data.filterHeartrate);
					mHeartrateProgressValue.setText(String.valueOf(data.filterHeartrate));	
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
					if(mFitnessApplication.getFitnessService().getFitnessManager(user).isOldCheckStableHeartrate())return;
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
		public void onChangeAimStrength(User user, int strength) {
			// TODO Auto-generated method stub
			if(mFitnessApplication.getFitnessService().getCurrentUser().equals(user)==false)return;
		}

		@Override
		public void onChangeUserState(final User user, final int state) {
			// TODO Auto-generated method stub
			if(mFitnessApplication.getFitnessService().getCurrentUser().equals(user)==false)return;
			
			Runnable runnable = new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					int color = 0x000000;
					int progressColor = 0x000000;
					int progressUnfinishedColor = 0x000000;
					int heartrateIconId = R.drawable.fitness_main_graph_heart;
					String message = "";
					Log.i("여기오냐","여기오냐");
					// TODO Auto-generated method stub
					if(mFitnessApplication.getFitnessService().getFitnessManager(user).isOldCheckStableHeartrate())return;
					if(skipMessage)return;
					
					if(state==FitnessManager.STATE_STOP){
						message = getResources().getString(R.string.string_fitness_main_message_none);
						color = getResources().getColor(R.color.range_color_normal);
						progressColor = getResources().getColor(R.color.donut_progress_not_used);
						progressUnfinishedColor = progressColor;
						heartrateIconId = R.drawable.fitness_main_no_heart_rate_grey_heart;
					}
					else if(state==FitnessManager.STATE_WARM_UP_NEED){
						message = getResources().getString(R.string.string_fitness_main_message_warm_up_need);
						color = getResources().getColor(R.color.range_color_normal);
						progressColor = getResources().getColor(R.color.donut_progress_short);
						progressUnfinishedColor = getResources().getColor(R.color.donut_progress_unfinished);
					}
					else if(state==FitnessManager.STATE_WARM_UP_EXCEED_AIM){
						message = getResources().getString(R.string.string_fitness_main_message_warm_up_exeed_aim);
						color = getResources().getColor(R.color.range_color_high);
						progressColor = getResources().getColor(R.color.donut_progress_high);
						progressUnfinishedColor = getResources().getColor(R.color.donut_progress_unfinished);
					}
					else if(state==FitnessManager.STATE_WARM_UP_COMPLETE){
						message = getResources().getString(R.string.string_fitness_main_message_warm_up_complete);
						color = getResources().getColor(R.color.range_color_aim);
						progressColor = getResources().getColor(R.color.donut_progress_aim);
						progressUnfinishedColor = getResources().getColor(R.color.donut_progress_unfinished);
					}
					else if(state==FitnessManager.STATE_SHORT_AIM){
						message = getResources().getString(R.string.string_fitness_main_message_short_aim);
						color = getResources().getColor(R.color.range_color_normal);
						progressColor = getResources().getColor(R.color.donut_progress_short);
						progressUnfinishedColor = getResources().getColor(R.color.donut_progress_unfinished);
					}
					else if(state==FitnessManager.STATE_ARCHIEVE_AIM){
						message = getResources().getString(R.string.string_fitness_main_message_achieve_aim);
						color = getResources().getColor(R.color.range_color_aim);
						progressColor = getResources().getColor(R.color.donut_progress_aim);
						progressUnfinishedColor = getResources().getColor(R.color.donut_progress_unfinished);
					}
					else if(state==FitnessManager.STATE_EXCEED_AIM){
						message = getResources().getString(R.string.string_fitness_main_message_exceed_aim);
						color = getResources().getColor(R.color.range_color_high);
						progressColor = getResources().getColor(R.color.donut_progress_high);
						progressUnfinishedColor = getResources().getColor(R.color.donut_progress_unfinished);
					}
					
					mMessage.setText(message);
					mMessage.setTextColor(color);
					mHeartrateProgress.setFinishedStrokeColor(progressColor);
					mHeartrateProgress.setUnfinishedStrokeColor(progressUnfinishedColor);
					mHeartrateProgressValue.setTextColor(progressColor);
					mHeartrateIcon.setImageResource(heartrateIconId);
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