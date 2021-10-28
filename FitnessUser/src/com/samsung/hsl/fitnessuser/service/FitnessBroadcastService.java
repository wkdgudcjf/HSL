package com.samsung.hsl.fitnessuser.service;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.Context;
import android.content.IntentFilter;

import com.google.gson.Gson;
import com.samsung.hsl.fitnessuser.sqlite.FitnessData;
import com.samsung.hsl.fitnessuser.sqlite.User;

public class FitnessBroadcastService {
	private static final String tag = FitnessBroadcastService.class.getName();
	/** @brief Fitness BLE 스캔을 끝냈을때 생기는 브로드캐스트 */
	public static final String BROADCAST_STOP_COMM_SCAN = "com.samsung.hsl.fitness.receive.scanstart";
	/** @brief Fitness BLE 스캔을 끝냈을때 생기는 브로드캐스트 */
	public static final String BROADCAST_CONNECT_SUCCESS = "com.samsung.hsl.fitness.connect.success";
	/** @brief Fitness BLE 스캔을 시작했을때 생기는 브로드캐스트 */
	public static final String BROADCAST_START_COMM_SCAN = "com.samsung.hsl.fitness.receive.scanstop";
	/** @brief 디바이스 리스트가 변경되었을 때 생기는 브로드캐스트 */
	public static final String BROADCAST_CHANGE_CONNECT_DEVICE_LIST = "com.samsung.hsl.fitness.change.connect.device";
	/** @brief 디바이스 리스트가 변경되었을 때 생기는 브로드캐스트 */
	public static final String BROADCAST_CHANGE_DEVICE_LIST = "com.samsung.hsl.fitness.change.device";
	/** @brief 디바이스와의  연결 상태가 변경되었을 때 생기는 브로드캐스트 */
	public static final String BROADCAST_CHANGE_CONNECTION_STATUS = "com.samsung.hsl.fitness.change.connection";
    /** @brief Fitness 데이터를 받았을때 생기는 브로드캐스트 */
	public static final String BROADCAST_RECEIVE_FITNESS_DATA = "com.samsung.hsl.fitness.receive.fitness";
	/** @brief Fitness BLE 스캔을 끝냈을때 생기는 브로드캐스트 */
	public static final String BROADCAST_RECEIVE_FITNESS_FALL = "com.samsung.hsl.fitness.receive.fall";
	/** @brief 목표 강도가 변경되었을 때 생기는 브로드캐스트 */
	public static final String BROADCAST_CHANGE_AIM_STRENGTH = "com.samsung.hsl.fitness.change.aimStrength";
	/** @brief 사용자 상태가 변경되었을 때 생기는 브로드캐스트 */
	public static final String BROADCAST_CHANGE_USER_STATE = "com.samsung.hsl.fitness.change.userState";
	/** @brief 사용자 정보가 변경되었을 때 생기는 브로드캐스트 */
	public static final String BROADCAST_CHANGE_USER_INFO = "com.samsung.hsl.fitness.change.userInfo";
	
	/** @brief 등록된 콜백을 관리하는 맵 */
	@Deprecated
	HashMap<String,ArrayList<FitnessBroadcastCallback>> mCallbackMap = new HashMap<String, ArrayList<FitnessBroadcastCallback>>();
	/** @brief 등록된 리스너를 관리하는 맵 */
	ArrayList<FitnessBroadcastListener> mListenerList = new ArrayList<FitnessBroadcastListener>();
	
	/** @brief 브로드캐스트를 배열 형태로 저장하는 리스트 */
	String[] mBroadcastList = {
			BROADCAST_START_COMM_SCAN,
			BROADCAST_STOP_COMM_SCAN,
			BROADCAST_CHANGE_DEVICE_LIST,
			BROADCAST_CHANGE_CONNECTION_STATUS,
			BROADCAST_CHANGE_CONNECT_DEVICE_LIST,
			BROADCAST_CONNECT_SUCCESS
	};
	
	IntentFilter mIntentFilter = new IntentFilter();
	
	Context context;
	
