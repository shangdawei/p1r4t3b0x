package com.piratebox;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;

import com.piratebox.server.Server;
import com.piratebox.server.ServerConfiguration;
import com.piratebox.utils.Callback;
import com.piratebox.utils.IptablesRunner;
import com.piratebox.utils.PreferencesKeys;
import com.piratebox.utils.StatUtils;
import com.piratebox.widget.P1R4T3B0XWidget;
import com.piratebox.wifiap.WifiApManager;

public class System {

    public static final String EVENT_STATE_CHANGE = "eventStateChange";
    public static final String EVENT_STATISTIC_UPDATE = "eventStatisticUpdate";
    public static final int NOTIFICATION_ID_NETWORK = 1;

    private HashMap<String, ArrayList<Callback>> listeners = new HashMap<String, ArrayList<Callback>>();
    
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
    private Handler connectedUsersHandler;
    private Handler addStatHandler;
    private IptablesRunner iptablesRunner;
    private long startTime = 0L;
    private final Handler scanHandler = new Handler();
    private final int period;
    private Runnable scanTask;

    private static System instance = null;

    public static System getInstance(Context ctx) {
        if (instance == null) {
            instance = new System(ctx);
        }
        return instance;
    }
    
    private System(final Context ctx) {
        this.ctx = ctx;
        
        iptablesRunner = new IptablesRunner(ctx);

        config = new WifiConfiguration();
        config.SSID = ServerConfiguration.WIFI_AP_NAME;

        setServerState(ServerState.STATE_OFF);

        connectedUsersHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if ((Integer) msg.obj <= 0) {
                    setServerState(ServerState.STATE_WAITING);
                } else {
                    setServerState(ServerState.STATE_SENDING);
                }
            }
        };
        addStatHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                StatUtils.addStatForFile(ctx, (File) msg.obj);
                dispatchEvent(EVENT_STATISTIC_UPDATE);
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
        
        try {
            new File(ServerConfiguration.getRootDir()).createNewFile();
        } catch (IOException e) {
            Log.e(this.getClass().getName(), e.toString());
        }

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        period = 1000 * Integer.parseInt(settings.getString(PreferencesKeys.NOTIFICATION_FREQUENCY, PreferencesKeys.NOTIFICATION_DEFAULT_FREQUENCY));
        
        initScan();
        
        if (settings.getBoolean(PreferencesKeys.NOTIFICATION, false)) {
            scanHandler.postDelayed(scanTask, period);
        }
    }

    public void start() {
        iptablesRunner.setup();
        startHotspot();
        setServerState(ServerState.STATE_WAITING);
        
        server = new Server();
        server.setConnectedUsersHandler(connectedUsersHandler);
        server.setAddStatHandler(addStatHandler);
        server.start();
        startTime = java.lang.System.currentTimeMillis();
        StatUtils.resetStat(ctx, StatUtils.STAT_FILE_DL_SESSION);
        dispatchEvent(EVENT_STATISTIC_UPDATE);
    }

    public void stop() {
        StatUtils.resetStat(ctx, StatUtils.STAT_FILE_DL_SESSION);
        dispatchEvent(EVENT_STATISTIC_UPDATE);
        startTime = 0L;
        
        stopHotspot();
        iptablesRunner.teardown();
        setServerState(ServerState.STATE_OFF);
        server.stopRun();
        server = null;
    }
    public void setServerState(ServerState state) {
        this.state = state;
        dispatchEvent(EVENT_STATE_CHANGE, getServerState());
    }

    public ServerState getServerState() {
        return state;
    }
    
    public long getStartTime() {
        return startTime;
    }
    
    public void resetAllStats() {
        StatUtils.resetAllStats(ctx);
        dispatchEvent(EVENT_STATISTIC_UPDATE);
    }
    
    public void setNotificationState(boolean enabled) {
        if (enabled) {
            scanHandler.postDelayed(scanTask, period);
        } else {
            scanHandler.removeCallbacks(scanTask);
        }
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
    
    private void initScan() {
        
        scanTask = new Runnable() {
            public void run() {
                if (ServerState.STATE_SENDING.equals(System.this.getServerState())) {
                    scanHandler.postDelayed(this, period);
                    return;
                }
                
                final WifiManager mgr = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
                final WifiApManager apMgr = new WifiApManager(ctx);
                
                final boolean wifiApEnabled = apMgr.isWifiApEnabled();
                
                apMgr.setWifiApEnabled(config, false);
                ctx.registerReceiver(new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context c, Intent i){
                        apMgr.setWifiApEnabled(config, wifiApEnabled);
                        
                        for (ScanResult scan : mgr.getScanResults()) {
                            if (ServerConfiguration.WIFI_AP_NAME.equals(scan.SSID)) {
                                addNetworkNotification();
                            } else {
                                removeNetworkNotification();
                            }
                        }
                        
                        scanHandler.postDelayed(System.this.scanTask, period);
                    }
                }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                
                if (! mgr.startScan()) {
                    apMgr.setWifiApEnabled(config, wifiApEnabled);
                    scanHandler.postDelayed(this, period);
                }
            }
        };
        scanHandler.removeCallbacks(scanTask);
    }
    
    private void addNetworkNotification() {
        Notification notification = new Notification(R.drawable.main_ico,
                ctx.getResources().getString(R.string.notification_network),
                java.lang.System.currentTimeMillis());
        
        notification.setLatestEventInfo(ctx,
                ctx.getResources().getString(R.string.notification_network),
                "", null);
        
        NotificationManager mgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        mgr.notify(NOTIFICATION_ID_NETWORK, notification);
    }
    
    private void removeNetworkNotification() {
        NotificationManager mgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        mgr.cancel(NOTIFICATION_ID_NETWORK);
    }

    
    
    public void addEventListener(String event, Callback c) {
        if (listeners.get(event) == null) {
            listeners.put(event, new ArrayList<Callback>());
        }
        listeners.get(event).add(c);
    }
    
    public void removeEventListener(String event, Callback c) {
        if (listeners.get(event) == null) {
            listeners.put(event, new ArrayList<Callback>());
        }
        listeners.get(event).remove(c);
    }
    
    private void dispatchEvent(String event, Object value) {
        if (listeners.get(event) == null) {
            return;
        }
        
        for (Callback c : listeners.get(event)) {
            c.call(value);
        }
    }
    
    private void dispatchEvent(String event) {
        dispatchEvent(event, null);
    }
}
