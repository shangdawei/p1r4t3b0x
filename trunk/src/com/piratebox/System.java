package com.piratebox;

import java.util.ArrayList;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.os.Handler;
import android.os.Message;

import com.piratebox.server.Server;
import com.piratebox.server.ServerConfiguration;
import com.piratebox.utils.Callback;
import com.piratebox.utils.IptablesRunner;
import com.piratebox.wifiap.WifiApManager;

public class System {

    private WifiConfiguration config;
    private WifiConfiguration savedConfig;

    private ArrayList<Callback> listeners = new ArrayList<Callback>();
    
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

    private Server server;
    private Context ctx;
    private Handler handler;
    private IptablesRunner iptablesRunner;

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
        
        // TODO create folder if not exists
    }

    public void start() {
        iptablesRunner.setup();
        startHotspot();
        setServerState(ServerState.STATE_WAITING);
        
        server = new Server();
        server.addConnectedUsersListener(handler);
        server.start();
    }

    public void stop() {
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
        listeners.add(c);
    }
    
    public void removeStateChangeListener(Callback c) {
        listeners.remove(c);
    }
    
    private void callStateChangeListeners() {
        for (Callback c : listeners) {
            c.call(getServerState());
        }
    }
}
