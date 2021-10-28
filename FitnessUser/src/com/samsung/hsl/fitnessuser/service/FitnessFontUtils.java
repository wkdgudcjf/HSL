package com.samsung.hsl.fitnessuser.service;

import java.util.Locale;

import com.samsung.hsl.fitnessuser.R;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class FitnessFontUtils {
	private static final String tag = FitnessFontUtils.class.getName();
	public static final String NotoSans_Bold = "font/NotoSans-Bold.otf";
	public static final String NotoSans_Light = "font/NotoSans-Light.otf";
	public static final String NotoSans_Medium = "font/NotoSans-Medium.otf";
	public static final String NotoSans_Regular = "font/NotoSans-Regular.otf";
	public static final String Roboto_Bold = "font/Roboto-Bold.ttf";
	public static final String Roboto_Light = "font/Roboto-Light.ttf";
	public static final String Roboto_Medium = "font/Roboto-Medium.ttf";
	public static final String Roboto_Regular = "font/Roboto-Regular.ttf";

	static Typeface Typeface_NotoSans_Bold = null;
	static Typeface Typeface_NotoSans_Light = null;
	static Typeface Typeface_NotoSans_Medium = null;
	static Typeface Typeface_NotoSans_Regular = null;
	static Typeface Typeface_Roboto_Bold = null;
	static Typeface Typeface_Roboto_Light = null;
	static Typeface Typeface_Roboto_Medium = null;
	static Typeface Typeface_Roboto_Regular = null;

	public static Typeface getFont(Context context, String font) {
		if (Typeface_NotoSans_Bold == null)
			Typeface_NotoSans_Bold = Typeface.createFromAsset(context.getAssets(), NotoSans_Bold);
		if (Typeface_NotoSans_Light == null)
			Typeface_NotoSans_Light = Typeface.createFromAsset(context.getAssets(), NotoSans_Light);
		if (Typeface_NotoSans_Medium == null)
			Typeface_NotoSans_Medium = Typeface.createFromAsset(context.getAssets(), NotoSans_Medium);
		if (Typeface_NotoSans_Regular == null)
			Typeface_NotoSans_Regular = Typeface.createFromAsset(context.getAssets(), NotoSans_Regular);
		if (Typeface_Roboto_Bold == null)
			Typeface_Roboto_Bold = Typeface.createFromAsset(context.getAssets(), Roboto_Bold);
		if (Typeface_Roboto_Light == null)
			Typeface_Roboto_Light = Typeface.createFromAsset(context.getAssets(), Roboto_Light);
		if (Typeface_Roboto_Medium == null)
			Typeface_Roboto_Medium = Typeface.createFromAsset(context.getAssets(), Roboto_Medium);
		if (Typeface_Roboto_Regular == null)
			Typeface_Roboto_Regular = Typeface.createFromAsset(context.getAssets(), Roboto_Regular);

		if (font.equals(NotoSans_Bold))
			return Typeface_NotoSans_Bold;
		if (font.equals(NotoSans_Light))
			return Typeface_NotoSans_Light;
		if (font.equals(NotoSans_Medium))
			return Typeface_NotoSans_Medium;
		if (font.equals(NotoSans_Regular))
			return Typeface_NotoSans_Regular;
		if (font.equals(Roboto_Bold))
			return Typeface_Roboto_Bold;
		if (font.equals(Roboto_Light))
			return Typeface_Roboto_Light;
		if (font.equals(Roboto_Medium))
			return Typeface_Roboto_Medium;
		if (font.equals(Roboto_Regular))
			return Typeface_Roboto_Regular;
		return Typeface_Roboto_Regular;
	}

	public static void setFont(Context context, TextView view) {
		if (context == null || view == null)
			return;

		String language_ko = context.getResources().getString(R.string.language_ko);
		
		String font = NotoSans_Medium;
		switch (view.getId()) {
		//Top Bar
		case R.id.fitness_top_bar_title_textview:
			if(view.getText().toString().equals(context.getResources().getString(R.string.string_fitness_find_password_title)))font = NotoSans_Bold;
			else if(view.getText().toString().equals(context.getResources().getString(R.string.string_fitness_sign_up_title)))font = NotoSans_Bold;
			else if(view.getText().toString().equals(context.getResources().getString(R.string.string_fitness_main_title)))font = Roboto_Bold;
			break;
		//로그인
		case R.id.fitness_intro_login_sign_in_imagebutton:
		case R.id.fitness_intro_login_sign_up_imagebutton:
		case R.id.fitness_intro_login_password_box_edittext:
			if(Locale.getDefault().getDisplayLanguage().equals(language_ko))font = NotoSans_Bold;
			else font = Roboto_Bold;
			break;
		//회원가입
		case R.id.fitness_sign_up_btn:
		case R.id.fitness_sign_up_id_textview:
		case R.id.fitness_sign_up_pw_textview:
		case R.id.fitness_sign_up_pw2_textview:
		case R.id.fitness_sign_up_name_textview:
		case R.id.fitness_sign_up_birth_textview:
		case R.id.fitness_sign_up_man_textview:
		case R.id.fitness_sign_up_women_textview:
		case R.id.fitness_sign_up_height_textview:
		case R.id.fitness_sign_up_weight_textview:
			if(Locale.getDefault().getDisplayLanguage().equals(language_ko))font = NotoSans_Bold;
			else font = Roboto_Bold;
			break;
		// Dialog
		case R.id.fitness_sign_up_popup_birth_1_text:
		case R.id.fitness_sign_up_popup_birth_2_text:
		case R.id.fitness_sign_up_popup_birth_3_text:
		case R.id.fitness_sign_up_popup_height_1_text:
		case R.id.fitness_sign_up_popup_height_2_text:
		case R.id.fitness_sign_up_popup_weight_1_text:
		case R.id.fitness_sign_up_popup_weight_2_text:
			font = Roboto_Medium;
			break;
			
		//메인
		case R.id.fitness_main_sub_top_bar_left_textview:
		case R.id.fitness_main_sub_top_bar_right_textview:
			if(Locale.getDefault().getDisplayLanguage().equals(language_ko))font = NotoSans_Bold;
			else font = Roboto_Bold;
			break;
		case R.id.fitness_main_graph_message_textview:
		case R.id.fitness_main_graph_time_textview:
			if(Locale.getDefault().getDisplayLanguage().equals(language_ko))font = NotoSans_Bold;
			else font = Roboto_Bold;
			break;
		case R.id.fitness_main_range_heartrate_value_textview:
		case R.id.fitness_main_range_calorie_value_textview:
		case R.id.fitness_main_range_temperature_value_textview:
			font = NotoSans_Regular;
			break;
		case R.id.fitness_main_range_aim_value_textview:
			if(Locale.getDefault().getDisplayLanguage().equals(language_ko))font = NotoSans_Medium;
			else font = Roboto_Medium;
			break;
		//목표 관리
		case R.id.fitness_aim_manager_donutprogress_value_textview:
			font = Roboto_Bold;
			break;
		case R.id.fitness_aim_manager_message_textview:
		case R.id.fitness_aim_manager_confirm_btn:
		case R.id.fitness_aim_manager_stable_heartrate_button:
			if(Locale.getDefault().getDisplayLanguage().equals(language_ko))font = NotoSans_Bold;
			else font = Roboto_Bold;
		//슬라이드 메뉴
		case R.id.fitness_slide_menu_bar_welcom_textview:
		case R.id.fitness_slide_menu_log_out_textview:
		case R.id.fitness_slide_menu_connection_textview:
		case R.id.fitness_slide_menu_aim_manager_textview:
		case R.id.fitness_slide_menu_calendar_textview:
		case R.id.fitness_slide_menu_my_info_textview:
		case R.id.fitness_slide_menu_test_textview:
			if(Locale.getDefault().getDisplayLanguage().equals(language_ko))font = NotoSans_Bold;
			else font = Roboto_Bold;
			break;
		//목표 관리
		case R.id.fitness_aim_manager_level1_range_textview:
		case R.id.fitness_aim_manager_level2_range_textview:
		case R.id.fitness_aim_manager_level3_range_textview:
		case R.id.fitness_aim_manager_level4_range_textview:
			font = Roboto_Bold;
			break;
		default:
			break;
		}

		view.setTypeface(getFont(context, font));
	}

	/**
	 * 해당 그룹의 폰트를 설정한다.
	 * @param group 모든 뷰 그룹
	 */
	public static void setViewGroupFont(ViewGroup group) {
		for (int i = 0; i < group.getChildCount(); i++) {
			View child = group.getChildAt(i);
			if (child instanceof TextView)
				setFont(group.getContext(), (TextView) child);
			else if (child instanceof ViewGroup)
				setViewGroupFont((ViewGroup) child);
		}
	}

}
