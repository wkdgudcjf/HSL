package com.samsung.hsl.fitnessuser.comm;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import com.samsung.hsl.fitnessuser.service.FitnessBroadcastService;
import com.samsung.hsl.fitnessuser.service.FitnessUserService;
import com.samsung.hsl.fitnessuser.sqlite.FitnessBLE;
import com.samsung.hsl.fitnessuser.sqlite.FitnessData;
import com.samsung.hsl.fitnessuser.sqlite.FitnessSQLiteManager;
import com.samsung.hsl.fitnessuser.sqlite.User;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

@SuppressLint("NewApi")
public class FitnessBluetoothGattCallback extends BluetoothGattCallback {
	static public String TAG = "FitnessBluetoothGattCallback";
	public User user; // user 받아야함
	FitnessUserService mFitnessService;
	FitnessBluetoothManager mFitnessBluetoothManager;
	FitnessSQLiteManager mFitnessSQLiteManager;
	FitnessDevice data;
	String mBluetoothDeviceAddress; // 이것도 받아야함
	BluetoothGatt mBluetoothGatt; // 이것도 받아야함.
	Handler mHandler = new Handler();
	boolean isFirst;
	FitnessBLE ble = new FitnessBLE();
	public int count;
	public boolean isTest;
	File logFile;
	PrintWriter logFileWriter;
	public static final UUID PRV_SERVICE = UUID.fromString("fc1cff01-71f1-887f-c5fa-a19b3dca3230");
	public static final UUID WRT_CHARAC = UUID.fromString("fc1cff07-71f1-887f-c5fa-a19b3dca3230");
	public static final UUID RECV_CHARAC = UUID.fromString("fc1cff08-71f1-887f-c5fa-a19b3dca3230");
	public static final UUID CCC = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
	
	public FitnessBluetoothGattCallback(FitnessUserService service,FitnessDevice data,boolean check)
	{
		this.mFitnessService = service;
		mFitnessBluetoothManager = mFitnessService.getFitnessBluetoothManager();
		mFitnessSQLiteManager = mFitnessService.getFitnessSQLiteManager();
		this.data = data;
		mBluetoothDeviceAddress = data.macAddress;
		user = data.user;
		isTest = check;
		
		if(isTest)createLogFile();
	}
	
