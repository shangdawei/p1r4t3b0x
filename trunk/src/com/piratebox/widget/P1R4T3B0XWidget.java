package com.piratebox.widget;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

import com.piratebox.R;

public class P1R4T3B0XWidget extends AppWidgetProvider {
	DateFormat df = new SimpleDateFormat("hh:mm:ss");

	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		
		final int N = appWidgetIds.length;
		// Perform this loop procedure for each App Widget that belongs to this
		// provider
		for (int i = 0; i < N; i++) {
			int appWidgetId = appWidgetIds[i];
			
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget);
			
			// switch serverState, set text accordingly
			
			views.setTextViewText(R.id.widgetlabel, "");
			
			appWidgetManager.updateAppWidget(appWidgetId, views);
			//http://buildmobile.com/how-to-code-an-android-widget/
		}
	}
}
