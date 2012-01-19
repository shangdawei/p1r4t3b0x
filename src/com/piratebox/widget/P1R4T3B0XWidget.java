package com.piratebox.widget;

import com.piratebox.P1R4T3B0XActivity;
import com.piratebox.R;
import com.piratebox.server.Server;
import com.piratebox.server.Server.ServerState;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class P1R4T3B0XWidget extends AppWidgetProvider {

	private static final String ACTION_WIDGET_RECEIVER = "ActionReceiverWidget";
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		
		Server server = P1R4T3B0XActivity.getServer();
		final int N = appWidgetIds.length;
		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.widget);
		
		if (server != null) {
			server.updateWidgets();
		} else {
			String display = context.getString(R.string.widget_system_off);
			views.setTextViewText(R.id.widgetlabel, display);
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
			Server server = P1R4T3B0XActivity.getServer();
			if (server == null || ServerState.STATE_OFF.equals(server.getServerState())) {
				server = new Server(context);
			} else {
				server.shutdown();
				server = null;
			}
		}
	}

	// public void setButtonState(Boolean state) {
	//
	// }
}
