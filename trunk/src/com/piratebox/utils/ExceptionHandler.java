/**
 * 
 */
package com.piratebox.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.widget.Toast;

/**
 * @author Aylatan
 */
public class ExceptionHandler {
    
    public static void handle(String tag, String message, Context ctx, boolean showToUser) {
        Log.e(tag, message);
        if (showToUser) {
            Toast.makeText(ctx, message, Toast.LENGTH_LONG).show();
        }
    }

    public static void handle(String tag, String message, Context ctx) {
        handle(tag, message, ctx, true);
    }
    
    public static void handle(String tag, Exception e, Context ctx) {
        handle(tag, e.toString(), ctx);
    }

    public static void handle(Object sender, String message, Context ctx) {
        handle(sender.getClass().getName(), message, ctx);
    }
    
    public static void handle(Object sender, Exception e, Context ctx) {
        handle(sender.getClass().getName(), e, ctx);
    }
    
    public static void handle(String tag, String message) {
        handle(tag, message, null, false);
    }
    
    public static void handle(String tag, Exception e) {
        handle(tag, e.toString());
    }
    
    public static void handle(Object sender, Exception e) {
        handle(sender.getClass().getName(), e);
    }
    
    public static void handle(Object sender, String message) {
        handle(sender.getClass().getName(), message);
    }
    
    public static void handle(String tag, int stringId, Context ctx) {
        handle(tag, Resources.getSystem().getString(stringId), ctx);
    }
    
    public static void handle(Object sender, int stringId, Context ctx){
        handle(sender.getClass().getName(), Resources.getSystem().getString(stringId), ctx);
    }
}
