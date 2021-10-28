package com.samsung.hsl.fitnesstrainer.comm;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;

import com.google.gson.Gson;
import com.samsung.hsl.fitnesstrainer.service.FitnessBroadcastService;
import com.samsung.hsl.fitnesstrainer.service.FitnessTrainerService;
import com.samsung.hsl.fitnesstrainer.sqlite.FitnessData;
import com.samsung.hsl.fitnesstrainer.sqlite.User;

import android.os.Handler;
import android.os.Looper;

public class FitnessWifiProcessServer extends Thread {
	private static final String tag = FitnessWifiProcessServer.class.getName();

	/** @breif 클라이언트로부터 운동 정보를 받아서 처리하는 스레드 */
	FitnessTrainerService mFitnessService;
	FitnessDevice mFitnessDevice;
	Socket mSocket;
	IFitnessCommManager.onDeviceListener serviceListener;
	IFitnessCommManager.onDeviceListener managerListener;
	FitnessBroadcastService mFitnessBroadcastService;
	Handler handler = new Handler(Looper.getMainLooper());
	
	Gson gson = new Gson();

	public FitnessWifiProcessServer(FitnessTrainerService service,FitnessDevice device, Socket socket,
			IFitnessCommManager.onDeviceListener serviceListener,IFitnessCommManager.onDeviceListener managerListener) {
		mFitnessService = service;
		mFitnessDevice = device;
		mSocket = socket;
		try {
			socket.setReuseAddress(true);
			socket.setSoLinger(true, 0);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.serviceListener = serviceListener;
		this.managerListener = managerListener;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			mSocket.setSoTimeout(0);
			
			BufferedReader input = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));

			String message;
			while (!Thread.currentThread().isInterrupted()) {
				message = input.readLine();
				if(message==null)break;
				
				if(message.equals(FitnessWifiManager.TYPE_FINTESS_DATA))
				{
					message = input.readLine();
					if(message==null)break;
					FitnessData data = gson.fromJson(message, FitnessData.class);
					FitnessBroadcastService.sendBroadcast(FitnessBroadcastService.BROADCAST_RECEIVE_FITNESS_DATA,mFitnessDevice.user,data);
				}
				else if(message.equals(FitnessWifiManager.TYPE_AIM_STRENGTH))
				{
					message = input.readLine();
					if(message==null)break;
					int strength = gson.fromJson(message, int.class);
					mFitnessService.getFitnessPreference().putStrength(mFitnessDevice.user, strength);
				}
				else if(message.equals(FitnessWifiManager.TYPE_USER_STATE))
				{
					message = input.readLine();
					if(message==null)break;
					int state = gson.fromJson(message, int.class);
					if(mFitnessService.getFitnessManager(mFitnessDevice.user)!=null)mFitnessService.getFitnessManager(mFitnessDevice.user).setState(state);
				}
				else if(message.equals(FitnessWifiManager.TYPE_USER_INFO))
				{
					message = input.readLine();
					if(message==null)break;
					User user = gson.fromJson(message, User.class);
					mFitnessDevice.user.setUser(user);
					mFitnessService.getFitnessSQLiteManager().insertOrUpdateUser(mFitnessDevice.user);
				}
				
			}

		} catch (IOException e) {
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
				managerListener.onDeviceDisconnected(mFitnessDevice);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
