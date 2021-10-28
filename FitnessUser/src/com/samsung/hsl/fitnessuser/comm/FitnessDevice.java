package com.samsung.hsl.fitnessuser.comm;

import com.samsung.hsl.fitnessuser.sqlite.User;

/**
 * @brief 피트니스 디바이스의 정보를 갖는 클래스
 * @details 여러 종류의 디바이스와 연결 형태와 상관없이 하나의 형태로 표현하기 위해 사용한다.
 * @author jiwon
 *
 */
public class FitnessDevice {
	
	public String macAddress;
	public String ipAddress; // 주소
	public int port; // 포트
	public String name; // 기기명
	public boolean isConnected; // 접속여부
	public IFitnessCommManager.Type commType; // 통신 형식
	public User user;
	
	public int sw; // sw 버전
	public int hw; // hw 버전
	public int battery; // 배터리
	
	public FitnessDevice(){
	
	}
	public FitnessDevice(String macAddress,String ipAddress,String name,boolean isCon,int sw,int hw,int battery)
	{
		this.macAddress = macAddress;
		this.ipAddress = ipAddress;
		this.name = name;
		this.isConnected = isCon;
		this.sw = sw;
		this.hw = hw;
		this.battery = battery;
	}

}
