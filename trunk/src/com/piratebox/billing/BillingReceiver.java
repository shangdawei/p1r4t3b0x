package com.piratebox.billing;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

import com.piratebox.R;
import com.piratebox.billing.util.BillingConstants;
import com.piratebox.billing.util.BillingConstants.ResponseCode;

public class BillingReceiver extends BroadcastReceiver {

    private static final String JSON_FIELD_NONCE = "nonce";
    private static final String JSON_FIELD_NOTIFICATION_ID = "notificationId";
    private static final String JSON_FIELD_ORDER_ID = "orderId";
    private static final String JSON_FIELD_PACKAGE_NAME = "packageName";
    private static final String JSON_FIELD_PRODUCT_ID = "productId";
    private static final String JSON_FIELD_PURCHASE_TIME = "purchaseTime";
    private static final String JSON_FIELD_PURCHASE_STATE = "purchaseState";
    private static final String JSON_FIELD_DEVELOPER_PAYLOAD = "developerPayload";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (BillingConstants.ACTION_PURCHASE_STATE_CHANGED.equals(action)) {
            String signedData = intent.getStringExtra(BillingConstants.INAPP_SIGNED_DATA);
            String signature = intent.getStringExtra(BillingConstants.INAPP_SIGNATURE);
            // Do something with the signedData and the signature.

            //validate data with signature
            
            try {
                Toast.makeText(context, R.string.thank_you, Toast.LENGTH_LONG).show();
                
                JSONObject json = new JSONObject(signedData);
                BillingService.confirmNotifications(new String[]{(String) json.get(JSON_FIELD_NOTIFICATION_ID)});
            } catch (Exception e) {
                Log.e(this.getClass().getName(), e.toString());
            }
            
        } else if (BillingConstants.ACTION_NOTIFY.equals(action)) {
            String notifyId = intent.getStringExtra(BillingConstants.NOTIFICATION_ID);
            try {
                BillingService.getPurchaseInformation(new String[]{notifyId});
            } catch (RemoteException e) {
                Log.e(this.getClass().getName(), e.toString());
            }

        } else if (BillingConstants.ACTION_RESPONSE_CODE.equals(action)) {
            long requestId = intent.getLongExtra(BillingConstants.INAPP_REQUEST_ID, -1);
            int responseCodeIndex = intent.getIntExtra(BillingConstants.INAPP_RESPONSE_CODE, ResponseCode.RESULT_ERROR.ordinal());
            if (! BillingConstants.ResponseCode.RESULT_OK.equals(BillingConstants.ResponseCode.valueOf(responseCodeIndex))) {
                Toast.makeText(context, R.string.error_during_payment, Toast.LENGTH_LONG).show();
            }

        }
    }

}