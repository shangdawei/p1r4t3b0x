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

    public static final String WIDGET_RECEIVER_INIT = "WidgetReceiverInit";
    
	private static final String WIDGET_RECEIVER_CLICK = "WidgetReceiverClick";
	
	private boolean initialized = false;
	private Callback updateWidgetsCallback;
	
	@Override
	public void onUpdate(final Context context, final AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		
		final int N = appWidgetIds.length;
		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.widget);

        init(context);
        System system = System.getInstance(context);
        updateWidgetsCallback.call(system.getServerState());
		
		for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];
            Intent intent = new Intent(context, P1R4T3B0XWidget.class);
            intent.setAction(WIDGET_RECEIVER_CLICK);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

            views.setOnClickPendingIntent(R.id.widgetlabel, pendingIntent);

			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		if (WIDGET_RECEIVER_CLICK.equals(intent.getAction())) {
			System system = System.getInstance(context);
			if (ServerState.STATE_OFF.equals(system.getServerState())) {
				system.start();
			} else {
				system.stop();
			}
		} else if (WIDGET_RECEIVER_INIT.equals(intent.getAction())) {
		    init(context);
		}
	}

	private void init(final Context context) {
        if (!initialized) {
            final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            System system = System.getInstance(context);
            updateWidgetsCallback = new Callback() {
                @Override
                public void call(Object arg) {
                    int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(context, P1R4T3B0XWidget.class));

                    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);

                    views.setTextViewText(R.id.widgetlabel, context.getString(((ServerState) arg).val()));

                    appWidgetManager.updateAppWidget(ids, views);
                }
            };
            
            system.addStateChangeListener(updateWidgetsCallback);
            updateWidgetsCallback.call(system.getServerState());
            initialized = true;
        }
	}
}
