package com.piratebox.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.piratebox.R;
import com.piratebox.System;
import com.piratebox.System.ServerState;
import com.piratebox.utils.Callback;

public class P1R4T3B0XWidget extends AppWidgetProvider {

	private static final String ACTION_WIDGET_RECEIVER = "ActionReceiverWidget";
	private static boolean addedListenner = false;
	
	@Override
	public void onUpdate(final Context context, final AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		
		final int N = appWidgetIds.length;
		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.widget);
		
		if (!addedListenner) {
	        System system = System.getInstance(context);
    		system.addStateChangedListener(new Callback() {
                @Override
                public void call(Object arg) {
                    ServerState state = (ServerState) arg;
                    int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(context, P1R4T3B0XWidget.class));
                    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
                    views.setTextViewText(R.id.widgetlabel, context.getString(state.val()));
                    appWidgetManager.updateAppWidget(ids, views);
                }
            });
		}
		
		for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];
            Intent intent = new Intent(context, P1R4T3B0XWidget.class);
            intent.setAction(ACTION_WIDGET_RECEIVER);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

            views.setOnClickPendingIntent(R.id.widgetlabel, pendingIntent);

			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		if (ACTION_WIDGET_RECEIVER.equals(intent.getAction())) {
			System system = System.getInstance(context);
			if (ServerState.STATE_OFF.equals(system.getServerState())) {
				system.start();
			} else {
				system.stop();
			}
		}
	}

	// public void setButtonState(Boolean state) {
	//
	// }
}
