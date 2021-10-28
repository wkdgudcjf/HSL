package com.samsung.hsl.fitnessuser.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import com.samsung.hsl.fitnessuser.service.FitnessBroadcastService.FitnessBroadcastListener;
import com.samsung.hsl.fitnessuser.sqlite.FitnessData;
import com.samsung.hsl.fitnessuser.sqlite.FitnessList;
import com.samsung.hsl.fitnessuser.sqlite.FitnessSQLiteManager;
import com.samsung.hsl.fitnessuser.sqlite.User;

/** 현재 운동중인 정보를 관리하는 기능 */
public class FitnessManager {
	private static final String tag = FitnessManager.class.getName();
	
	public static final int STATE_STOP = 0x00;
	public static final int STATE_WARM_UP_NEED = 0x01;
	public static final int STATE_WARM_UP_EXCEED_AIM = 0x02;
	public static final int STATE_WARM_UP_COMPLETE = 0x03;
	public static final int STATE_SHORT_AIM = 0x04;
	public static final int STATE_EXCEED_AIM = 0x05;
	public static final int STATE_ARCHIEVE_AIM = 0x06;

	private static final int TIME_COMPLETE_WARM_UP = 30000;
	private static final int TIME_COMPLETE_WARM_UP_MESSAGE = 3000;
	
	private int mState = STATE_STOP;
	private boolean isCheckState = true; 
	int strength = FitnessUtils.STRENGTH_WARM_UP;
	FitnessUserService mFitnessService;
	FitnessSQLiteManager mFitnessSQLiteManager;
	FitnessPreference mFitnessPreference;
	FitnessBroadcastService mFitnessBroadcastService;
	/** @brief 현재 사용자를 저장하는 변수 */
	User mUser;
	/** @brief 운동 중인 FitnessList 정보를 저장하는 변수 */
	FitnessList mFitnessList = new FitnessList();
	/** @brief 운동 시작하고 발생한 Fitness 정보를 저장하는 변수 */
	ArrayList<FitnessData> mFitnessDataList = new ArrayList<FitnessData>();

	public FitnessManager(User user,FitnessUserService service) {
		mFitnessService = service;
		mFitnessSQLiteManager = service.getFitnessSQLiteManager();
		mFitnessPreference = service.getFitnessPreference();
		mFitnessBroadcastService = service.getFitnessBroadcastService();
		mFitnessBroadcastService.addBroadcastObserver(mFitnessBroadcastListener);
		strength = mFitnessPreference.getStrength(mUser, FitnessUtils.STRENGTH_WARM_UP);
		mUser = user;
	}

	/** @brief 현재 사용자를 얻어온다. */
	public User getUser() {
		return mUser;
	}

	public void startFitness() {
		mFitnessList = new FitnessList();
		mFitnessList.userId = mUser.id;
		mFitnessList.startTime = System.currentTimeMillis();
		mFitnessSQLiteManager.insert(mFitnessList);
		mFitnessDataList.clear();
		setState(STATE_WARM_UP_NEED);
	}

	public void close(){
		if(mState!=STATE_STOP)stopFitness();
	}
	
	
	public void stopFitness() {
		setState(STATE_STOP);
	}

	public void addFitnessData(int heartrate, int filterHeartrate, float skinTemperature, float humidity, float consumeCalorie, int power, int fall) {
		FitnessData fitness = new FitnessData();
		fitness.listId = mFitnessList.id;
		fitness.heartrate = heartrate;
		fitness.filterHeartrate = filterHeartrate;
		fitness.skinTemperature = skinTemperature;
		fitness.humidity = humidity;
		fitness.consumeCalorie = consumeCalorie;
		fitness.power = power;
		fitness.fall = fall;
		mFitnessSQLiteManager.insert(fitness);
		mFitnessDataList.add(fitness);
		if(fall==1)FitnessBroadcastService.sendBroadcast(FitnessBroadcastService.BROADCAST_RECEIVE_FITNESS_FALL,mUser);
		if(isCheckState)checkState();
	}

	/** @brief 현재 상태를 체크할지 여부를 정한다. */
	public void setCheckState(boolean check){
		isCheckState = check;
	}
	
	/** @brief 현재 상태를 설정한다. */
	public void setState(int state){
		if(mState!=state)
		{
			mState = state;
			FitnessBroadcastService.sendBroadcast(FitnessBroadcastService.BROADCAST_CHANGE_USER_STATE,mUser,mState);
		}
	}

	/** @brief 현재 상태를 얻어온다. */
	public int getState() {
		return mState;
	}

	
	TimerTask warmingUpTask = null;
	/** @brief 값에 따라 현재 상태를 설정한다. */
	private void checkState() {
		if(getCurrentFitnessData()==null)return;
		
		if (getState() == STATE_WARM_UP_NEED) 
		{
			//처음 심박수의 1.5배가 넘거나 피부온도가 1도 이상 상승했으면 30초후 준비 운동 완료 상태로 변경
			if (warmingUpTask==null && isCompleteWarmingUp()) 
			{
				warmingUpTask = new TimerTask() {
					boolean pastMessageTime = false;
					@Override
					public void run() {
						// TODO Auto-generated method stub
						setState(STATE_WARM_UP_COMPLETE);
						if(pastMessageTime==false){
							pastMessageTime = true;
							return;
						}
						
						this.cancel();
						warmingUpTask = null;
					}
				};
				Timer timer = new Timer();
				timer.schedule(warmingUpTask, TIME_COMPLETE_WARM_UP, TIME_COMPLETE_WARM_UP_MESSAGE);
			}
			//준비운동 중에 목표강도를 초과한 경우
			if(isExceedAimHeartrate(getCurrentFitnessData().filterHeartrate)){
				setState(STATE_WARM_UP_EXCEED_AIM);
			}
		}
		//준비운동 단계에서 목표 강도를 초과한 경우
		else if(getState() == STATE_WARM_UP_EXCEED_AIM)
		{
			if(isExceedAimHeartrate(getCurrentFitnessData().filterHeartrate)==false){
				setState(STATE_WARM_UP_NEED);
			}
		}
		else
		{
			if(warmingUpTask!=null || getCurrentFitnessData()==null)return;
			
			boolean isShort = isShortAimHeartrate(getCurrentFitnessData().filterHeartrate);
			boolean isExceed = isExceedAimHeartrate(getCurrentFitnessData().filterHeartrate);
			if (isShort){
				setState(STATE_SHORT_AIM);
			}else if(isExceed){
				setState(STATE_EXCEED_AIM);
			}else {
				setState(STATE_ARCHIEVE_AIM);
			}
		} 
	}

