package com.samsung.hsl.fitnessuser.comm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Iterator;

import com.google.gson.Gson;
import com.samsung.hsl.fitnessuser.service.FitnessBroadcastService;
import com.samsung.hsl.fitnessuser.service.FitnessBroadcastService.FitnessBroadcastListener;
import com.samsung.hsl.fitnessuser.service.FitnessUserService;
import com.samsung.hsl.fitnessuser.service.FitnessUserService.MODE;
import com.samsung.hsl.fitnessuser.sqlite.FitnessData;
import com.samsung.hsl.fitnessuser.sqlite.User;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

/**
 * @breif 동일한 네트워크에 속한 디바이스와의 통신을 담당하는 클래스
 * @details 동일한 AP에 연결된 디바이스와 통신을 담당한다. Android의 NSD(Network Service Discovery)를
 *          사용한다. {@link http
 *          ://developer.android.com/training/connect-devices-wirelessly
 *          /nsd.html}
 * @author jiwon
 * 
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class FitnessWifiManager implements IFitnessCommManager {
	private static String tag = FitnessWifiManager.class.getName();
	
	public static final String TYPE_FINTESS_DATA = "TYPE_FINTESS_DATA";
	public static final String TYPE_AIM_STRENGTH = "TYPE_AIM_STRENGTH";
	public static final String TYPE_USER_STATE = "TYPE_USER_STATE";
	public static final String TYPE_USER_INFO = "TYPE_USER_INFO";
	
	
	public static final int SERVICE_PORT = 6152;
	
	FitnessUserService mFitnessService;
	FitnessNsdManager mFitnessNsdManager = null;
	
	FitnessWifiFoundServer mFitnessWifiFoundServer = null;
	HashMap<String,FitnessWifiFoundClient> mFitnessWifiFoundClientList = new HashMap<String,FitnessWifiFoundClient>();
	HashMap<String,FitnessWifiProcessServer> mFitnessWifiProcessServerList = new HashMap<String,FitnessWifiProcessServer>();
	HashMap<String,FitnessWifiProcessClient> mFitnessWifiProcessClientList = new HashMap<String,FitnessWifiProcessClient>();
	
	IFitnessCommManager.onDeviceListener mOnDeviceListener;
	
	Gson gson = new Gson();
	
	public FitnessWifiManager(FitnessUserService service,IFitnessCommManager.onDeviceListener listener) {
		mFitnessService = service;
		mFitnessNsdManager = new FitnessNsdManager(service);
		mOnDeviceListener = listener;
		if(service.getServiceMode()==FitnessUserService.MODE.USER)
			FitnessBroadcastService.getInstance(service).addBroadcastObserver(mFitnessBroadcastListener);
	}

	FitnessBroadcastListener mFitnessBroadcastListener = new FitnessBroadcastListener(){

		@Override
		public void onReceiveFitnessData(User user, FitnessData data) {
			// TODO Auto-generated method stub
			if(mFitnessService.getServiceMode()==MODE.USER){
				Iterator<String> iter = mFitnessWifiProcessClientList.keySet().iterator();
				while(iter.hasNext()){
					mFitnessWifiProcessClientList.get(iter.next()).sendMessage(TYPE_FINTESS_DATA,gson.toJson(data));
				}
			}
		}

		@Override
		public void onReceiveFitnessFall(User user) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onChangeAimStrength(User user, int strength) {
			// TODO Auto-generated method stub
			if(mFitnessService.getServiceMode()==MODE.USER){
				Iterator<String> iter = mFitnessWifiProcessClientList.keySet().iterator();
				while(iter.hasNext()){
					mFitnessWifiProcessClientList.get(iter.next()).sendMessage(TYPE_AIM_STRENGTH,gson.toJson(strength));
				}
			}
		}

		@Override
		public void onChangeUserState(User user, int state) {
			// TODO Auto-generated method stub
			if(mFitnessService.getServiceMode()==MODE.USER){
				Iterator<String> iter = mFitnessWifiProcessClientList.keySet().iterator();
				while(iter.hasNext()){
					mFitnessWifiProcessClientList.get(iter.next()).sendMessage(TYPE_USER_STATE,gson.toJson(state));
				}
			}
		}

		@Override
		public void onChangeUserInfo(User user) {
			// TODO Auto-generated method stub
			if(mFitnessService.getServiceMode()==MODE.USER){
				Iterator<String> iter = mFitnessWifiProcessClientList.keySet().iterator();
				while(iter.hasNext()){
					mFitnessWifiProcessClientList.get(iter.next()).sendMessage(TYPE_USER_INFO,gson.toJson(user));
				}
			}
		}

		
	};
	
	IFitnessCommManager.onDeviceListener mOnManagerListener = new onDeviceListener() {
		
		@Override
		public void onDeviceFound(FitnessDevice device) {
			// TODO Auto-generated method stub
			device.commType = Type.WIFI;
			mOnDeviceListener.onDeviceFound(device);
		}
		
		@Override
		public void onDeviceDisconnected(FitnessDevice device) {
			// TODO Auto-generated method stub
			device.isConnected = false;
			if(mFitnessService.getServiceMode()==FitnessUserService.MODE.TRAINTER){
				mFitnessWifiProcessServerList.remove(device.macAddress);
			}
			else {
				mFitnessWifiProcessClientList.remove(device.macAddress);
			}
			
			mOnDeviceListener.onDeviceDisconnected(device);
		}
		
		@Override
		public void onDeviceConnected(FitnessDevice device) {
			// TODO Auto-generated method stub
			device.isConnected = true;
			if(isWifiDirect(device.ipAddress))device.commType = Type.WIFI_DIRECT;
			else device.commType = Type.WIFI;
			
			if(mFitnessService.getServiceMode()==FitnessUserService.MODE.TRAINTER){
				FitnessWifiProcessServer server = mFitnessWifiProcessServerList.get(device.macAddress);
				if(server!=null)server.tearDown();
				server = new FitnessWifiProcessServer(mFitnessService,device, mFitnessWifiFoundServer.getSocket(device.macAddress), mOnDeviceListener,mOnManagerListener);
				mFitnessWifiProcessServerList.put(device.macAddress, server);
				server.start();
				
			}
			mOnDeviceListener.onDeviceConnected(device);
		}
	};

	/**
	 * 트레이너(서버)에서 사용되는 매소드
	 */
	
	public void registerService(){
		mFitnessWifiFoundServer = new FitnessWifiFoundServer(mFitnessService,mOnManagerListener);
		mFitnessWifiFoundServer.start();
		mFitnessNsdManager.registerService();
	}

	public void unregisterService(){
		mFitnessWifiFoundServer.tearDown();
		mFitnessNsdManager.unregisterService();
	}
	
	/**
	 * 사용자(클라이언트)에서 사용되는 매소드 
	 */
	public void resolveDevice(InetAddress address,int port){
		FitnessWifiFoundClient client = mFitnessWifiFoundClientList.get(getMacFromArpCache(address.getHostAddress()));
		
		if(client==null){
			client = new FitnessWifiFoundClient(address,port,mOnManagerListener);
			mFitnessWifiFoundClientList.put(getMacFromArpCache(address.getHostAddress()), client);
			client.start();
		}
	}
	
	@Override
	public void startScan() {
		// TODO Auto-generated method stub
		stopScan();
		
		mFitnessWifiFoundClientList.clear();
		mFitnessNsdManager.discoverServices();
	}

	@Override
	public void connect(FitnessDevice device,boolean isTest) {
		// TODO Auto-generated method stub
		if(mFitnessService.getServiceMode()==FitnessUserService.MODE.TRAINTER){
			
		}else {
			FitnessWifiProcessClient client = mFitnessWifiProcessClientList.get(device.macAddress);
			if(client!=null)Log.i(tag, device.macAddress+" already connected");
			else {
				client = new FitnessWifiProcessClient(mFitnessService,device,mOnManagerListener);
				mFitnessWifiProcessClientList.put(device.macAddress, client);
				client.start();
			}
		}
	}

	@Override
	public void stopScan() {
		// TODO Auto-generated method stub
		mFitnessNsdManager.stopDiscovery();
		
		Iterator<String> iter = mFitnessWifiFoundClientList.keySet().iterator();
		while(iter.hasNext()){
			mFitnessWifiFoundClientList.get(iter.next()).tearDown();
		}
	}
	
	@Override
	public void disconnect(FitnessDevice device) {
		// TODO Auto-generated method stub
		if(mFitnessService.getServiceMode()==FitnessUserService.MODE.TRAINTER){
			FitnessWifiProcessServer server = mFitnessWifiProcessServerList.get(device.macAddress);
			if(server!=null)server.tearDown();
		}else {
			FitnessWifiProcessClient client = mFitnessWifiProcessClientList.get(device.macAddress);
			if(client!=null)client.tearDown();
		}
		
	}
	
	@Override
	public boolean isEnable() {
		// TODO Auto-generated method stub
		WifiManager mWifiManager = (WifiManager)mFitnessService.getSystemService(Context.WIFI_SERVICE);
		return mWifiManager.isWifiEnabled();
	}
	
	@Override
	public boolean isAvailable() {
		// TODO Auto-generated method stub
		ConnectivityManager connManager = (ConnectivityManager) mFitnessService.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		return mWifi.isConnected();
	}

	@Override
	public boolean isSupported() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Type getType() {
		// TODO Auto-generated method stub
		return IFitnessCommManager.Type.WIFI;
	}

	@Override
	public void modifyUsermFitnessUserData(User user) {
		// TODO Auto-generated method stub
		if(mFitnessService.getServiceMode()==MODE.USER){
			Iterator<String> iter = mFitnessWifiProcessClientList.keySet().iterator();
			// 여기서 바뀐 유저 정보 보내주고 트레이너는 받아야한다.
		}
	}
	
	public static String getMacFromArpCache(String ip) {
	    if (ip == null)
	        return null;
	    BufferedReader br = null;
	    try {
	        br = new BufferedReader(new FileReader("/proc/net/arp"));
	        String line;
	        while ((line = br.readLine()) != null) {
	            String[] splitted = line.split(" +");
	            if (splitted != null && splitted.length >= 4 && ip.equals(splitted[0])) {
	                // Basic sanity check
	                String mac = splitted[3];
	                if (mac.matches("..:..:..:..:..:..")) {
	                    return mac;
	                } else {
	                    return null;
	                }
	            }
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    } finally {
	        try {
	            br.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
	    return null;
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}
	
	public static String WIFI_DIRECT_PATTERN = "192.168.49.*";
	public static boolean isWifiDirect(String ip){
		return ip.matches(WIFI_DIRECT_PATTERN);
	}
}
