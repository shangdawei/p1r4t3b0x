package com.piratebox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.piratebox.server.Server;
import com.piratebox.server.ServerConfiguration;
import com.piratebox.utils.Callback;
import com.piratebox.widget.P1R4T3B0XWidget;
import com.piratebox.wifiap.WifiApManager;

public class System {
	public static final String TEMP_SCRIPT = "tmp_script";
	public static final String IPTABLES = "iptables";
	public static final String PREF_SETTINGS = "settings";

	private WifiConfiguration config;
	private WifiConfiguration savedConfig;
	
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
	private ServerState state;
	
	private Server server;
	private Context ctx;
	private Callback callback;
	
	private static System instance = null;
	public static System getInstance(Context ctx) {
		if (instance == null) {
			instance = new System(ctx);
		}
		return instance;
	}
	
	private System(Context ctx) {
		this.ctx = ctx;
		
		config = new WifiConfiguration();
		config.SSID = ServerConfiguration.WIFI_AP_NAME;
		
		state = ServerState.STATE_OFF;
		
		callback = new Callback() {
			@Override
			public void call(Object arg) {
				if ((Integer) arg <= 0) {
					state = ServerState.STATE_WAITING;
				} else {
					state = ServerState.STATE_SENDING;
				}
				
				updateWidgets();
			}
		};
		
		//TODO create folder if not exists
	}
	
	public void start() {
		startRedirection();
		startHotspot();
		state = ServerState.STATE_WAITING;
		updateWidgets();
		
		server = new Server();
		server.addConnectedUsersListener(callback);
		server.start();
	}
	
	public void stop() {

		stopHotspot();
		stopRedirection();
		state = ServerState.STATE_OFF;
		updateWidgets();
		server.stopRun();
		server = null;
	}
	
	public void updateWidgets() {
		AppWidgetManager mgr = AppWidgetManager.getInstance(ctx);
		int[] ids = mgr.getAppWidgetIds(new ComponentName(ctx, P1R4T3B0XWidget.class));
		
		RemoteViews views = new RemoteViews(ctx.getPackageName(),
				R.layout.widget);

		views.setTextViewText(R.id.widgetlabel, ctx.getString(state.val()));
		
		mgr.updateAppWidget(ids, views);
	}

	private void startRedirection() {
		try {
			String iptables = loadIptables();
			StringBuilder script = new StringBuilder();
			script.append(iptables).append(" --version\n");
			// TODO Add lines !!!!

			int res = runScript(script.toString());
			if (res != 1) {
				Toast.makeText(ctx, R.string.error_redirect, Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			Log.e(this.getClass().getName(), e.toString());
		}
	}

	private void stopRedirection() {
		try {
			String iptables = loadIptables();
			StringBuilder script = new StringBuilder();
			script.append(iptables).append(" --version\n");

			// TODO Add lines !!!!

			int res = runScript(script.toString());
			if (res != 1) {
				Toast.makeText(ctx, R.string.error_redirect, Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			Log.e(this.getClass().getName(), e.toString());
		}
	}

	private String loadIptables() throws IOException {
		File tmpFolder = ctx.getDir("tmp", Context.MODE_PRIVATE);

		File f = new File(tmpFolder, IPTABLES);
		f.setExecutable(true);
		f.deleteOnExit();

		InputStream is = ctx.getResources().openRawResource(R.raw.iptables);
		byte[] buff = new byte[is.available()];
		is.read(buff);
		is.close();

		FileOutputStream out = new FileOutputStream(f);
		out.write(buff);
		out.close();
		return f.getAbsolutePath();
	}

	private int runScript(String script) throws IOException,
			InterruptedException {

		File tmpFolder = ctx.getDir("tmp", Context.MODE_PRIVATE);

		File f = new File(tmpFolder, TEMP_SCRIPT);
		f.setExecutable(true);
		f.deleteOnExit();

		// Write the script to be executed
		PrintWriter out = new PrintWriter(new FileOutputStream(f));
		if (new File("/system/bin/sh").exists()) {
			out.write("#!/system/bin/sh\n");
		}
		out.write(script);
		if (!script.endsWith("\n")) {
			out.write("\n");
		}
		out.write("exit\n");
		out.flush();
		out.close();
		Process exec = Runtime.getRuntime()
				.exec("su -c " + f.getAbsolutePath());
		return exec.waitFor();
	}

	private void startHotspot() {
		WifiApManager mgr = new WifiApManager(ctx);
		savedConfig = mgr.getWifiApConfiguration();
		mgr.setWifiApEnabled(config, true);
	}

	private void stopHotspot() {
		WifiApManager mgr = new WifiApManager(ctx);
		mgr.setWifiApEnabled(config, false);
		mgr.setWifiApConfiguration(savedConfig);
	}

	public ServerState getServerState() {
		return state;
	}
}
