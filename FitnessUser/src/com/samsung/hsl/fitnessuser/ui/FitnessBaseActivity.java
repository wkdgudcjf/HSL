package com.samsung.hsl.fitnessuser.ui;

import java.util.Timer;
import java.util.TimerTask;

import com.google.samples.apps.iosched.ui.widget.ScrimInsetsFrameLayout;
import com.samsung.hsl.fitnessuser.R;
import com.samsung.hsl.fitnessuser.service.FitnessFontUtils;
import com.samsung.hsl.fitnessuser.service.FitnessUserApplication;
import com.samsung.hsl.fitnessuser.service.FitnessUserService;
import com.samsung.hsl.fitnessuser.service.FitnessUtils;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FitnessBaseActivity extends FitnessFontActivity {
	private static final String tag = FitnessBaseActivity.class.getName();
	
	/** @brief Drawer 버튼으로 상속받는 액티비티에서 변경하고자 할 경우 수정해야한다. */
	protected ImageButton mTopBarLeftButton;
    /** @brief 홈 버튼으로 상속받는 액티비티에서 변경하고자 할 경우 수정해야한다. */
    protected ImageButton mTopBarRightButton;
    /** @brief 타이틀 텍스트로 상속받는 액티비티에서 변경하고자 할 경우 수정해야한다. */
    protected TextView mTopBarTitleText;
    
    protected DrawerLayout mDrawerLayout;
    
    protected ScrimInsetsFrameLayout mDrawerFragment;
    RelativeLayout mlogoutTouchLayout;
    RelativeLayout mConnectionTouchLayout;
    RelativeLayout mAimManagerTouchLayout;
    RelativeLayout mCalendarTouchLayout;
    RelativeLayout mMyInfoTouchLayout;
    RelativeLayout mTestTouchLayout;
    RelativeLayout mTopbarLayout;
	
    protected TextView mDrawerWelcomeText;
    protected LogoutDialog mLogoutDialog;
	
	TextView mTestTextView;
    protected void onCreate(Bundle savedInstanceState,int layout) {
        super.onCreate(savedInstanceState);
        setContentView(layout);

        mTopBarLeftButton = (ImageButton) findViewById(R.id.fitness_top_bar_left_imagebutton);
        mTopBarLeftButton.setOnClickListener(mTopBarClickListener);
        mTopBarRightButton = (ImageButton) findViewById(R.id.fitness_top_bar_right_imagebutton);
        mTopBarRightButton.setOnClickListener(mTopBarClickListener);
        mTopBarTitleText = (TextView)findViewById(R.id.fitness_top_bar_title_textview);
        
        initSlideMenu();
        
        FitnessFontUtils.setViewGroupFont((ViewGroup)findViewById(android.R.id.content));
        
    }
    
    @Override
    public void onBackPressed() {
    	// TODO Auto-generated method stub
    	if (mDrawerOpened)
    		mDrawerLayout.closeDrawer(mDrawerFragment);
    	else
    		super.onBackPressed();
    }
    
    @Override
	protected void onDestroy() 
	{
		super.onDestroy();
	}
    
    protected void initSlideMenu(){
    	mDrawerLayout = (DrawerLayout) findViewById(R.id.layout_main);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mDrawerLayout.setDrawerListener(mDrawerListener);
        mDrawerLayout.setFitsSystemWindows(true);
        mDrawerLayout.setFocusableInTouchMode(false);
        
        mDrawerFragment = (ScrimInsetsFrameLayout) findViewById(R.id.navigation_framelayout);
        mlogoutTouchLayout = (RelativeLayout) findViewById(R.id.fitness_slide_menu_logout_layout);
        mConnectionTouchLayout = (RelativeLayout) findViewById(R.id.fitness_slide_menu_connection_layout);
        mAimManagerTouchLayout = (RelativeLayout) findViewById(R.id.fitness_slide_menu_aim_manager_layout);
        mCalendarTouchLayout = (RelativeLayout) findViewById(R.id.fitness_slide_menu_calendar_layout);
        mMyInfoTouchLayout = (RelativeLayout) findViewById(R.id.fitness_slide_menu_my_info_layout);
        mTestTouchLayout = (RelativeLayout) findViewById(R.id.fitness_slide_menu_test_layout);
        
        mlogoutTouchLayout.setOnClickListener(mSlideMenuClickListener);
        mConnectionTouchLayout.setOnClickListener(mSlideMenuClickListener);
        mAimManagerTouchLayout.setOnClickListener(mSlideMenuClickListener);
        mCalendarTouchLayout.setOnClickListener(mSlideMenuClickListener);
        mMyInfoTouchLayout.setOnClickListener(mSlideMenuClickListener);
        mTestTouchLayout.setOnClickListener(mSlideMenuClickListener);
        mDrawerWelcomeText = (TextView) findViewById(R.id.fitness_slide_menu_bar_welcom_textview);
        mTestTextView = (TextView) findViewById(R.id.fitness_slide_menu_test_textview);
        mLogoutDialog = new LogoutDialog(this, mLogoutDismissListener);
    }
    
    OnClickListener mSlideMenuClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.fitness_slide_menu_logout_layout:
				mLogoutDialog.show();
				break;

			case R.id.fitness_slide_menu_connection_layout:
				mDrawerLayout.closeDrawer(mDrawerFragment);
				Intent intent = new Intent(FitnessBaseActivity.this,FitnessConnectionActivity.class);
				startActivity(intent);
				break;
				//목표관리
			case R.id.fitness_slide_menu_aim_manager_layout:
				mDrawerLayout.closeDrawer(mDrawerFragment);
				intent = new Intent(FitnessBaseActivity.this,FitnessAimManagerActivity.class);
				startActivity(intent);
				break;
				//운동달력
			case R.id.fitness_slide_menu_calendar_layout:
				mDrawerLayout.closeDrawer(mDrawerFragment);
				intent = new Intent(FitnessBaseActivity.this,FitnessCalendarLoadingActivity.class);
				startActivity(intent);
				break;
				//내정보
			case R.id.fitness_slide_menu_my_info_layout:
				mDrawerLayout.closeDrawer(mDrawerFragment);
				intent = new Intent(FitnessBaseActivity.this,FitnessMyInfoActivity.class);
				startActivity(intent);
				break;
			//테스트 시작
			case R.id.fitness_slide_menu_test_layout:
				mDrawerLayout.closeDrawer(mDrawerFragment);
				FitnessUserService service = ((FitnessUserApplication)getApplication()).getFitnessService();
				
				if(service.getFitnessManager().isTestRunning())
				{
					service.getFitnessManager().stopTest();
					FitnessUtils.closeLogFile();
					timer.cancel();
				}
				else 
				{
					service.getFitnessManager().startTest();
					FitnessUtils.createLogFile();
					TimerTask task = new TimerTask(){

						@Override
						public void run() {
							// TODO Auto-generated method stub
							FitnessUtils.testLog(FitnessBaseActivity.this.getApplicationContext());
						}
						
					};
					timer = new Timer();
					timer.schedule(task,1000,100);
				}
					
				break;
			}
		}
	};
	TimerTask task;
	Timer timer;
	
    OnClickListener mTopBarClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.fitness_top_bar_left_imagebutton:
				if(mDrawerOpened)mDrawerLayout.closeDrawer(mDrawerFragment);
				else mDrawerLayout.openDrawer(mDrawerFragment);
				break;

			case R.id.fitness_top_bar_right_imagebutton:
				Intent intent = new Intent(FitnessBaseActivity.this,FitnessMainActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
				startActivity(intent);
				break;
			}
		}
	};

	boolean mDrawerOpened = false;
	DrawerListener mDrawerListener = new DrawerListener(){

		@Override
		public void onDrawerClosed(View arg0)
		{
			// TODO Auto-generated method stub
			mDrawerOpened = false;
		}

		@Override
		public void onDrawerOpened(View arg0)
		{
			// TODO Auto-generated method stub
			mDrawerOpened = true;
		}

		@Override
		public void onDrawerSlide(View arg0, float arg1)
		{
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onDrawerStateChanged(int arg0)
		{
			// TODO Auto-generated method stub
			FitnessUserService service = ((FitnessUserApplication)getApplication()).getFitnessService();
			String name = service.getFitnessManager().getUser().name;
			mDrawerWelcomeText.setText(getResources().getString(R.string.string_fitness_slide_menu_welcome_head)+" "+name+getResources().getString(R.string.string_fitness_slide_menu_welcome_tail));
			if(service.getFitnessManager().isTestRunning())mTestTextView.setText(getResources().getString(R.string.string_fitness_slide_menu_test_end));
			else mTestTextView.setText(getResources().getString(R.string.string_fitness_slide_menu_test_start));
		}
		
	};
	
	protected class LogoutDialog extends Dialog implements OnClickListener
	{
		OnDismissListener mListener = null;
		ImageButton mConfirmButton;
		ImageButton mCancelButton;
		
		public LogoutDialog(Context context, OnDismissListener listener)
		{
			super(context);
			// TODO Auto-generated constructor stub
			requestWindowFeature(Window.FEATURE_NO_TITLE);
			setContentView(R.layout.dialog_fitness_logout);

			getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
			
			mListener = listener;
			mConfirmButton = (ImageButton)findViewById(R.id.fitness_slide_menu_logout_pop_up_confirm_btn_imagebutton);
			mCancelButton = (ImageButton)findViewById(R.id.fitness_slide_menu_logout_pop_up_cancel_btn_imagebutton);
			
			mConfirmButton.setOnClickListener(this);
			mCancelButton.setOnClickListener(this);
			
		}

		@Override
		public void onClick(View v) 
		{
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.fitness_slide_menu_logout_pop_up_confirm_btn_imagebutton:
				if(mListener!=null){
					mListener.onDismiss(this);
				}
				dismiss();
				break;

			case R.id.fitness_slide_menu_logout_pop_up_cancel_btn_imagebutton:
				dismiss();
				break;
			}
		}
	}
	
	OnDismissListener mLogoutDismissListener = new OnDismissListener(){

		@Override
		public void onDismiss(DialogInterface arg0) {
			// TODO Auto-generated method stub
			mDrawerLayout.closeDrawer(mDrawerFragment);
			Intent intent = new Intent(FitnessBaseActivity.this,FitnessIntroLoginActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(intent);
		}
		
	};
}
