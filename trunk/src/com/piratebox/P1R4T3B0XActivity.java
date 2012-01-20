package com.piratebox;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.piratebox.System.ServerState;

public class P1R4T3B0XActivity extends Activity {
	private System system;

	private Button startBtn;
	private Button stopBtn;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		startBtn = (Button) findViewById(R.id.startBtn);
		startBtn.setOnClickListener(startBtnListener);

		stopBtn = (Button) findViewById(R.id.stopBtn);
		stopBtn.setOnClickListener(stopBtnListener);

		system = System.getInstance(this);
		// Button settingsBtn = (Button)findViewById(R.id.settings);
		// settingsBtn.setOnClickListener(settingsBtnListener);
	}
	
	@Override
	protected void onStart() {
		super.onStart();

		setButtonsState();
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

			system.start();
			
			setButtonsState();
		}
	};

	private OnClickListener stopBtnListener = new OnClickListener() {
		public void onClick(View v) {
			system.stop();
			
			setButtonsState();
		}
	};

	private void setButtonsState() {
		if (ServerState.STATE_OFF.equals(system.getServerState())) {
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