	private static FitnessBroadcastService mFitnessBroadcastService = null;
	public static FitnessBroadcastService getInstance(Context context){
		if(mFitnessBroadcastService==null)mFitnessBroadcastService = new FitnessBroadcastService(context);
		return mFitnessBroadcastService;
	}
	
	public void close(){
		mCallbackMap.clear();
		mListenerList.clear();
		mFitnessBroadcastService = null;
	}
	
	private FitnessBroadcastService(Context context){
		for(int i=0;i<mBroadcastList.length;i++){
			mCallbackMap.put(mBroadcastList[i], new ArrayList<FitnessBroadcastCallback>());
			mIntentFilter.addAction(mBroadcastList[i]);
		}
		this.context = context;
	}
	
	public static void sendBroadcast(String action){
		if(mFitnessBroadcastService==null)return;
		mFitnessBroadcastService.onReceive(action);
	}
	
	// 등록된 브로드캐스트에 콜백 메소드를 호출한다.
	public void onReceive(String action) {
		// TODO Auto-generated method stub
		for(int i=0;i<mBroadcastList.length;i++){
			String broadcast = mBroadcastList[i];
			if(action.contentEquals(broadcast)){
				ArrayList<FitnessBroadcastCallback> mCallbackList = mCallbackMap.get(broadcast);
				for(int j=0;j<mCallbackList.size();j++){
					FitnessBroadcastCallback callback = mCallbackList.get(j);
					if(callback==null)continue;
					callback.receive();
				}
			}
		}
	}
	
	public void addBroadcastObserver(String broadcast,FitnessBroadcastCallback callback){
		ArrayList<FitnessBroadcastCallback> callbackList = mCallbackMap.get(broadcast);
		if(callbackList==null)return;
		callbackList.add(callback);
	}
	public void removeBoradcastObserver(String broadcast,FitnessBroadcastCallback callback){
		ArrayList<FitnessBroadcastCallback> callbackList = mCallbackMap.get(broadcast);
		if(callbackList==null)return;
		callbackList.remove(callback);
	}
	
	public interface FitnessBroadcastCallback{
		public void receive();
	}
	
	/** 리스너 구현 */
	
	public interface FitnessBroadcastListener{
		public void onReceiveFitnessData(User user,FitnessData data);
		public void onReceiveFitnessFall(User user);
		public void onChangeAimStrength(User user,int strength);
		public void onChangeUserState(User user,int state);
		public void onChangeUserInfo(User user);
	}
	
	public void addBroadcastObserver(FitnessBroadcastListener listener){
		mListenerList.add(listener);
	}
	public void removeBoradcastObserver(FitnessBroadcastListener listener){
		mListenerList.remove(listener);
	}
	
	public static Gson gson = new Gson();
	public static void sendBroadcast(String action,User user,FitnessData data){
		if(mFitnessBroadcastService==null)return;
		for(int j=0;j<mFitnessBroadcastService.mListenerList.size();j++){
			FitnessBroadcastListener listener = mFitnessBroadcastService.mListenerList.get(j);
			if(action.equals(BROADCAST_RECEIVE_FITNESS_DATA))listener.onReceiveFitnessData(user, data);
		}
	}
	
	public static void sendBroadcast(String action,User user){
		if(mFitnessBroadcastService==null)return;
		for(int j=0;j<mFitnessBroadcastService.mListenerList.size();j++){
			FitnessBroadcastListener listener = mFitnessBroadcastService.mListenerList.get(j);
			if(action.equals(BROADCAST_RECEIVE_FITNESS_FALL))listener.onReceiveFitnessFall(user);
			if(action.equals(BROADCAST_CHANGE_USER_INFO))listener.onChangeUserInfo(user);
		}
	}
	
	public static void sendBroadcast(String action,User user,int data){
		if(mFitnessBroadcastService==null)return;
		for(int j=0;j<mFitnessBroadcastService.mListenerList.size();j++){
			FitnessBroadcastListener listener = mFitnessBroadcastService.mListenerList.get(j);
			if(action.equals(BROADCAST_CHANGE_AIM_STRENGTH))listener.onChangeAimStrength(user, data);
			if(action.equals(BROADCAST_CHANGE_USER_STATE))listener.onChangeUserState(user, data);
		}
	}
}
