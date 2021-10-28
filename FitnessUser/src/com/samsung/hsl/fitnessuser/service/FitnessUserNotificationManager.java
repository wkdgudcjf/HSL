package com.samsung.hsl.fitnessuser.service;

import java.util.List;

import com.github.lzyzsd.circleprogress.DonutProgress;
import com.samsung.hsl.fitnessuser.R;
import com.samsung.hsl.fitnessuser.sqlite.User;
import com.samsung.hsl.fitnessuser.ui.FitnessMainActivity;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;


public class FitnessUserNotificationManager {
	public boolean isPopup = false;
	View mPopupView;                            //항상 보이게 할 뷰
	private WindowManager.LayoutParams mParams;  //layout params 객체. 뷰의 위치 및 크기
	private WindowManager mWindowManager;          //윈도우 매니저
	private Handler handler = new Handler();
	FitnessUserService mFitnessService;
	
	public FitnessUserNotificationManager(FitnessUserService service){
		mFitnessService = service;
		createView();
	}
	
	public FitnessUserService getFitnessService(){return mFitnessService;}
	
	public void createView() {
		// TODO Auto-generated method stub
		if(mPopupView==null)
		{
			LayoutInflater mInflater = (LayoutInflater)getFitnessService().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mPopupView = mInflater.inflate(R.layout.layout_fitenss_popup, null);
		}
	     //최상위 윈도우에 넣기 위한 설정
	     mParams = new WindowManager.LayoutParams(
	            WindowManager.LayoutParams.WRAP_CONTENT,
	            WindowManager.LayoutParams.WRAP_CONTENT,
	            WindowManager.LayoutParams.TYPE_PHONE,//항상 최 상위. 터치 이벤트 받을 수 있음.
	            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,  //포커스를 가지지 않음
	            PixelFormat.TRANSLUCENT);                                        //투명
	     mParams.gravity = Gravity.CENTER;                   //왼쪽 상단에 위치하게 함.
	}
	
	public void close(){
		if(mWindowManager != null)
		{        //서비스 종료시 뷰 제거. *중요 : 뷰를 꼭 제거 해야함.
	         if(mPopupView != null)
	         {
	        	 mWindowManager.removeView(mPopupView);
	        	 isPopup = false;
	         }
	    }
	}

	public void exceedHeartrate(User user,final int heartrate) {
		// TODO Auto-generated method stub
		ActivityManager am = (ActivityManager)getFitnessService().getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningTaskInfo> Info = am.getRunningTasks(1);
		ComponentName topActivity = Info.get(0).topActivity;
		String topactivityname = topActivity.getPackageName(); // 패키지명
		FitnessUserApplication fbm = (FitnessUserApplication)getFitnessService().getApplication();
		if(topactivityname.compareTo(fbm.getPackageName()) == 0) // 현재 앱이 윈도우에 있을경우 
		{
			return;
		}
		else
		{
	        mWindowManager = (WindowManager) getFitnessService().getSystemService(getFitnessService().WINDOW_SERVICE);  //윈도우 매니저
	        if(isPopup)
	        {
		       	mWindowManager.removeView(mPopupView);
		    }
	        	Runnable runnable = new Runnable() {
					@Override
	                public void run() {
	                    handler.post(new Runnable() { // This thread runs in the UI
	                        @Override
	                        public void run() {
	                        	TextView temp = (TextView)mPopupView.findViewById(R.id.fitness_main_graph_heart_value_textview);
	                        	ImageButton xbutton = (ImageButton) mPopupView.findViewById(R.id.fitness_short_noti_graph_x_btn);
	                        	ImageButton goappbutton = (ImageButton) mPopupView.findViewById(R.id.fitness_short_noti_graph_confrim_btn);
	                        	temp.setText(""+heartrate); // 현재 수치를 넣어준다.
	                        	DonutProgress mHeartrateProgress = (DonutProgress)mPopupView.findViewById(R.id.fitness_main_graph_donutprogress);
	                        	mHeartrateProgress.setProgress(heartrate); // 현재 수치를 넣어준다.
	    			          	mWindowManager.addView(mPopupView, mParams);
	    			          	isPopup = true;
	    			          	xbutton.setOnClickListener(mViewClickListener);
	    			          	goappbutton.setOnClickListener(mViewClickListener);
	                        }
	                    });
	                }
	            };
	            new Thread(runnable).start();  
		}
	}

	OnClickListener mViewClickListener = new OnClickListener()
	{
		@Override
		public void onClick(View v) {
			switch (v.getId())
			{
				case R.id.fitness_short_noti_graph_x_btn:
				{
					mWindowManager.removeView(mPopupView);
					isPopup = false;
					break;
				}
				case R.id.fitness_short_noti_graph_confrim_btn:
				{
					mWindowManager.removeView(mPopupView);
					isPopup = false;
					Intent i = new Intent(getFitnessService(),FitnessMainActivity.class);
					i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					getFitnessService().startActivity(i);
					break;
				}
			}
		}
	};
}
