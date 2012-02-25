package com.piratebox.billing;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class BillingService extends Service {

	
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		try {
			boolean bindResult = bindService(new Intent(
					"com.android.vending.billing.MarketBillingService.BIND"),
					this, Context.BIND_AUTO_CREATE);
			if (bindResult) {
				Log.i(this.getClass().getName(), "Service bind successful.");
			} else {
				Log.e(this.getClass().getName(), "Could not bind to the MarketBillingService.");
			}
		} catch (SecurityException e) {
			Log.e(this.getClass().getName(), "Security exception: " + e);
		}
	}
}
