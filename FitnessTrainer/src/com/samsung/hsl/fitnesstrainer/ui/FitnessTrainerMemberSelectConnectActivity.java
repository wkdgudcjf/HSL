package com.samsung.hsl.fitnesstrainer.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
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

import com.samsung.hsl.fitnesstrainer.R;
import com.samsung.hsl.fitnesstrainer.comm.FitnessDevice;
import com.samsung.hsl.fitnesstrainer.comm.IFitnessCommManager;
import com.samsung.hsl.fitnesstrainer.service.FitnessBroadcastService;
import com.samsung.hsl.fitnesstrainer.service.FitnessBroadcastService.FitnessBroadcastCallback;
import com.samsung.hsl.fitnesstrainer.service.FitnessTrainerApplication;
import com.samsung.hsl.fitnesstrainer.sqlite.User;

public class FitnessTrainerMemberSelectConnectActivity extends FitnessFontActivity{
private static final String tag = FitnessTrainerMainActivity.class.getName();
	
	ListView mListView;
	ListViewAdapter mAdapter;
	ImageButton mRegisterButton;
	String address,name;
	ArrayList<User> mAdapterUserList = new ArrayList<User>();
	ProgressDialog connDialog;
	Handler handler = new Handler();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		address = intent.getExtras().getString("address");
		name = intent.getExtras().getString("name");
		setContentView(R.layout.layout_fitness_trainer_user_list_add_connection);
		
		TextView mTopBarTitleText = (TextView)findViewById(R.id.fitness_top_bar_title_textview); 
		mTopBarTitleText.setText(getResources().getString(R.string.string_fitness_slide_menu_connection));
		TextView devicename = (TextView)findViewById(R.id.connection_item_text); 
		devicename.setText(name);
		
		mRegisterButton = (ImageButton)findViewById(R.id.fitness_trainer_user_register_button);
		mRegisterButton.setOnClickListener(mOnClickListener); 
		connDialog = new ProgressDialog(this);
		mListView = (ListView)findViewById(R.id.fitness_trainer_user_listview);
		mAdapter = new ListViewAdapter();
		mListView.setAdapter(mAdapter);
		
		mListView.setOnItemClickListener(mOnItemClickListener);
		FitnessBroadcastService.getInstance(this).addBroadcastObserver(FitnessBroadcastService.BROADCAST_CONNECT_SUCCESS, mFitnessConnectSuccessBroadcastCallback);
	}
	FitnessBroadcastCallback mFitnessConnectSuccessBroadcastCallback = new FitnessBroadcastCallback() {
		@Override
		public void receive() {
		
			Runnable runnable = new Runnable(){
	
				@Override
				public void run() {
					// TODO Auto-generated method stub
					connDialog.dismiss();
					setResult(RESULT_OK);
					finish();
				}
				
			};
	
			runOnUiThread(runnable);
		}
	};
	@Override
	protected void onDestroy() 
	{
		super.onDestroy();
		FitnessBroadcastService.getInstance(this).removeBoradcastObserver(FitnessBroadcastService.BROADCAST_CONNECT_SUCCESS, mFitnessConnectSuccessBroadcastCallback);
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		mAdapterUserList.clear();
		List<User> mUserList = ((FitnessTrainerApplication)getApplication()).getSQLiteManager().selectAllUsers();
		List<FitnessDevice> mConnectedDeviceList = ((FitnessTrainerApplication)getApplication()).getFitnessService().getConnectedDeviceList();
		for(int i=0;i<mUserList.size();i++){
			User user = mUserList.get(i);
			boolean isConnected = false;
			for(int j=0;j<mConnectedDeviceList.size();j++){
				if(user.equals(mConnectedDeviceList.get(j).user)){
					isConnected = true;
				}
			}
			if(isConnected==false)mAdapterUserList.add(user);
		}
		mAdapter.notifyDataSetChanged();
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
			return mAdapterUserList.size();
		}

		@Override
		public Object getItem(int index) {
			// TODO Auto-generated method stub
			return mAdapterUserList.get(index);
		}

		@Override
		public long getItemId(int index) {
			// TODO Auto-generated method stub
			return mAdapterUserList.get(index).id;
		}

		@Override
		public View getView(final int position, View view, ViewGroup parent)
		{
			// TODO Auto-generated method stub
			if(view==null)
			{
				LayoutInflater mInflater = (LayoutInflater)FitnessTrainerMemberSelectConnectActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				view = mInflater.inflate(R.layout.item_fitness_trainer_user_list, parent,false);
			}
			
			//레아이웃 값 설정
			ImageView picture = (ImageView)view.findViewById(R.id.fitness_trainer_user_list_item_picture);
			TextView name = (TextView)view.findViewById(R.id.fitness_trainer_user_list_item_name);
			TextView email = (TextView)view.findViewById(R.id.fitness_trainer_user_list_item_email);
			
			User user = mAdapterUserList.get(position);
			
			// 사진
			if(user.picture!=null){
				Bitmap bitmap = BitmapFactory.decodeByteArray(user.picture, 0, user.picture.length);
				BitmapDrawable drawable = new BitmapDrawable(bitmap);
				picture.setImageDrawable(drawable);
			}else{
				picture.setImageResource(R.drawable.fitness_register_memeber_automatic_photo);
			}
			
			name.setText(user.name);
			email.setText(user.email);
			
			return view;
		}
		
	}
	
	OnItemClickListener mOnItemClickListener = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> parent, View view, final int position,
				long id) {
			Runnable connectFailRunnable = new Runnable(){
				public void run() {
					if(connDialog.isShowing()){
						  connDialog.dismiss();
						  Toast toast = Toast.makeText(FitnessTrainerMemberSelectConnectActivity.this, getString(R.string.string_fitness_connection_connection_fail), Toast.LENGTH_SHORT); 
						  toast.show();
					}
				};
			};
			// TODO Auto-generated method stub
			connDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			connDialog.setCancelable(false);
			connDialog.setMessage(getResources().getString(R.string.string_fitness_connection_connecting)); 
			connDialog.show();
			User user = mAdapterUserList.get(position);
			((FitnessTrainerApplication)getApplication()).getFitnessService().setCurrentUser(user);
			FitnessDevice fd = new FitnessDevice(address,null,name,false,0,0,0);
			fd.commType = IFitnessCommManager.Type.BLUETOOTH_LOW_ENERGY;
			((FitnessTrainerApplication)getApplication()).getFitnessService().connect(fd);
			//5초동안 응답이 없으면 취소
			handler.postDelayed(connectFailRunnable, 5000);
		}
	};
	
	OnClickListener mOnClickListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.fitness_trainer_user_register_button:
				Intent intent = new Intent(FitnessTrainerMemberSelectConnectActivity.this,FitnessTrainerSignUpActivity.class);
				startActivity(intent);
				break;

			default:
				break;
			}
		}
		
	};
}
