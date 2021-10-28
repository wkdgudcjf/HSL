package com.samsung.hsl.fitnesstrainer.ui;

import com.samsung.hsl.fitnesstrainer.R;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

public class OnSwipeTouchListener extends SimpleOnGestureListener {
	private static final String tag = OnSwipeTouchListener.class.getName();

	private static final int MIN_DISTANCE = 3;
	private LinearLayout frontLayout;
	private RelativeLayout backLayout;
	private Animation inFromRight, outToRight, outToLeft, inFromLeft;

	public OnSwipeTouchListener(Context ctx, View convertView) {
		frontLayout = (LinearLayout) convertView.findViewById(R.id.fitness_trainer_main_item_container);
		backLayout = (RelativeLayout) convertView.findViewById(R.id.fitness_trainer_main_item_button_container);
		inFromRight = AnimationUtils.loadAnimation(ctx, R.anim.in_from_right);
		outToRight = AnimationUtils.loadAnimation(ctx, R.anim.out_to_right);
		outToLeft = AnimationUtils.loadAnimation(ctx, R.anim.out_to_left);
		inFromLeft = AnimationUtils.loadAnimation(ctx, R.anim.in_from_left);
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		float diffX = e2.getX() - e1.getX();
		float diffY = e2.getY() - e1.getY();
		if (Math.abs(diffX) > Math.abs(diffY)) {
			if (Math.abs(diffX) > MIN_DISTANCE) {
				if (diffX < 0) {
					Log.i(tag, "Swipe Right to Left");
					if (backLayout.getVisibility() == View.GONE && frontLayout.getVisibility() == View.VISIBLE) {
						backLayout.setVisibility(View.VISIBLE);
						backLayout.startAnimation(inFromRight);
					}
				} else {
					Log.i(tag, "Swipe Left to Right");
					if (backLayout.getVisibility() != View.GONE && frontLayout.getVisibility() == View.VISIBLE) {
						backLayout.startAnimation(outToRight);
						backLayout.setVisibility(View.GONE);
					}
				}
				return true;
			}
		}
		return false;
	}

}