	/** @brief 준비 운동 완료 조건을 만족했는지 확인한다. */
	private boolean isCompleteWarmingUp(){
		if(getCurrentFitnessData()==null)return false;
		
		boolean heartrate = (int) (mFitnessDataList.get(0).filterHeartrate * 1.5f) <= getCurrentFitnessData().filterHeartrate;
		boolean skinTemperature = (float) FitnessUtils.DEFAULT_STABLE_SKIN_TEMPERATURE + 1 <= getCurrentFitnessData().skinTemperature;
		return heartrate || skinTemperature;
	}
	
	/** @brief 현재 운동 정보 목록을 제공하는 함수 */
	public FitnessList getCurrentFitnessList() {
		return mFitnessList;
	}

	/** @brief 운동 시작 후 발생한 전체 운동 정보를 제공하는 함수 */
	public ArrayList<FitnessData> getFitnessDataList() {
		return mFitnessDataList;
	}

	/** @brief 현재 운동 정보를 제공하는 함수 */
	public FitnessData getCurrentFitnessData() {
		if (mFitnessDataList.size() > 0)
			return mFitnessDataList.get(mFitnessDataList.size() - 1);
		else
			return null;
	}

	/** @brief 사용자의 안정심박수가 오래되었는지 확인하는 함수 */
	public boolean isOldCheckStableHeartrate() {
		try {
			Calendar calendar = Calendar.getInstance();
			SimpleDateFormat format = new SimpleDateFormat(
					"yyyy-MM-dd hh:mm:ss");
			calendar.setTime(format.parse(mUser.checktime));
			Calendar now = Calendar.getInstance();
			now.add(Calendar.DAY_OF_MONTH, -15);
			// 15일보다 이전에 측정한 값이면
			if (now.after(calendar))
				return true;
			else
				return false;
		} catch (Exception e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			return true;
		}
	}

	public int getStrength(){
		return strength;
	}
	
	public void setStrength(int strength){
		this.strength = strength;
	}
	
	/**
	 * 현재 목표 심박수를 초과했는지 여부
	 * @param heartrate 대상 심박수
	 * @return
	 */
	public boolean isExceedAimHeartrate(int heartrate){
		return heartrate > FitnessUtils.karvonen(mUser, strength) ? true : false;
	}
	
	/**
	 * 현재 목표 심박수를 미달했는지 여부
	 * @param heartrate 대상 심박수
	 * @return
	 */
	public boolean isShortAimHeartrate(int heartrate){
		return heartrate < FitnessUtils.karvonen(mUser, strength-1) ? true : false;
	}
	
	public void setStableHeartrate(int stableHeartrate) {
		mUser.stableHeartrate = stableHeartrate;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Calendar calendar = Calendar.getInstance();
		mUser.checktime = format.format(calendar.getTime());
		mFitnessSQLiteManager.update(mUser);
	}

	
	FitnessBroadcastListener mFitnessBroadcastListener = new FitnessBroadcastListener(){

		@Override
		public void onReceiveFitnessData(User user, FitnessData data) {
			// TODO Auto-generated method stub
			if(mUser.equals(user)==false)return;
			
			addFitnessData(data.heartrate, data.filterHeartrate, data.skinTemperature, data.humidity, data.consumeCalorie, data.power, data.fall);
		}

		@Override
		public void onReceiveFitnessFall(User user) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onChangeAimStrength(User user, int strength) {
			// TODO Auto-generated method stub
			if(mUser.equals(user)==false)return;
			
			setStrength(strength);
			if(isCheckState)checkState();
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
	
	/** @breif 테스트를 위한 변수 */
	TimerTask task = null;
	boolean testRunning = false;

	int testHeartrate = 0;
	public void startTest() {
		startFitness();
		
		task = new TimerTask() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Random random = new Random();
				FitnessData data = new FitnessData();
				data.filterHeartrate = testHeartrate++;
				data.heartrate = testHeartrate;
				data.skinTemperature = random.nextInt(5) + 34;
				data.humidity = random.nextInt(5) + 134;
				data.consumeCalorie = random.nextInt(200);
				data.power = random.nextInt(100);
				data.fall = testHeartrate%10==0?1:0;
				FitnessBroadcastService.sendBroadcast(FitnessBroadcastService.BROADCAST_RECEIVE_FITNESS_DATA, getUser(), data);
			}
		};

		Timer timer = new Timer();
		timer.schedule(task, 1000, 1000);
		testRunning = true;
	}

	public void stopTest() {
		if (task != null)
			task.cancel();
		stopFitness();
		testRunning = false;
	}

	public boolean isTestRunning() {
		return testRunning;
	}
}
