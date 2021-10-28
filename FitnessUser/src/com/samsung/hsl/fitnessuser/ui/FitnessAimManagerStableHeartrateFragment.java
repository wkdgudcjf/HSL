package com.samsung.hsl.fitnessuser.ui;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.lzyzsd.circleprogress.DonutProgress;
import com.samsung.hsl.fitnessuser.R;
import com.samsung.hsl.fitnessuser.service.FitnessBroadcastService;
import com.samsung.hsl.fitnessuser.service.FitnessBroadcastService.FitnessBroadcastListener;
import com.samsung.hsl.fitnessuser.service.FitnessFontUtils;
import com.samsung.hsl.fitnessuser.service.FitnessUserApplication;
import com.samsung.hsl.fitnessuser.service.FitnessUtils;
import com.samsung.hsl.fitnessuser.sqlite.FitnessData;
import com.samsung.hsl.fitnessuser.sqlite.User;

public class FitnessAimManagerStableHeartrateFragment extends Fragment implements OnClickListener {
	private static final String tag = FitnessAimManagerActivity.class.getName();
	
	private static final int STATE_READY = 0x00;
	private static final int STATE_START = 0x01;
	private static final int STATE_COMPLETE = 0x02;
	int currentState = STATE_READY;
	
	FitnessUserApplication mFitnessApplication;
	FitnessBroadcastService mFitnessBroadcastService;
	
	ImageView mHeartrateStaticImage;
	DonutProgress mHeartrateProgress;
	TextView mHeartrateValue;
	TextView mMessage;
	Button mHeartrateButton;
	ImageView heart_bpm_imageview;
	ArrayList<FitnessData> mFitnessDataList = new ArrayList<FitnessData>();
	
    public FitnessAimManagerStableHeartrateFragment() {
        // Empty constructor required for fragment subclasses
    	
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_fitness_aim_manager_stable_heartrate, container, false);
        
        mHeartrateStaticImage = (ImageView)view.findViewById(R.id.fitness_aim_manager_donutprogress_imageview);
        mHeartrateProgress = (DonutProgress)view.findViewById(R.id.fitness_aim_manager_donutprogress);
        mMessage = (TextView)view.findViewById(R.id.fitness_aim_manager_message_textview);
        mHeartrateValue = (TextView)view.findViewById(R.id.fitness_aim_manager_donutprogress_value_textview);
        mFitnessApplication = ((FitnessUserApplication)getActivity().getApplication());
        mHeartrateButton = (Button)view.findViewById(R.id.fitness_aim_manager_stable_heartrate_button);
        heart_bpm_imageview = (ImageView)view.findViewById(R.id.fitness_aim_graph_heart_imageview);
        mHeartrateButton.setOnClickListener(this);
        
        currentState = STATE_READY;
        
        FitnessFontUtils.setViewGroupFont((ViewGroup)view);
        
        return view;
    }
    
    
    
    @Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mFitnessBroadcastService = ((FitnessUserApplication)getActivity().getApplication()).mFitnessBroadcastService;
		mFitnessBroadcastService.addBroadcastObserver(mFitnessBroadcastListener);
	}
	
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		mFitnessBroadcastService.removeBoradcastObserver(mFitnessBroadcastListener);
		mHandler.removeCallbacks(timeCheck);
	}
	
	Handler mHanlder2 = new Handler() {
		 @Override
	        public void handleMessage(Message msg)
		 	{
	            super.handleMessage(msg);
	            heart_bpm_imageview.setVisibility(View.INVISIBLE);
	        }
	};
	

	Handler mHandler = new Handler();
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (currentState) {
		//시작버튼을 누른 상황
		case STATE_READY:
			if(((FitnessBaseActivity)getActivity()).mTestTouchLayout.getVisibility()==View.INVISIBLE){
				if(!(mFitnessApplication.getFitnessService().isCommAvail()))
				{
					Intent intent = new Intent(getActivity(), FitnessConnectionActivity.class);
					startActivity(intent);
					return;
				}
			}
			mHeartrateStaticImage.setVisibility(View.INVISIBLE);
			mHeartrateProgress.setVisibility(View.VISIBLE);
			mMessage.setText(getResources().getString(R.string.string_fitness_aim_manager_current_stable_heartrate));
			mHeartrateButton.setText(getResources().getString(R.string.string_fitness_aim_manager_stable_cancel_button));
			mFitnessDataList.clear();
			mHandler = new Handler();
			mHandler.postDelayed(timeCheck, CHECK_STABLE_HEARTRATE_TIME);
			currentState = STATE_START;
			break;
		//취소버튼을 누른 상황
		case STATE_START:
			mHeartrateStaticImage.setVisibility(View.VISIBLE);
			mHeartrateProgress.setVisibility(View.INVISIBLE);
			mMessage.setText(getResources().getString(R.string.string_fitness_aim_manager_ready_stable_heartrate));
			mHeartrateButton.setText(getResources().getString(R.string.string_fitness_aim_manager_stable_start_button));
			mFitnessDataList.clear();
			mHandler.removeCallbacks(timeCheck);
			currentState = STATE_READY;
			break;
		//완료버튼을 누른 상황
		case STATE_COMPLETE:
			mFitnessApplication.getFitnessService().getFitnessManager().setStableHeartrate(FitnessUtils.average(mFitnessDataList, 0, FitnessUtils.MAX_HEARTRATE));
			goToMain();
			break;
		}
	}

	//테스트용 20초간 측정
	private static final int CHECK_STABLE_HEARTRATE_TIME = 20 * 1000;
	//5분간 측정
//	private static final int CHECK_STABLE_HEARTRATE_TIME = 5 * 60 * 1000;
	Runnable timeCheck = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			currentState = STATE_COMPLETE;
			mHeartrateButton.setText(getResources().getString(R.string.string_fitness_aim_manager_complete_button));
			int stableHeartrate = FitnessUtils.average(mFitnessDataList, 0, FitnessUtils.MAX_HEARTRATE);
			mMessage.setText(getResources().getString(R.string.string_fitness_aim_manager_complete_stable_heartrate)+stableHeartrate);
		}
	};
	
	void goToMain(){
		getActivity().finish();
	}
	
	FitnessBroadcastListener mFitnessBroadcastListener = new FitnessBroadcastListener(){

		@Override
		public void onReceiveFitnessData(final User user, FitnessData data) {
			// TODO Auto-generated method stub
			if(mFitnessApplication.getFitnessService().getCurrentUser().equals(user)==false)return;
			
			mHanlder2.sendEmptyMessage(0);
			mHandler.postDelayed(new Runnable() {
			      @SuppressLint("NewApi")
			      @Override
			      public void run()
			      {
			    	  heart_bpm_imageview.setVisibility(View.VISIBLE);
			   	  }
			  }, 500);
			
			Runnable runnable = new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					FitnessData fitness = mFitnessApplication.getFitnessService().getFitnessManager(user).getCurrentFitnessData();
					mHeartrateValue.setText(String.valueOf(fitness.filterHeartrate));
					mHeartrateProgress.setProgress(fitness.filterHeartrate);
					
					if(currentState==STATE_START){
						mFitnessDataList.add(fitness);
						int stableHeartrate = FitnessUtils.average(mFitnessDataList, 0, FitnessUtils.MAX_HEARTRATE);
						mMessage.setText(getResources().getString(R.string.string_fitness_aim_manager_current_stable_heartrate));
					}
				}
			};
			
			getActivity().runOnUiThread(runnable);
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
			
		}
		
	};
}