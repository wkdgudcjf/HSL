package com.samsung.hsl.fitnessuser.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import com.samsung.hsl.fitnessuser.R;
import com.samsung.hsl.fitnessuser.comm.FitnessBluetoothManager;
import com.samsung.hsl.fitnessuser.comm.FitnessDevice;
import com.samsung.hsl.fitnessuser.comm.FitnessWifiDirectManager;
import com.samsung.hsl.fitnessuser.comm.FitnessWifiManager;
import com.samsung.hsl.fitnessuser.comm.IFitnessCommManager;
import com.samsung.hsl.fitnessuser.comm.IFitnessCommManager.Type;
import com.samsung.hsl.fitnessuser.service.FitnessBroadcastService.FitnessBroadcastCallback;
import com.samsung.hsl.fitnessuser.service.FitnessBroadcastService.FitnessBroadcastListener;
import com.samsung.hsl.fitnessuser.sqlite.FitnessData;
import com.samsung.hsl.fitnessuser.sqlite.FitnessSQLiteManager;
import com.samsung.hsl.fitnessuser.sqlite.User;
import com.samsung.hsl.fitnessuser.ui.FitnessMainActivity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class FitnessUserService extends Service implements IFitnessCommManager.onDeviceListener {
	private static final String tag = FitnessUserService.class.getName();
	
	/** @brief 서비스가 현재 실행중임을 알리는 Notification */
	private NotificationManager mNotificationManager = null;
	/** NOTIFICATION Unique ID */
	private static final int NOTIFICATION_SERVICE_RUNNING = 0x01;
	
	public MODE mServiceMode = MODE.USER;
	public enum MODE {
		TRAINTER,
		USER
	}
	
	public MODE getServiceMode(){
		return mServiceMode;
	}
	
	/**
	 * @brief LocalBinder. 
	 * @details 앱과 같은 프로세스에서 동작하는 것을 가정하여 별도의 IPC를 위한 처리를 하지 않아도 되는 로컬바인더 활용
	 * @author jiwon
	 *
	 */
	public class LocalBinder extends Binder{
		public FitnessUserService getService(){
			return FitnessUserService.this;
		}
	}

	/** @brief 바인더 객체 생성 */
	private final IBinder mBinder = new LocalBinder();
	
	/**
	 * @brief 서비스 생성
	 * @details 초기 작업 수행 및 서비스가 동작중임을 알리는 Notification을 보여준다.
	 */
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		
		// Notification 알림
		//mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		//showNotification();

		FitnessSQLiteManager.checkExternalDirectory();
		mFitnessSQLiteManager = new FitnessSQLiteManager(this);
		mFitnessPreference = new FitnessPreference(this);
		mFitnessBroadcastService = FitnessBroadcastService.getInstance(this);
		mFitnessBluetoothManager = new FitnessBluetoothManager(this,this);
		mFitnessWifiManager = new FitnessWifiManager(this,this);
		mFitnessWifiDirectManager = new FitnessWifiDirectManager(this,this);

		mFitnessNotificationManager = initFitnessNotificationManager();
		initBroadcast();
		mServiceMode = MODE.USER;
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(tag, "Service Start");
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }
	
	/**
	 * @brief onBind 호출 시 클라이언트(앱)에 바인더를 리턴한다.
	 */
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		Log.i(tag, "Service onBind");
		return mBinder;
	}

	/**
	 * @brief 서비스 종료
	 * @details 여러 자원을 해제한다.
	 */
	@Override
    public void onDestroy() {
		Log.i(tag, "Service Stop");
		closeFitnessManager();
		getFitnessBroadcastService().close();
		getFitnessSQLiteManager().close();
    }
	
	/**
     * @brief 서비스가 현재 동작중임을 알려준다.
     * @details 최소 요구 버전이 4.0이므로 Notification.Builder를 사용할 수 없다.
     */
    private void showNotification() 
    {
    	// In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = "FitnessService is running...";

        // Set the icon, scrolling text and timestamp
        Notification notification = new Notification(R.drawable.ic_launcher, text,
                System.currentTimeMillis());

        // The PendingIntent to launch our activity if the user selects this notification
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, FitnessMainActivity.class), 0);

        // Set the info for the views that show in the notification panel.
        notification.setLatestEventInfo(this, "Fitness", text, contentIntent);
        notification.flags |= notification.FLAG_AUTO_CANCEL;
        
        // Send the notification.
        mNotificationManager.notify(NOTIFICATION_SERVICE_RUNNING, notification);
        
    }
	
    /** 운동 관련 기능 */
    protected HashMap<String,FitnessManager> mFitnessManagerMap = new HashMap<String,FitnessManager>();
    protected User mCurrentUser = null; 
	public void setCurrentUser(User user) {
		// TODO Auto-generated method stub
		mCurrentUser = user;
	}

	public User getCurrentUser() {
		// TODO Auto-generated method stub
		return mCurrentUser;
	}

	public FitnessManager getFitnessManager() {
		// TODO Auto-generated method stub
		return getFitnessManager(mCurrentUser);
	}
	
	public FitnessManager getFitnessManager(User user) {
		// TODO Auto-generated method stub
		FitnessManager fitnessManager = mFitnessManagerMap.get(user.email);
		if(fitnessManager==null){
			fitnessManager = new FitnessManager(user,this);
			mFitnessManagerMap.put(user.email,fitnessManager);
			//fitnessManager.startFitness();
		}
		return fitnessManager;
	}
	
    public void closeFitnessManager(){
    	Iterator<Entry<String,FitnessManager>> iterator = mFitnessManagerMap.entrySet().iterator();
    	while(iterator.hasNext()){
    		iterator.next().getValue().close();
    	}
    }
    
    /** Notification 기능 */
    protected FitnessUserNotificationManager mFitnessNotificationManager = null;

	public FitnessUserNotificationManager initFitnessNotificationManager() {
		// TODO Auto-generated method stub
		return new FitnessUserNotificationManager(this);
	}
	
    public FitnessUserNotificationManager getFitnessNotificationManager(){return mFitnessNotificationManager;}
    
	/** Bluetooth 통신 관련 기능 */
    protected FitnessBluetoothManager mFitnessBluetoothManager = null;
	public FitnessBluetoothManager getFitnessBluetoothManager(){return mFitnessBluetoothManager;}
	
	/** Wifi 통신 관련 기능 */
	protected FitnessWifiManager mFitnessWifiManager = null;
	public FitnessWifiManager getFitnessWifiManager(){return mFitnessWifiManager;}
	
	/** Wifi 통신 관련 기능 */
	protected FitnessWifiDirectManager mFitnessWifiDirectManager = null;
	public FitnessWifiDirectManager getFitnessWifiDirectManager(){return mFitnessWifiDirectManager;}
	
	/** SQLite 관련 기능 */
	protected FitnessSQLiteManager mFitnessSQLiteManager = null;
	public FitnessSQLiteManager getFitnessSQLiteManager(){return mFitnessSQLiteManager;}
	
	/** preference 기능 */
	protected FitnessPreference mFitnessPreference = null;
	public FitnessPreference getFitnessPreference(){return mFitnessPreference;}
	
	/** 브로드캐스트 기능 */
	protected FitnessBroadcastService mFitnessBroadcastService = null;
	public FitnessBroadcastService getFitnessBroadcastService(){return mFitnessBroadcastService;}
	
	public IFitnessCommManager getFitnessCommManager(IFitnessCommManager.Type type){
		if(type==IFitnessCommManager.Type.BLUETOOTH_LOW_ENERGY){
			return getFitnessBluetoothManager();
		}
		else if(type==IFitnessCommManager.Type.WIFI){
			return getFitnessWifiManager();
		}
		else {
			return getFitnessWifiDirectManager();
		}
	}
	
	/** @brief 해당 통신이 현재 가능한 상태인지 리턴한다. */
	public boolean isCommEnable(){
		return getFitnessCommManager(IFitnessCommManager.Type.BLUETOOTH_LOW_ENERGY).isEnable() 
				&& getFitnessCommManager(IFitnessCommManager.Type.WIFI).isEnable() 
				&& getFitnessCommManager(IFitnessCommManager.Type.WIFI_DIRECT).isEnable();
	}
	
	/** @brief 통신 기기가 연결되 있는지 확인하는 부분. */
	public boolean isCommAvail(){
		return getFitnessCommManager(IFitnessCommManager.Type.BLUETOOTH_LOW_ENERGY).isAvailable(); 
	}
	
	/** @brief 해당 통신을 기기가 지원하는지 리턴한다. */
	public boolean isCommSupported(){
		return getFitnessCommManager(IFitnessCommManager.Type.BLUETOOTH_LOW_ENERGY).isSupported() 
				|| getFitnessCommManager(IFitnessCommManager.Type.WIFI).isSupported()
				|| getFitnessCommManager(IFitnessCommManager.Type.WIFI_DIRECT).isSupported();
	}
	
	/** @breif FitnessDevice 객체를 관리하는 리스트 */
	protected HashMap<String,FitnessDevice> mFitnessDeviceList = new HashMap<String,FitnessDevice>();
	
	Handler handler = new Handler();
	public static final long SCAN_PERIOD = 5000;
	/** 
	 * @brief 주변기기 검색을 시작한다.
	 * @details 5초후 스캔을 중지한다. 하위 클래스에서 오버라이딩해야한다.
	 */
	public void startScan(){
		handler.postDelayed(new Runnable() {
		       @Override
		       public void run() {
		    	   stopScan();
		       }
		    }, SCAN_PERIOD);
		getFitnessCommManager(IFitnessCommManager.Type.BLUETOOTH_LOW_ENERGY).startScan();
		getFitnessCommManager(IFitnessCommManager.Type.WIFI).startScan();
		getFitnessCommManager(IFitnessCommManager.Type.WIFI_DIRECT).startScan();
	}
	/** 
	 * @brief 정보수정.
	 * @details 각 통신에 정보 수정을 요청.
	 */
	public void modifyUsermFitnessUserData(User user) {
		getFitnessCommManager(IFitnessCommManager.Type.BLUETOOTH_LOW_ENERGY).modifyUsermFitnessUserData(user);
		getFitnessCommManager(IFitnessCommManager.Type.WIFI).modifyUsermFitnessUserData(user);
		getFitnessCommManager(IFitnessCommManager.Type.WIFI_DIRECT).modifyUsermFitnessUserData(user);
	}
	/** 
	 * @brief 주변기기 검색을 멈춘다.
	 * @details 하위 클래스에서 오버라이딩해야한다.
	 */
	public void stopScan(){
		getFitnessCommManager(IFitnessCommManager.Type.BLUETOOTH_LOW_ENERGY).stopScan();
		getFitnessCommManager(IFitnessCommManager.Type.WIFI).stopScan();	
		//getFitnessCommManager(IFitnessCommManager.Type.WIFI_DIRECT).stopScan();
	}
	
	/** @brief 검색된 디바이스 정보를 리턴한다. */
	public List<FitnessDevice> getScanedDeviceList(){
		ArrayList<FitnessDevice> list = new ArrayList<FitnessDevice>();
		Iterator<String> iter = mFitnessDeviceList.keySet().iterator();
		while(iter.hasNext()){
			list.add(mFitnessDeviceList.get(iter.next()));
		}
		return list;
	}
	/** @brief 해당 디바이스와 연결한다. */
	public void connect(FitnessDevice device,boolean isTest){
		Log.i(tag, "Connecting ... "+device.macAddress+" / "+device.commType);
		getFitnessCommManager(device.commType).connect(device,isTest);
	}
	/** @brief 연결된 디바이스 정보를 리턴한다. */
	public List<FitnessDevice> getConnectedDeviceList(){
		ArrayList<FitnessDevice> list = new ArrayList<FitnessDevice>();
		list.addAll(getConnectedDeviceList(IFitnessCommManager.Type.BLUETOOTH_LOW_ENERGY));
		list.addAll(getConnectedDeviceList(IFitnessCommManager.Type.WIFI));
		list.addAll(getConnectedDeviceList(IFitnessCommManager.Type.WIFI_DIRECT));
		return list;
	}
	
	public List<FitnessDevice> getConnectedDeviceList(IFitnessCommManager.Type type){
		ArrayList<FitnessDevice> list = new ArrayList<FitnessDevice>();
		Iterator<String> iter = mFitnessDeviceList.keySet().iterator();
		while(iter.hasNext()){
			FitnessDevice device = mFitnessDeviceList.get(iter.next());
			if(device.isConnected && device.commType==type)list.add(device);
		}
		return list;
	}
	/** @brief 연결되지 않은 디바이스 정보를 리턴한다. */
	public List<FitnessDevice> getUnConnectedDeviceList(){
		ArrayList<FitnessDevice> list = new ArrayList<FitnessDevice>();
		list.addAll(getUnConnectedDeviceList(IFitnessCommManager.Type.BLUETOOTH_LOW_ENERGY));
		list.addAll(getUnConnectedDeviceList(IFitnessCommManager.Type.WIFI));
		list.addAll(getUnConnectedDeviceList(IFitnessCommManager.Type.WIFI_DIRECT));
		return list;
	}
	
	public List<FitnessDevice> getUnConnectedDeviceList(IFitnessCommManager.Type type){
		ArrayList<FitnessDevice> list = new ArrayList<FitnessDevice>();
		Iterator<String> iter = mFitnessDeviceList.keySet().iterator();
		while(iter.hasNext()){
			FitnessDevice device = mFitnessDeviceList.get(iter.next());
			if(!device.isConnected && device.commType==type)list.add(device);
		}
		return list;
	}
	
	/** @brief 해당 디바이스와 연결을 끊는다. */
	public void disconnect(FitnessDevice device){
		getFitnessCommManager(device.commType).disconnect(device);
	}
	
	/** @brief 서비스 브로드캐스트를 초기화한다. */
	private void initBroadcast(){
		mFitnessBroadcastService.addBroadcastObserver(FitnessBroadcastService.BROADCAST_START_COMM_SCAN, mStartScanCallback);
		mFitnessBroadcastService.addBroadcastObserver(mFitnessBroadcastListener);
	}
	
	FitnessBroadcastCallback mStartScanCallback = new FitnessBroadcastCallback(){

		@Override
		public void receive() {
			// TODO Auto-generated method stub
			List<FitnessDevice> connectedDevice = getConnectedDeviceList();
			mFitnessDeviceList.clear();
			for(int i=0;i<connectedDevice.size();i++){
				mFitnessDeviceList.put(connectedDevice.get(i).macAddress,connectedDevice.get(i));
			}
		}
		
	};
	
	@Override
	public void onDeviceFound(FitnessDevice device) {
		// TODO Auto-generated method stub
		//Log.i(tag, "onDeviceFound - mac : "+device.macAddress+" / ip : "+device.ipAddress+" / type : "+device.commType + " / isConnected : "+device.isConnected);
		
		mFitnessDeviceList.put(device.macAddress, device);
		FitnessBroadcastService.sendBroadcast(FitnessBroadcastService.BROADCAST_CHANGE_DEVICE_LIST);
	}

	@Override
	public void onDeviceConnected(FitnessDevice device) {
		// TODO Auto-generated method stub
		if(device.commType==Type.BLUETOOTH_LOW_ENERGY)device.user = getCurrentUser();
		Log.i(tag, "onDeviceConnected - mac : "+device.macAddress+" / ip : "+device.ipAddress+" / type : "+device.commType + " / isConnected : "+device.isConnected);
		mFitnessDeviceList.put(device.macAddress, device);
		if(device.user!=null){
			FitnessManager manager = getFitnessManager(device.user);
			boolean check = device.commType==Type.BLUETOOTH_LOW_ENERGY ? true : false;
			manager.setCheckState(check);
		}
		
		FitnessBroadcastService.sendBroadcast(FitnessBroadcastService.BROADCAST_CHANGE_DEVICE_LIST);
	}

	@Override
	public void onDeviceDisconnected(FitnessDevice device) {
		// TODO Auto-generated method stub
		Log.i(tag, "onDeviceDisconnected - mac : "+device.macAddress+" / ip : "+device.ipAddress+" / type : "+device.commType + " / isConnected : "+device.isConnected);
		
		mFitnessDeviceList.remove(device.macAddress);
		
		FitnessBroadcastService.sendBroadcast(FitnessBroadcastService.BROADCAST_CHANGE_DEVICE_LIST);
	}

	FitnessBroadcastListener mFitnessBroadcastListener = new FitnessBroadcastListener(){

		@Override
		public void onReceiveFitnessData(User user, FitnessData data) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onReceiveFitnessFall(User user) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onChangeAimStrength(User user, int strength) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onChangeUserState(User user, int state) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onChangeUserInfo(User user) {
			// TODO Auto-generated method stub
			// 현재 선택한 유저의 정보가 변경된 경우
			if(getCurrentUser()!=null && getCurrentUser().email.equals(user.email))getCurrentUser().setUser(user);
//			List<FitnessDevice> deviceList = getConnectedDeviceList();
//			for(int i=0;i<deviceList.size();i++){
//				User cursor = deviceList.get(i).user;
//				if(cursor==null)continue;
//				if(cursor.email.equals(user.email))cursor.setUser(user);
//			}
			Iterator<String> iter = mFitnessManagerMap.keySet().iterator();
			while(iter.hasNext()){
				FitnessManager manager = mFitnessManagerMap.get(iter.next());
				if(manager==null)continue;
				if(manager.getUser().email.equals(user.email))manager.getUser().setUser(user);
			}
		}
		
	};
}
