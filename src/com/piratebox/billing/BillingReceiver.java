package com.piratebox.billing;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.piratebox.billing.util.BillingConstants;
import com.piratebox.billing.util.BillingConstants.ResponseCode;

public class BillingReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (BillingConstants.ACTION_PURCHASE_STATE_CHANGED.equals(action)) {
            String signedData = intent.getStringExtra(BillingConstants.INAPP_SIGNED_DATA);
            String signature = intent.getStringExtra(BillingConstants.INAPP_SIGNATURE);
            // Do something with the signedData and the signature.

        } else if (BillingConstants.ACTION_NOTIFY.equals(action)) {
            String notifyId = intent.getStringExtra(BillingConstants.NOTIFICATION_ID);
            // Do something with the notifyId.
            
            
//            confirm notifications

        } else if (BillingConstants.ACTION_RESPONSE_CODE.equals(action)) {
            long requestId = intent.getLongExtra(BillingConstants.INAPP_REQUEST_ID, -1);
            int responseCodeIndex = intent.getIntExtra(BillingConstants.INAPP_RESPONSE_CODE, ResponseCode.RESULT_ERROR.ordinal());
            // Do something with the requestId and the responseCodeIndex.

        }
    }

}