package com.samsung.hsl.fitnesstrainer.comm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;

import com.google.gson.Gson;
import com.samsung.hsl.fitnesstrainer.service.FitnessTrainerService;
import com.samsung.hsl.fitnesstrainer.sqlite.User;

import android.util.Log;

/**
 * @brief 소켓 서버를 열고 클라이언트에게 디바이스 정보를 보내주거나 운동 정보를 받아서 처리한다.
 * @author jiwon
 *
 */
public class FitnessWifiFoundServer extends Thread {
	private static final String tag = FitnessWifiFoundServer.class.getName();
	
	ServerSocket mServerSocket = null;
	HashMap<String, Socket> mClientSocketList = new HashMap<String, Socket>();
    IFitnessCommManager.onDeviceListener mOnDeviceListener;
    FitnessTrainerService mFitnessService;
    Gson gson = new Gson();
    
    public FitnessWifiFoundServer(FitnessTrainerService service,IFitnessCommManager.onDeviceListener listener) {
    	mFitnessService = service;
    	mOnDeviceListener = listener;
    }
    
    /** @breif 클라이언트로부터 접속을 받아 타입에 따라 처리를 분기하는 스레드 */
    @Override
    public void run() {

        try {
            mServerSocket = new ServerSocket(FitnessWifiManager.SERVICE_PORT);

            while (!Thread.currentThread().isInterrupted()) {
				Log.i(tag, "ServerSocket Created, awaiting connection");
				Socket socket = mServerSocket.accept();
				Log.i(tag, "Connected.");
				try{
					 //Todo : 타입에 따라서 디바이스 정보를 전송하거나 계속해서 데이터를 받는다.
				        PrintWriter output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
				        BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				        
				        String messageStr = input.readLine();
				        Log.i(tag, messageStr);
				        if(messageStr.equals(FitnessNsdManager.SERVICE_REQUEST_DEVICE_INFO))
				        {
				        	output.println(android.os.Build.MODEL);
				        	output.flush();
				        	socket.close();
				        	continue;
				        }
				        else if(messageStr.equals(FitnessNsdManager.SERVICE_SENDING_FITNESS_DATA))
				        {
				        	socket.setSoTimeout(5000);
				        	
				        	
				        	FitnessDevice device = new FitnessDevice();
				        	device.macAddress = FitnessWifiManager.getMacFromArpCache(socket.getInetAddress().getHostAddress());
				        	device.ipAddress = socket.getInetAddress().getHostAddress();
				        	device.port = socket.getPort();
				        	if(mClientSocketList.get(device.macAddress)!=null){
				        		try{
				        			mClientSocketList.get(device.macAddress).close();
				        		}catch(IOException e){
				        			e.printStackTrace();
				        		}
				        	}
				        	
				        	// TODO 초기 전송 데이터를 받는다.
				        	//기기명 받기
							String message = input.readLine();
							if(message==null)throw new IOException();
							device.name = message;
							
							//사용자 받기
							message = input.readLine();
							if(message==null)throw new IOException();
							
							device.user = gson.fromJson(message, User.class);
							if(device.user==null)throw new IOException();
							
							//강도 받기
							message = input.readLine();
							if(message==null)break;
							int strength = gson.fromJson(message, int.class);
							mFitnessService.getFitnessPreference().putStrength(device.user, strength);
							
							//현재 상태 받기
							message = input.readLine();
							if(message==null)break;
							int state = gson.fromJson(message, int.class);
							if(mFitnessService.getFitnessManager(device.user)!=null)mFitnessService.getFitnessManager(device.user).setState(state);
							
				        	mClientSocketList.put(device.macAddress, socket);
				        	mOnDeviceListener.onDeviceConnected(device);
				        }
				    }
				   catch(IOException e){
					   e.printStackTrace();
				   }
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        tearDown();
        
    }
    
    public Socket getSocket(String address){
    	return mClientSocketList.get(address);
    }
    
    public void tearDown() {
		interrupt();
        try {
        	if(mServerSocket!=null) mServerSocket.close();
            Iterator<String> iter = mClientSocketList.keySet().iterator();
            while(iter.hasNext()){
            	Socket socket = mClientSocketList.get(iter.next());
            	if(socket!=null) socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
