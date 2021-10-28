package com.samsung.hsl.fitnesstrainer.service;

import java.util.ArrayList;

import com.samsung.hsl.fitnesstrainer.sqlite.FitnessSQLiteManager;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

/**
 * @brief Application 전역 변수 관리를 위한 클래스
 * @details 앱(UI)에서 공통적으로 접근하여 사용하기 위한 객체들을 관리한다.<br>
 * 			서비스, 데이터베이스(SQLite) 등등을 관리한다.
 * @author jiwon
 *
 */
public class FitnessTrainerApplication extends Application{
	private static final String tag = FitnessTrainerApplication.class.getName();
	protected boolean mIsBound = false;
	private FitnessTrainerService mFitnessService = null;
	private FitnessSQLiteManager mFitnessSQLiteManager = null;
	public FitnessBroadcastService mFitnessBroadcastService = null;
	public ArrayList<Activity> alist;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		Log.i(tag, "onCreate");
		super.onCreate();
		doBindService();
		alist = new ArrayList<Activity>();
		mFitnessBroadcastService = FitnessBroadcastService.getInstance(this);
		FitnessSQLiteManager.checkExternalDirectory();
		mFitnessSQLiteManager = new FitnessSQLiteManager(this);
	}
	
	@Override
	public void onTerminate() {
		// TODO Auto-generated method stub
		Log.i(tag, "onTerminate");
	    doUnbindService();
		super.onTerminate();
	}

	public FitnessSQLiteManager getSQLiteManager() {
		return mFitnessSQLiteManager;
	}
	
	public FitnessTrainerService getFitnessService(){
		return mFitnessService;
	}
	void doUnbindService() {
		Log.i(tag, "doUnbindService");
	    if (mIsBound) {
	        // Detach our existing connection.
	        unbindService(mConnection);
	        mIsBound = false;
	    }
	}

	protected ServiceConnection mConnection = new ServiceConnection(){

		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			// TODO Auto-generated method stub
			mFitnessService = ((FitnessTrainerService.LocalBinder)service).getService();
			Log.i(tag,"onServiceConnected");
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			// TODO Auto-generated method stub
			mFitnessService = null;
			Log.i(tag,"onServiceDisconnected");
		}
		
	};
	protected void doBindService() {
		// TODO Auto-generated method stub
        Log.i(tag, "doBindService");
        // Establish a connection with the service.  We use an explicit        
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).
        bindService(new Intent(this, FitnessTrainerService.class), mConnection, Context.BIND_AUTO_CREATE);
        mIsBound = true;
	}
}
