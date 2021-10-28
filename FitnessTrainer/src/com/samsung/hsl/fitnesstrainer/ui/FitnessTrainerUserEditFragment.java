package com.samsung.hsl.fitnesstrainer.ui;


import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.StringTokenizer;

import com.samsung.hsl.fitnesstrainer.R;
import com.samsung.hsl.fitnesstrainer.service.FitnessFontUtils;
import com.samsung.hsl.fitnesstrainer.service.FitnessTrainerApplication;
import com.samsung.hsl.fitnesstrainer.sqlite.FitnessSQLiteManager;
import com.samsung.hsl.fitnesstrainer.sqlite.User;
import com.samsung.hsl.fitnesstrainer.ui.dialog.FitnessBirthdayDialog;
import com.samsung.hsl.fitnesstrainer.ui.dialog.FitnessHeightDialog;
import com.samsung.hsl.fitnesstrainer.ui.dialog.FitnessPhotoDialog;
import com.samsung.hsl.fitnesstrainer.ui.dialog.FitnessWeightDialog;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FitnessTrainerUserEditFragment extends Fragment implements OnClickListener {
	private static final String tag = FitnessTrainerUserEditFragment.class.getName();

	private static final int REQ_CODE_PICK_IMAGE_FROM_ALBUM = 0x01;
	private static final int REQ_CODE_PICK_IMAGE_FROM_CAMERA = 0x02;
	
	private static final int SIZE_HEIGHT_PICTURE = 240;
	private static final int SIZE_WIDTH_PICTURE = 240;
	
	User mUser;
	protected EditText mUserEmail;
	protected EditText mUserName;
	EditText mUserPassword;
	EditText mUserPasswordConfirm;
	// 확인
	TextView mConfirm;
	
	// 성별
	ImageView mUserManGender;
	ImageView mUserWomenGender;
	
	// 생년 월일
	protected TextView mBirth;
	FitnessBirthdayDialog mBirthdayDialog;
	// 키
	protected TextView mHeight;
	FitnessHeightDialog mHeightDialog;
	
	// 몸무게
	protected TextView mWeight;
	FitnessWeightDialog mWeightDialog;
	
	// 사진
	ImageView mPicture;
	byte[] mPictureData;
	FitnessPhotoDialog mPhotoDialog;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.layout_fitness_trainer_sign_up,container, false);
		
		mUser = ((FitnessTrainerApplication)getActivity().getApplication()).getFitnessService().getCurrentUser();
		initialize(view);
		loadUserInfo();
		
		FitnessFontUtils.setViewGroupFont((ViewGroup)view);
		
		return view;
	}

	void initialize(View view){
		
		((RelativeLayout)view.findViewById(R.id.fitness_top_bar_layout)).setVisibility(View.GONE);
		
		mUserEmail = (EditText) view.findViewById(R.id.fitness_sign_up_id_textview);
		mUserEmail.setEnabled(false);
		
		mUserName = (EditText) view.findViewById(R.id.fitness_sign_up_name_textview);
		
		mUserManGender = (ImageView)view.findViewById(R.id.fitness_sign_up_gender_man_btn);
		mUserWomenGender = (ImageView)view.findViewById(R.id.fitness_sign_up_gender_women_btn);
		mUserManGender.setSelected(true);
		

		// 몸무게 이벤트 설정
		mBirth = (TextView) view.findViewById(R.id.fitness_sign_up_birth_textview);
		mBirth.setOnClickListener(mBirthdayClickListener);
		mBirthdayDialog = new FitnessBirthdayDialog(getActivity(),mBirthdayDismissListener);
		
		// 키 이벤트 설정
		mHeight = (TextView) view.findViewById(R.id.fitness_sign_up_height_textview);
		mHeight.setOnClickListener(mHeightClickListener);
		mHeightDialog = new FitnessHeightDialog(getActivity(),mHeightDismissListener);
		
		// 몸무게 이벤트 설정
		mWeight = (TextView) view.findViewById(R.id.fitness_sign_up_weight_textview);
		mWeight.setOnClickListener(mWeightClickListener);
		mWeightDialog = new FitnessWeightDialog(getActivity(),mWeightDismissListener);

		mPicture = (ImageView) view.findViewById(R.id.fitness_sign_up_photo_image);
		mPicture.setOnClickListener(mPhotoClickListener);
		mPhotoDialog = new FitnessPhotoDialog(getActivity(),mPhotoDismissListener);
		
		// 확인
		mConfirm = (TextView)view.findViewById(R.id.fitness_sign_up_btn);
		mConfirm.setText(getResources().getString(R.string.string_fitness_my_info_button));
		mConfirm.setOnClickListener(this);
	}
	
	void loadUserInfo(){
		
		mUserEmail.setText(mUser.email);
		mUserName.setText(mUser.name);
		
		//생년월일
		mBirth.setText(mUser.birthday);
		
		//성별
		if(mUser.gender.equals(getResources().getString(R.string.string_fitness_sign_up_man))){
			mUserManGender.setSelected(true);
			mUserWomenGender.setSelected(false);
		}else{
			mUserManGender.setSelected(false);
			mUserWomenGender.setSelected(true);
		}
		
		// 키
		mHeight.setText(String.valueOf(mUser.height));
		
		// 몸무게
		mWeight.setText(String.valueOf(mUser.weight));
		
		// 사진
		mPictureData = mUser.picture;
		if(mPictureData!=null){
			Bitmap bitmap = BitmapFactory.decodeByteArray(mPictureData, 0, mPictureData.length);
			BitmapDrawable drawable = new BitmapDrawable(bitmap);
			mPicture.setImageDrawable(drawable);
		}
		
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

		switch (v.getId()) {
		// 확인버튼
		case R.id.fitness_sign_up_btn:
			signUp();
			break;
		case R.id.fitness_sign_up_women_textview:
		case R.id.fitness_sign_up_gender_women_btn:
			mUserWomenGender.setSelected(true);
			mUserManGender.setSelected(false);
			break;
		case R.id.fitness_sign_up_man_textview:
		case R.id.fitness_sign_up_gender_man_btn:
			mUserManGender.setSelected(true);
			mUserWomenGender.setSelected(false);
			break;
		}
	}

	private void dispatchTakePictureFromAlbumIntent(){
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(Intent.createChooser(intent, "Select Picture"),REQ_CODE_PICK_IMAGE_FROM_ALBUM);
	}
	
	private void dispatchTakePictureFromCameraIntent() {
	    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
	        startActivityForResult(takePictureIntent, REQ_CODE_PICK_IMAGE_FROM_CAMERA);
	    }
	}
	
	/** @brief 사진 검색에 대한 결과를 받을 때 사용한다. */
	public void onActivityResult(int requestCode, int resultCode,
			Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

		BitmapDrawable drawbleImage = null;
		switch (requestCode) {
		case REQ_CODE_PICK_IMAGE_FROM_ALBUM:
			if (resultCode == getActivity().RESULT_OK) {
				try {
					Uri selectedImage = imageReturnedIntent.getData();
		            InputStream imageStream = getActivity().getContentResolver().openInputStream(selectedImage);
		            drawbleImage = new BitmapDrawable(imageStream);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			break;
		case REQ_CODE_PICK_IMAGE_FROM_CAMERA:
			if (resultCode == getActivity().RESULT_OK) {
		        Bundle extras = imageReturnedIntent.getExtras();
		        Bitmap imageBitmap = (Bitmap) extras.get("data");
		        drawbleImage = new BitmapDrawable(imageBitmap);
		    }
			break;
		}
		
		if(drawbleImage!=null){
			Bitmap resizedBitmap = Bitmap.createScaledBitmap(drawbleImage.getBitmap(), SIZE_WIDTH_PICTURE, SIZE_HEIGHT_PICTURE, false);
			drawbleImage = new BitmapDrawable(resizedBitmap);
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			drawbleImage.getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, stream);
			mPictureData = stream.toByteArray();
			mPicture.setImageDrawable(drawbleImage);
		}
	}
	
			
	/** @brief 생년월일 텍스트뷰가 클릭되었을 때 발생하는 이벤트 */
	OnClickListener mBirthdayClickListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			int year,month,day;
			String str = mBirth.getText().toString();
			if(str.compareTo(getResources().getString(R.string.string_fitness_sign_up_hint_birthday))==0)
			{
				year = Calendar.getInstance().get(Calendar.YEAR);
				month = Calendar.getInstance().get(Calendar.MONTH);
				day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
				mBirthdayDialog.setDate(year, month, day);
				mBirthdayDialog.show();
			}
			else
			{
				StringTokenizer stk = new StringTokenizer(str,"-");
				try{
					year = Integer.valueOf(stk.nextToken());
					month = Integer.valueOf(stk.nextToken());
					day = Integer.valueOf(stk.nextToken());
					
				}catch (Exception e) {
					// TODO: handle exception
					year = Calendar.getInstance().get(Calendar.YEAR);
					month = Calendar.getInstance().get(Calendar.MONTH);
					day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
				}
				mBirthdayDialog.setDate(year, month, day);
				mBirthdayDialog.show();
			}
		}
	};
	
	/** @brief 생년월일  다이얼로그에서 dismiss()가 호출되었을 때 발생하는 이벤트 */
	OnDismissListener mBirthdayDismissListener = new OnDismissListener(){

		@Override
		public void onDismiss(DialogInterface dialog) {
			// TODO Auto-generated method stub
			int year = ((FitnessBirthdayDialog)dialog).getYear();
			int month = ((FitnessBirthdayDialog)dialog).getMonth();
			int day = ((FitnessBirthdayDialog)dialog).getDay();
			
			mBirth.setText(year+"-"+month+"-"+day);
		}
	};

	/** @brief 포토클릭시 이벤트 */
	OnClickListener mPhotoClickListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			mPhotoDialog.show();
		}
		
	};
	
	/** @brief 포토 다이얼로그에서 dismiss()가 호출되었을 때 발생하는 이벤트 */
	OnDismissListener mPhotoDismissListener = new OnDismissListener(){

		@Override
		public void onDismiss(DialogInterface dialog) {
			// TODO Auto-generated method stub
			boolean isp = (((FitnessPhotoDialog)dialog).isPhoto());
			if(isp){
				dispatchTakePictureFromAlbumIntent();
			}else{
				dispatchTakePictureFromCameraIntent();
			}
		}
		
	};
	
	/** @brief 키 텍스트뷰가 클릭되었을 때 발생하는 이벤트 */
	OnClickListener mHeightClickListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			float height = 0;
			try{
				height = Float.valueOf(mHeight.getText().toString());
			}
			catch(Exception e){
				height = 170;
			}
			mHeightDialog.setHeight(height);
			mHeightDialog.show();
		}
		
	};
	
	/** @brief 키 다이얼로그에서 dismiss()가 호출되었을 때 발생하는 이벤트 */
	OnDismissListener mHeightDismissListener = new OnDismissListener(){

		@Override
		public void onDismiss(DialogInterface dialog) {
			// TODO Auto-generated method stub
			float f = (((FitnessHeightDialog)dialog).getHeight());
			String str = String.format("%.1f", f);
			mHeight.setText(String.valueOf(((FitnessHeightDialog)dialog).getHeight()));
		}
		
	};

	/** @brief 몸무게  텍스트뷰가  클릭되었을 때 발생하는 이벤트 */
	OnClickListener mWeightClickListener = new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			float weight = 0;
			try{
				weight = Float.valueOf(mWeight.getText().toString());
			}
			catch(Exception e){
				weight = 50;
			}
			mWeightDialog.setWeight(weight);
			mWeightDialog.show();
		}
		
	};
	
	/** @brief 몸무게 다이얼로그에서 dismiss()가 호출되었을 때 발생하는 이벤트 */
	OnDismissListener mWeightDismissListener = new OnDismissListener(){

		@Override
		public void onDismiss(DialogInterface dialog) {
			// TODO Auto-generated method stub
			float f = (((FitnessWeightDialog)dialog).getWeight());
			String str = String.format("%.1f", f);
			mWeight.setText(str);
		}
		
	};
	
	/** @brief 데이터가 제대로 입력되어있는지 확인한다. */
	protected boolean isValidUserInfo(){
		// 기본 정보
		if(mUserEmail.getText().toString().length()==0)return false;
		if(mUserName.getText().toString().length()==0)return false;
		if(mUserPassword.getText().toString().length()==0)return false;
		if(mUserPasswordConfirm.getText().toString().length()==0)return false;
		//비밀번호 확인이 잘못되어있으면
		if(mUserPasswordConfirm.getText().toString().contentEquals(
				mUserPassword.getText().toString())==false)return false;
		
		// 생년 월일
		if(mBirth.getText().toString().compareTo(getResources().getString(R.string.string_fitness_sign_up_hint_birthday))==0) return false;
		
		// 키
		if(mHeight.getText().toString().length()==0)return false;
		
		// 몸무게
		if(mWeight.getText().toString().length()==0)return false;
		return true;
	}
	
	/** @brief 이미 존재하는 아이디인지 확인 후 안내메시지 혹은 로그인 화면으로 이동한다. */
	void signUp() {
		// TODO Auto-generated method stub
		FitnessSQLiteManager db = ((FitnessTrainerApplication) getActivity().getApplication()).getSQLiteManager();
		
		mUser.name = mUserName.getText().toString();
		mUser.birthday = mBirth.getText().toString();
		if(mUserManGender.isSelected()){
			mUser.gender = getResources().getString(R.string.string_fitness_sign_up_man);
		}else{
			mUser.gender = getResources().getString(R.string.string_fitness_sign_up_women);
		}
		try{
			mUser.weight = Float.valueOf(mWeight.getText().toString());
			mUser.height = Float.valueOf(mHeight.getText().toString());
		}catch(Exception e){
			mUser.weight = 0;
			mUser.height = 0;
		}
		mUser.picture = mPictureData;
		db.update(mUser);
		Toast.makeText(getActivity(), getResources().getString(R.string.string_fitness_trainer_user_edit_toast), Toast.LENGTH_SHORT).show();
		((FitnessTrainerApplication)getActivity().getApplication()).getFitnessService().modifyUsermFitnessUserData(mUser);
		((TextView)getActivity().findViewById(R.id.fitness_top_bar_title_textview)).setText(mUser.name+" "+getActivity().getResources().getString(R.string.string_fitness_trainer_user_name_tail));
	}
	
}
