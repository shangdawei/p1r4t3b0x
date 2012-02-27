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

/**
 * This class uses reflection to call hidden methods of {@link WifiManager}.
 * @author Aylatan
 */
public class WifiApManager {

    /**
     * The lookup key for an int that indicates whether Wi-Fi AP is enabled,
     * disabled, enabling, disabling, or failed.  Retrieve it with
     * {@link android.content.Intent#getIntExtra(String,int)}.
     *
     * @see #WIFI_AP_STATE_DISABLED
     * @see #WIFI_AP_STATE_DISABLING
     * @see #WIFI_AP_STATE_ENABLED
     * @see #WIFI_AP_STATE_ENABLING
     * @see #WIFI_AP_STATE_FAILED
     *
     * @hide
     */
    public static final String EXTRA_WIFI_AP_STATE = "wifi_state";
    
    /**
     * The previous Wi-Fi state.
     *
     * @see #EXTRA_WIFI_AP_STATE
     */
    public static final String EXTRA_PREVIOUS_WIFI_AP_STATE = "previous_wifi_state";
    
    /**
     * Wi-Fi AP is currently being disabled. The state will change to
     * {@link #WIFI_AP_STATE_DISABLED} if it finishes successfully.
     *
     * @see #WIFI_AP_STATE_CHANGED_ACTION
     * @see #getWifiApState()
     */
    public static final int WIFI_AP_STATE_DISABLING = 0;
    
    /**
     * Wi-Fi AP is disabled.
     *
     * @see #WIFI_AP_STATE_CHANGED_ACTION
     * @see #getWifiState()
     */
    public static final int WIFI_AP_STATE_DISABLED = 1;
    
    /**
     * Wi-Fi AP is currently being enabled. The state will change to
     * {@link #WIFI_AP_STATE_ENABLED} if it finishes successfully.
     *
     * @see #WIFI_AP_STATE_CHANGED_ACTION
     * @see #getWifiApState()
     */
    public static final int WIFI_AP_STATE_ENABLING = 2;
    
    /**
     * Wi-Fi AP is enabled.
     *
     * @see #WIFI_AP_STATE_CHANGED_ACTION
     * @see #getWifiApState()
     */
    public static final int WIFI_AP_STATE_ENABLED = 3;
    
    /**
     * Wi-Fi AP is in a failed state. This state will occur when an error occurs during
     * enabling or disabling
     *
     * @see #WIFI_AP_STATE_CHANGED_ACTION
     * @see #getWifiApState()
     */
    public static final int WIFI_AP_STATE_FAILED = 4;
    
    
	/**
	 * The {@link WifiManager} instance.
	 */
	private final WifiManager wifiManager;

	/**
	 * Creates a new {@link WifiApManager}.
	 * @param context the context of the application
	 */
	public WifiApManager(Context context) {
		wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
	}

	/**
	 * Turns on or off the wifi access point with the given configuration.
	 * The wifi will be turned off if {@code enabled} is true.
	 * @param config The configuration to set to the wifi access point
	 * @param enabled the state in which to set the wifi access point
	 * @return {@code true} if the operation succeeds, {@code false} otherwise
	 */
	public boolean setWifiApEnabled(WifiConfiguration config,
			boolean enabled) {
		
		try {
			if (enabled) {
				wifiManager.setWifiEnabled(false);
			}
			
			Method method = wifiManager.getClass().getMethod(
					"setWifiApEnabled", WifiConfiguration.class,
					boolean.class);
			return (Boolean) method.invoke(wifiManager, config, enabled);
		} catch (Exception e) {
			Log.e(this.getClass().getName(), e.toString());
			return false;
		}
	}
    
    /**
     * Gets the wifi access point enabled state.
     * @return One of {@link #WIFI_AP_STATE_DISABLED},
     *         {@link #WIFI_AP_STATE_DISABLING}, {@link #WIFI_AP_STATE_ENABLED},
     *         {@link #WIFI_AP_STATE_ENABLING}, {@link #WIFI_AP_STATE_FAILED}
     * @see #isWifiApEnabled()
     */
    public int getWifiApState() {
        try {
            Method method = wifiManager.getClass().getMethod(
                    "getWifiApState");
            return (Integer) method.invoke(wifiManager);
        } catch (Exception e) {
            Log.e(this.getClass().getName(), e.toString());
            return WIFI_AP_STATE_FAILED;
        }
    }
    
    /**
     * Return whether wifi access point is enabled or disabled.
     * @return {@code true} if wifi access point is enabled
     * @see #getWifiApState()
     */
    public boolean isWifiApEnabled() {
        try {
            Method method = wifiManager.getClass().getMethod(
                    "isWifiApEnabled");
            return (Boolean) method.invoke(wifiManager);
        } catch (Exception e) {
            Log.e(this.getClass().getName(), e.toString());
            return false;
        }
    }
	
	/**
	 * Updates the current wifi access point configuration without changing its state.
	 * @param config the new configuration to be applied
	 */
	public void setWifiApConfiguration(WifiConfiguration config) {
		setWifiApEnabled(config, getWifiApState() == WifiManager.WIFI_STATE_ENABLED);
	}
}
