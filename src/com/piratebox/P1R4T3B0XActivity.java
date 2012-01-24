package com.piratebox;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.piratebox.System.ServerState;
import com.piratebox.utils.Callback;

public class P1R4T3B0XActivity extends Activity {
	private System system;

	private Button startStopBtn;
	
	private Handler updateHandler = new Handler();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		
		startStopBtn = (Button) findViewById(R.id.startStopBtn);
		startStopBtn.setOnClickListener(startStopBtnListener);

		system = System.getInstance(this);
		Callback callback = new Callback() { 
            @Override
            public void call(Object arg) {
                setButtonState();
                TextView statusTxt = (TextView) findViewById(R.id.status_value);
                statusTxt.setText(getResources().getString(((ServerState) arg).val()));
            }
        };
        system.addStateChangeListener(callback);
        callback.call(system.getServerState());

        //TODO uptime should be calculated only when service is online
        Runnable updateUptimeTask = new Runnable() {
            public void run() {
                
                long milis = java.lang.System.currentTimeMillis() - system.getStartTime();
                int sec = (int)((milis / 1000) % 60);
                int min = (int)((milis / 1000 / 60) % 60);
                int hour = (int)((milis / 1000 / 60 / 60) % 24);
                int day = (int)(milis / 1000 / 60 / 60 / 24);
                StringBuilder timeStr = new StringBuilder();
                timeStr.append(day).append("d")
                .append(hour).append("h")
                .append(min).append("m")
                .append(sec).append("s");

                TextView statusTxt = (TextView) findViewById(R.id.uptime_value);
                statusTxt.setText(timeStr.toString());
                
                updateHandler.postDelayed(this, 200);
            }
        };

        updateHandler.removeCallbacks(updateUptimeTask);
        updateHandler.postDelayed(updateUptimeTask, 100);

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
