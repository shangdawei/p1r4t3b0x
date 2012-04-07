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

import java.io.File;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
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
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;

import com.piratebox.server.Server;
import com.piratebox.server.ServerConfiguration;
import com.piratebox.utils.Callback;
import com.piratebox.utils.ExceptionHandler;
import com.piratebox.utils.IptablesRunner;
import com.piratebox.utils.PreferencesKeys;
import com.piratebox.utils.StatUtils;
import com.piratebox.widget.P1R4T3B0XWidget;
import com.piratebox.wifiap.WifiApManager;

/**
 * This class describes the main system of the service.
 * It manages the server, the redirections, the notifications and dispatches events.
 * 
 * @author Aylatan
 */
public class System /*extends Service*/ {

    /**
     * Event dispatched when the state of the server changes.
     * The {@link Callback} listener will be passed the new {@link ServerState} as argument.
     */
    public static final String EVENT_STATE_CHANGE = "eventStateChange";
    /**
     * Event dispatched when the statistics change.
     * The {@link Callback} listener will be passed null.
     * The statistics can be accessed by the {@link StatUtils} class.
     */
    public static final String EVENT_STATISTIC_UPDATE = "eventStatisticUpdate";
    /**
     * The id of the "Network found" notification.
     */
    public static final int NOTIFICATION_ID_NETWORK = 1;
    
    /**
     * The prefix of the wifi access point interface name.
     */
    public static final String WIFI_AP_INTERFACE_NAME_PREFIX = "wl0";

    private HashMap<String, ArrayList<Callback>> listeners = new HashMap<String, ArrayList<Callback>>();
    
    /**
     * The different states the server can have.
     * Calling {@code val()} on the {@link ServerState} will return the id of the human readable state.
     */
    public static enum ServerState {
        STATE_OFF(R.string.system_state_off),
        STATE_WAITING(R.string.system_state_waiting),
        STATE_SENDING(R.string.system_state_sending);

        private final int value;

        private ServerState(int value) {
            this.value = value;
        }

        /**
         * Returns the id of the string of the human readable value for this {@link ServerState}.
         * @return a string id
         */
        public int val() {
            return value;
        }
    }

    private ServerState state;
    private WifiConfiguration config;
    private Server server;
    private Context ctx;
    private Handler connectedUsersHandler;
    private Handler addStatHandler;
    private IptablesRunner iptablesRunner;
    private long startTime = 0L;
    private final Handler scanHandler = new Handler();
    private Runnable scanTask;
    private BroadcastReceiver scanResultReceiver = null;

    private static System instance = null;

    /**
     * {@link System} is a singleton and should always be accessed by this method.
     * @param ctx the context in which the system will run
     * @return the unique {@link System} instance for the application
     */
    public static System getInstance(Context ctx) {
        if (instance == null) {
            instance = new System(ctx);
        }
        return instance;
    }
    
    /**
     * Returns the {@link Context} instance with which {@link System} was initialised.
     * @return A {@link Context} instance if {@link System} has been initialised, {@code null} otherwise.
     */
    public static Context getContext() {
        if (instance == null) {
            return null;
        }
        return instance.ctx;
    }
    
    /**
     * Private constructor.
     * {@link System} is a singleton and should always be accessed by {@code getInstance(Context ctx)} method.
     * @param ctx
     */
    private System(final Context ctx) {
        this.ctx = ctx;
        
        iptablesRunner = new IptablesRunner(ctx);

        //Sets the specific wifi access point configuration
        config = new WifiConfiguration();
        config.SSID = ServerConfiguration.WIFI_AP_NAME;

        setServerState(ServerState.STATE_OFF);

        //Set handlers so that child thread can send messages to this instance
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

        //Initialises the widgets
        Intent intent = new Intent(ctx, P1R4T3B0XWidget.class);
        intent.setAction(P1R4T3B0XWidget.WIDGET_RECEIVER_INIT);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, 0, intent, 0);
        try {
            pendingIntent.send();
        } catch (CanceledException e) {
            ExceptionHandler.handle(this, e);
        }
        
        //Sets the root directory to the preference defined value
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        ServerConfiguration.setRootDir(settings.getString(PreferencesKeys.SELECT_DIR, ServerConfiguration.DEFAULT_ROOT_DIR));

