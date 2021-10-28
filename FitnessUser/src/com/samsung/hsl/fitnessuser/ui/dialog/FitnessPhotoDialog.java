package com.samsung.hsl.fitnessuser.ui.dialog;

import com.samsung.hsl.fitnessuser.R;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;


public class FitnessPhotoDialog extends FitnessFontDialog implements android.view.View.OnClickListener
{
	private static final String tag = FitnessPhotoDialog.class.getName();
	
	Context context;
	OnDismissListener mListener = null;
	ImageButton mAlbum;
	ImageButton mPicture;
	boolean isPhoto;
	public FitnessPhotoDialog(Context context,OnDismissListener listener)
	{
		super(context);
		this.context = context;
		mListener = listener;
		// TODO Auto-generated constructor stub
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.dialog_fitness_photo);
		getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
		mAlbum = (ImageButton)findViewById(R.id.fitness_sign_up_popup_photo_album);
		mPicture = (ImageButton)findViewById(R.id.fitness_sign_up_popup_photo_picture);
		mAlbum.setOnClickListener(this);
		mPicture.setOnClickListener(this);

	}

	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.fitness_sign_up_popup_photo_album:
			isPhoto = true;
			if(mListener!=null){
				mListener.onDismiss(this);
			}
			dismiss();
			break;

		case R.id.fitness_sign_up_popup_photo_picture:
			isPhoto = false;
			if(mListener!=null){
				mListener.onDismiss(this);
			}
			dismiss();
			break;
		}
	}
	public boolean isPhoto() {
		// TODO Auto-generated method stub
		return isPhoto;
	}
}
