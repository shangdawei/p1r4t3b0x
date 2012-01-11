package com.piratebox;

import com.piratebox.server.Server;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class P1R4T3B0XActivity extends Activity {
	
	Server server;
	Button startBtn;
	Button stopBtn;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        startBtn = (Button)findViewById(R.id.startBtn);
        startBtn.setOnClickListener(startBtnListener);
        
        stopBtn = (Button)findViewById(R.id.stopBtn);
        stopBtn.setOnClickListener(stopBtnListener);
        stopBtn.setEnabled(false);
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
}