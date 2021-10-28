package com.samsung.hsl.fitnessuser.ui;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.StringTokenizer;

import com.samsung.hsl.fitnessuser.R;
import com.samsung.hsl.fitnessuser.service.FitnessUserApplication;
import com.samsung.hsl.fitnessuser.sqlite.FitnessSQLiteManager;
import com.samsung.hsl.fitnessuser.sqlite.User;
import com.samsung.hsl.fitnessuser.ui.dialog.FitnessBirthdayDialog;
import com.samsung.hsl.fitnessuser.ui.dialog.FitnessHeightDialog;
import com.samsung.hsl.fitnessuser.ui.dialog.FitnessPhotoDialog;
import com.samsung.hsl.fitnessuser.ui.dialog.FitnessWeightDialog;

import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class FitnessSignUpActivity extends FitnessBaseActivity implements OnClickListener {
	private static final String tag = FitnessSignUpActivity.class.getName();

	private static final int REQ_CODE_PICK_IMAGE_FROM_ALBUM = 0x01;
	private static final int REQ_CODE_PICK_IMAGE_FROM_CAMERA = 0x02;
	
	private static final int SIZE_HEIGHT_PICTURE = 240;
	private static final int SIZE_WIDTH_PICTURE = 240;
	
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
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState,R.layout.layout_fitness_sign_up);
		initialize();
	}

	void initialize(){
		mTopBarTitleText.setText(getResources().getString(R.string.string_fitness_sign_up_title));
		mTopBarLeftButton.setVisibility(View.INVISIBLE);
		mTopBarRightButton.setVisibility(View.INVISIBLE);

		mUserEmail = (EditText) findViewById(R.id.fitness_sign_up_id_textview);
		mUserPassword = (EditText) findViewById(R.id.fitness_sign_up_pw_textview);
		mUserPasswordConfirm = (EditText) findViewById(R.id.fitness_sign_up_pw2_textview);
		mUserName = (EditText) findViewById(R.id.fitness_sign_up_name_textview);
		
		mUserManGender = (ImageView)findViewById(R.id.fitness_sign_up_gender_man_btn);
		mUserWomenGender = (ImageView)findViewById(R.id.fitness_sign_up_gender_women_btn);
		mUserManGender.setSelected(true);
		

		// 몸무게 이벤트 설정
		mBirth = (TextView) findViewById(R.id.fitness_sign_up_birth_textview);
		mBirth.setOnClickListener(mBirthdayClickListener);
		mBirthdayDialog = new FitnessBirthdayDialog(this,mBirthdayDismissListener);
		
		// 키 이벤트 설정
		mHeight = (TextView) findViewById(R.id.fitness_sign_up_height_textview);
		mHeight.setOnClickListener(mHeightClickListener);
		mHeightDialog = new FitnessHeightDialog(this,mHeightDismissListener);
		
		// 몸무게 이벤트 설정
		mWeight = (TextView) findViewById(R.id.fitness_sign_up_weight_textview);
		mWeight.setOnClickListener(mWeightClickListener);
		mWeightDialog = new FitnessWeightDialog(this,mWeightDismissListener);

		mPicture = (ImageView) findViewById(R.id.fitness_sign_up_photo_image);
		mPicture.setOnClickListener(mPhotoClickListener);
		mPhotoDialog = new FitnessPhotoDialog(this,mPhotoDismissListener);
		
		// 확인
		mConfirm = (TextView)findViewById(R.id.fitness_sign_up_btn);
		mConfirm.setOnClickListener(this);
		
		mUserEmail.requestFocus();
		InputMethodManager imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
		imm.showSoftInput(mUserEmail, InputMethodManager.SHOW_IMPLICIT);
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
	    if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
	        startActivityForResult(takePictureIntent, REQ_CODE_PICK_IMAGE_FROM_CAMERA);
	    }
	}
	
	/** @brief 사진 검색에 대한 결과를 받을 때 사용한다. */
	protected void onActivityResult(int requestCode, int resultCode,
			Intent imageReturnedIntent) {
		super.onActivityResult(requestCode, resultCode, imageReturnedIntent);

		BitmapDrawable drawbleImage = null;
		switch (requestCode) {
		case REQ_CODE_PICK_IMAGE_FROM_ALBUM:
			if (resultCode == RESULT_OK) {
				try {
					Uri selectedImage = imageReturnedIntent.getData();
		            InputStream imageStream = getContentResolver().openInputStream(selectedImage);
		            drawbleImage = new BitmapDrawable(imageStream);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			break;
		case REQ_CODE_PICK_IMAGE_FROM_CAMERA:
			if (resultCode == RESULT_OK) {
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
	void signUp(){
		if(!isValidUserInfo()){
			Toast.makeText(this, getResources().getString(R.string.string_fitness_sign_up_not_validate), Toast.LENGTH_SHORT).show();
			return;
		}
		
		FitnessSQLiteManager db = ((FitnessUserApplication) getApplication()).getSQLiteManager();
		User user = new User();
		user.email = mUserEmail.getText().toString();
		user.password = mUserPassword.getText().toString();
		user.name = mUserName.getText().toString();
		user.birthday = mBirth.getText().toString();
		if(mUserManGender.isSelected()){
			user.gender = getResources().getString(R.string.string_fitness_sign_up_man);
		}else{
			user.gender = getResources().getString(R.string.string_fitness_sign_up_women);
		}
		try{
			user.weight = Float.valueOf(mWeight.getText().toString());
			user.height = Float.valueOf(mHeight.getText().toString());
		}catch(Exception e){
			user.weight = 0;
			user.height = 0;
		}
		user.picture = mPictureData;
		db.insert(user);
		// 정상적으로 등록됐으면
		if (user.id > 0) {
			finish();
		} else {
			Toast.makeText(this, getResources().getString(R.string.string_fitness_sign_up_exist_email), Toast.LENGTH_SHORT).show();
		}
	}
}
