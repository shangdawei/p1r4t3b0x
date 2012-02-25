/**
 * This is a file from P1R4T3B0X, a program that lets you share files with everyone.
 * Copyright (C) 2012 by Aylatan
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * The GNU General Public License can be found at http://www.gnu.org/licenses.
 */

package com.piratebox;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.piratebox.utils.StatUtils;

/**
 * This class describes the main {@link Activity} of the application.
 * From this {@link Activity}, the user can see the statistics and launch / stop the service.
 * This {@link Activity} listens to the {@link System#EVENT_STATE_CHANGE} and {@link System#EVENT_STATISTIC_UPDATE} events.
 * 
 * @author Aylatan
 */
public class P1R4T3B0XActivity extends Activity {
    private final String CRITTERCISM_APP_ID = "4f30546db093150ce40004a4";
    
	private System system;

	private Button startStopBtn;
	
	private Handler updateHandler = new Handler();

	/**
	 * Initialises the {@link Activity} and register the callbacks.
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// TODO uncomment
//		Crittercism.init(getApplicationContext(), CRITTERCISM_APP_ID);
		
		setContentView(R.layout.main);
		
		system = System.getInstance(this);
        
		startStopBtn = (Button) findViewById(R.id.startStopBtn);
		startStopBtn.setOnClickListener(startStopBtnListener);

        
        addCallbacks();
	}

	
	/**
	 * Adds the callbacks that will be used in this {@link Activity}.
	 */
	private void addCallbacks() {
	    //Initialises the task to be run to update the uptime display
        final Runnable updateUptimeTask = new Runnable() {
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

                TextView uptimeTxt = (TextView) findViewById(R.id.uptime_value);
                uptimeTxt.setText(timeStr.toString());
                
                updateHandler.postDelayed(this, 200);
            }
        };
        updateHandler.removeCallbacks(updateUptimeTask);
        
        Callback onStateChange = new Callback() { 
            @Override
            public void call(Object arg) {
                ServerState state = (ServerState) arg;
                //Update button status
                setButtonState();
                //Update status text
                TextView statusTxt = (TextView) findViewById(R.id.status_value);
                statusTxt.setText(getResources().getString(state.val()));
                
                //Set or reset the timer for the uptime update
                if (ServerState.STATE_OFF.equals(state)) {
                    updateHandler.removeCallbacks(updateUptimeTask);
                    TextView uptimeTxt = (TextView) findViewById(R.id.uptime_value);
                    uptimeTxt.setText(R.string.not_running);
                } else {
                    updateHandler.postDelayed(updateUptimeTask, 100);
                }
            }
        };
        system.addEventListener(System.EVENT_STATE_CHANGE, onStateChange);
        onStateChange.call(system.getServerState());
        

        Callback onUpdateStatistic = new Callback() { 
            @Override
            public void call(Object arg) {
                TextView filesDl = (TextView) findViewById(R.id.filesdl_value);
                TextView topDl1 = (TextView) findViewById(R.id.topdl1);
                TextView topDl2 = (TextView) findViewById(R.id.topdl2);
                TextView topDl3 = (TextView) findViewById(R.id.topdl3);
                TextView topDl4 = (TextView) findViewById(R.id.topdl4);
                TextView topDl5 = (TextView) findViewById(R.id.topdl5);

                SharedPreferences stats = getSharedPreferences(StatUtils.STATS_STORAGE, 0);
                int statFilesDl = stats.getInt(StatUtils.STAT_FILE_DL, 0);
                int statFilesDlSession = stats.getInt(StatUtils.STAT_FILE_DL_SESSION, 0);
                String[] topDls = StatUtils.getTopDls(P1R4T3B0XActivity.this);
                
                filesDl.setText(statFilesDl + " (" + statFilesDlSession + ")");
                topDl1.setText(topDls[0]);
                topDl2.setText(topDls[1]);
                topDl3.setText(topDls[2]);
                topDl4.setText(topDls[3]);
                topDl5.setText(topDls[4]);
            }
        };
        system.addEventListener(System.EVENT_STATISTIC_UPDATE, onUpdateStatistic);
        onUpdateStatistic.call(null);
	}
	
	/**
	 * Listener for the start/stop button.
	 * Switch the {@link System} state.
	 */
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
//
//	        if (ServerState.STATE_OFF.equals(system.getServerState())) {
//	            system.start();
//	        } else {
//	            system.stop();
//	        }
		}
	};

	/**
	 * Changes the start/stop button text depending on the {@link System} state.
	 */
	private void setButtonState() {
		if (ServerState.STATE_OFF.equals(system.getServerState())) {
			startStopBtn.setText(getResources().getString(R.string.start));
		} else {
            startStopBtn.setText(getResources().getString(R.string.stop));
		}
	}
	
	/**
	 * Initialises the option menu.
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.option_menu, menu);
	    return true;
	}
	
	/**
	 * Opens the {@link SettingsActivity} on click on the "Settings" menu item.
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
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
	
	/**
	 * Opens the {@link SettingsActivity}.
	 */
	private void openSettings() {
		startActivity(new Intent(this, SettingsActivity.class));
	}	
}
