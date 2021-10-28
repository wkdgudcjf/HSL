package com.samsung.hsl.fitnesstrainer.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.GestureDetectorCompat;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.samsung.hsl.fitnesstrainer.R;
import com.samsung.hsl.fitnesstrainer.comm.FitnessDevice;
import com.samsung.hsl.fitnesstrainer.comm.IFitnessCommManager;
import com.samsung.hsl.fitnesstrainer.service.FitnessBroadcastService;
import com.samsung.hsl.fitnesstrainer.service.FitnessBroadcastService.FitnessBroadcastCallback;
import com.samsung.hsl.fitnesstrainer.service.FitnessBroadcastService.FitnessBroadcastListener;
import com.samsung.hsl.fitnesstrainer.service.FitnessFontUtils;
import com.samsung.hsl.fitnesstrainer.service.FitnessManager;
import com.samsung.hsl.fitnesstrainer.service.FitnessTrainerApplication;
import com.samsung.hsl.fitnesstrainer.service.FitnessTrainerService;
import com.samsung.hsl.fitnesstrainer.sqlite.FitnessData;
import com.samsung.hsl.fitnesstrainer.sqlite.User;

public class FitnessTrainerMainActivity extends FitnessTrainerBaseActivity {
	private static final String tag = FitnessTrainerMainActivity.class
			.getName();

	ListView mListView;
	ListViewAdapter mAdapter;