	void createLogFile(){
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
	
	public void setBluetoothGatt(BluetoothGatt bgt)
	{
		this.mBluetoothGatt = bgt;
	}

	public void setCopyUser(User user) {
		// TODO Auto-generated method stub
		this.user.birthday = user.birthday;
		this.user.gender = user.gender;
		this.user.name = user.name;
		this.user.picture = user.picture;
		this.user.height = user.height;
		this.user.weight = user.weight;
	}
	// 운동 시작
	public void startFitness() {
		// TODO Auto-generated method stub
		Log.i("test", "Bluetooth에 의해 startFitness 호출");
		mFitnessService.getFitnessManager(user).startFitness();
	}
	// 운동 종료.
	public void stopFitness() {
		// TODO Auto-generated method stub
		mFitnessService.getFitnessManager(user).stopFitness();
	}
	/* 0xaa 0x88 yy mm dd hh mm ss 00 00 00 00 00 00 age height weight gender 0x88 0xaa */
	public void sendFirstMessage() {
		// TODO Auto-generated method stub
		StringTokenizer stk = new StringTokenizer(user.birthday,"-");
		Calendar c = Calendar.getInstance();
		char age = (char) (c.get(Calendar.YEAR) - Integer.parseInt(stk.nextToken()));
		char gender = 0;
		if(user.gender.compareTo("남자")==0)
			gender = 1;
		char height = (char) user.height;
		char weight = (char) user.weight;
		byte[] data = new byte[20];
		data[0] = (byte) 0xaa; data[1] = (byte) 0x88; data[2] = (byte) (c.get(Calendar.YEAR)%100); data[3] = (byte) (c.get(Calendar.MONTH)+1);
		data[4] = (byte) c.get(Calendar.DAY_OF_MONTH); data[5] = (byte) c.get(Calendar.HOUR_OF_DAY); data[6] = (byte) c.get(Calendar.MINUTE); data[7] = (byte) c.get(Calendar.SECOND);
		data[8] = (byte) 0x00; data[9] = (byte) 0x00; data[10] = (byte) 0x00; data[11] = (byte) 0x00;
		data[12] = (byte) 0x00; data[13] = (byte) 0x00; data[14] = (byte) age; data[15] = (byte) height;
		data[16] = (byte) weight; data[17] = (byte) gender; data[18] = (byte) 0x88; data[19] = (byte) 0xaa;
		CmdUtils.printRaw(1, data);
		writeCharacteristic(data);
	}
	/* 0xaa 0x88 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0x66 0xaa */
	public void sendSecondMessage() 
	{
		// TODO Auto-generated method stub
		byte[] data = new byte[20];
		data[0] = (byte) 0xaa; data[1] = (byte) 0x88; data[2] = (byte) 0x00; data[3] = (byte) 0x00;
		data[4] = (byte) 0x00; data[5] = (byte) 0x88; data[6] = (byte) 0x00; data[7] = (byte) 0x00;
		data[8] = (byte) 0x00; data[9] = (byte) 0x00; data[10] = (byte) 0x00; data[11] = (byte) 0x00;
		data[12] = (byte) 0x00; data[13] = (byte) 0x00; data[14] = (byte) 0x00; data[15] = (byte) 0x00;
		data[16] = (byte) 0x00; data[17] = (byte) 0x00; data[18] = (byte) 0x66; data[19] = (byte) 0xaa;
		CmdUtils.printRaw(1, data);
		writeCharacteristic(data);
	}
	
	/* 0xaa 0x88 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 0x77 0xaa */
	public void sendTestMessage() 
	{
		// TODO Auto-generated method stub
		byte[] data = new byte[20];
		data[0] = (byte) 0xaa; data[1] = (byte) 0x88; data[2] = (byte) 0x00; data[3] = (byte) 0x00;
		data[4] = (byte) 0x00; data[5] = (byte) 0x88; data[6] = (byte) 0x00; data[7] = (byte) 0x00;
		data[8] = (byte) 0x00; data[9] = (byte) 0x00; data[10] = (byte) 0x00; data[11] = (byte) 0x00;
		data[12] = (byte) 0x00; data[13] = (byte) 0x00; data[14] = (byte) 0x00; data[15] = (byte) 0x00;
		data[16] = (byte) 0x00; data[17] = (byte) 0x00; data[18] = (byte) 0x77; data[19] = (byte) 0xaa;
		CmdUtils.printRaw(1, data);
		writeCharacteristic(data);
	}

	/* 0xaa 0x88 00 00 00 00 00 00 00 00 00 00 00 00 00 00 count1 count2 0x54 0xaa */
	public void sendThirdMessage() 
	{
		// TODO Auto-generated method stub
		byte[] bytes = ByteBuffer.allocate(4).putInt(count).array();
		final byte[] data = new byte[20];
		data[0] = (byte) 0xaa; data[1] = (byte) 0x88; data[2] = (byte) 0x00; data[3] = (byte) 0x00;
		data[4] = (byte) 0x00; data[5] = (byte) 0x00; data[6] = (byte) 0x00; data[7] = (byte) 0x00;
		data[8] = (byte) 0x00; data[9] = (byte) 0x00; data[10] = (byte) 0x00; data[11] = (byte) 0x00;
		data[12] = (byte) 0x00; data[13] = (byte) 0x00; data[14] = (byte) 0x00; data[15] = (byte) 0x00;
		data[16] = (byte) bytes[3]; data[17] = (byte) bytes[2]; data[18] = (byte) 0x54; data[19] = (byte) 0xaa;
		CmdUtils.printRaw(1, data);
		writeCharacteristic(data);
		
	}
	 @SuppressLint("NewApi")
	private void broadcastUpdate(final BluetoothGattCharacteristic characteristic)
	{
	     final byte[] data = characteristic.getValue();
         reciveFitnessData(data);
    }
	// 데이터를 받았을때 나오는 메소드.
	public void reciveFitnessData(final byte[] data) 
	{
		CmdUtils.printRaw(0, data);
		if(data[18]==(byte)0x88)
		{
			if(isFirst) // 처음 연결될때만 진행. 왜냐하면 myinfo에서 정보 바꿀때 이걸 진행하면 안됨.
			{
				//SW HW 를 디비에 저장한다. 근데 어떤 어드레스인지 알아야한다. 그럼 콜백메소드를 몇번이나?
				int sw = Integer.parseInt(CmdUtils.byteToHexString(data[10]), 16);
				int hw = Integer.parseInt(CmdUtils.byteToHexString(data[11]), 16);
				ble.address = mBluetoothDeviceAddress;
				ble.sw = sw;
				ble.hw = hw;
				mFitnessSQLiteManager.insert(ble);
				List<FitnessDevice> list = mFitnessService.getScanedDeviceList();
	             for(int i=0;i<list.size();i++)
	             {
	            	 if(list.get(i).macAddress.compareTo(mBluetoothDeviceAddress)==0)
	            	 {
	            		 list.get(i).sw = sw;
	            		 list.get(i).hw = hw;
	            		 //최초 연결
	            		 mFitnessBluetoothManager.mOnDeviceListener.onDeviceConnected(list.get(i));
	            	 }
	             }
	             if(isTest)
	             {
	            	 sendTestMessage();
	             }
	             else
	             {
	            	 sendSecondMessage();
	             }
				isFirst = false;
			}
		}
		else if(data[18]==(byte)0x55)
		{
			sendThirdMessage();
		}
		else if(data[18]==(byte)0x66)
		{
			final int heartrate = Integer.parseInt(CmdUtils.byteToHexString(data[2]), 16);
			final int filterHeartrate = Integer.parseInt(CmdUtils.byteToHexString(data[3]), 16);
			int skinTemperatureLower = Integer.parseInt(CmdUtils.byteToHexString(data[4]), 16);
			int skinTemperatureUpper = Integer.parseInt(CmdUtils.byteToHexString(data[5]), 16);
			skinTemperatureUpper = skinTemperatureUpper << 8;
			float skinTemperature = skinTemperatureLower+skinTemperatureUpper;
			int humidityLower = Integer.parseInt(CmdUtils.byteToHexString(data[6]), 16);
			int humidityUpper = Integer.parseInt(CmdUtils.byteToHexString(data[7]), 16);
			humidityUpper = humidityUpper << 8;
			float humidity = humidityLower+humidityUpper;
			int consumeCalorieLower = Integer.parseInt(CmdUtils.byteToHexString(data[8]), 16);
			int consumeCalorieUpper = Integer.parseInt(CmdUtils.byteToHexString(data[9]), 16);
			
			int countLower = Integer.parseInt(CmdUtils.byteToHexString(data[12]), 16);
			int countUpper = Integer.parseInt(CmdUtils.byteToHexString(data[13]), 16);
			countUpper = countUpper << 8;
			count = countLower + countUpper;
					
			consumeCalorieUpper = consumeCalorieUpper << 8;
			float consumeCalorie = consumeCalorieLower+consumeCalorieUpper;
			int battery = Integer.parseInt(CmdUtils.byteToHexString(data[10]), 16);
			int fall = Integer.parseInt(CmdUtils.byteToHexString(data[11]), 16);
			// 배터리랑 sw/hw 버젼 계속 갱신해줘야한다.
			ble.address = mBluetoothDeviceAddress;
			mFitnessService.getFitnessSQLiteManager().select(ble);
			List<FitnessDevice> list = mFitnessService.getScanedDeviceList();
             for(int i=0;i<list.size();i++)
             {
            	 if(list.get(i).macAddress.compareTo(mBluetoothDeviceAddress)==0)
            	 {
            		 list.get(i).sw = ble.sw;
            		 list.get(i).hw = ble.hw;
            		 list.get(i).battery = battery;
            	 }
             }
            sendBroadcastFitnessData(heartrate, filterHeartrate, skinTemperature/10.0f, humidity, consumeCalorie/10.0f, battery, fall);
			if(mFitnessService.getFitnessManager(user).isExceedAimHeartrate(heartrate) && mFitnessService.getFitnessNotificationManager()!=null) // 심박수가 넘어갈 경우.
			{
				mFitnessService.getFitnessNotificationManager().exceedHeartrate(null, heartrate); 
			}
		}
		else
		{
				logFileWriter.println(CmdUtils.bytesToHexString(data));
//				logHandler.post(new Runnable(){
//
//					@Override
//					public void run() {
//						// TODO Auto-generated method stub
//						if(logToastDelayed){
//							Log.i(TAG, "show log toast");
//							Toast.makeText(mFitnessService.getApplicationContext(), CmdUtils.bytesToHexString(data),Toast.LENGTH_SHORT).show();
//							logToastDelayed = false;
//							
//							TimerTask logToastDelayedTask = new TimerTask() {
//								
//								@Override
//								public void run() {
//									// TODO Auto-generated method stub
//									logToastDelayed = true;
//								}
//							};
//							logToastTimer.schedule(logToastDelayedTask, 2000);
//						}
//					}
//					
//				});
		}
	}
	
	boolean logToastDelayed = true;
	Handler logHandler = new Handler(Looper.getMainLooper());
	Timer logToastTimer = new Timer();
	TimerTask logToastDelayedTask;
	
	void sendBroadcastFitnessData(int heartrate, int filterHeartrate, float skinTemperature, float humidity, float consumeCalorie, int power, int fall){
		FitnessData data = new FitnessData();
		data.heartrate = heartrate;
		data.filterHeartrate = filterHeartrate;
		data.skinTemperature = skinTemperature;
		data.humidity = humidity;
		data.consumeCalorie = consumeCalorie;
		data.power = power;
		data.fall = fall;
		FitnessBroadcastService.sendBroadcast(FitnessBroadcastService.BROADCAST_RECEIVE_FITNESS_DATA, user, data);
		FitnessBroadcastService.sendBroadcast(FitnessBroadcastService.BROADCAST_CHANGE_DEVICE_LIST);
	}
	
	 @Override
     public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState)
     {
         if (newState == BluetoothProfile.STATE_CONNECTED) {
              Log.i(TAG, "Connected to GATT server.");
             // Attempts to discover services after successful connection.
             Log.i(TAG, "Attempting to start service discovery:" +
                     mBluetoothGatt.discoverServices());
             List<FitnessDevice> list = mFitnessService.getScanedDeviceList();
             for(int i=0;i<list.size();i++)
             {
            	 if(list.get(i).macAddress.compareTo(mBluetoothDeviceAddress)==0)
            	 {
            		 list.get(i).isConnected = true;
            		 list.get(i).user = this.user;
            	 }
             }
        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
          Log.i(TAG, "Disconnected from GATT server.");
          List<FitnessDevice> list = mFitnessService.getScanedDeviceList();
          for(int i=0;i<list.size();i++)
          {
         	 if(list.get(i).macAddress.compareTo(mBluetoothDeviceAddress)==0)
         	 {
         		 list.get(i).isConnected = false;
         		 mFitnessBluetoothManager.mOnDeviceListener.onDeviceDisconnected(list.get(i));
         	 }
          }
          stopFitness();
      }
   }
	 
