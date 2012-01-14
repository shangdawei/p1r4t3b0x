package com.piratebox;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.content.res.Resources;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.piratebox.server.Server;
import com.piratebox.server.ServerConfiguration;
import com.piratebox.wifiap.WifiApManager;

public class P1R4T3B0XActivity extends Activity {

	public static Server server;
	
	private static String iptables;
	
	private Button startBtn;
	private Button stopBtn;
	private WifiConfiguration config;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		config = new WifiConfiguration();
		config.SSID = ServerConfiguration.WIFI_AP_NAME;
		// config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);

		startBtn = (Button) findViewById(R.id.startBtn);
		startBtn.setOnClickListener(startBtnListener);

		stopBtn = (Button) findViewById(R.id.stopBtn);
		stopBtn.setOnClickListener(stopBtnListener);

		if (server == null) {
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
			if (server != null) {
				server.shutdown();
			}

			server = new Server();
			startRedirection();
			startHotspot();
			startBtn.setEnabled(false);
			stopBtn.setEnabled(true);
		}
	};

	private OnClickListener stopBtnListener = new OnClickListener() {

		public void onClick(View v) {
			if (server != null) {
				server.shutdown();
			}

			stopHotspot();
			stopRedirection();
			server = null;
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
			iptables = loadIptables();
			Runtime.getRuntime().exec(iptables + " --version");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void stopRedirection() {

	}

	private String loadIptables() throws IOException {
		InputStream is = Resources.getSystem().openRawResource(R.raw.iptables);
		File f = new File("/tmp/piratebox/iptables");
		f.createNewFile();
		
		OutputStream out = new FileOutputStream(f);

		byte[] buff = new byte[2048];
		while (true) {
			int read = is.read(buff, 0, 2048);
			if (read <= 0)
				break;
			out.write(buff, 0, read);
		}
		out.flush();
		out.close();
		is.close();
		
		return f.getAbsolutePath();
	}
	
	
	private void startHotspot() {
		new WifiApManager(this).setWifiApEnabled(config, true);
	}

	private void stopHotspot() {
		new WifiApManager(this).setWifiApEnabled(config, false);
	}
}