        initScan();
    }

    /**
     * Starts the system.
     * Sets the redirection, create and run a new {@link Server}, starts the hotspot, resets the stats for the session.
     */
    public void start() {
        setServerState(ServerState.STATE_WAITING);
        
        server = new Server();
        server.setConnectedUsersHandler(connectedUsersHandler);
        server.setAddStatHandler(addStatHandler);
        server.start();
        
        WifiApStateReceiver.setOnChangeCallback(new Callback() {
            
            @Override
            public void call(Object arg) {
                int state = ((Intent)arg).getIntExtra(WifiApManager.EXTRA_WIFI_AP_STATE, -1);
                if (state == WifiApManager.WIFI_AP_STATE_ENABLED) {
                    try {
                        iptablesRunner.setup(getWlanInterfaceName());
                    } catch (Exception e) {
                        ExceptionHandler.handle(this, R.string.error_script_loading, ctx.getApplicationContext());
                    }
                }
            }
        });
        
        startHotspot();
        
        startTime = java.lang.System.currentTimeMillis();
        StatUtils.resetStat(ctx, StatUtils.STAT_FILE_DL_SESSION);
        dispatchEvent(EVENT_STATISTIC_UPDATE);

//        ctx.startService(new Intent(ctx, System.class));
    }

    /**
     * Stops the system.
     * Resets the stats for the session, stops the hotspot, destroy the {@link Server} instance, resets the redirection.
     */
    public void stop() {
        StatUtils.resetStat(ctx, StatUtils.STAT_FILE_DL_SESSION);
        dispatchEvent(EVENT_STATISTIC_UPDATE);
        startTime = 0L;
        
        WifiApStateReceiver.setOnChangeCallback(new Callback() {
            
            @Override
            public void call(Object arg) {
                int state = ((Intent)arg).getIntExtra(WifiApManager.EXTRA_WIFI_AP_STATE, -1);
                if (state == WifiApManager.WIFI_AP_STATE_DISABLING) {
                    try {
                        iptablesRunner.teardown(getWlanInterfaceName());
                        WifiApStateReceiver.setOnChangeCallback(null);
                    } catch (Exception e) {
                        ExceptionHandler.handle(this, R.string.error_script_loading, ctx.getApplicationContext());
                    }
                }
            }
        });
        
        stopHotspot();
        
        setServerState(ServerState.STATE_OFF);
        server.stopRun();
        server = null;
        
//        stopSelf();
    }
    
    /**
     * Sets the server state and dispatch event {@link #EVENT_STATE_CHANGE}
     * @param state the new state to set
     */
    private void setServerState(ServerState state) {
        this.state = state;
        dispatchEvent(EVENT_STATE_CHANGE, getServerState());
    }

    /**
     * Returns the current state of the server.
     * @return a {@link ServerState}
     */
    public ServerState getServerState() {
        return state;
    }
    
    /**
     * Returns the time from which the system has started.
     * @return the time from which the system has started, or 0L if the system is not currently running 
     */
    public long getStartTime() {
        return startTime;
    }
    
    /**
     * Reset all statistics to 0.
     */
    public void resetAllStats() {
        StatUtils.resetAllStats(ctx);
        dispatchEvent(EVENT_STATISTIC_UPDATE);
    }
    
    /**
     * Sets whether or not the user must be warned when there is a P1R4T3B0X network in range.
     * @param enabled true to enable the notification, false to disable it
     */
    public void setNotificationState(boolean enabled) {
        if (enabled) {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
            int period = 1000 * Integer.parseInt(settings.getString(PreferencesKeys.NOTIFICATION_FREQUENCY, PreferencesKeys.NOTIFICATION_DEFAULT_FREQUENCY));
            
            scanHandler.postDelayed(scanTask, period);
        } else {
            scanHandler.removeCallbacks(scanTask);
            if (scanResultReceiver != null) {
                ctx.unregisterReceiver(scanResultReceiver);
                scanResultReceiver = null;
            }
        }
    }

    /**
     * Starts the wifi access point.
     */
    private void startHotspot() {
        WifiApManager mgr = new WifiApManager(ctx);
        try {
            mgr.setWifiApEnabled(config, true);
        } catch (Exception e) {
            ExceptionHandler.handle(this, R.string.error_ap_start, ctx);
        }
    }

    /**
     * Stops the wifi access point.
     */
    private void stopHotspot() {
        WifiApManager mgr = new WifiApManager(ctx);
        try {
            mgr.setWifiApEnabled(config, false);
        } catch (Exception e) {
            ExceptionHandler.handle(this, R.string.error_ap_stop, ctx);
        }
    }
    
    /**
     * Initialises the scan task
     */
    private void initScan() {
        scanTask = new Runnable() {
            
            public void run() {
                final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
                //Defines the call frequency to the user defined value
                final int period = 1000 * Integer.parseInt(settings.getString(PreferencesKeys.NOTIFICATION_FREQUENCY, PreferencesKeys.NOTIFICATION_DEFAULT_FREQUENCY));
                
                try {
                    //If we are currently sending a file, try again later, as we need to disable the wifi access point to perform the scan
                    if (ServerState.STATE_SENDING.equals(System.this.getServerState())) {
                        scanHandler.postDelayed(this, period);
                        return;
                    }
                    
                    final WifiManager mgr = (WifiManager) ctx.getSystemService(Context.WIFI_SERVICE);
                    final WifiApManager apMgr = new WifiApManager(ctx);
                    
                    //Save the current states to restore them later
                    final boolean wifiEnabled = mgr.isWifiEnabled();
                    final boolean wifiApEnabled = apMgr.isWifiApEnabled();
                    
                    //Disable the wifi access point to allow usage of the wifi
                    apMgr.setWifiApEnabled(config, false);
                    
                    //Set the action on scan results
                    if (scanResultReceiver == null) {
                        scanResultReceiver = new BroadcastReceiver() {
                            @Override
                            public void onReceive(Context c, Intent i){
                                //Restore the wifi access point as soon as possible
                                try {
                                    //Restore wifi state
                                    apMgr.setWifiApEnabled(config, wifiApEnabled);
                                    if (!wifiEnabled) {
                                        mgr.setWifiEnabled(false);
                                    }
                                    
                                    //If a network is found with the name P1R4T3B0X, send a notification
                                    for (ScanResult scan : mgr.getScanResults()) {
                                        if (ServerConfiguration.WIFI_AP_NAME.equals(scan.SSID)) {
//                                        if ("BigPond0CB5".equals(scan.SSID)) {
                                            addNetworkNotification();
                                        } else {
                                            removeNetworkNotification();
                                        }
                                    }
                                } catch (Exception e) {
                                    ExceptionHandler.handle(this, R.string.error_network_scan, ctx);
                                }
                                
                                //Launch next scan
                                if (settings.getBoolean(PreferencesKeys.NOTIFICATION, false)) {
                                    scanHandler.postDelayed(System.this.scanTask, period);
                                }
                            }
                        };
                        
                        ctx.registerReceiver(scanResultReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                    }
                    
                    //If the wifi cannot be enabled or if the scan does not starts, restore states and try again later
                    if (! (mgr.setWifiEnabled(true) && mgr.startScan())) {
                        apMgr.setWifiApEnabled(config, wifiApEnabled);
                        if (!wifiEnabled) {
                            mgr.setWifiEnabled(false);
                        }
                        if (settings.getBoolean(PreferencesKeys.NOTIFICATION, false)) {
                            scanHandler.postDelayed(this, period);
                        }
                    }
                    
                } catch (Exception e) {
                    ExceptionHandler.handle(this, R.string.error_network_scan, ctx);
                }
                
                if (settings.getBoolean(PreferencesKeys.NOTIFICATION, false)) {
                    scanHandler.postDelayed(this, period);
                }
            }
        };
        
        ctx.registerReceiver(scanResultReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        scanHandler.removeCallbacks(scanTask);
    }
    
    /**
     * Creates a dispatch a notification to tell the user that a P1R4T3B0X network is in range.
     */
    private void addNetworkNotification() {
        Notification notification = new Notification(R.drawable.piratebox_ico,
                ctx.getResources().getString(R.string.notification_network),
                java.lang.System.currentTimeMillis());

        // Adding a sound
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(ctx);
        notification.sound = Uri.parse(settings.getString(PreferencesKeys.NOTIFICATION_RINGTONE, ""));
        
        // Adding a vibration
        if (settings.getBoolean(PreferencesKeys.NOTIFICATION_VIBRATE, false)) {
            notification.defaults |= Notification.DEFAULT_VIBRATE;
        } else {
            notification.vibrate = new long[]{0L};
        }
        
        notification.setLatestEventInfo(ctx.getApplicationContext(),
                ctx.getResources().getString(R.string.notification_network),
                "", PendingIntent.getActivity(ctx, 0, new Intent(), 0));
        
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        
        NotificationManager mgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        mgr.notify(NOTIFICATION_ID_NETWORK, notification);
    }
    
    /**
     * Removes a notification about a network in range.
     */
    private void removeNetworkNotification() {
        NotificationManager mgr = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        mgr.cancel(NOTIFICATION_ID_NETWORK);
    }
    
    /**
     * Retrieves the interface name associated with the wifi access point interface
     * @return The name of the interface of the wifi access point, or null if no such interface is found.
     * @throws SocketException 
     */
    private String getWlanInterfaceName() throws SocketException {

        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        while (interfaces.hasMoreElements()) {
            String interfaceName = interfaces.nextElement().getName();
            if (interfaceName.startsWith(WIFI_AP_INTERFACE_NAME_PREFIX)) {
                return interfaceName;
            }
        }
        return null;
    }

    
    
    /**
     * Adds a listener for the event {@code event}
     * @param event the event to listen
     * @param c the callback to be called when the event is dispatched
     */
    public void addEventListener(String event, Callback c) {
        if (listeners.get(event) == null) {
            listeners.put(event, new ArrayList<Callback>());
        }
        listeners.get(event).add(c);
    }
    
    /**
     * Removes a listener for the event {@code event}.
     * @param event the event to remove the listener
     * @param c the callback that had been added
     */
    public void removeEventListener(String event, Callback c) {
        if (listeners.get(event) == null) {
            listeners.put(event, new ArrayList<Callback>());
        }
        listeners.get(event).remove(c);
    }
    
    /**
     * Dispatch {@code event} and give {@code value} to the listeners.
     * @param event the event to be dispatched
     * @param value the value to give to listeners
     */
    private void dispatchEvent(String event, Object value) {
        if (listeners.get(event) == null) {
            return;
        }
        
        for (Callback c : listeners.get(event)) {
            c.call(value);
        }
    }
    
    /**
     * Dispatch {@code event} and give {@code null} to the listeners.
     * @param event the event to be dispatched
     */
    private void dispatchEvent(String event) {
        dispatchEvent(event, null);
    }
    
//    /**
//     * No binding allowed, returns null.
//     *
//     * @see android.app.Service#onBind(android.content.Intent)
//     */
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
}
