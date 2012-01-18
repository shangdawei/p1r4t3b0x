package com.piratebox.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

import com.piratebox.R;

public class P1R4T3B0XWidget extends AppWidgetProvider {
	
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		
		final int N = appWidgetIds.length;
		// Perform this loop procedure for each App Widget that belongs to this
		// provider
		for (int i = 0; i < N; i++) {
			int appWidgetId = appWidgetIds[i];
			
			RemoteViews views = new RemoteViews(context.getPackageName(),
					R.layout.widget);
			
			String display = context.getString(R.string.widget_system_off);
			
			views.setTextViewText(R.id.widgetlabel, display);
			
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}
	
//	public void setButtonState(Boolean state) {
//		
//	}
}
