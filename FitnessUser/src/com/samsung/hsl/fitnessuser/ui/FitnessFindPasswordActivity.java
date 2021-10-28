package com.samsung.hsl.fitnessuser.ui;
import com.samsung.hsl.fitnessuser.R;
import com.samsung.hsl.fitnessuser.service.FitnessMailManager;
import com.samsung.hsl.fitnessuser.service.FitnessUserApplication;
import com.samsung.hsl.fitnessuser.sqlite.User;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

public class FitnessFindPasswordActivity extends FitnessFontActivity implements OnClickListener{
private static final String TAG = FitnessFindPasswordActivity.class.getName();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.layout_fitness_find_password);
		
		sendMail();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.fitness_find_password_login_imagebutton:
			goToSignIn();
			break;

		default:
			break;
		}
	}
	
	void sendMail(){
		final User user = ((FitnessUserApplication)getApplication()).getSQLiteManager().select();
		if(user.id==0)Toast.makeText(this, getResources().getString(R.string.string_fitness_find_password_no_user), Toast.LENGTH_SHORT).show();
		else {
			final FitnessMailManager mailManager = new FitnessMailManager("kjo6152@gmail.com","dhfldksk");
			Thread network = new Thread(){
				@Override
				public void run() {
					Log.i(TAG, "Send Email : "+user.email);
					try {
						mailManager.sendMail("Fitness app. your Password", "Your password is "+user.password, "kjo6152@gmail.com", user.email);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			network.start();
		}
	}
	
	void goToSignIn(){
		Intent intent = new Intent(this,FitnessIntroLoginActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		startActivity(intent);
	}
}
