package com.samsung.hsl.fitnesstrainer.ui;

import java.util.ArrayList;

import com.samsung.hsl.fitnesstrainer.R;
import com.samsung.hsl.fitnesstrainer.service.FitnessFontUtils;
import com.samsung.hsl.fitnesstrainer.service.FitnessTrainerApplication;
import com.samsung.hsl.fitnesstrainer.sqlite.User;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
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

public class FitnessTrainerUserListActivity extends FitnessFontActivity{
private static final String tag = FitnessTrainerMainActivity.class.getName();
	
	ListView mListView;
	ListViewAdapter mAdapter;
	ImageButton mRegisterButton;
	
	ArrayList<User> mAdapterUserList = new ArrayList<User>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.layout_fitness_trainer_user_list);
		
		TextView mTopBarTitleText = (TextView)findViewById(R.id.fitness_top_bar_title_textview); 
		mTopBarTitleText.setText(getResources().getString(R.string.string_fitness_trainer_user_list_title));
		
		mRegisterButton = (ImageButton)findViewById(R.id.fitness_trainer_user_register_button);
		mRegisterButton.setOnClickListener(mOnClickListener); 
		
		mListView = (ListView)findViewById(R.id.fitness_trainer_user_listview);
		mAdapter = new ListViewAdapter();
		mListView.setAdapter(mAdapter);
		
		mListView.setOnItemClickListener(mOnItemClickListener);
		
	}
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		mAdapterUserList = ((FitnessTrainerApplication)getApplication()).getSQLiteManager().selectAllUsers();
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
				LayoutInflater mInflater = (LayoutInflater)FitnessTrainerUserListActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
			}else {
				picture.setImageResource(R.drawable.fitness_register_memeber_automatic_photo);
			}
			
			name.setText(user.name+" "+getResources().getString(R.string.string_fitness_trainer_user_name_tail));
			email.setText(user.email);
			
			FitnessFontUtils.setViewGroupFont((ViewGroup)view);
			
			return view;
		}
		
	}
	
	OnItemClickListener mOnItemClickListener = new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			User user = mAdapterUserList.get(position);
			((FitnessTrainerApplication)getApplication()).getFitnessService().setCurrentUser(user);
			Intent intent = new Intent(FitnessTrainerUserListActivity.this,FitnessTrainerUserInfoActivity.class);
			startActivity(intent);
		}
		
	};
	
	OnClickListener mOnClickListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.fitness_trainer_user_register_button:
				Intent intent = new Intent(FitnessTrainerUserListActivity.this,FitnessTrainerSignUpActivity.class);
				startActivity(intent);
				break;

			default:
				break;
			}
		}
		
	};
}
