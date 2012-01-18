package com.piratebox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import android.app.Activity;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.piratebox.server.Server;
import com.piratebox.server.ServerConfiguration;
import com.piratebox.server.Server.ServerState;
import com.piratebox.wifiap.WifiApManager;

public class P1R4T3B0XActivity extends Activity {

	public static final String TEMP_SCRIPT = "tmp_script";
	public static final String IPTABLES = "iptables";

	public static Server server;

	private Button startBtn;
	private Button stopBtn;
	private WifiConfiguration config;
	private WifiConfiguration savedConfig;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		server = new Server(this);

		config = new WifiConfiguration();
		config.SSID = ServerConfiguration.WIFI_AP_NAME;

		startBtn = (Button) findViewById(R.id.startBtn);
		startBtn.setOnClickListener(startBtnListener);

		stopBtn = (Button) findViewById(R.id.stopBtn);
		stopBtn.setOnClickListener(stopBtnListener);

		if (ServerState.STATE_OFF.equals(server.getServerState())) {
			startBtn.setEnabled(true);
			stopBtn.setEnabled(false);
		} else {
			startBtn.setEnabled(false);
			stopBtn.setEnabled(true);
		}

		// Button settingsBtn = (Button)findViewById(R.id.settings);
		// settingsBtn.setOnClickListener(settingsBtnListener);
	}

	private OnClickListener startBtnListener = new OnClickListener() {

		public void onClick(View v) {
			try {
				Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
				ArrayList<String> interfaceNames = new ArrayList<String>();
				while (interfaces.hasMoreElements()) {
					interfaceNames.add(interfaces.nextElement().getName());
				}
				Toast.makeText(getBaseContext(), interfaceNames.toString(), Toast.LENGTH_LONG).show();
			} catch (SocketException e) {
				e.printStackTrace();
			}

			server.start();
			startRedirection();
			startHotspot();
			startBtn.setEnabled(false);
			stopBtn.setEnabled(true);
		}
	};

	private OnClickListener stopBtnListener = new OnClickListener() {

		public void onClick(View v) {
			server.shutdown();
			stopHotspot();
			stopRedirection();
			startBtn.setEnabled(true);
			stopBtn.setEnabled(false);
		}
	};

	// private OnClickListener settingsBtnListener = new OnClickListener() {
	// public void onClick(View v) {
	// P1R4T3B0XActivity.this.startActivity(new Intent(P1R4T3B0XActivity.this,
	// SettingsActivity.class));
	// }
	// };

	private void startRedirection() {
		try {
			String iptables = loadIptables();
			StringBuilder script = new StringBuilder();
			script.append(iptables).append(" --version\n");
			// TODO Add lines !!!!

			int res = runScript(script.toString());
			if (res != 1) {
				Toast.makeText(getBaseContext(), R.string.error_redirect, Toast.LENGTH_LONG).show();
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
				Toast.makeText(getBaseContext(), R.string.error_redirect, Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			Log.e(this.getClass().getName(), e.toString());
		}
	}

	private String loadIptables() throws IOException {
		File tmpFolder = getDir("tmp", MODE_PRIVATE);

		File f = new File(tmpFolder, IPTABLES);
		f.setExecutable(true);
		f.deleteOnExit();

		InputStream is = getResources().openRawResource(R.raw.iptables);
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

		File tmpFolder = getDir("tmp", MODE_PRIVATE);

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
		WifiApManager mgr = new WifiApManager(this);
		savedConfig = mgr.getWifiApConfiguration();
		mgr.setWifiApEnabled(config, true);
	}

	private void stopHotspot() {
		WifiApManager mgr = new WifiApManager(this);
		mgr.setWifiApEnabled(config, false);
		mgr.setWifiApConfiguration(savedConfig);
	}
}