   @Override
   public void onServicesDiscovered(BluetoothGatt gatt, int status) {
         if (status == BluetoothGatt.GATT_SUCCESS) {
        	 Handler mHandler = new Handler(Looper.getMainLooper());
                mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						enableDevCharateristic();
					}
                 }, 1000);
         } 
         else 
         {
             Log.w(TAG, "onServicesDiscovered received: " + status);
         }
    }

  	@Override
	public void onCharacteristicWrite(BluetoothGatt gatt,
			BluetoothGattCharacteristic characteristic, int status) {
		super.onCharacteristicWrite(gatt, characteristic, status);
		Log.d("BluetoothGattCallback", "onCharacteristicWrite");
	}
	
	@Override
	public void onDescriptorRead(BluetoothGatt gatt,
			BluetoothGattDescriptor descriptor, int status) {
		super.onDescriptorRead(gatt, descriptor, status);
		Log.d("BluetoothGattCallback", "onDescriptorRead");
        if (status == BluetoothGatt.GATT_SUCCESS) 
        {
			BluetoothGattCharacteristic mRECVcharac = descriptor.getCharacteristic();
			enableIndication(true, mRECVcharac, false /*true : indi, false : noti*/);
		}
	}
	
	@Override
	public void onDescriptorWrite(BluetoothGatt gatt,
			BluetoothGattDescriptor descriptor, int status) {
		super.onDescriptorRead(gatt, descriptor, status);
		Log.d("BluetoothGattCallback", "onDescriptorWrite");
        if (status == BluetoothGatt.GATT_SUCCESS) {
        	if(user==null)
        		return;
        	isFirst = true;
        	sendFirstMessage();
        	startFitness();
        	FitnessBroadcastService.sendBroadcast(FitnessBroadcastService.BROADCAST_CONNECT_SUCCESS);
		}
	}
	
    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt,
                                        BluetoothGattCharacteristic characteristic) {
    	 Log.i(TAG, "change.");
         broadcastUpdate(characteristic);
    }

	@Override
	public void onCharacteristicRead(BluetoothGatt gatt,
			BluetoothGattCharacteristic characteristic, int status) {
		super.onCharacteristicRead(gatt, characteristic, status);
		Log.d("BluetoothGattCallback", "onCharacteristicRead");
        if (status == BluetoothGatt.GATT_SUCCESS) 
        {
        	UUID charUuid =characteristic.getUuid();
			if (charUuid.equals(RECV_CHARAC)) setDevCharateristic(characteristic);   
        }
	}


	@SuppressLint("NewApi")
	public boolean enableIndication(boolean enable,	BluetoothGattCharacteristic characteristic, boolean isIndi) {
		if (mBluetoothGatt == null)	return false;
		if (!mBluetoothGatt.setCharacteristicNotification(characteristic,enable))	return false;
		BluetoothGattDescriptor clientConfig = characteristic.getDescriptor(CCC);
		if (clientConfig == null)	return false;
		if (isIndi) {
			clientConfig.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
		} else {
			clientConfig.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		}
		return mBluetoothGatt.writeDescriptor(clientConfig);
	}
	
	@SuppressLint("NewApi")
	private void setDevCharateristic(BluetoothGattCharacteristic charc) {
		BluetoothGattDescriptor mRECVccc = charc.getDescriptor(CCC);
		if (mRECVccc == null) {
			Log.e("setDevCharateristic","CCC for Recv charateristic not found!");
			return;
		}
		mBluetoothGatt.readDescriptor(mRECVccc);
	}
	

    @SuppressLint("NewApi")
	public void writeCharacteristic(byte[] data){
    	BluetoothGattService mPRV = mBluetoothGatt.getService(PRV_SERVICE);
		if (mPRV == null) {
			Log.e("enableDevNotification", "PRV service not found!");
			return;
		}
        BluetoothGattCharacteristic mWRTcharac = mPRV.getCharacteristic(WRT_CHARAC);
        if (mWRTcharac == null) {
            Log.e("writeCharacteristic","WRT_CHARAC charateristic not found!");
            return;
        }
        mWRTcharac.setValue(data);
        mBluetoothGatt.writeCharacteristic(mWRTcharac);
    }
    
	@SuppressLint("NewApi")
	//데스크립터 교환 .
	public void enableDevCharateristic() {
    	BluetoothGattService mPRV = mBluetoothGatt.getService(PRV_SERVICE);
		if (mPRV == null) {
			Log.e("enableDevNotification", "PPV service not found!");
			return;
		}
		BluetoothGattCharacteristic mRECVcharac = mPRV.getCharacteristic(RECV_CHARAC);
		if (mRECVcharac == null) {
			Log.e("enableDevNotification", "mRECVcharac charateristic not found!");
			return;
		}
		mBluetoothGatt.readCharacteristic(mRECVcharac);
		BluetoothGattDescriptor mRECVccc = mRECVcharac.getDescriptor(CCC);
		if (mRECVccc == null) {
			Log.e("enableDevNotification", "CCC for RECV_CHARAC charateristic not found!");
			return;
		}
		mBluetoothGatt.readDescriptor(mRECVccc);
    }
	public void disconnect() {
		if(isTest)closeLogFile();
		mBluetoothGatt.disconnect();
		mBluetoothGatt =  null;
	}
	
	void closeLogFile(){
		logFileWriter.close();
	}
}
