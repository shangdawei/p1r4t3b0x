/**
 * 
 */
package com.piratebox;

import com.piratebox.utils.Callback;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @author Aylatan
 */
public class WifiApStateReceiver extends BroadcastReceiver {
    
    private static Callback onChangeCallback;

    public static void setOnChangeCallback(Callback onChangeCallback) {
        WifiApStateReceiver.onChangeCallback = onChangeCallback;
    }

    /* (non-Javadoc)
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        WifiApStateReceiver.onChangeCallback.call(intent);
    }

}
