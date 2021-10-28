package com.samsung.hsl.fitnessuser.comm;

import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;

import com.google.gson.Gson;
import com.samsung.hsl.fitnessuser.service.FitnessBroadcastService;
import com.samsung.hsl.fitnessuser.service.FitnessBroadcastService.FitnessBroadcastListener;
import com.samsung.hsl.fitnessuser.service.FitnessUserService;
import com.samsung.hsl.fitnessuser.service.FitnessUserService.MODE;
import com.samsung.hsl.fitnessuser.sqlite.FitnessData;
import com.samsung.hsl.fitnessuser.sqlite.User;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Handler;
import android.util.Log;

public class FitnessWifiDirectManager implements IFitnessCommManager{
	private final static String tag = FitnessWifiDirectManager.class.getSimpleName();
	
	FitnessUserService mFitnessService;
	Channel mChannel;
	WifiP2pManager mManager;
	onDeviceListener mOnDeviceListener;
	IntentFilter intentFilter = new IntentFilter();
	
	BroadcastReceiver receiver = null;
	FitnessDevice mOwnerFitnessDevice;
	Gson gson = new Gson();
	
	public FitnessWifiDirectManager(FitnessUserService service,onDeviceListener listener) {
		// TODO Auto-generated constructor stub
		mFitnessService = service;
		mOnDeviceListener = listener;
		
	    mManager = (WifiP2pManager) service.getSystemService(Context.WIFI_P2P_SERVICE);
	    mChannel = mManager.initialize(service, service.getMainLooper(), null);
	    receiver = new WiFiDirectBroadcastReceiver();
	    
	    intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
        
        service.registerReceiver(receiver, intentFilter);
		FitnessBroadcastService.getInstance(service).addBroadcastObserver(mReceiveFitnessDataCallback);
	}
	
	FitnessWifiProcessClient mWifiProcessClient;
	FitnessBroadcastListener mReceiveFitnessDataCallback = new FitnessBroadcastListener(){

		@Override
		public void onReceiveFitnessData(User user, FitnessData data) {
			// TODO Auto-generated method stub
			if(mFitnessService.getServiceMode()==MODE.USER){
				if(mWifiProcessClient==null)return;
				mWifiProcessClient.sendMessage(FitnessWifiManager.TYPE_FINTESS_DATA,gson.toJson(data));
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
				if(mWifiProcessClient==null)return;
				mWifiProcessClient.sendMessage(FitnessWifiManager.TYPE_AIM_STRENGTH,gson.toJson(strength));
			}
		}

		@Override
		public void onChangeUserState(User user, int state) {
			// TODO Auto-generated method stub
			if(mFitnessService.getServiceMode()==MODE.USER){
				if(mWifiProcessClient==null)return;
				mWifiProcessClient.sendMessage(FitnessWifiManager.TYPE_USER_STATE,gson.toJson(state));
			}
		}

		@Override
		public void onChangeUserInfo(User user) {
			// TODO Auto-generated method stub
			if(mFitnessService.getServiceMode()==MODE.USER){
				if(mWifiProcessClient==null)return;
				mWifiProcessClient.sendMessage(FitnessWifiManager.TYPE_USER_INFO,gson.toJson(user));
			}
		}
		
	};
	
