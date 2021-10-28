package com.samsung.hsl.fitnesstrainer.ui;

import java.util.ArrayList;
import java.util.List;

import com.samsung.hsl.fitnesstrainer.R;
import com.samsung.hsl.fitnesstrainer.comm.FitnessDevice;
import com.samsung.hsl.fitnesstrainer.comm.IFitnessCommManager;
import com.samsung.hsl.fitnesstrainer.service.FitnessBroadcastService;
import com.samsung.hsl.fitnesstrainer.service.FitnessBroadcastService.FitnessBroadcastCallback;
import com.samsung.hsl.fitnesstrainer.service.FitnessTrainerApplication;
import com.samsung.hsl.fitnesstrainer.ui.dialog.ConnectDialog;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FitnessTrainerMemberConnectAddActivity extends FitnessFontActivity implements OnClickListener{
private static final String TAG = FitnessTrainerMainActivity.class.getName();
	
	ListView mListView;
	ListViewAdapter mAdapter;
	ImageButton mSearchButton;
	TextView mTopBarTitleText;
	FitnessTrainerApplication fbm;
	ArrayList<FitnessDevice> mDeviceList = new ArrayList<FitnessDevice>();
	Handler handler = new Handler();
	ConnectDialog mConnectDialog;
	ProgressDialog scanDialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.layout_fitness_trainer_member_connect_add);
				
		scanDialog = new ProgressDialog(this);
		mTopBarTitleText = (TextView)findViewById(R.id.fitness_top_bar_title_textview);
		mTopBarTitleText.setText(getResources().getString(R.string.string_fitness_trainer_register_memeber_title));
		
		mSearchButton = (ImageButton)findViewById(R.id.fitness_trainer_connection_search);
		mSearchButton.setSelected(true);
		mSearchButton.setOnClickListener(this);
		
		mListView = (ListView)findViewById(R.id.fitness_trainer_member_add_listview);
		mAdapter = new ListViewAdapter();
		mListView.setAdapter(mAdapter);
		
		FitnessBroadcastService.getInstance(this).addBroadcastObserver(FitnessBroadcastService.BROADCAST_CHANGE_DEVICE_LIST, mFitnessSacnListBroadcastCallback);
		FitnessBroadcastService.getInstance(this).addBroadcastObserver(FitnessBroadcastService.BROADCAST_START_COMM_SCAN, mFitnessStartScanBroadcastCallback);
		FitnessBroadcastService.getInstance(this).addBroadcastObserver(FitnessBroadcastService.BROADCAST_STOP_COMM_SCAN, mFitnessStopScanBroadcastCallback);
		
		mListView.setOnItemClickListener(mOnItemClickListener);
		
		fbm = (FitnessTrainerApplication)getApplication();
		if(fbm.getFitnessService()==null)
		{
			handler.postDelayed(checkComm, 1000);
			mSearchButton.setVisibility(View.INVISIBLE);
		}
		else 
		{
			checkComm.run();
		}
	}
	@Override
	protected void onDestroy() 
	{
		super.onDestroy();
		FitnessBroadcastService.getInstance(this).removeBoradcastObserver(FitnessBroadcastService.BROADCAST_CHANGE_DEVICE_LIST, mFitnessSacnListBroadcastCallback);
		FitnessBroadcastService.getInstance(this).removeBoradcastObserver(FitnessBroadcastService.BROADCAST_START_COMM_SCAN, mFitnessStartScanBroadcastCallback);
		FitnessBroadcastService.getInstance(this).removeBoradcastObserver(FitnessBroadcastService.BROADCAST_STOP_COMM_SCAN, mFitnessStopScanBroadcastCallback);
	}
	// 스캔 리스트 갱신
		FitnessBroadcastCallback mFitnessSacnListBroadcastCallback = new FitnessBroadcastCallback() {
			@Override
			public void receive() {
			
				Runnable runnable = new Runnable(){
		
					@Override
					public void run() {
						// TODO Auto-generated method stub
						mAdapter.refreshAdapter(fbm.getFitnessService().getUnConnectedDeviceList(IFitnessCommManager.Type.BLUETOOTH_LOW_ENERGY));
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
				if(mSearchButton.isSelected())
				{
					mSearchButton.setSelected(!mSearchButton.isSelected());
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
				if(!mSearchButton.isSelected())
				{
					mSearchButton.setSelected(!mSearchButton.isSelected());
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
			fbm = (FitnessTrainerApplication)getApplicationContext();
			if(!fbm.getFitnessService().isCommSupported())
			{
				Log.i(TAG, "ble is not supported");
				Toast toast = Toast.makeText(FitnessTrainerMemberConnectAddActivity.this, "Bluetooth Low Energy를 지원하지 않습니다.", 
						Toast.LENGTH_SHORT); 
				toast.show();
				
			}
			
			mConnectDialog = new ConnectDialog(FitnessTrainerMemberConnectAddActivity.this,mConnectListener);
			if(!fbm.getFitnessService().isCommEnable())
			{
				mConnectDialog.show();
			}
			
		}
		
	};
	
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
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
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
				LayoutInflater mInflater = (LayoutInflater)FitnessTrainerMemberConnectAddActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = mInflater.inflate(R.layout.item_fitness_trainer_connection, parent,false);
			}
			
			ImageView icon = (ImageView)view.findViewById(R.id.fitness_device_connect_icon);
			TextView deviceName = (TextView)view.findViewById(R.id.connection_item_text);
			
			FitnessDevice device = mDeviceList.get(position);
			
			if(device.commType==IFitnessCommManager.Type.WIFI
					|| device.commType==IFitnessCommManager.Type.WIFI_DIRECT){
				icon.setImageResource(R.drawable.fitness_popup_wifi_icon);
			}else {
				icon.setImageResource(R.drawable.fitness_device_connect_ble_icon);
			}
			String str = device.name!=null ? device.name : device.user!=null ? device.user.name : getResources().getString(R.string.string_fitness_connection_unknown);
			
			deviceName.setText(str);
			return view;
		}
		
	}
	
	OnItemClickListener mOnItemClickListener = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
				//여기서 타켓 디바이스를 다음액티비티로 넘긴다
				FitnessDevice fd =  mDeviceList.get(position);
				Intent intent = new Intent(FitnessTrainerMemberConnectAddActivity.this, FitnessTrainerMemberSelectConnectActivity.class);
				intent.putExtra("address", fd.macAddress);
				intent.putExtra("name", fd.name);
				startActivityForResult(intent,1);
		}
		
	};
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		// 수행을 제대로 한 경우
		if(requestCode == 1) // 회원추가 activity 이동했을때만.
		{
			if(resultCode == RESULT_OK) // 추가가 완료됬을때만.
			{	
				finish();
			}
		}
	}
	@Override
	public void onClick(View v) 
	{
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.fitness_trainer_connection_search:
			if(!fbm.getFitnessService().isCommEnable())
			{
				mConnectDialog.show();
			}
			else
			{
				if(mSearchButton.isSelected()){
					fbm.getFitnessService().startScan();
				}
				else{
					fbm.getFitnessService().stopScan();
				}
			}
			break;
		}
	}
}
