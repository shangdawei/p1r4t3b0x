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

public class P1R4T3B0XActivity extends Activity {
	private System system;

	private Button startStopBtn;
	
	private Handler updateHandler = new Handler();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.main);
		
		system = System.getInstance(this);
        
		startStopBtn = (Button) findViewById(R.id.startStopBtn);
		startStopBtn.setOnClickListener(startStopBtnListener);

        
        addCallbacks();
	}
	
	private void addCallbacks() {
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
                setButtonState();
                TextView statusTxt = (TextView) findViewById(R.id.status_value);
                statusTxt.setText(getResources().getString(state.val()));
                
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
