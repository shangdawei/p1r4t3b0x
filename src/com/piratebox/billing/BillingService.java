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

package com.piratebox.billing;

import android.app.Activity;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.android.vending.billing.IMarketBillingService;
import com.piratebox.billing.util.BillingConstants;

/**
 * This class provides methods to send purchase request to the MarketService.
 * 
 * @author Aylatan
 */
public class BillingService extends Service implements ServiceConnection {
    
    /**
     * The MarketService identifier.
     */
    private static final String MARKET_BILLING_SERVICE = "com.android.vending.billing.MarketBillingService.BIND";

    /**
     * Used to hold the MarketService instance.
     */
    private static IMarketBillingService marketService;
    
    /**
     * Used to hold the package name.
     */
    private static String packageName;

    /**
     * Binds the MarketService and initialises the package name.
     * 
     * @see android.app.Service#onCreate()
     */
    @Override
    public void onCreate() {
        try {
            boolean bindResult = bindService(new Intent(MARKET_BILLING_SERVICE), this, Context.BIND_AUTO_CREATE);
            if (!bindResult) {
                Log.e(this.getClass().getName(), "Could not bind to the MarketBillingService.");
            }
        } catch (SecurityException e) {
            Log.e(this.getClass().getName(), "Security exception: " + e);
        }
        
        packageName = getPackageName();
    }

    /**
     * Initialises the marketService variable with the provided instance.
     * 
     * @see android.content.ServiceConnection#onServiceConnected(android.content.ComponentName, android.os.IBinder)
     */
    public void onServiceConnected(ComponentName name, IBinder service) {
        marketService = IMarketBillingService.Stub.asInterface(service);
    }

    /**
     * Nothing to do on disconnect.
     * 
     * @see android.content.ServiceConnection#onServiceDisconnected(android.content.ComponentName)
     */
    public void onServiceDisconnected(ComponentName name) {
    }

    /**
     * Nothing to do on bind.
     * 
     * @see android.app.Service#onBind(android.content.Intent)
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Checks if the "in app billing" feature is accessible in the current system.
     * @return {@code true} if the system supports the "in app billing" feature, {@code false} otherwise
     * @throws RemoteException if fails accessing the market service.
     */
    public static boolean isInAppBillingSupported() throws RemoteException {
        Bundle request = makeRequestBundle(BillingConstants.REQUEST_TYPE_CHECK_BILLING_SUPPORTED);
        Bundle response = marketService.sendBillingRequest(request);

        switch (BillingConstants.ResponseCode.valueOf((Integer) response.get(BillingConstants.BILLING_RESPONSE_RESPONSE_CODE))) {
        case RESULT_OK:
            return true;
        case RESULT_BILLING_UNAVAILABLE:
            return false;
        case RESULT_ERROR:
            Log.e(BillingService.class.getName() + "#isInAppBillingSupported", "Error trying to get in-app billing informations.");
            return false;
        case RESULT_DEVELOPER_ERROR:
            Log.e(BillingService.class.getName() + "#isInAppBillingSupported", "Developper error trying to get in-app billing informations.");
            return false;
        default:
            Log.e(BillingService.class.getName() + "#isInAppBillingSupported", "Unknown response.");
            return false;
        }
    }
    
    /**
     * Requests a purchase for the given itemId, and open the market activity.
     * @param itemId the id of the item to puchase
     * @param activity the activity in which the market activity should be opened
     * @return {@code false} if something went wrong, {@code true} otherwise
     * @throws RemoteException if fails accessing the market service.
     */
    public static boolean requestPurchase(String itemId, Activity activity) throws RemoteException {

        Bundle request = makeRequestBundle(BillingConstants.REQUEST_TYPE_REQUEST_PURCHASE);
        request.putString(BillingConstants.BILLING_REQUEST_ITEM_ID, itemId);
        Bundle response = marketService.sendBillingRequest(request);
        
        int responseCode = response.getInt(BillingConstants.ACTION_RESPONSE_CODE);
        if (! BillingConstants.ResponseCode.RESULT_OK.equals(BillingConstants.ResponseCode.valueOf(responseCode))) {
            return false;
        }
        
        PendingIntent pendingIntent = response.getParcelable(BillingConstants.BILLING_RESPONSE_PURCHASE_INTENT);
        
        try {
            activity.startIntentSender(pendingIntent.getIntentSender(), new Intent(), 0, 0, 0);
        } catch (SendIntentException e) {
            Log.e(BillingService.class.getName() + "#requestPurchase", "" + e);
            return false;
        }
        return true;
    }
    
    /**
     * Requests a purchase information for the given notification ids.
     * @param notifyIds the notification ids to get informations for
     * @return {@code false} if something went wrong, {@code true} otherwise
     * @throws RemoteException if fails accessing the market service.
     */
    public static boolean getPurchaseInformation(String[] notifyIds) throws RemoteException {
        Bundle request = makeRequestBundle("GET_PURCHASE_INFORMATION");
        request.putLong(BillingConstants.BILLING_REQUEST_NONCE, Nonce.getNonce());
        request.putStringArray(BillingConstants.BILLING_REQUEST_NOTIFY_IDS, notifyIds);
        Bundle response = marketService.sendBillingRequest(request);

        return (Integer)response.get(BillingConstants.BILLING_RESPONSE_RESPONSE_CODE) == 0;
    }
    
    /**
     * Sends a confirm notification for the provided notification ids.
     * @param notifyIds the notification ids to confirm
     * @return {@code false} if something went wrong, {@code true} otherwise
     * @throws RemoteException if fails accessing the market service.
     */
    public static boolean confirmNotifications(String[] notifyIds) throws RemoteException {
        Bundle request = makeRequestBundle(BillingConstants.REQUEST_TYPE_CONFIRM_NOTIFICATIONS);
        request.putStringArray(BillingConstants.BILLING_REQUEST_NOTIFY_IDS, notifyIds);
        Bundle response = marketService.sendBillingRequest(request);

        return (Integer)response.get(BillingConstants.BILLING_RESPONSE_RESPONSE_CODE) == 0;
    }


    /**
     * Creates a base {@link Bundle} with the provided method that can be used to perform requests.
     * @param method the method to set to the {@link Bundle}
     * @return a {@link Bundle} with its method, api version and package name field filled.
     */
    private static Bundle makeRequestBundle(String method) {
        Bundle request = new Bundle();
        request.putString(BillingConstants.BILLING_REQUEST_METHOD, method);
        request.putInt(BillingConstants.BILLING_REQUEST_API_VERSION, 1);
        request.putString(BillingConstants.BILLING_REQUEST_PACKAGE_NAME, packageName);
        return request;
    }
}


