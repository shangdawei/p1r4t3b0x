package com.piratebox;

import java.util.ArrayList;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.piratebox.server.Server;
import com.piratebox.server.ServerConfiguration;
import com.piratebox.utils.Callback;
import com.piratebox.utils.IptablesRunner;
import com.piratebox.widget.P1R4T3B0XWidget;
import com.piratebox.wifiap.WifiApManager;

public class System {


    private ArrayList<Callback> stateChangeListeners = new ArrayList<Callback>();
    
    public static enum ServerState {
        STATE_OFF(R.string.widget_system_off),
        STATE_WAITING(R.string.widget_system_waiting),
        STATE_SENDING(R.string.widget_system_sending);

        private final int value;

        private ServerState(int value) {
            this.value = value;
        }

        public int val() {
            return value;
        }
    }

    private ServerState state;
    private WifiConfiguration config;
    private WifiConfiguration savedConfig;
    private Server server;
    private Context ctx;
    private Handler handler;
    private IptablesRunner iptablesRunner;
    private long startTime = 0L;

    private static System instance = null;

    public static System getInstance(Context ctx) {
        if (instance == null) {
            instance = new System(ctx);
        }
        return instance;
    }

    private System(Context ctx) {
        this.ctx = ctx;
        
        iptablesRunner = new IptablesRunner(ctx);

        config = new WifiConfiguration();
        config.SSID = ServerConfiguration.WIFI_AP_NAME;

        setServerState(ServerState.STATE_OFF);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if ((Integer) msg.obj <= 0) {
                    setServerState(ServerState.STATE_WAITING);
                } else {
                    setServerState(ServerState.STATE_SENDING);
                }
            }
        };

        Intent intent = new Intent(ctx, P1R4T3B0XWidget.class);
        intent.setAction(P1R4T3B0XWidget.WIDGET_RECEIVER_INIT);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, 0, intent, 0);
        try {
            pendingIntent.send();
        } catch (CanceledException e) {
            Log.e(this.getClass().getName(), e.toString());
        }
        
        // TODO create folder if not exists
    }

    public void start() {
        iptablesRunner.setup();
        startHotspot();
        setServerState(ServerState.STATE_WAITING);
        
        server = new Server();
        server.addConnectedUsersListener(handler);
        server.start();
        startTime = java.lang.System.currentTimeMillis();
    }

    public void stop() {
        startTime = 0L;
        
        stopHotspot();
        iptablesRunner.teardown();
        setServerState(ServerState.STATE_OFF);
        server.stopRun();
        server = null;
    }
    public void setServerState(ServerState state) {
        this.state = state;
        callStateChangeListeners();
    }

    public ServerState getServerState() {
        return state;
    }
    
    public long getStartTime() {
        return startTime;
    }

    private void startHotspot() {
        WifiApManager mgr = new WifiApManager(ctx);
        savedConfig = mgr.getWifiApConfiguration();
        mgr.setWifiApEnabled(config, true);
    }

    private void stopHotspot() {
        WifiApManager mgr = new WifiApManager(ctx);
        mgr.setWifiApEnabled(config, false);
        mgr.setWifiApConfiguration(savedConfig);
    }

    public void addStateChangeListener(Callback c) {
        stateChangeListeners.add(c);
    }
    
    public void removeStateChangeListener(Callback c) {
        stateChangeListeners.remove(c);
    }
    
    private void callStateChangeListeners() {
        for (Callback c : stateChangeListeners) {
            c.call(getServerState());
        }
    }
}
