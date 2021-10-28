package com.samsung.hsl.fitnesstrainer.service;

import com.samsung.hsl.fitnesstrainer.sqlite.User;


public class FitnessTrainerNotificationManager {
	FitnessTrainerService mFitnessService;
	
	public FitnessTrainerNotificationManager(FitnessTrainerService service){
		mFitnessService = service;
		createView();
	}
	
	public FitnessTrainerService getFitnessService(){return mFitnessService;}

	public void createView() {
		// TODO Auto-generated method stub
		
	}

	public void close() {
		// TODO Auto-generated method stub
		
	}

	public void exceedHeartrate(User user, int heartrate) {
		// TODO Auto-generated method stub
		
	}
}
