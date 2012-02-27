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

public class BillingService extends Service implements ServiceConnection {
    
    private static final String MARKET_BILLING_SERVICE = "com.android.vending.billing.MarketBillingService.BIND";

    private static IMarketBillingService marketService;
    private static String packageName;

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

    public void onServiceConnected(ComponentName name, IBinder service) {
        marketService = IMarketBillingService.Stub.asInterface(service);
    }

    public void onServiceDisconnected(ComponentName name) {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

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
    
    public static boolean requestPurchase(String itemId, Activity activity) throws RemoteException {

        Bundle request = makeRequestBundle(BillingConstants.REQUEST_TYPE_REQUEST_PURCHASE);
        request.putString(BillingConstants.BILLING_REQUEST_ITEM_ID, itemId);
        Bundle response = marketService.sendBillingRequest(request);
        PendingIntent pendingIntent = response.getParcelable(BillingConstants.BILLING_RESPONSE_PURCHASE_INTENT);
        
        try {
            activity.startIntentSender(pendingIntent.getIntentSender(), new Intent(), 0, 0, 0);
            return true;
        } catch (SendIntentException e) {
            Log.e(BillingService.class.getName() + "#requestPurchase", "" + e);
        }
        
        return false;
    }
    
    public static boolean getPurchaseInformation(String[] notifyIds) throws RemoteException {
        Bundle request = makeRequestBundle("GET_PURCHASE_INFORMATION");
        request.putLong(BillingConstants.BILLING_REQUEST_NONCE, getNonce());
        request.putStringArray(BillingConstants.BILLING_REQUEST_NOTIFY_IDS, notifyIds);
        Bundle response = marketService.sendBillingRequest(request);

        return (Integer)response.get(BillingConstants.BILLING_RESPONSE_RESPONSE_CODE) == 0;
    }
    
    public static boolean confirmNotifications(String[] notifyIds) throws RemoteException {
        Bundle request = makeRequestBundle(BillingConstants.REQUEST_TYPE_CONFIRM_NOTIFICATIONS);
        request.putStringArray(BillingConstants.BILLING_REQUEST_NOTIFY_IDS, notifyIds);
        Bundle response = marketService.sendBillingRequest(request);

        return (Integer)response.get(BillingConstants.BILLING_RESPONSE_RESPONSE_CODE) == 0;
    }


    private static Bundle makeRequestBundle(String method) {
        Bundle request = new Bundle();
        request.putString(BillingConstants.BILLING_REQUEST_METHOD, method);
        request.putInt(BillingConstants.BILLING_REQUEST_API_VERSION, 1);
        request.putString(BillingConstants.BILLING_REQUEST_PACKAGE_NAME, packageName);
        return request;
    }
    
    private static long getNonce() {
        return 0L;
    }
}


