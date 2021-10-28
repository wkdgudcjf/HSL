package com.samsung.hsl.fitnessuser.comm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import com.google.gson.Gson;
import com.samsung.hsl.fitnessuser.service.FitnessUserService;

import android.os.Handler;

public class FitnessWifiProcessClient extends Thread {
	private static final String tag = FitnessWifiProcessClient.class.getName();

	FitnessUserService mIFitnessService;
	FitnessDevice mFitnessDevice;
	Socket mSocket;
	Gson gson = new Gson();
	
	IFitnessCommManager.onDeviceListener mOnDeviceListener;
	PrintWriter output;
	BufferedReader input;
	
	public FitnessWifiProcessClient(FitnessUserService service, FitnessDevice device,IFitnessCommManager.onDeviceListener listener){
		mIFitnessService = service;
		mFitnessDevice = device;
		mOnDeviceListener = listener;
	}
	
	@Override
	public void run() {
		
		try {
			mSocket = new Socket(mFitnessDevice.ipAddress,mFitnessDevice.port);
			mSocket.setTcpNoDelay(true);
			
			mOnDeviceListener.onDeviceConnected(mFitnessDevice);
			
			output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream())),true);
			input = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
			
			// TODO 초기 필요 데이터를 전송한다.
			//통신 목적
			output.println(FitnessNsdManager.SERVICE_SENDING_FITNESS_DATA);
			//기기명
			output.println(android.os.Build.MODEL);
			//유저정보
			output.println(gson.toJson(mIFitnessService.getCurrentUser()));
			//강도
			output.println(gson.toJson(mIFitnessService.getFitnessManager(mIFitnessService.getCurrentUser()).getStrength()));
			//현재 상태
			output.println(gson.toJson(mIFitnessService.getFitnessManager(mIFitnessService.getCurrentUser()).getState()));
			
			output.flush();
			
			while (!Thread.currentThread().isInterrupted()) {
				if(input.readLine()==null)break;
			}
			
		}
		
		catch (IOException e){
			e.printStackTrace();
		}
		
		tearDown();
        
	}
	
	public void tearDown() {
		interrupt();
        try {
        	if(mSocket!=null){
        		mSocket.close();
        		mSocket = null;
        		mOnDeviceListener.onDeviceDisconnected(mFitnessDevice);
        	}
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	Handler handler = new Handler();
	synchronized public void sendMessage(final String type, final String data) {
		handler.post(new Runnable(){
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if(output==null)return;
				output.println(type);
				output.println(data);
				output.flush();
			}
		});
		
	}
}
