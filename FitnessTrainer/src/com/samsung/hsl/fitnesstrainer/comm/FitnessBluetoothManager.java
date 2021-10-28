package com.samsung.hsl.fitnesstrainer.comm; 


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.samsung.hsl.fitnesstrainer.service.FitnessBroadcastService;
import com.samsung.hsl.fitnesstrainer.service.FitnessTrainerService;
import com.samsung.hsl.fitnesstrainer.sqlite.User;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

public class FitnessBluetoothManager implements IFitnessCommManager {
	private final static String TAG = FitnessBluetoothManager.class.getSimpleName();

	private FitnessTrainerService mFitnessService;
	onDeviceListener mOnDeviceListener;
	
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothManager mBluetoothManager;
	private boolean mScanning;

	 public HashMap<String,FitnessBluetoothGattCallback> mFitnessBluetoothGattAdressMap = new HashMap<String,FitnessBluetoothGattCallback>();
	
	 // Device scan callback.
	 @SuppressLint("NewApi")
	private BluetoothAdapter.LeScanCallback mLeScanCallback =
	         new BluetoothAdapter.LeScanCallback()
	 {
		 @Override
	     public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord)
	     {
		  	 if(device.getName()==null)
				 return;
		  	 //새로 검색된 디바이스
	         addDevice(device.getAddress(),device.getName(),false,0,0,0);
	      }
	 };
	 public void addDevice(String adr,String name,boolean isCon,int sw,int hw,int battery)
	 {
		 List<FitnessDevice> list = mFitnessService.getScanedDeviceList();
         for(int i=0;i<list.size();i++)
         {
        	 if(list.get(i).macAddress.compareTo(adr)==0)
        	 {
        		 return ;
        	 }
         }
		 FitnessDevice device = new FitnessDevice(adr,null,name,isCon,sw,hw,battery);
		 device.commType=IFitnessCommManager.Type.BLUETOOTH_LOW_ENERGY;
		 mOnDeviceListener.onDeviceFound(device);
	 }
	

	@SuppressLint("NewApi")
	public FitnessBluetoothManager(FitnessTrainerService service, onDeviceListener listener)
	{
		mFitnessService = service;
		mOnDeviceListener = listener;
		
		mBluetoothManager = (BluetoothManager) mFitnessService.getSystemService(Context.BLUETOOTH_SERVICE); // 블루투스 서비스 얻기.
		mBluetoothAdapter = mBluetoothManager.getAdapter(); // 블루투스 어댑터 얻기
	}
	
	@Override
	public boolean isEnable()
	{
		if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) 
		{
			return false;
		}
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isSupported()
	{
		if (!mFitnessService.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))  // ble 지원 여부.
		{
			return false;
		}
		return true;
	}

	@SuppressLint("NewApi")
	@Override
	public void startScan() 
	{
		// Stops scanning after a pre-defined scan period.
	    Log.i(TAG, "ble scan true");
	    mScanning = true;
	    FitnessBroadcastService.sendBroadcast(FitnessBroadcastService.BROADCAST_START_COMM_SCAN);
	    mBluetoothAdapter.startLeScan(mLeScanCallback);
	}

	@SuppressLint("NewApi")
	@Override
	public void connect(FitnessDevice device) 
	{

		if (mBluetoothAdapter == null || device.macAddress == null) 
		{
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
        }
/*
        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && device.macAddress.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) 
        {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return;
            }
        }
*/
		if(mScanning)
			stopScan();
		
        final BluetoothDevice bledevice = mBluetoothAdapter.getRemoteDevice(device.macAddress);
        if (bledevice == null) 
        {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return ;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        if(mFitnessService.getCurrentUser()!=null)
        {
        	device.user = mFitnessService.getCurrentUser();
        }
        
        FitnessBluetoothGattCallback mGattCallback = new FitnessBluetoothGattCallback(mFitnessService,device);
        BluetoothGatt mBluetoothGatt = bledevice.connectGatt(mFitnessService, false, mGattCallback);
        mGattCallback.setBluetoothGatt(mBluetoothGatt);
        mFitnessBluetoothGattAdressMap.put(device.macAddress, mGattCallback);
        Log.d(TAG, "이메일 : "+device.user.email +"이름 : "+device.user.name + "mac : "+device.macAddress);
	}
	public void modifyUsermFitnessUserData(User user) 
	{
		// TODO Auto-generated method stub
		Iterator<String> iter = mFitnessBluetoothGattAdressMap.keySet().iterator();
		while(iter.hasNext()){
			FitnessBluetoothGattCallback mGattCallback = mFitnessBluetoothGattAdressMap.get(iter.next());
			if(mGattCallback.user.email.equals(user.email))
			{
				mGattCallback.setCopyUser(user);
				mGattCallback.sendFirstMessage();
			}
		}
	}

	@SuppressLint("NewApi")
	@Override
	public void disconnect(FitnessDevice device) {
		 FitnessBluetoothGattCallback mGattCallback = mFitnessBluetoothGattAdressMap.get(device.macAddress);
		 if(mGattCallback!=null)
			 mGattCallback.disconnect();
		 mFitnessBluetoothGattAdressMap.remove(device.macAddress);
	}
	@SuppressLint("NewApi")
	@Override
	public void stopScan() 
	{
        Log.i(TAG, "ble scan false");
		if(mScanning){
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
			FitnessBroadcastService.sendBroadcast(FitnessBroadcastService.BROADCAST_STOP_COMM_SCAN);
		}
		mScanning = false;
	}

	@Override
	public Type getType()
	{
        return Type.BLUETOOTH_LOW_ENERGY;
	}
	
	@Override
	public boolean isAvailable() {
		if(!isEnable())
			return false;
		if(!isSupported())
			return false;
		if(mFitnessBluetoothGattAdressMap.isEmpty())
			return false;
		return true;
	}


	@Override
	public void close() {
		// TODO Auto-generated method stub
		
	}

	
}
