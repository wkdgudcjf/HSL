package com.samsung.hsl.fitnesstrainer.service;

import com.samsung.hsl.fitnesstrainer.sqlite.User;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class FitnessPreference {

	private final String PREFERENCE_FILE = "com.samsung.hsl.fitness.pref";

	public final static String PREFERENCE_SAVE_PASSWORD = "isSavePassword";
	public final static String PREFERENCE_PASSWORD = "password";
	public final static String PREFERENCE_STRENGTH = "strength_";

	Context context;

	public FitnessPreference(Context context) {
		this.context = context;
	}

	public void put(String key, String value) {
		SharedPreferences pref = context.getSharedPreferences(PREFERENCE_FILE,
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();

		editor.putString(key, value);
		editor.commit();
	}

	public void put(String key, boolean value) {
		SharedPreferences pref = context.getSharedPreferences(PREFERENCE_FILE,
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();

		editor.putBoolean(key, value);
		editor.commit();
	}

	public void putStrength(User user, int strength) {
		SharedPreferences pref = context.getSharedPreferences(PREFERENCE_FILE,
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = pref.edit();
		editor.putInt(PREFERENCE_STRENGTH+user.email, strength);
		editor.commit();
		
		FitnessBroadcastService.sendBroadcast(FitnessBroadcastService.BROADCAST_CHANGE_AIM_STRENGTH, user, strength);
	}

	public String getValue(String key, String value) {
		SharedPreferences pref = context.getSharedPreferences(PREFERENCE_FILE,
				Activity.MODE_PRIVATE);

		try {
			return pref.getString(key, value);
		} catch (Exception e) {
			return value;
		}

	}

	public int getStrength(User user, int strength) {
		SharedPreferences pref = context.getSharedPreferences(PREFERENCE_FILE,
				Activity.MODE_PRIVATE);

		try {
			return pref.getInt(PREFERENCE_STRENGTH+user.email, strength);
		} catch (Exception e) {
			return strength;
		}

	}

	public boolean getValue(String key, boolean value) {
		SharedPreferences pref = context.getSharedPreferences(PREFERENCE_FILE,
				Activity.MODE_PRIVATE);

		try {
			return pref.getBoolean(key, value);
		} catch (Exception e) {
			return value;
		}
	}
}