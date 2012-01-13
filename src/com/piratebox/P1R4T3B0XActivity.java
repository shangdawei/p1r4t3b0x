package com.piratebox;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.piratebox.server.Server;

public class P1R4T3B0XActivity extends Activity {
	
	public static Server server;
	private Button startBtn;
	private Button stopBtn;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        startBtn = (Button)findViewById(R.id.startBtn);
        startBtn.setOnClickListener(startBtnListener);
        
        stopBtn = (Button)findViewById(R.id.stopBtn);
        stopBtn.setOnClickListener(stopBtnListener);
        
        if (server == null) {
        	startBtn.setEnabled(true);
        	stopBtn.setEnabled(false);
        } else {
        	startBtn.setEnabled(false);
        	stopBtn.setEnabled(true);
        }
        
//        Button settingsBtn = (Button)findViewById(R.id.settings);
//        settingsBtn.setOnClickListener(settingsBtnListener);
    }
 
    private OnClickListener startBtnListener = new OnClickListener() {
		
        public void onClick(View v) {
        	if (server != null) {
        		server.shutdown();
        	}
        	
        	server = new Server();
        	startBtn.setEnabled(false);
        	stopBtn.setEnabled(true);
        }
    };
 
    private OnClickListener stopBtnListener = new OnClickListener() {
		
        public void onClick(View v) {
        	if (server != null) {
        		server.shutdown();
        	}
        	
        	server = null;
        	startBtn.setEnabled(true);
        	stopBtn.setEnabled(false);
        }
    };
 
//    private OnClickListener settingsBtnListener = new OnClickListener() {
//        public void onClick(View v) {
//            P1R4T3B0XActivity.this.startActivity(new Intent(P1R4T3B0XActivity.this, SettingsActivity.class));
//        }
//    };
}