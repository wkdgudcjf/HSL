/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.samsung.hsl.fitnessuser.comm;

import com.samsung.hsl.fitnessuser.service.FitnessUserService;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Build;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class FitnessNsdManager{
    public static final String tag = FitnessNsdManager.class.getName();
    
    FitnessUserService mFitnessService;

    NsdManager mNsdManager;

    public static final String SERVICE_TYPE = "_http._tcp.";
    public static final String SERVICE_NAME = "com.samsung.hsl.fitness";
    public static final String SERVICE_REQUEST_DEVICE_INFO = "SERVICE_REQUEST_DEVICE_INFO";
    public static final String SERVICE_SENDING_FITNESS_DATA = "SERVICE_SENDING_FITNESS_DATA";

    public FitnessNsdManager(FitnessUserService service) {
    	mFitnessService = service;
        mNsdManager = (NsdManager) service.getSystemService(Context.NSD_SERVICE);
        
    }

	NsdManager.DiscoveryListener mDiscoveryListener = new NsdManager.DiscoveryListener() {

        @Override
        public void onDiscoveryStarted(String regType) {
            Log.i(tag, "Service discovery started");
        }

        @Override
        public void onServiceFound(NsdServiceInfo service) {
            if (service.getServiceType().equals(SERVICE_TYPE) && service.getServiceName().contains(SERVICE_NAME))
            {
            	try{
            		mNsdManager.resolveService(service, new NsdManager.ResolveListener() {

            	        @Override
            	        public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
            	            Log.i(tag, "Resolve failed" + errorCode);
            	        }

            	        @Override
            	        public void onServiceResolved(NsdServiceInfo serviceInfo) {
            	            Log.i(tag, "Resolve Succeeded. " + serviceInfo);
            	            
            	            // TODO 서비스에 접속하여 디바이스에 대한 정보를 얻어온다. 
            	            mFitnessService.getFitnessWifiManager().resolveDevice(serviceInfo.getHost(),serviceInfo.getPort());
            	           
            	        }
            	    });
            		
            	}catch(IllegalArgumentException e){
            		e.printStackTrace();
            	}
            }
        }

        @Override
        public void onServiceLost(NsdServiceInfo service) {
            Log.i(tag, "service lost" + service);
        }
        
        @Override
        public void onDiscoveryStopped(String serviceType) {
            Log.i(tag, "Discovery stopped: " + serviceType);        
        }

        @Override
        public void onStartDiscoveryFailed(String serviceType, int errorCode) {
            Log.i(tag, "Discovery failed: Error code:" + errorCode);
            mNsdManager.stopServiceDiscovery(this);
        }

        @Override
        public void onStopDiscoveryFailed(String serviceType, int errorCode) {
            Log.i(tag, "Discovery failed: Error code:" + errorCode);
            mNsdManager.stopServiceDiscovery(this);
        }
    };

	NsdManager.RegistrationListener mRegistrationListener = new NsdManager.RegistrationListener() {

        @Override
        public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
        	Log.i(tag, "onServiceRegistered");
        }
        
        @Override
        public void onRegistrationFailed(NsdServiceInfo arg0, int arg1) {
        	Log.i(tag, "onRegistrationFailed");
        }

        @Override
        public void onServiceUnregistered(NsdServiceInfo arg0) {
        	Log.i(tag, "onServiceUnregistered");
        }
        
        @Override
        public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
        	Log.i(tag, "onUnregistrationFailed");
        }
        
    };

    public void registerService() {
    	Log.i(tag,"registerService");
        NsdServiceInfo serviceInfo  = new NsdServiceInfo();
        serviceInfo.setPort(FitnessWifiManager.SERVICE_PORT);
        serviceInfo.setServiceName(SERVICE_NAME);
        serviceInfo.setServiceType(SERVICE_TYPE);

        mNsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
        
    }

    public void unregisterService() {
        mNsdManager.unregisterService(mRegistrationListener);
    }
    
    public void discoverServices() {
    	try{
    		mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    		Log.i(tag, "discoverServices");
    	}catch(IllegalArgumentException e){
    		e.printStackTrace();
    	}
    }
    
    public void stopDiscovery() {
    	try{
    		 mNsdManager.stopServiceDiscovery(mDiscoveryListener);
    		Log.i(tag, "stopDiscovery");
    	}catch(IllegalArgumentException e){
    		//e.printStackTrace();
    	}
    }


}
