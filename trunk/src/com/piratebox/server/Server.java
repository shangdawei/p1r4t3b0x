package com.piratebox.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;
import android.widget.RemoteViews;

import com.piratebox.R;
import com.piratebox.widget.P1R4T3B0XWidget;

public class Server extends Thread {
	
	public static enum ServerState {
		STATE_OFF(R.string.widget_system_off),
		STATE_WAITING(R.string.widget_system_waiting),
		STATE_SENDING(R.string.widget_system_sending);
		
		private final int value;
		private ServerState(int value) {
			this.value = value;
		}
		public int val() {
			return value;
		}
	}
	
	private ServerSocket listenSocket;
	private ArrayList<Connection> connections = new ArrayList<Connection>(); 
	private ServerState state;
	private int connectedUsers = 0;
	private Context ctx;
	
	public Server(Context ctx) {
		this.ctx = ctx;
		
		try {
			listenSocket = new ServerSocket(ServerConfiguration.PORT);
		} catch (IOException e) {
			Log.e(this.getClass().getName(), e.toString());
		}
		
		state = ServerState.STATE_OFF;
		updateWidgets();
	}
	
	public void run() {
		try{
			removeConnectedUser();
			while(true)
			{
				Socket clientSocket = listenSocket.accept();
                connections.add(new Connection (clientSocket, this));
			}
		}
		catch(IOException e) {
			Log.e(this.getClass().getName(), e.toString());
		}
	}
	
	public void shutdown() {
		try {
			for (Connection conn : connections) {
				conn.stop();
			}
			listenSocket.close();
		} catch (IOException e) {
			Log.e(this.getClass().getName(), e.toString());
		}
		
		state = ServerState.STATE_OFF;
		updateWidgets();
		
		stop();
	}
	
	public ServerState getServerState() {
		return state;
	}
	
	public void addConnectedUser() {
		connectedUsers++;
		onConnectedUsersChanged();
	}
	
	public void removeConnectedUser() {
		connectedUsers--;
		onConnectedUsersChanged();
	}
	
	private void onConnectedUsersChanged() {
		if (connectedUsers <= 0) {
			connectedUsers = 0;
			state = ServerState.STATE_WAITING;
		} else {
			state = ServerState.STATE_SENDING;
		}
		
		updateWidgets();
	}
	
	private void updateWidgets() {
		AppWidgetManager mgr = AppWidgetManager.getInstance(ctx);
		int[] ids = mgr.getAppWidgetIds(new ComponentName(ctx, P1R4T3B0XWidget.class));
		
		RemoteViews views = new RemoteViews(ctx.getPackageName(),
				R.layout.widget);

		views.setTextViewText(R.id.widgetlabel, ctx.getString(state.val()));
		
		mgr.updateAppWidget(ids, views);
	}
}
