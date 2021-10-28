package com.samsung.hsl.fitnessuser.ui;
import java.util.ArrayList;
import java.util.List;

import com.samsung.hsl.fitnessuser.R;
import com.samsung.hsl.fitnessuser.comm.FitnessDevice;
import com.samsung.hsl.fitnessuser.comm.IFitnessCommManager;
import com.samsung.hsl.fitnessuser.service.FitnessBroadcastService;
import com.samsung.hsl.fitnessuser.service.FitnessBroadcastService.FitnessBroadcastCallback;
import com.samsung.hsl.fitnessuser.service.FitnessUserApplication;
import com.samsung.hsl.fitnessuser.ui.dialog.ConnectDialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class FitnessConnectionActivity extends FitnessBaseActivity implements OnClickListener
{
	private static final String TAG = FitnessConnectionActivity.class.getName();
	
	private static final String TITLE_TEXT = "Connection";
	
	ListView mListView = null;
	ListViewAdapter mAdapter = null;
	ArrayList<FitnessDevice> mDeviceList = new ArrayList<FitnessDevice>();
	ImageButton mConfirm = null;
	ImageButton mSearch = null;
	FitnessUserApplication fbm;
	Handler handler = new Handler();
	ProgressDialog scanDialog;
	ProgressDialog connDialog;
	ConnectDialog mConnectDialog;
	boolean isTest;
	// 스캔 리스트 갱신
	FitnessBroadcastCallback mFitnessSacnListBroadcastCallback = new FitnessBroadcastCallback() {
		@Override
		public void receive() {
		
			Runnable runnable = new Runnable(){
	
				@Override
				public void run() {
					// TODO Auto-generated method stub
					//Wifi Direct는 계속 스캔중이기때문에 연결 도중 스캔 리스트가 변경될 수 있음
					//if(connDialog.isShowing())return;
					connDialog.dismiss();
					mAdapter.refreshAdapter(fbm.getFitnessService().getScanedDeviceList());
				}
				
			};
			
			runOnUiThread(runnable);
		}
	};

	// 연결 리스트 갱신
		FitnessBroadcastCallback mFitnessConnectListBroadcastCallback = new FitnessBroadcastCallback() {
			@Override
			public void receive() {
			
				Runnable runnable = new Runnable(){
		
					@Override
					public void run() {
						// TODO Auto-generated method stub
						connDialog.dismiss();
						mAdapter.refreshAdapter(fbm.getFitnessService().getConnectedDeviceList());
					}
					
				};
				
				runOnUiThread(runnable);
			}
		};
		
	// 스캔 시작
	FitnessBroadcastCallback mFitnessStartScanBroadcastCallback = new FitnessBroadcastCallback() {
		@Override
			public void receive() {
			// start
			if(mSearch.isSelected())
			{
				mSearch.setSelected(!mSearch.isSelected());
			}
			if(!scanDialog.isShowing())
			{
				scanDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				scanDialog.setMessage("검색중입니다....."); 
				scanDialog.setCancelable(true);
				scanDialog.show();
			}
		}
	};
	
	// 스캔 종료
	FitnessBroadcastCallback mFitnessStopScanBroadcastCallback = new FitnessBroadcastCallback() {
		@Override
			public void receive() {
			// stop
			if(!mSearch.isSelected())
			{
				mSearch.setSelected(!mSearch.isSelected());
			}
			if(scanDialog.isShowing())
			{
				scanDialog.dismiss();
			}
		}
	};
			
	
	Runnable checkComm = new Runnable(){
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			fbm = (FitnessUserApplication)getApplicationContext();
			if(!fbm.getFitnessService().isCommSupported())
			{
				Log.i(TAG, "ble is not supported");
				Toast toast = Toast.makeText(FitnessConnectionActivity.this, "Bluetooth Low Energy를 지원하지 않습니다.", 
						Toast.LENGTH_SHORT); 
				toast.show();
				
			}
			
			mConnectDialog = new ConnectDialog(FitnessConnectionActivity.this,mConnectListener);
			if(!fbm.getFitnessService().isCommEnable())
			{
				mConnectDialog.show();
			}
			
		}
		
	};

    OnClickListener mTopBarClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.fitness_top_bar_left_imagebutton:
				if(mDrawerOpened)mDrawerLayout.closeDrawer(mDrawerFragment);
				else mDrawerLayout.openDrawer(mDrawerFragment);
				break;

			case R.id.fitness_top_bar_right_imagebutton:
				if(isTest) // 테스트버젼이 맞다면
				{
					isTest = false;
					mTopBarRightButton.setImageResource(R.drawable.greenball);
				}
				else
				{
					isTest = true;
					mTopBarRightButton.setImageResource(R.drawable.redball);
				}
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState,R.layout.layout_fitness_connection);
		
		getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		mTopBarTitleText.setText(TITLE_TEXT);
		mTopBarLeftButton.setVisibility(View.INVISIBLE);
		mTopBarRightButton.setVisibility(View.VISIBLE);
		mTopBarRightButton.setOnClickListener(mTopBarClickListener);
		mTopBarRightButton.setImageResource(R.drawable.greenball);
		scanDialog = new ProgressDialog(this);
		connDialog = new ProgressDialog(this);
		isTest = false;
		mListView = (ListView)findViewById(R.id.connection_listview);
		mAdapter = new ListViewAdapter();
		mListView.setAdapter(mAdapter);
		
		
		mConfirm = (ImageButton)findViewById(R.id.connection_confirm);
		mConfirm.setOnClickListener(this);
		
		mSearch = (ImageButton)findViewById(R.id.connection_search);
		mSearch.setOnClickListener(this);
		mSearch.setSelected(true);
		FitnessBroadcastService.getInstance(this).addBroadcastObserver(FitnessBroadcastService.BROADCAST_CHANGE_DEVICE_LIST, mFitnessSacnListBroadcastCallback);
		FitnessBroadcastService.getInstance(this).addBroadcastObserver(FitnessBroadcastService.BROADCAST_CHANGE_CONNECT_DEVICE_LIST, mFitnessConnectListBroadcastCallback);
		FitnessBroadcastService.getInstance(this).addBroadcastObserver(FitnessBroadcastService.BROADCAST_START_COMM_SCAN, mFitnessStartScanBroadcastCallback);
		FitnessBroadcastService.getInstance(this).addBroadcastObserver(FitnessBroadcastService.BROADCAST_STOP_COMM_SCAN, mFitnessStopScanBroadcastCallback);
		
		// 여기서 가져와서 
		
		// 이미 접속된 있는건 들어야와함.
		// 트레이너 앱 테스트를 위한 임시 코드
		fbm = (FitnessUserApplication)getApplicationContext();
		FitnessBroadcastService.sendBroadcast(FitnessBroadcastService.BROADCAST_CHANGE_CONNECT_DEVICE_LIST);
		if(fbm.getFitnessService()==null)
		{
			handler.postDelayed(checkComm, 1000);
			mConfirm.setVisibility(View.INVISIBLE);
			mSearch.setVisibility(View.INVISIBLE);
		}
		else 
		{
			checkComm.run();
		}
	}
	
	/** @brief 접속 다이얼로그에서 dismiss()가 호출되었을 때 발생하는 이벤트 */
	OnDismissListener mConnectListener = new OnDismissListener(){

		@Override
		public void onDismiss(DialogInterface dialog) {
			// TODO Auto-generated method stub
			int result = ((ConnectDialog)dialog).getResult();
			if(result == 1){
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, 2);
			}
			if(result == 2){
			    WifiManager wManager = (WifiManager)getSystemService(Activity.WIFI_SERVICE);
				/*if(wManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED || 
						wManager.getWifiState() == WifiManager.WIFI_STATE_ENABLING )
				{
					wManager.setWifiEnabled(false);
				}
				else
				{*/
					wManager.setWifiEnabled(true);
				//}
			}
		}
	};
	@Override
	protected void onDestroy() 
	{
		super.onDestroy();
		FitnessBroadcastService.getInstance(this).removeBoradcastObserver(FitnessBroadcastService.BROADCAST_CHANGE_CONNECT_DEVICE_LIST, mFitnessConnectListBroadcastCallback);
		FitnessBroadcastService.getInstance(this).removeBoradcastObserver(FitnessBroadcastService.BROADCAST_CHANGE_DEVICE_LIST, mFitnessSacnListBroadcastCallback);
		FitnessBroadcastService.getInstance(this).removeBoradcastObserver(FitnessBroadcastService.BROADCAST_START_COMM_SCAN, mFitnessStartScanBroadcastCallback);
		FitnessBroadcastService.getInstance(this).removeBoradcastObserver(FitnessBroadcastService.BROADCAST_STOP_COMM_SCAN, mFitnessStopScanBroadcastCallback);
	}
	@Override
	public void onClick(View v) 
	{
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.connection_confirm:
			finish();
			break;
		case R.id.connection_search:
			if(!fbm.getFitnessService().isCommEnable())
			{
				mConnectDialog.show();
			}
			else
			{
				if(mSearch.isSelected()){
					fbm.getFitnessService().startScan();
				}
				else{
					fbm.getFitnessService().stopScan();
				}
			}
			break;
		}
	}
	
	class ListViewAdapter extends BaseAdapter
	{
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mDeviceList.size();
		}

		@Override
		public Object getItem(int index) {
			// TODO Auto-generated method stub
			return mDeviceList.get(index);
		}

		@Override
		public long getItemId(int index) {
			// TODO Auto-generated method stub
			return mDeviceList.get(index).macAddress.hashCode();
		}
		public void refreshAdapter(List<FitnessDevice> items) {
			  mDeviceList.clear();
			  mDeviceList.addAll(items);
			  this.notifyDataSetChanged();
		}
		@Override
		public View getView(final int position, View view, ViewGroup parent)
		{
			// TODO Auto-generated method stub
			if(view==null)
			{
				LayoutInflater mInflater = (LayoutInflater)FitnessConnectionActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = mInflater.inflate(R.layout.item_fitness_user_connection, null);
			}
			
			ImageView icon = (ImageView)view.findViewById(R.id.fitness_device_connect_icon);
			TextView deviceName = (TextView)view.findViewById(R.id.connection_item_text);
			TextView battery = (TextView)view.findViewById(R.id.connection_item_battery_text);
			TextView version = (TextView)view.findViewById(R.id.connection_item_version);
			ProgressBar prog = (ProgressBar)view.findViewById(R.id.connection_item_battery);
			LinearLayout ll = (LinearLayout)view.findViewById(R.id.connection_item_connected_list);
			final ImageButton button = (ImageButton)view.findViewById(R.id.connection_item_connect);
			
			FitnessDevice device = mDeviceList.get(position);
			
			if(device.commType==IFitnessCommManager.Type.WIFI
					|| device.commType==IFitnessCommManager.Type.WIFI_DIRECT){
				icon.setImageResource(R.drawable.fitness_popup_wifi_icon);
			}else {
				icon.setImageResource(R.drawable.fitness_device_connect_ble_icon);
			}
			
			button.setSelected(!device.isConnected);
			ll.setVisibility(View.INVISIBLE);
			
			if(device.isConnected){
				if(device.commType==IFitnessCommManager.Type.BLUETOOTH_LOW_ENERGY)ll.setVisibility(View.VISIBLE);
				battery.setText(device.battery+"%");
				prog.setProgress(device.battery);
				version.setText(device.sw+" "+device.hw);
			}
			
			String str = device.name!=null ? device.name : device.user!=null ? device.user.name : getResources().getString(R.string.string_fitness_connection_unknown);
			
			deviceName.setText(str);
			button.setOnClickListener(new OnClickListener()
			{
				@Override
				public void onClick(View v)
				{
					Runnable connectFailRunnable = new Runnable(){
							public void run() {
								if(connDialog.isShowing()){
									  button.setSelected(!button.isSelected());
									  connDialog.dismiss();
									  Toast toast = Toast.makeText(FitnessConnectionActivity.this, getString(R.string.string_fitness_connection_connection_fail), Toast.LENGTH_SHORT); 
								}
							};
						};
						
						int stringId = button.isSelected() ? R.string.string_fitness_connection_connecting : R.string.string_fitness_connection_disconnecting;
						String message = getResources().getString(stringId);
						connDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
						connDialog.setCancelable(false);
						connDialog.setMessage(message); 
						connDialog.show();
						
						//5초동안 응답이 없으면 취소
						handler.postDelayed(connectFailRunnable, 5000);
						
						//연결 요청
						if(button.isSelected())fbm.getFitnessService().connect(mDeviceList.get(position),isTest);
						else fbm.getFitnessService().disconnect(mDeviceList.get(position));
						
						//버튼 변경
						button.setSelected(!button.isSelected());
				}
			});
			
			return view;
		}
		
	}

}

