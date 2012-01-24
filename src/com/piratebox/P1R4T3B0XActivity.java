package com.piratebox;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.piratebox.System.ServerState;
import com.piratebox.utils.Callback;

public class P1R4T3B0XActivity extends Activity {
	private System system;

	private Button startStopBtn;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		startStopBtn = (Button) findViewById(R.id.startStopBtn);
		startStopBtn.setOnClickListener(startStopBtnListener);

		system = System.getInstance(this);
		system.addStateChangedListener(new Callback() { 
            @Override
            public void call(Object arg) {
                setButtonState();
            }
        });
	}
	
	private OnClickListener startStopBtnListener = new OnClickListener() {
		public void onClick(View v) {
			try {
				Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
				ArrayList<String> interfaceNames = new ArrayList<String>();
				while (interfaces.hasMoreElements()) {
					interfaceNames.add(interfaces.nextElement().getName());
				}
				Toast.makeText(getBaseContext(), interfaceNames.toString(), Toast.LENGTH_LONG).show();
			} catch (SocketException e) {
				Log.e(this.getClass().getName(), e.toString());
			}

	        if (ServerState.STATE_OFF.equals(system.getServerState())) {
	            system.start();
	        } else {
	            system.stop();
	        }
		}
	};

	private void setButtonState() {
		if (ServerState.STATE_OFF.equals(system.getServerState())) {
			startStopBtn.setText(getResources().getString(R.string.start));
		} else {
            startStopBtn.setText(getResources().getString(R.string.stop));
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.option_menu, menu);
	    return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
		    case R.id.settings:
		        openSettings();
		        return true;
		    default:
		        return super.onOptionsItemSelected(item);
	    }
	}
	
	private void openSettings() {
		startActivity(new Intent(this, SettingsActivity.class));
	}
}
