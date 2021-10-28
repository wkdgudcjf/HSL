package com.samsung.hsl.fitnessuser.service;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.samsung.hsl.fitnessuser.comm.CmdUtils;
import com.samsung.hsl.fitnessuser.sqlite.FitnessData;
import com.samsung.hsl.fitnessuser.sqlite.FitnessSQLiteManager;
import com.samsung.hsl.fitnessuser.sqlite.User;

/**
 * @brief Fitness와 관련된 각종 계산식을 포함하는 클래스
 * @details 카로보넨공식, 평균값 계산 등의 연산을 처리한다.
 * @author jiwon
 *
 */
public class FitnessUtils {
	private static final String tag =  FitnessUtils.class.getName();
	/** @brief 최대 심박수 */
	public static final int MAX_HEARTRATE = 220;
	public static final int DEFAULT_AGE = 25;
	public static final int DEFAULT_STABLE_HEARTRATE = 70;
	public static final int DEFAULT_STABLE_SKIN_TEMPERATURE = 36;
	
	/** @brief 초과 단계 */
	public static final int STRENGTH_EXCEED = 4;
	
	/** @brief 고강도 단계 */
	public static final int STRENGTH_HIGH = 3;
	public static final float STRENGTH_PERCENTAGE_HIGH = 1f;
	
	/** @brief 일반 단계 */
	public static final int STRENGTH_COMMON = 2;
	public static final float STRENGTH_PERCENTAGE_COMMON = 0.85f;
	
	/** @brief 지방분해 단계 */
	public static final int STRENGTH_FAT_BUNNING = 1;
	public static final float STRENGTH_PERCENTAGE_FAT_BUNNING = 0.65f;
	
	/** @brief 준비운동 단계 */
	public static final int STRENGTH_WARM_UP = 0;
	public static final float STRENGTH_PERCENTAGE_WARM_UP = 0.5f;

	/**
	 * @brief 심박수의 평균을 계산한다.
	 * @details 최소, 최대값을 포함하는 범위 내에 해당하는 평균값을 계산한다.
	 * @param heartrateList
	 *            심박수 리스트
	 * @param min
	 *            심박수의 최소값
	 * @param max
	 *            심박수의 최대값
	 * @return 평균 심박수
	 */
	public static int average(int[] heartrateList, int min, int max) {
		float sum = 0;
		int cnt = 0;
		for (int i = 0; i < heartrateList.length; i++) {
			if (min <= heartrateList[i] && heartrateList[i] <= max) {
				sum += heartrateList[i];
				cnt++;
			}
		}
		return (int) sum / cnt;
	}
	
	public static int average(ArrayList<FitnessData> heartrateList, int min, int max) {
		float sum = 0;
		int cnt = 0;
		for (int i = 0; i < heartrateList.size(); i++) {
			if (min <= heartrateList.get(i).filterHeartrate && heartrateList.get(i).filterHeartrate <= max) {
				sum += heartrateList.get(i).filterHeartrate;
				cnt++;
			}
		}
		return (int) (sum / cnt);
	}

	/**
	 * @brief 카르보넨 공식의 강도별 최대값을 계산한다.
	 * @param user
	 *            사용자
	 * @param strength
	 *            운동강도
	 * @return
	 */
	public static int karvonen(User user,int strength){
		int age = getAge(user.birthday);
		int stableHeartrate = user.stableHeartrate;

		if(strength<0)return 0;
		
		return karvonen(age,stableHeartrate,getStrengthPercentage(strength));
	}
	
	/**
	 * @brief 카르보넨 공식을 계산한다.
	 * @param age
	 *            나이
	 * @param stabilityHeartrate
	 *            안정심박수
	 * @param strength
	 *            운동강도
	 * @return
	 */
	public static int karvonen(int age, int stableHeartrate,
			float strengthPercentage) {
		int maxHeartrate = MAX_HEARTRATE - age;
		return (int) ((maxHeartrate - stableHeartrate) * strengthPercentage + stableHeartrate);
	}

	/**
	 * 운동 강도에 따른 %를 구한다.
	 * @param strength 운동 강도
	 * @see FitnessUtils
	 * @return
	 */
	public static float getStrengthPercentage(int strength) {
		switch (strength) {
		case STRENGTH_HIGH:
			return STRENGTH_PERCENTAGE_HIGH;
		case STRENGTH_COMMON:
			return STRENGTH_PERCENTAGE_COMMON;
		case STRENGTH_FAT_BUNNING:
			return STRENGTH_PERCENTAGE_FAT_BUNNING;
		default:
			return STRENGTH_PERCENTAGE_WARM_UP;
		}
	}

	public static int getAge(String birthday){
		try {
			Calendar calendar = Calendar.getInstance();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			calendar.setTime(format.parse(birthday));
			Calendar now = Calendar.getInstance();
			int age = now.get(Calendar.YEAR) - calendar.get(Calendar.YEAR) + 1;
			return age;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return DEFAULT_AGE;
		}
	}
	
	/**
	 * dp를 pixel로 변환한다.
	 * 
	 * @param dp
	 *            변환할 dp 값
	 * @param context
	 * @return 변환된 pixel 값
	 */
	public static float convertDpToPixels(float dp, Context context) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float px = dp * metrics.densityDpi / 160f;
		return px;

	}
	
	/**
	 * pixel을 dp로 변환한다.
	 * 
	 * @param pixels
	 *            변환할 pixel 값
	 * @param context
	 * @return 변환된 dp 값
	 */
	public static float convertPixelsToDp(float pixels, Context context) {
		Resources resources = context.getResources();
		DisplayMetrics metrics = resources.getDisplayMetrics();
		float dp = pixels * (160f / metrics.densityDpi);
		return dp;

	}
	
	/** 테스트 매소드 */
	static boolean isTest = true;
	static File logFile;
	static PrintWriter logFileWriter;
	
	public static void createLogFile(){
		//테스트인 경우 파일 생성
		try{
				//현재 시간
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				Calendar calendar = Calendar.getInstance();
				String fileName = format.format(calendar.getTime())+" BluetoothLog.txt";
				File file = new File(FitnessSQLiteManager.DATABASE_DIR+File.separatorChar+fileName);
				//파일 생성 실패
				if(file.createNewFile()==false){
					isTest = false;
					return;
				}
				logFile = file;
				logFileWriter = new PrintWriter(logFile);
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public static void closeLogFile(){
		logFileWriter.close();
	}
	
	public static void testLog(final Context context){
		if(isTest){
			final byte[] data = new byte[]{0x01,0x02,0x03};
			logFileWriter.println(CmdUtils.bytesToHexString(data));
			logHandler.post(new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					if(logToastDelayed){
						Log.i(tag, "show log toast");
						Toast.makeText(context, CmdUtils.bytesToHexString(data),Toast.LENGTH_SHORT).show();
						logToastDelayed = false;
						
						TimerTask logToastDelayedTask = new TimerTask() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								logToastDelayed = true;
							}
						};
						logToastTimer.schedule(logToastDelayedTask, 2000);
					}
				}
				
			});
		}
	}
	
	static boolean logToastDelayed = true;
	static Handler logHandler = new Handler(Looper.getMainLooper());
	static Timer logToastTimer = new Timer();
	static TimerTask logToastDelayedTask;
}