	@Override
	public boolean isEnable() 
	{
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isSupported() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isAvailable() {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public void startScan() {
		// TODO Auto-generated method stub
		if(isDiscoveryRunning==false){
			isDiscoveryRunning = true;
			mDiscoveryTimer = new Timer();
			mDiscoveryTimer.schedule(new DiscoveryTask(),0,DISCOVER_PEERS_TIME);
		}
	}

	@Override
	public void connect(final FitnessDevice device,boolean isTest) {
		final WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = device.macAddress;
		config.wps.setup = WpsInfo.PBC;
		
		if(mFitnessService.getServiceMode()==FitnessUserService.MODE.TRAINTER)
		{
			config.groupOwnerIntent = 15;
		}
		else 
		{
			config.groupOwnerIntent = 0;
		}
		
		mManager.connect(mChannel, config, new ActionListener() {
			 
            @Override
            public void onSuccess() {
            	Log.i(tag, "connect onSuccess");
            	mOwnerFitnessDevice = device;
            }
 
            @Override
            public void onFailure(int reason) {
            	Log.i(tag, "connect onFailure "+reason);
            }
        });
		
	}

	@Override
	public void disconnect(FitnessDevice device) {
		// TODO Auto-generated method stub
		Log.i(tag, "WifiDirectManager disconnect"+device.macAddress);
		if(mFitnessService.getServiceMode()==FitnessUserService.MODE.TRAINTER)
		{
			mFitnessService.getFitnessWifiManager().disconnect(device);
		}
		else 
		{
			if(mWifiProcessClient!=null)mWifiProcessClient.tearDown();
			mWifiProcessClient = null;
		}
	}

	@Override
	public void stopScan() {
		// TODO Auto-generated method stub
		if(isDiscoveryRunning==true){
			mDiscoveryTimer.cancel();
			isDiscoveryRunning = false;
		}
	}

	@Override
	public Type getType() {
		// TODO Auto-generated method stub
		return Type.WIFI_DIRECT;
	}
	
	@Override
	public void modifyUsermFitnessUserData(User user) {
		// TODO Auto-generated method stub
		if(mFitnessService.getServiceMode()==MODE.USER){
			// 여기서 바뀐 유저 정보 보내주고 트레이너는 받아야한다.
		}
	}
	
	class WiFiDirectBroadcastReceiver extends BroadcastReceiver {
	    @Override
	    public void onReceive(Context context, Intent intent) {

	        String action = intent.getAction();

	        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) 
	        {
	        	 Log.i(tag, "WIFI_P2P_STATE_CHANGED_ACTION");
	        } 
	        else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) 
	        {
	        	onChangeDeviceList();
	        } 
	        else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action))
	        {
	        	Log.i(tag, "WIFI_P2P_CONNECTION_CHANGED_ACTION");
	        	if (mManager == null) return;
	        	NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
	            if (networkInfo.isConnected()) {
	            	onWifiDirectConnected();
	            }else {
	            	onWifiDirectDisconnected();
	            }
	        } 
	        else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) 
	        {
	        	Log.i(tag, "WIFI_P2P_THIS_DEVICE_CHANGED_ACTION");
	        }
	    }
	}
	
	void onChangeDeviceList(){
		mManager.requestPeers(mChannel, new PeerListListener() 
		{
		    @Override
		    public void onPeersAvailable(WifiP2pDeviceList deviceList)
		    {
		        Collection<WifiP2pDevice> devList = deviceList.getDeviceList();
		        for(WifiP2pDevice device:devList)
		        {
		        	if(device.status==WifiP2pDevice.AVAILABLE)
		        	{
		        		FitnessDevice fd = new FitnessDevice(device.deviceAddress,null,device.deviceName,false,0,0,0);
		        		fd.commType = IFitnessCommManager.Type.WIFI_DIRECT;
		        		mOnDeviceListener.onDeviceFound(fd);
		        	}
		        	
		        }
		    }
		});
	}
	
	void onWifiDirectConnected(){
    	Log.i(tag, "onWifiDirectConnected");
        // 그룹 오너 IP를 찾기 위해 접속 정보를 요청한다.
    	mManager.requestConnectionInfo(mChannel, new ConnectionInfoListener()
		{
			@Override
			public void onConnectionInfoAvailable(WifiP2pInfo info) 
			{
				if(info.groupFormed&&info.isGroupOwner)
				{
					//서버는 아무 행동도 하지 않는다.
					//WifiManager에서 처리
				}
				else
				{
					Log.i(tag, "connection onConnectionInfoAvailable "+info.groupOwnerAddress);
					if(mOwnerFitnessDevice==null)return;
					
					mOwnerFitnessDevice.ipAddress = info.groupOwnerAddress.getHostAddress();
					mOwnerFitnessDevice.port = FitnessWifiManager.SERVICE_PORT;
					mOwnerFitnessDevice.commType = IFitnessCommManager.Type.WIFI_DIRECT;
					mOwnerFitnessDevice.isConnected = true;
					
					//클라이언트로써 host address 에 접속한다.
					if(mWifiProcessClient!=null)Log.i(tag, mOwnerFitnessDevice.ipAddress+" already connected");
					else {
						mWifiProcessClient = new FitnessWifiProcessClient(mFitnessService,mOwnerFitnessDevice,mUserDeviceListener);
						mWifiProcessClient.start();
					}
					stopScan();
				}
			}
			
		});
	}
	
	void onWifiDirectDisconnected(){
		Log.i(tag, "onWifiDirectDisconnected");
	}
	
	public void createGroup(){
		mManager.createGroup(mChannel, new ActionListener() {
			
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				Log.i(tag, "createGroup onSuccess");
			}
			
			@Override
			public void onFailure(int arg0) {
				// TODO Auto-generated method stub
				Log.i(tag, "createGroup onFailure");
			}
		});
	}
	
	private static final int DISCOVER_PEERS_TIME = 10000;
	boolean isDiscoveryRunning = false;
	Timer mDiscoveryTimer = null;
	class DiscoveryTask extends TimerTask{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			discoverPeers();
		}
		
	}
	
	void discoverPeers(){
		mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            //@Override
            public void onSuccess() {
                //피어 연결 준비 성공
            	Log.i(tag, "startScan onSuccess");
            }
            //@Override
            public void onFailure(int reasonCode) {
            	//피어 연결 준비 실패
            	Log.i(tag, "startScan onFailure");
            }
        });
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	Handler handler = new Handler();
	onDeviceListener mUserDeviceListener = new onDeviceListener(){

		@Override
		public void onDeviceFound(FitnessDevice device) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onDeviceConnected(FitnessDevice device) {
			// TODO Auto-generated method stub
			mOnDeviceListener.onDeviceConnected(device);
		}

		@Override
		public void onDeviceDisconnected(final FitnessDevice device) {
			// TODO Auto-generated method stub
			handler.postDelayed(new Runnable(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					mManager.cancelConnect(mChannel, null);
					mManager.removeGroup(mChannel, null);
					mWifiProcessClient = null;
					mOnDeviceListener.onDeviceDisconnected(device);
				}
				
			}, 5000);
			
		}
		
	};
}
