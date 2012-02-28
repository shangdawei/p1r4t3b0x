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

import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.widget.Toast;

import com.piratebox.R;
import com.piratebox.billing.util.BillingConstants;
import com.piratebox.billing.util.BillingConstants.ResponseCode;
import com.piratebox.utils.ExceptionHandler;

/**
 * This class implements the {@link BroadcastReceiver} used to handle message from the MarketService for the In-App Billing functionality.
 * 
 * @author Aylatan
 */
public class BillingReceiver extends BroadcastReceiver {

    /**
     * The json field name for the nonce.
     */
    private static final String JSON_FIELD_NONCE = "nonce";
    
    /**
     * The json field name for the notification id.
     */
    private static final String JSON_FIELD_NOTIFICATION_ID = "notificationId";

    /**
     * Defines what to do for the different messages.
     * 
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (BillingConstants.ACTION_PURCHASE_STATE_CHANGED.equals(action)) {
            String signedData = intent.getStringExtra(BillingConstants.INAPP_SIGNED_DATA);
            String signature = intent.getStringExtra(BillingConstants.INAPP_SIGNATURE);
            
            try {
                JSONObject json = new JSONObject(signedData);
                long nonce = (Long) json.get(JSON_FIELD_NONCE);
                
                // Check data signature and nonce
                if (!Nonce.isNonceKnown(nonce) || ! BillingSecurity.checkData(signedData, signature)) {
                    return;
                }
                Nonce.removeNonce(nonce);

                // Display a thank you message
                Toast.makeText(context, R.string.thank_you, Toast.LENGTH_LONG).show();
                
                // Send confirmation
                BillingService.confirmNotifications(new String[]{(String) json.get(JSON_FIELD_NOTIFICATION_ID)});
            } catch (Exception e) {
                ExceptionHandler.handle(this, R.string.error_during_payment, context.getApplicationContext());
            }
            
        } else if (BillingConstants.ACTION_NOTIFY.equals(action)) {
            String notifyId = intent.getStringExtra(BillingConstants.NOTIFICATION_ID);
            try {
                // Ask for the details of the transaction
                BillingService.getPurchaseInformation(new String[]{notifyId});
            } catch (RemoteException e) {
                ExceptionHandler.handle(this, R.string.error_during_payment, context.getApplicationContext());
            }

        } else if (BillingConstants.ACTION_RESPONSE_CODE.equals(action)) {
            
            int responseCodeIndex = intent.getIntExtra(BillingConstants.INAPP_RESPONSE_CODE, ResponseCode.RESULT_ERROR.ordinal());
            if (! BillingConstants.ResponseCode.RESULT_OK.equals(BillingConstants.ResponseCode.valueOf(responseCodeIndex))) {
                ExceptionHandler.handle(this, R.string.error_during_payment, context);
            }

        }
    }

}