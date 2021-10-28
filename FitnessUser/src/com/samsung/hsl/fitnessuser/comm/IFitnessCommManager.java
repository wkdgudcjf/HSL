package com.samsung.hsl.fitnessuser.comm;

import com.samsung.hsl.fitnessuser.sqlite.User;

/**
 * @breif 통신에 대한 기본적인 인터페이스
 * @author jiwon
 *
 */
public interface IFitnessCommManager {
	public static enum Type{
		BLUETOOTH_LOW_ENERGY,
		WIFI,
		WIFI_DIRECT
	}
	
	public interface onDeviceListener{
		void onDeviceFound(FitnessDevice device);
		void onDeviceConnected(FitnessDevice device);
		void onDeviceDisconnected(FitnessDevice device);
	}
	
	/** @brief 해당 통신이 현재 가능한 상태인지 리턴한다. */
	public boolean isEnable();
	/** @brief 해당 통신을 기기가 지원하는지 리턴한다. */
	public boolean isSupported();
	/** @brief 해당 통신을 기기가 사용가능한지 리턴한다. */
	public boolean isAvailable();
	/** @brief 주변기기 검색을 시작한다. */
	public void startScan();
	/** @brief 해당 디바이스와 연결한다. */
	public void connect(FitnessDevice device,boolean isTest);
	/** @brief 해당 디바이스와 연결을 끊는다. */
	public void disconnect(FitnessDevice device);
	/** @brief 주변기기 검색을 멈춘다. */
	public void stopScan();
	/** @brief 연결 타입을 리턴한다. */
	public Type getType();
	/** @brief 유저 정보가 바꼈을경우. */
	public void modifyUsermFitnessUserData(User user);
	/** @brief 모든 연결을 끊고 리소스를 해제한다. */
	public void close();
}