	Handler handler = new Handler();
	ArrayList<FitnessDevice> mFitnessDeviceList = new ArrayList<FitnessDevice>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState, R.layout.layout_fitness_trainer_main);

		mTopBarTitleText.setText(getResources().getString(R.string.string_fitness_main_title));
		mTopBarLeftButton.setVisibility(View.VISIBLE);
		mTopBarRightText.setVisibility(View.VISIBLE);
		
		mListView = (ListView) findViewById(R.id.fitness_memeber_main_listview);
		
		mAdapter = new ListViewAdapter();
		mListView.setAdapter(mAdapter);
		
		refreshList();
	}

	Timer timer;
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onStart();
		FitnessBroadcastService.getInstance(this).addBroadcastObserver(FitnessBroadcastService.BROADCAST_START_EXERCISE,mFitnessStartExerciseCallback);
		FitnessBroadcastService.getInstance(this).addBroadcastObserver(FitnessBroadcastService.BROADCAST_END_EXERCISE,mFitnessEndExerciseCallback);
		FitnessBroadcastService.getInstance(this).addBroadcastObserver(FitnessBroadcastService.BROADCAST_CHANGE_DEVICE_LIST,mFitnessBroadcastCallback);
		FitnessBroadcastService.getInstance(this).addBroadcastObserver(mFitnessBroadcastListener);
		
		refreshList();
		
		timer = new Timer();
		TimerTask task = new TimerTask(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				runOnUiThread(runnable);
			}
		};
		
		timer.schedule(task, 2000, 2000);
		
		
	}

	void refreshList(){
		mFitnessDeviceList.clear();
		FitnessTrainerService service = ((FitnessTrainerApplication)getApplication()).getFitnessService();
		if(service!=null){
			List<FitnessDevice> deviceList = service.getConnectedDeviceList();
			for(int i=0;i<deviceList.size();i++){
				
				User user = deviceList.get(i).user;
				if(user!=null)mFitnessDeviceList.add(deviceList.get(i));
			}
		}
		mAdapter.refreshAdapter();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onStop();
		FitnessBroadcastService.getInstance(this).removeBoradcastObserver(FitnessBroadcastService.BROADCAST_CHANGE_DEVICE_LIST,mFitnessBroadcastCallback);
		FitnessBroadcastService.getInstance(this).removeBoradcastObserver(FitnessBroadcastService.BROADCAST_START_EXERCISE,mFitnessStartExerciseCallback);
		FitnessBroadcastService.getInstance(this).removeBoradcastObserver(FitnessBroadcastService.BROADCAST_END_EXERCISE,mFitnessEndExerciseCallback);
		FitnessBroadcastService.getInstance(this).removeBoradcastObserver(mFitnessBroadcastListener);
		timer.cancel();
	}
	Runnable startupdater = new Runnable() {
		public void run() {
			long starttime = ((FitnessTrainerApplication)getApplication()).getFitnessService().mStarttime;
			long curtime = System.currentTimeMillis();
			long runtime = (curtime - starttime)/1000;
			long hour = runtime/3600;
			long min = (runtime-(hour*3600))/60;
			long sec = runtime-(hour*3600)-(min*60);
			if(hour==0)
			{
				if(min==0)
				{
					mTopBarRightText.setText("time = "+sec+" sec");
				}
				else
				{
					mTopBarRightText.setText("time = "+min+" : "+sec);
				}
			}
			else
			{
				mTopBarRightText.setText("time = "+hour+" : "+min+" : "+sec);
			}
		}
	};
	Runnable endupdater = new Runnable() {
		public void run() {
			mTopBarRightText.setText("");
		}
	};
	FitnessBroadcastCallback mFitnessStartExerciseCallback = new FitnessBroadcastCallback(){

		@Override
		public void receive() {
			// TODO Auto-generated method stub
			handler.post(startupdater);
			// 운동시작
		}
		
	};
	FitnessBroadcastCallback mFitnessEndExerciseCallback = new FitnessBroadcastCallback(){

		@Override
		public void receive() {
			// TODO Auto-generated method stub
			handler.post(endupdater);
			((FitnessTrainerApplication)getApplication()).getFitnessService().disconnectAll();
			//모든 연결 끊기.
			//운동종료
		}
		
	};

	class ListViewAdapter extends BaseAdapter {
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mFitnessDeviceList.size() + 1;
		}

		@Override
		public Object getItem(int index) {
			// TODO Auto-generated method stub
			if (index >= mFitnessDeviceList.size())
				return null;
			return mFitnessDeviceList.get(index);
		}

		@Override
		public long getItemId(int index) {
			// TODO Auto-generated method stub
			return 0;
		}
		public void refreshAdapter() {
			this.notifyDataSetChanged();
		}
		@Override
		public View getView(final int position, View view, ViewGroup parent) {
			// TODO Auto-generated method stub
			if (view == null) {
				LayoutInflater mInflater = (LayoutInflater) FitnessTrainerMainActivity.this
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = mInflater.inflate(R.layout.item_fitness_trainer_main,
						parent, false);
				final ViewHolder holder = new ViewHolder();
				holder.container = (RelativeLayout) view
						.findViewById(R.id.fitness_trainer_main_item);
				holder.deleteButton = (ImageButton) view
						.findViewById(R.id.fitness_trainer_main_delete_button);
				holder.addButton = (ImageButton) view
						.findViewById(R.id.fitness_trainer_main_add_button);
				holder.swipeTouchListener = new GestureDetectorCompat(
						FitnessTrainerMainActivity.this,
						new OnSwipeTouchListener(
								FitnessTrainerMainActivity.this, view));
				
				holder.container.setOnTouchListener(new OnTouchListener() {
					@Override
					public boolean onTouch(View v, MotionEvent event) {
						return holder.swipeTouchListener.onTouchEvent(event);
					}
				});
				
				view.setTag(holder);
			}

			// 레이아웃 Visible 설정
			LinearLayout itemLayout = ((LinearLayout) view.findViewById(R.id.fitness_trainer_main_item_container));
			final RelativeLayout itemBtnLayout = ((RelativeLayout) view.findViewById(R.id.fitness_trainer_main_item_button_container));
			RelativeLayout itemAddLayout = ((RelativeLayout) view.findViewById(R.id.fitness_trainer_main_add_button_container));
						
			// 이벤트를 위한 리스너 설정
			final ViewHolder holder = (ViewHolder) view.getTag();
			
			holder.container.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					if(position==mFitnessDeviceList.size()){
						Intent intent = new Intent(FitnessTrainerMainActivity.this, FitnessTrainerMemberConnectAddActivity.class);
						startActivity(intent);
					}else {
						((FitnessTrainerApplication)getApplication()).getFitnessService().setCurrentUser(mFitnessDeviceList.get(position).user);
						Intent intent = new Intent(FitnessTrainerMainActivity.this, FitnessTrainerUserReportActivity.class);
						startActivity(intent);
					}
				}
			});
			holder.deleteButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					((FitnessTrainerApplication)getApplication()).getFitnessService().disconnect(mFitnessDeviceList.get(position));
					itemBtnLayout.setVisibility(View.GONE);
					ListViewAdapter.this.notifyDataSetChanged();
				}
			});

			
			holder.addButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					holder.container.callOnClick();
				}
			});

			// 마지막 위치면
			if (position >= mFitnessDeviceList.size()) {
				itemLayout.setVisibility(View.INVISIBLE);
				itemBtnLayout.setVisibility(View.GONE);
				itemAddLayout.setVisibility(View.VISIBLE);

			} else {
				itemLayout.setVisibility(View.VISIBLE);
				itemAddLayout.setVisibility(View.INVISIBLE);
				
				// 레아이웃 값 설정
				FitnessDevice device = mFitnessDeviceList.get(position);
				FitnessManager fitnessManager = ((FitnessTrainerApplication)getApplication()).getFitnessService().getFitnessManager(device.user);
				
				FitnessData data = fitnessManager.getCurrentFitnessData();
				int state = fitnessManager.getState();
				
				TextView nameText = (TextView)view.findViewById(R.id.fitness_trainer_main_item_name);
				TextView deviceText = (TextView)view.findViewById(R.id.fitness_trainer_main_item_device_name);
				TextView heartrateText = (TextView)view.findViewById(R.id.fitness_trainer_main_item_heartrate_value);
				final ImageView heartrateImage = (ImageView)view.findViewById(R.id.fitness_trainer_main_item_heartrate_icon);
				TextView calorieText = (TextView)view.findViewById(R.id.fitness_trainer_main_item_calorie);
				TextView skinTemperatureText = (TextView)view.findViewById(R.id.fitness_trainer_main_item_skin_temperature);
				TextView stateText = (TextView)view.findViewById(R.id.fitness_trainer_main_item_state);

				ImageView icon = (ImageView)view.findViewById(R.id.fitness_trainer_main_item_device_battery_icon);
				TextView battery = (TextView)view.findViewById(R.id.fitness_trainer_main_item_device_battery_text);
				TextView version = (TextView)view.findViewById(R.id.fitness_trainer_main_item_device_version);
				ProgressBar prog = (ProgressBar)view.findViewById(R.id.fitness_trainer_main_item_device_battery);
				
				if(device.commType==IFitnessCommManager.Type.BLUETOOTH_LOW_ENERGY)
				{
					battery.setText(device.battery+"%");
					prog.setProgress(device.battery);
					version.setText(device.sw+" "+device.hw);
				}
				else
				{
					battery.setVisibility(View.INVISIBLE);
					prog.setVisibility(View.INVISIBLE);
					version.setVisibility(View.INVISIBLE);
					icon.setVisibility(View.INVISIBLE);
				}
				nameText.setText(device.user.name);
				deviceText.setText(getString(R.string.string_fitness_trainer_main_device_column)+" : "+device.name);
				if(data!=null){
					heartrateText.setText(String.valueOf(data.filterHeartrate));
					calorieText.setText(String.valueOf(data.consumeCalorie)+getString(R.string.string_fitness_trainer_main_calroie_unit));
					skinTemperatureText.setText(String.valueOf(data.skinTemperature)+getString(R.string.string_fitness_trainer_main_temperature_unit));
				}

				stateText.setText(getStateString(state));
				stateText.setTextColor(getStateColor(state));

				if(state==FitnessManager.STATE_WARM_UP_EXCEED_AIM || state==FitnessManager.STATE_EXCEED_AIM){
					heartrateImage.setImageResource(R.drawable.fitness_member_main_02_exceed_heart);
					heartrateText.setTextColor(getResources().getColor(R.color.trainer_main_exceed));
				}else {
					heartrateImage.setImageResource(R.drawable.fitness_member_main_02_aim_heart);
					heartrateText.setTextColor(getResources().getColor(R.color.trainer_main_aim));
				}
				
				if(device.battery!=0){
					Handler mHanlder2 = new Handler() {
						 @Override
					        public void handleMessage(Message msg)
						 	{
					            super.handleMessage(msg);
					            if(heartrateImage.getVisibility()==View.VISIBLE)
					            	heartrateImage.setVisibility(View.INVISIBLE);
					        }
					};
					mHanlder2.sendEmptyMessage(0);
					Handler mHandler = new Handler();
					mHandler.postDelayed(new Runnable() {
					      @SuppressLint("NewApi")
					      @Override
					      public void run()
					      {
					    	  if(heartrateImage.getVisibility()==View.INVISIBLE)
					    		  heartrateImage.setVisibility(View.VISIBLE);
					   	  }
					  }, 500);
				}
				FitnessFontUtils.setViewGroupFont((ViewGroup)view);
			}

			

			return view;
		}

	}
	
	static class ViewHolder {
		RelativeLayout container;
		ImageButton deleteButton;
		ImageButton addButton;
		GestureDetectorCompat swipeTouchListener;
	}
	
	FitnessBroadcastCallback mFitnessBroadcastCallback = new FitnessBroadcastCallback(){

		@Override
		public void receive() {
			// TODO Auto-generated method stub
			isChanged = true;
//			runOnUiThread(runnable);
		}
		
	};

	boolean isChanged = false;
	FitnessBroadcastListener mFitnessBroadcastListener = new FitnessBroadcastListener() {

		@Override
		public void onReceiveFitnessData(User user, FitnessData data) {
			// TODO Auto-generated method stub
			isChanged = true;
//			runOnUiThread(runnable);
		}

		@Override
		public void onReceiveFitnessFall(User user) {
			// TODO Auto-generated method stub
			isChanged = true;
//			runOnUiThread(runnable);
		}

		@Override
		public void onChangeAimStrength(User user, int strength) {
			// TODO Auto-generated method stub
			isChanged = true;
//			runOnUiThread(runnable);
		}

		@Override
		public void onChangeUserState(User user, int state) {
			// TODO Auto-generated method stub
			isChanged = true;
//			runOnUiThread(runnable);
		}

		@Override
		public void onChangeUserInfo(User user) {
			// TODO Auto-generated method stub
			
		}

	};

	Runnable runnable = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			refreshList();
		}
	};
	
	String getStateString(int state){
		if(state==FitnessManager.STATE_STOP)return getResources().getString(R.string.string_fitness_trainer_user_report_state_stop);
		else if(state==FitnessManager.STATE_WARM_UP_NEED)return getResources().getString(R.string.string_fitness_trainer_user_report_state_warm_up_need);
		else if(state==FitnessManager.STATE_WARM_UP_EXCEED_AIM)return getResources().getString(R.string.string_fitness_trainer_user_report_state_warm_up_exceed_aim);
		else if(state==FitnessManager.STATE_WARM_UP_COMPLETE)return getResources().getString(R.string.string_fitness_trainer_user_report_state_warm_up_complete);
		else if(state==FitnessManager.STATE_SHORT_AIM)return getResources().getString(R.string.string_fitness_trainer_user_report_state_short_aim);
		else if(state==FitnessManager.STATE_EXCEED_AIM)return getResources().getString(R.string.string_fitness_trainer_user_report_state_exceed_aim);
		else if(state==FitnessManager.STATE_ARCHIEVE_AIM)return getResources().getString(R.string.string_fitness_trainer_user_report_state_archieve_aim);
		return getStateString(FitnessManager.STATE_STOP);
	}
	
	int getStateColor(int state){
		if(state==FitnessManager.STATE_STOP)return getResources().getColor(R.color.range_color_normal);
		else if(state==FitnessManager.STATE_WARM_UP_NEED)return getResources().getColor(R.color.range_color_normal);
		else if(state==FitnessManager.STATE_WARM_UP_EXCEED_AIM)return getResources().getColor(R.color.range_color_high);
		else if(state==FitnessManager.STATE_WARM_UP_COMPLETE)return getResources().getColor(R.color.range_color_aim);
		else if(state==FitnessManager.STATE_SHORT_AIM)return getResources().getColor(R.color.range_color_normal);
		else if(state==FitnessManager.STATE_EXCEED_AIM)return getResources().getColor(R.color.range_color_high);
		else if(state==FitnessManager.STATE_ARCHIEVE_AIM)return getResources().getColor(R.color.range_color_aim);
		return getStateColor(FitnessManager.STATE_STOP);
	}
}
