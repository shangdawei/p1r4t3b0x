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
}
