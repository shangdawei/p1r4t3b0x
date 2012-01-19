package com.piratebox;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.piratebox.server.Server;
import com.piratebox.server.Server.ServerState;

public class P1R4T3B0XActivity extends Activity {
	public static volatile Server server;

	private Button startBtn;
	private Button stopBtn;
	private Context ctx;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		ctx = this;
		
		startBtn = (Button) findViewById(R.id.startBtn);
		startBtn.setOnClickListener(startBtnListener);

		stopBtn = (Button) findViewById(R.id.stopBtn);
		stopBtn.setOnClickListener(stopBtnListener);
		
		setButtonsState();

		// Button settingsBtn = (Button)findViewById(R.id.settings);
		// settingsBtn.setOnClickListener(settingsBtnListener);
	}

	public static Server getServer() {
		return server;
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

			server = new Server(ctx);
			
			setButtonsState();
		}
	};

	private OnClickListener stopBtnListener = new OnClickListener() {
		public void onClick(View v) {
			server.shutdown();
			server = null;
		}
	};

	private void setButtonsState() {
		if (server == null || ServerState.STATE_OFF.equals(server.getServerState())) {
			startBtn.setEnabled(true);
			stopBtn.setEnabled(false);
		} else {
			startBtn.setEnabled(false);
			stopBtn.setEnabled(true);
		}
	}
	// private OnClickListener settingsBtnListener = new OnClickListener() {
	// public void onClick(View v) {
	// P1R4T3B0XActivity.this.startActivity(new Intent(P1R4T3B0XActivity.this,
	// SettingsActivity.class));
	// }
	// };
}
