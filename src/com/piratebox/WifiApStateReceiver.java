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
package com.piratebox;

import com.piratebox.utils.Callback;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * This class handles Broadcasts about the wifi access point state and calls the defined callback with the informations.
 * @author Aylatan
 */
public class WifiApStateReceiver extends BroadcastReceiver {
    
    private static Callback _onChangeCallback;

    public static void setOnChangeCallback(Callback onChangeCallback) {
        _onChangeCallback = onChangeCallback;
    }

    /**
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        if (_onChangeCallback != null) {
            _onChangeCallback.call(intent);
        }
    }

}
