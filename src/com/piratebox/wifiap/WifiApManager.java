/**
 * This is a file from P1R4T3B0X, a program that lets you share files with everyone.
 * Copyright (C) 2012 by Aylatan
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * The GNU General Public License can be found at http://www.gnu.org/licenses.
 */

package com.piratebox.wifiap;

import java.lang.reflect.Method;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiApManager {

	public static final int WIFI_AP_STATE_FAILED = 4;

	private final WifiManager mWifiManager;

	public WifiApManager(Context context) {
		mWifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
	}

	public boolean setWifiApEnabled(WifiConfiguration config,
			boolean enabled) {
		
		try {
			if (enabled) {
				mWifiManager.setWifiEnabled(false);
			}
			
			Method method = mWifiManager.getClass().getMethod(
					"setWifiApEnabled", WifiConfiguration.class,
					boolean.class);
			return (Boolean) method.invoke(mWifiManager, config, enabled);
		} catch (Exception e) {
			Log.e(this.getClass().getName(), e.toString());
			return false;
		}
	}
    
    public int getWifiApState() {
        try {
            Method method = mWifiManager.getClass().getMethod(
                    "getWifiApState");
            return (Integer) method.invoke(mWifiManager);
        } catch (Exception e) {
            Log.e(this.getClass().getName(), e.toString());
            return WIFI_AP_STATE_FAILED;
        }
    }
    
    public boolean isWifiApEnabled() {
        try {
            Method method = mWifiManager.getClass().getMethod(
                    "isWifiApEnabled");
            return (Boolean) method.invoke(mWifiManager);
        } catch (Exception e) {
            Log.e(this.getClass().getName(), e.toString());
            return false;
        }
    }
	
	public WifiConfiguration getWifiApConfiguration() {
		try {
			Method method = mWifiManager.getClass().getMethod(
					"getWifiApConfiguration", WifiConfiguration.class,
					boolean.class);
			return (WifiConfiguration) method.invoke(mWifiManager, new Object[]{});
		} catch (Exception e) {
			Log.e(this.getClass().getName(), e.toString());
			return null;
		}
	}
	
	public void setWifiApConfiguration(WifiConfiguration config) {
		setWifiApEnabled(config, getWifiApState() == WifiManager.WIFI_STATE_ENABLED);
	}
}
