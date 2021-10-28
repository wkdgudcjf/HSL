package com.samsung.hsl.fitnesstrainer.comm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class FitnessWifiFoundClient extends Thread {
	private static final String tag = FitnessWifiFoundClient.class.getName();

	FitnessDevice mFitnessDevice;
	Socket mSocket;

	IFitnessCommManager.onDeviceListener mOnDeviceListener;

	public interface onFitnessWifiListener {
		void onDeviceInfoResolved(FitnessDevice device);
	}

	public FitnessWifiFoundClient(InetAddress address, int port,IFitnessCommManager.onDeviceListener listener) {
		mFitnessDevice = new FitnessDevice();
		mFitnessDevice.ipAddress=address.getHostAddress();
		mFitnessDevice.port=port;
		
		mOnDeviceListener = listener;
	}
	
	@Override
	public void run() {
		try {
			mSocket = new Socket(mFitnessDevice.ipAddress,mFitnessDevice.port);

			PrintWriter output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream())));
			BufferedReader input = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
			
			String messageStr = FitnessNsdManager.SERVICE_REQUEST_DEVICE_INFO;
			output.println(messageStr);
			output.flush();
			
			messageStr = input.readLine();
			
			if (messageStr != null) {
				mFitnessDevice.name = messageStr;
				mFitnessDevice.macAddress = FitnessWifiManager.getMacFromArpCache(mFitnessDevice.ipAddress);
				mOnDeviceListener.onDeviceFound(mFitnessDevice);
			} 
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
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
        	}
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	
}
