/**
 * Copyright (C) 2009 - 2013 SC 4ViewSoft SRL
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.samsung.hsl.fitnessuser.ui;
import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer.FillOutsideLine;

import com.samsung.hsl.fitnessuser.service.FitnessUtils;

import android.content.Context;

public class FitnessLineGraph {
	private static final String tag = FitnessLineGraph.class.getName();
	private static final int DATASET_COUNT = 5;
	public static final int COUNT_OF_X_AXIS = 150;
	
	private XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();
	private XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
	private XYSeries mExceedSeries = new XYSeries("");
	private XYSeries mHighSeries = new XYSeries("");
	private XYSeries mCommonSeries = new XYSeries("");
	private XYSeries mFatBunningSeries = new XYSeries("");
	private XYSeries mWarmUpSeries = new XYSeries("");

	GraphicalView view;
	
	int warmUp = 50,fatBunning = 100, common = 150, high = 220;
	
	public FitnessLineGraph() {
		renderer.setShowAxes(false);
		renderer.setShowGrid(false);
		renderer.setShowGridX(false);
		renderer.setShowGridY(false);
		renderer.setShowLabels(false);
		renderer.setShowLegend(false);
		renderer.setPanEnabled(true, false);
		renderer.setZoomEnabled(false, false);
		renderer.setXAxisMin(0);
	    renderer.setXAxisMax(COUNT_OF_X_AXIS);
	    renderer.setYAxisMin(-1);
	    renderer.setYAxisMax(100);
	    renderer.setMargins(new int[]{0,0,0,0});
		
		for (int i = 0; i < DATASET_COUNT; i++) {
			XYSeriesRenderer xyRenderer = new XYSeriesRenderer();
			xyRenderer.setColor(0x00000000);
			xyRenderer.setPointStyle(PointStyle.POINT);
			FillOutsideLine fill = new FillOutsideLine(FillOutsideLine.Type.BOUNDS_ABOVE);
	        fill.setColor(getStrengthColor(i));
	        xyRenderer.addFillOutsideLine(fill);
			renderer.addSeriesRenderer(xyRenderer);
		}

		dataset.addSeries(mWarmUpSeries);
		dataset.addSeries(mFatBunningSeries);
		dataset.addSeries(mCommonSeries);
		dataset.addSeries(mHighSeries);
		dataset.addSeries(mExceedSeries);
		
		mWarmUpSeries.add(-1, 0.01);
		mFatBunningSeries.add(-1, 0.01);
		mCommonSeries.add(-1, 0.01);
		mHighSeries.add(-1, 0.01);
		mExceedSeries.add(-1, 0.01);
	}
	
	public GraphicalView getView(Context context) {
//		view = ChartFactory.getCubeLineChartView(context, dataset, renderer,0.5f);
		view = ChartFactory.getLineChartView(context, dataset, renderer);
		return view;
	}

	/**
	 * @brief 해당 위치로 그래프를 스크롤한다.
	 * @param endX 그래프의 끝 위치
	 */
	public void scrollTo(int endX){
		renderer.setXAxisMin(endX-COUNT_OF_X_AXIS);
		renderer.setXAxisMax(endX);
	}
	
	public int getXAxisMax(){
		return (int)renderer.getXAxisMax();
	}
	
	public int getXAxisMin(){
		return (int)renderer.getXAxisMin();
	}
	

	public void setAimStrength(int strength){
		for (int i = 0; i < DATASET_COUNT; i++) {
			XYSeriesRenderer xyRenderer = (XYSeriesRenderer) renderer.getSeriesRendererAt(i);
			if(i<strength){
//				xyRenderer.setColor(mColorShort & 0x00ffffff | 0x80000000);
				xyRenderer.getFillOutsideLine()[0].setColor(mColorShort & 0x00ffffff | 0x80000000);
			}else if(i>strength){
//				xyRenderer.setColor(mColorHigh & 0x00ffffff | 0x80000000);
				xyRenderer.getFillOutsideLine()[0].setColor(mColorHigh & 0x00ffffff | 0x80000000);
			}else {
//				xyRenderer.setColor(mColorAim & 0x00ffffff | 0x80000000);
				xyRenderer.getFillOutsideLine()[0].setColor(mColorAim & 0x00ffffff | 0x80000000);
			}
		}
	}
	
	int getStrength(int heartrate) {
		if (heartrate < warmUp)
			return FitnessUtils.STRENGTH_WARM_UP;
		else if (heartrate < fatBunning)
			return FitnessUtils.STRENGTH_FAT_BUNNING;
		else if (heartrate < common)
			return FitnessUtils.STRENGTH_COMMON;
		else if (heartrate < high)
			return FitnessUtils.STRENGTH_HIGH;
		else return FitnessUtils.STRENGTH_EXCEED;
	}

	int mColorShort = 0xf8f886;
	int mColorAim = 0xc2f473;
	int mColorHigh = 0xff944d;
	int getStrengthColor(int strength) {
		if (strength == FitnessUtils.STRENGTH_WARM_UP)
			return mColorShort & 0x00ffffff | 0x80000000;
		else if (strength == FitnessUtils.STRENGTH_FAT_BUNNING)
			return mColorShort & 0x00ffffff | 0x80000000;
		else if (strength == FitnessUtils.STRENGTH_COMMON)
			return mColorAim & 0x00ffffff | 0x80000000;
		else if (strength == FitnessUtils.STRENGTH_HIGH)
			return mColorHigh & 0x00ffffff | 0x80000000;
		else
			return mColorHigh & 0x00ffffff | 0x80000000;
	}
	
	int prevStrength = 0;
	double prevData = 0;
	public void addPoint(int x,int heartrate){
		int strength = getStrength(heartrate);
		if(x==1)prevStrength=strength;
		double data = getHeartrateRatio(heartrate,strength);
		if(prevStrength==strength){
			if(strength==FitnessUtils.STRENGTH_HIGH){
				mHighSeries.add(x, data);
				mCommonSeries.add(x, 0.01);
				mFatBunningSeries.add(x, 0.01);
				mWarmUpSeries.add(x, 0.01);
				mExceedSeries.add(x, 0.01);
			}else if(strength==FitnessUtils.STRENGTH_COMMON){
				mHighSeries.add(x, 0.01);
				mCommonSeries.add(x, data);
				mFatBunningSeries.add(x, 0.01);
				mWarmUpSeries.add(x, 0.01);
				mExceedSeries.add(x, 0.01);
			}else if(strength==FitnessUtils.STRENGTH_FAT_BUNNING){
				mHighSeries.add(x, 0.01);
				mCommonSeries.add(x, 0.01);
				mFatBunningSeries.add(x, data);
				mWarmUpSeries.add(x, 0.01);
				mExceedSeries.add(x, 0.01);
			}else if(strength==FitnessUtils.STRENGTH_WARM_UP){
				mHighSeries.add(x, 0.01);
				mCommonSeries.add(x, 0.01);
				mFatBunningSeries.add(x, 0.01);
				mWarmUpSeries.add(x, data);
				mExceedSeries.add(x, 0.01);
			}else if(strength==FitnessUtils.STRENGTH_EXCEED){
				mHighSeries.add(x, 0.01);
				mCommonSeries.add(x, 0.01);
				mFatBunningSeries.add(x, 0.01);
				mWarmUpSeries.add(x, 0.01);
				mExceedSeries.add(x, data);
			}
		}else {
			if(strength==FitnessUtils.STRENGTH_HIGH){
				mHighSeries.add(x-1, prevData); mHighSeries.add(x, data);
				mCommonSeries.add(x-1, 0.01); mCommonSeries.add(x, 0.01);
				mFatBunningSeries.add(x-1, 0.01); mFatBunningSeries.add(x, 0.01);
				mWarmUpSeries.add(x-1, 0.01); mWarmUpSeries.add(x, 0.01);
				mExceedSeries.add(x-1, 0.01); mExceedSeries.add(x, 0.01);
			}else if(strength==FitnessUtils.STRENGTH_COMMON){
				mHighSeries.add(x-1, 0.01); mHighSeries.add(x, 0.01);
				mCommonSeries.add(x-1, prevData); mCommonSeries.add(x, data);
				mFatBunningSeries.add(x-1, 0.01); mFatBunningSeries.add(x, 0.01);
				mWarmUpSeries.add(x-1, 0.01); mWarmUpSeries.add(x, 0.01);
				mExceedSeries.add(x-1, 0.01); mExceedSeries.add(x, 0.01);
			}else if(strength==FitnessUtils.STRENGTH_FAT_BUNNING){
				mHighSeries.add(x-1, 0.01); mHighSeries.add(x, 0.01);
				mCommonSeries.add(x-1, 0.01); mCommonSeries.add(x, 0.01);
				mFatBunningSeries.add(x-1, prevData); mFatBunningSeries.add(x, data);
				mWarmUpSeries.add(x-1, 0.01); mWarmUpSeries.add(x, 0.01);
				mExceedSeries.add(x-1, 0.01); mExceedSeries.add(x, 0.01);
			}else if(strength==FitnessUtils.STRENGTH_WARM_UP){
				mHighSeries.add(x-1, 0.01); mHighSeries.add(x, 0.01);
				mCommonSeries.add(x-1, 0.01); mCommonSeries.add(x, 0.01);
				mFatBunningSeries.add(x-1, 0.01); mFatBunningSeries.add(x, 0.01);
				mWarmUpSeries.add(x-1, prevData); mWarmUpSeries.add(x, data);
				mExceedSeries.add(x-1, 0.01); mExceedSeries.add(x, 0.01);
			}else if(strength==FitnessUtils.STRENGTH_EXCEED){
				mHighSeries.add(x-1, 0.01); mHighSeries.add(x, 0.01);
				mCommonSeries.add(x-1, 0.01); mCommonSeries.add(x, 0.01);
				mFatBunningSeries.add(x-1, 0.01); mFatBunningSeries.add(x, 0.01);
				mWarmUpSeries.add(x-1, 0.01); mWarmUpSeries.add(x, 0.01);
				mExceedSeries.add(x-1, prevData); mExceedSeries.add(x, data);
			}
		}
		
		prevStrength = strength;
		prevData = data;
	}
	
	public void clear(){
		mHighSeries.clear();
		mCommonSeries.clear();
		mFatBunningSeries.clear();
		mWarmUpSeries.clear();
		mExceedSeries.clear();
	}
	
	public void setKarvonen(int warmUp,int fatBunning,int common,int high){
		this.warmUp = warmUp;
		this.fatBunning = fatBunning;
		this.common = common;
		this.high = high;
	}
	
	double getHeartrateRatio(int heartrate,int strength){
		double result = heartrate;
		if(strength==FitnessUtils.STRENGTH_WARM_UP)
		{
			result = (double)(heartrate * (25.0f / warmUp)); 
		}
		else if(strength==FitnessUtils.STRENGTH_FAT_BUNNING)
		{
			result = (double)((heartrate-warmUp) * (25.0f / (fatBunning-warmUp)))+25; 
		}
		else if(strength==FitnessUtils.STRENGTH_COMMON)
		{
			result = (double)((heartrate-fatBunning) * (25.0f / (common-fatBunning)))+50; 
		}
		else if(strength==FitnessUtils.STRENGTH_HIGH)
		{
			result = (double)((heartrate-common) * (25.0f / (high-common)))+75; 
		}else {
			result = 100;
		}
		
		if(result==0)result = 0.01;
		return result;
	}
}
