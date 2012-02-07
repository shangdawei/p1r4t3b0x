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

package com.piratebox.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.widget.RemoteViews;

import com.piratebox.R;
import com.piratebox.System;
import com.piratebox.System.ServerState;
import com.piratebox.utils.Callback;

/**
 * This class describes the {@link AppWidgetProvider} used with this application.
 * @author Aylatan
 */
public class P1R4T3B0XWidget extends AppWidgetProvider {

    /**
     * The widget initialisation broadcast action.
     */
    public static final String WIDGET_RECEIVER_INIT = "WidgetReceiverInit";
    
	private final String WIDGET_RECEIVER_CLICK = "WidgetReceiverClick";
	private final int MS_BETWEEN_IMAGE_UPDATE = 300;
	
	private boolean initialized = false;
	private Callback updateWidgetsCallback;
	private Handler updateHandler = new Handler();
	private int currentImage = 0;
	private Runnable updateImage;
	
	/**
	 * Updates the widget content by reading the current state of the system.
	 * @see android.appwidget.AppWidgetProvider#onUpdate(android.content.Context, android.appwidget.AppWidgetManager, int[])
	 */
	@Override
	public void onUpdate(final Context context, final AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		
		final int N = appWidgetIds.length;
		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.widget);

		//Initialise the widget if not already done
        init(context);
        System system = System.getInstance(context);
        //Update the widget
        updateWidgetsCallback.call(system.getServerState());
		
        //Set a click action for all widgets
		for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];
            Intent intent = new Intent(context, P1R4T3B0XWidget.class);
            intent.setAction(WIDGET_RECEIVER_CLICK);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

            views.setOnClickPendingIntent(R.id.widgetimg, pendingIntent);

			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}

	/**
	 * Process messages.
	 * Can handle {@link P1R4T3B0XWidget#WIDGET_RECEIVER_CLICK} and {@link P1R4T3B0XWidget#WIDGET_RECEIVER_INIT} messages.
	 * @see android.appwidget.AppWidgetProvider#onReceive(android.content.Context, android.content.Intent)
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		if (WIDGET_RECEIVER_CLICK.equals(intent.getAction())) {
		    //On click, switch the system state
			System system = System.getInstance(context);
			if (ServerState.STATE_OFF.equals(system.getServerState())) {
				system.start();
			} else {
				system.stop();
			}
		} else if (WIDGET_RECEIVER_INIT.equals(intent.getAction())) {
		    //On init, launch the initialisation
		    init(context);
		}
	}

	/**
	 * Initialises the widget.
	 * Add a listener on {@code System.EVENT_STATE_CHANGE} to update the widget.
	 * @param context the application context
	 */
	private void init(final Context context) {
        if (!initialized) {
            System system = System.getInstance(context);
            updateWidgetsCallback = new Callback() {
                @Override
                public void call(Object arg) {
                    
                    updateHandler.removeCallbacks(updateImage);
                    currentImage = 0;
                    switch ((ServerState) arg) {
                        case STATE_OFF:
                            updateWidgetImg(context, R.drawable.piratebox_off);
                            break;
                        case STATE_WAITING:
                            updateWidgetImg(context, R.drawable.piratebox_waiting);
                            break;
                        case STATE_SENDING:
                            updateHandler.postDelayed(updateImage, 100);
                            break;
                    }
                }
            };
            

            updateImage = new Runnable() {
               public void run() {
                   int id;
                   
                   switch (currentImage) {
                       case 0:
                           id = R.drawable.piratebox_sending1;
                           break;
                       case 1:
                           id = R.drawable.piratebox_sending2;
                           break;
                       default:
                           id = R.drawable.piratebox_sending3;
                           break;
                   }
                   updateWidgetImg(context, id);
                   
                   id = (id+1)%3;
                   
                   updateHandler.postDelayed(this, MS_BETWEEN_IMAGE_UPDATE);
               }
           };
            
            
            system.addEventListener(System.EVENT_STATE_CHANGE, updateWidgetsCallback);
            updateWidgetsCallback.call(system.getServerState());
            initialized = true;
        }
	}
	
	/**
	 * Sets the given image to the widgets.
	 * @param context the application context
	 * @param imgId the id of the image to set
	 */
	private void updateWidgetImg(Context context, int imgId) {
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
	    int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(context, P1R4T3B0XWidget.class));
        
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
        views.setImageViewResource(R.id.widgetimg, imgId);
        appWidgetManager.updateAppWidget(ids, views);
	}
}
