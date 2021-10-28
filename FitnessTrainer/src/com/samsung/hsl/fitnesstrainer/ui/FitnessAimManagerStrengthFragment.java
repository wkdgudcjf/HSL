package com.samsung.hsl.fitnesstrainer.ui;

import com.samsung.hsl.fitnesstrainer.R;
import com.samsung.hsl.fitnesstrainer.service.FitnessFontUtils;
import com.samsung.hsl.fitnesstrainer.service.FitnessPreference;
import com.samsung.hsl.fitnesstrainer.service.FitnessTrainerApplication;
import com.samsung.hsl.fitnesstrainer.service.FitnessUtils;
import com.samsung.hsl.fitnesstrainer.sqlite.User;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class FitnessAimManagerStrengthFragment extends Fragment implements OnClickListener {
	private static final String tag = FitnessTrainerUserInfoActivity.class.getName();

	ImageView mHighStrengthButton;
	ImageView mCommonStrengthButton;
	ImageView mFatBunningStrengthButton;
	ImageView mWarmUpStrengthButton;
	Button mConfirmButton;
	
	TextView mHighKarvonenText;
	TextView mCommonKarvonenText;
	TextView mFatBunningKarvonenText;
	TextView mWarmUpKarvonenText;
	TextView mStableHeartrateText;
	
	FitnessPreference mFitnessPreference;
	FitnessTrainerApplication mFitnessApplication;
	
	public FitnessAimManagerStrengthFragment() {
		// Empty constructor required for fragment subclasses

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.layout_fitness_aim_manager_strength, container,
				false);

		mFitnessApplication = (FitnessTrainerApplication)getActivity().getApplication();
		
		mHighStrengthButton = (ImageView) view.findViewById(R.id.fitness_aim_manager_level_setting_level4_bar);
		mCommonStrengthButton = (ImageView) view.findViewById(R.id.fitness_aim_manager_level_setting_level3_bar);
		mFatBunningStrengthButton = (ImageView) view.findViewById(R.id.fitness_aim_manager_level_setting_level2_bar);
		mWarmUpStrengthButton = (ImageView) view.findViewById(R.id.fitness_aim_manager_level_setting_level1_bar);
		mConfirmButton = (Button)view.findViewById(R.id.fitness_aim_manager_confirm_btn);
		
		mHighStrengthButton.setOnClickListener(this);
		mCommonStrengthButton.setOnClickListener(this);
		mFatBunningStrengthButton.setOnClickListener(this);
		mWarmUpStrengthButton.setOnClickListener(this);
		mConfirmButton.setOnClickListener(this);
		
		mFitnessPreference = new FitnessPreference(getActivity());
		loadKarvonen(view);
		loadStrength();
		
		FitnessFontUtils.setViewGroupFont((ViewGroup)view);
		
		return view;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId())
		{
		case R.id.fitness_aim_manager_level_setting_level4_bar:
		case R.id.fitness_aim_manager_level_setting_level3_bar:
		case R.id.fitness_aim_manager_level_setting_level2_bar:
		case R.id.fitness_aim_manager_level_setting_level1_bar:
			setUIState(v.getId());
			break;
		case R.id.fitness_aim_manager_confirm_btn:
			saveStrength();
			getActivity().finish();
			break;
		}
	}

	void setUIState(int id) {
		mHighStrengthButton.setSelected(false);
		mCommonStrengthButton.setSelected(false);
		mFatBunningStrengthButton.setSelected(false);
		mWarmUpStrengthButton.setSelected(false);
		
		mHighKarvonenText.setTextColor(getResources().getColor(R.color.aim_manager_strength_range));
		mCommonKarvonenText.setTextColor(getResources().getColor(R.color.aim_manager_strength_range));
		mFatBunningKarvonenText.setTextColor(getResources().getColor(R.color.aim_manager_strength_range));
		mWarmUpKarvonenText.setTextColor(getResources().getColor(R.color.aim_manager_strength_range));
		
		if(id==R.id.fitness_aim_manager_level_setting_level4_bar){
			mHighStrengthButton.setSelected(true);
			mHighKarvonenText.setTextColor(getResources().getColor(R.color.aim_manager_strength_range_selected));
		}
		else if(id==R.id.fitness_aim_manager_level_setting_level3_bar){
			mCommonStrengthButton.setSelected(true);
			mCommonKarvonenText.setTextColor(getResources().getColor(R.color.aim_manager_strength_range_selected));
		}
		else if(id==R.id.fitness_aim_manager_level_setting_level2_bar){
			mFatBunningStrengthButton.setSelected(true);
			mFatBunningKarvonenText.setTextColor(getResources().getColor(R.color.aim_manager_strength_range_selected));
		}
		else if(id==R.id.fitness_aim_manager_level_setting_level1_bar){
			mWarmUpStrengthButton.setSelected(true);
			mWarmUpKarvonenText.setTextColor(getResources().getColor(R.color.aim_manager_strength_range_selected));
		}
	}

	void saveStrength() {
		int strength = FitnessUtils.STRENGTH_WARM_UP;
		if(mHighStrengthButton.isSelected())strength = FitnessUtils.STRENGTH_HIGH;
		else if(mCommonStrengthButton.isSelected())strength = FitnessUtils.STRENGTH_COMMON;
		else if(mFatBunningStrengthButton.isSelected())strength = FitnessUtils.STRENGTH_FAT_BUNNING;
		else if(mWarmUpStrengthButton.isSelected())strength = FitnessUtils.STRENGTH_WARM_UP;
		
		mFitnessPreference.putStrength(mFitnessApplication.getFitnessService().getCurrentUser(), strength);
	}
	
	void loadStrength(){
		int strength = mFitnessPreference.getStrength(mFitnessApplication.getFitnessService().getCurrentUser(), FitnessUtils.STRENGTH_WARM_UP);
		int id = R.id.fitness_aim_manager_level_setting_level1_bar;
		if(strength==FitnessUtils.STRENGTH_HIGH)id = R.id.fitness_aim_manager_level_setting_level4_bar;
		else if(strength==FitnessUtils.STRENGTH_COMMON)id = R.id.fitness_aim_manager_level_setting_level3_bar;
		else if(strength==FitnessUtils.STRENGTH_FAT_BUNNING)id = R.id.fitness_aim_manager_level_setting_level2_bar;
		else if(strength==FitnessUtils.STRENGTH_WARM_UP)id = R.id.fitness_aim_manager_level_setting_level1_bar;
		
		setUIState(id);
	}
	
	void loadKarvonen(View view){
		mHighKarvonenText = (TextView)view.findViewById(R.id.fitness_aim_manager_level4_range_textview);
		mCommonKarvonenText = (TextView)view.findViewById(R.id.fitness_aim_manager_level3_range_textview);
		mFatBunningKarvonenText = (TextView)view.findViewById(R.id.fitness_aim_manager_level2_range_textview);
		mWarmUpKarvonenText = (TextView)view.findViewById(R.id.fitness_aim_manager_level1_range_textview);
		mStableHeartrateText = (TextView)view.findViewById(R.id.fitness_aim_manager_stable_heartrate_textview);
		
		FitnessFontUtils.setFont(getActivity(), mHighKarvonenText);
        FitnessFontUtils.setFont(getActivity(), mCommonKarvonenText);
        FitnessFontUtils.setFont(getActivity(), mFatBunningKarvonenText);
        FitnessFontUtils.setFont(getActivity(), mWarmUpKarvonenText);
        FitnessFontUtils.setFont(getActivity(), mStableHeartrateText);
        
		User user = mFitnessApplication.getFitnessService().getFitnessManager().getUser();
		int high = FitnessUtils.karvonen(user, FitnessUtils.STRENGTH_HIGH);
		int common = FitnessUtils.karvonen(user, FitnessUtils.STRENGTH_COMMON);
		int fatBunning = FitnessUtils.karvonen(user, FitnessUtils.STRENGTH_FAT_BUNNING);
		int warmUp = FitnessUtils.karvonen(user, FitnessUtils.STRENGTH_WARM_UP);
		
		mHighKarvonenText.setText("85-100%\r\n("+common+" - "+high+")");
		mCommonKarvonenText.setText("65-85%\r\n("+fatBunning+" - "+common+")");
		mFatBunningKarvonenText.setText("50-65%\r\n("+warmUp+" - "+fatBunning+")");
		mWarmUpKarvonenText.setText("0-50%\r\n("+0+" - "+warmUp+")");
		mStableHeartrateText.setText(getResources().getString(R.string.string_fitness_aim_manager_current_heartrate)+user.stableHeartrate);
	}
}