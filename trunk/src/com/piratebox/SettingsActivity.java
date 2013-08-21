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


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.Toast;

import com.piratebox.billing.BillingService;
import com.piratebox.server.ServerConfiguration;
import com.piratebox.utils.ExceptionHandler;
import com.piratebox.utils.PreferencesKeys;
import com.piratebox.utils.Utils;

/**
 * This class describes the {@link Activity} for the settings.
 * From this {@link Activity} the user can define his preferences.
 * This includes:
 * <li>Changing the current shared directory</li>
 * <li>Change the notifications settings</li>
 * <li>Change the low battery behaviour</li>
 * <li>Reset all statistics</li>
 * <li>Show the help</li>
 * <li>Access to the "Donate" version</li>
 * 
 * @author Aylatan
 */
/**
 * @author Aylatan
 *
 */
public class SettingsActivity extends PreferenceActivity {
	
	private static final int DIRECTORY_CHOOSE_ACTIVITY_ID = 0;
    private static final String SHOOTER_ID = "beer.one";
    private static final String BEER_ID = "beer.five";
    private static final String LARGE_BEER_ID = "beer.ten";
    private static final String BEER_BARREL_ID = "beer.fifty";
//    private static final String SHOOTER_ID = "android.test.item_unavailable";
//    private static final String BEER_ID = "android.test.refunded";
//    private static final String LARGE_BEER_ID = "android.test.canceled";
//    private static final String BEER_BARREL_ID = "android.test.purchased";
	
	private BroadcastReceiver batteryBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent i){
            //Get the charging status
//            int status = i.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
//            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
//                                status == BatteryManager.BATTERY_STATUS_FULL;
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(c);

            //If the device is currently being charged, there is no need to stop the system
            if (/*isCharging || */!settings.getBoolean(PreferencesKeys.LOW_BAT, false)) {
                return;
            }
            
            PirateService.getInstance().stop();
            PirateService.getInstance().setNotificationState(false);
        }
    };
    /*
    
    Other solutions:
    onReceive:
    int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
    int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
    int level = -1;
    if (rawlevel >= 0 && scale > 0) {
        level = (rawlevel * 100) / scale;
    }
    
    using Intent.ACTION_BATTERY_CHANGE for the IntentFilter
	
	/**
	 * Initialises the {@link Activity}.
	 * 
	 * @see android.preference.PreferenceActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		startService(new Intent(this, BillingService.class));

		addPreferencesFromResource(R.xml.settings);
		
		setSelectDirSummary();
		
        CheckBoxPreference lowBatPref = (CheckBoxPreference) getPreferenceScreen().findPreference(PreferencesKeys.LOW_BAT);
        if (lowBatPref.isChecked()) {
            registerReceiver(batteryBroadcastReceiver, new IntentFilter(Intent.ACTION_BATTERY_LOW));
        }

		
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        //Get the currently selected frequency value.
        String value = settings.getString(PreferencesKeys.NOTIFICATION_FREQUENCY, PreferencesKeys.NOTIFICATION_DEFAULT_FREQUENCY);
		setNotificationsFrequencySummaryFromValue(value);
		

        getPreferenceScreen().findPreference(PreferencesKeys.NOTIFICATION_FREQUENCY).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                setNotificationsFrequencySummaryFromValue((String)newValue);
                return true;
            }
        });

        
        //Get the current ringtone URI.
        String ringtone = settings.getString(PreferencesKeys.NOTIFICATION_RINGTONE, "");
		setNotificationsRingtoneSummaryFromRingtoneURI(ringtone);
		
		setNotificationMenusStates();
        
        getPreferenceScreen().findPreference(PreferencesKeys.NOTIFICATION_RINGTONE).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                setNotificationsRingtoneSummaryFromRingtoneURI((String)newValue);
                return true;
            }
        });
	}
	
	/**
	 * Defines the actions to be performed when the user clicks an item of the preferences.
	 * 
	 * @see android.preference.PreferenceActivity#onPreferenceTreeClick(android.preference.PreferenceScreen, android.preference.Preference)
	 */
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		
		if (preference.equals(getPreferenceScreen().findPreference(PreferencesKeys.SELECT_DIR))) {
			openSelectDir();
        } else if(preference.equals(getPreferenceScreen().findPreference(PreferencesKeys.LOW_BAT))) {
            onLowBatterychange();
        } else if(preference.equals(getPreferenceScreen().findPreference(PreferencesKeys.RESET_STAT))) {
            resetStats();
		} else if(preference.equals(getPreferenceScreen().findPreference(PreferencesKeys.HELP))) {
			openHelp();
        } else if(preference.equals(getPreferenceScreen().findPreference(PreferencesKeys.NOTIFICATION))) {
            setNotificationMenusStates();
        } else if(preference.equals(getPreferenceScreen().findPreference(PreferencesKeys.Beer.BEER_SHOOTER))) {
            buyAShooter();
        } else if(preference.equals(getPreferenceScreen().findPreference(PreferencesKeys.Beer.BEER))) {
            buyABeer();
        } else if(preference.equals(getPreferenceScreen().findPreference(PreferencesKeys.Beer.BEER_LARGE))) {
            buyALargeBeer();
        } else if(preference.equals(getPreferenceScreen().findPreference(PreferencesKeys.Beer.BEER_BARREL))) {
            buyABeerBarrel();
        }
		        
		return true;
	}
	
	/**
	 * Sets the summary of the list item for the directory selection.
	 * The summary shows the current selected directory.
	 */
	private void setSelectDirSummary() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		
		String summary = getResources().getString(R.string.current);
		summary += " " + settings.getString(PreferencesKeys.SELECT_DIR, ServerConfiguration.DEFAULT_ROOT_DIR);
		getPreferenceScreen().findPreference(PreferencesKeys.SELECT_DIR).setSummary(summary);
	}
    
    /**
     * Sets the summary of the list item for the notification frequency.
     * The summary shows the current set frequency.
     */
    private void setNotificationsFrequencySummaryFromValue(String value) {
        CharSequence[] array = getResources().getTextArray(R.array.notification_frequency);
        CharSequence[] arrayReadable = getResources().getTextArray(R.array.notification_frequency_readable);
        //Get its index in the R.array.notification_frequency array.
        int index = Utils.indexOf(value, array);
        //Retrieve the readable value from the index in the R.array.notification_frequency_readable array.
        CharSequence readable = arrayReadable[index];
        
        String summary = getResources().getString(R.string.current);
        summary += " " + readable;
        getPreferenceScreen().findPreference(PreferencesKeys.NOTIFICATION_FREQUENCY).setSummary(summary);
    }

    /**
     * Sets the summary of the list item for the notification ringtone.
     * The summary shows the current set ringtone name.
     */
    private void setNotificationsRingtoneSummaryFromRingtoneURI(String ringtoneUri) {
        
        String ringtone;
        //If the ringtone is "Silent" then the URI is "Silent" or "".
        if ("Silent".equals(ringtoneUri) || "".equals(ringtoneUri)) {
            ringtone = getResources().getString(R.string.silent);
        } else {
            //Else get the ringtone name.
            ringtone = RingtoneManager.getRingtone(this, Uri.parse(ringtoneUri)).getTitle(this);
        }
        
        
        String summary = getResources().getString(R.string.current);
        summary += " " + ringtone;
        getPreferenceScreen().findPreference(PreferencesKeys.NOTIFICATION_RINGTONE).setSummary(summary);
    }
	
	/**
	 * Opens the {@link DirectoryChooserActivity}.
	 */
	private void openSelectDir() {
		startActivityForResult(new Intent(this, DirectoryChooserActivity.class), DIRECTORY_CHOOSE_ACTIVITY_ID);
	}
	
	/**
	 * Sets the state of the list items of the notification menu according to the state of the main item.
	 */
	private void setNotificationMenusStates() {
	    CheckBoxPreference pref = (CheckBoxPreference) getPreferenceScreen().findPreference(PreferencesKeys.NOTIFICATION);
	    boolean checked = pref.isChecked();

        getPreferenceScreen().findPreference(PreferencesKeys.NOTIFICATION_FREQUENCY).setEnabled(checked);
        getPreferenceScreen().findPreference(PreferencesKeys.NOTIFICATION_RINGTONE).setEnabled(checked);
        getPreferenceScreen().findPreference(PreferencesKeys.NOTIFICATION_VIBRATE).setEnabled(checked);
        
        PirateService.getInstance().setNotificationState(checked);
	}
	
	/**
	 * Registers or unregisters the broadcast receiver for the low battery event, depending on the preference status.
	 */
	private void onLowBatterychange() {
        CheckBoxPreference lowBatPref = (CheckBoxPreference) getPreferenceScreen().findPreference(PreferencesKeys.LOW_BAT);
	    if (lowBatPref.isChecked()) {
	        registerReceiver(batteryBroadcastReceiver, new IntentFilter(Intent.ACTION_BATTERY_LOW));
	    } else {
	        unregisterReceiver(batteryBroadcastReceiver);
	    }
	}
	
	/**
	 * Opens a dialog to confirm the user action.
	 * If the action is confirmed, all the statistics are deleted.
	 */
	private void resetStats() {
        AlertDialog.Builder confirm = new AlertDialog.Builder(this);
        confirm.setTitle(getResources().getString(R.string.confirm_title))
        .setMessage(getResources().getString(R.string.reset_confirm))
        .setCancelable(true)
        .setNegativeButton(R.string.no, null)
        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            
            public void onClick(DialogInterface dialog, int which) {
                PirateService.getInstance().resetAllStats();
                Toast.makeText(SettingsActivity.this, R.string.reset_done, Toast.LENGTH_SHORT).show();
            }
        })
        .show();
	}
	
	/**
	 * Opens the help content in a dialog box.
	 */
	private void openHelp() {
	    AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getResources().getString(R.string.help))
        .setMessage(getResources().getString(R.string.help_content))
        .setPositiveButton(R.string.close, null)
        .setCancelable(true)
        .show();
	}
	
	/**
	 * Opens the in app billing feature for the shooter item.
	 */
    private void buyAShooter() {
        try {
            if (BillingService.isInAppBillingSupported()) {
                BillingService.requestPurchase(SHOOTER_ID, this);
            }
        } catch (Exception e) {
            ExceptionHandler.handle(this, R.string.error_during_payment, getApplicationContext());
        }
    }
    
    /**
     * Opens the in app billing feature for the beer item.
     */
    private void buyABeer() {
        try {
            if (BillingService.isInAppBillingSupported()) {
                BillingService.requestPurchase(BEER_ID, this);
            }
        } catch (Exception e) {
            ExceptionHandler.handle(this, R.string.error_during_payment, getApplicationContext());
        }
    }
    
    /**
     * Opens the in app billing feature for the large beer item.
     */
    private void buyALargeBeer() {
        try {
            if (BillingService.isInAppBillingSupported()) {
                BillingService.requestPurchase(LARGE_BEER_ID, this);
            }
        } catch (Exception e) {
            ExceptionHandler.handle(this, R.string.error_during_payment, getApplicationContext());
        }
    }
    
    /**
     * Opens the in app billing feature for the beer barrel  item.
     */
    private void buyABeerBarrel() {
        try {
            if (BillingService.isInAppBillingSupported()) {
                BillingService.requestPurchase(BEER_BARREL_ID, this);
            }
        } catch (Exception e) {
            ExceptionHandler.handle(this, R.string.error_during_payment, getApplicationContext());
        }
    }
	
	/**
	 * Manages the result of the {@link DirectoryChooserActivity} and store it to the preferences.
	 * 
	 * @see android.preference.PreferenceActivity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == DIRECTORY_CHOOSE_ACTIVITY_ID && resultCode == RESULT_OK) {
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
			Editor edit = settings.edit();
			edit.putString(PreferencesKeys.SELECT_DIR, data.getAction());
			edit.commit();
	        ServerConfiguration.setRootDir(data.getAction());
			setSelectDirSummary();
		}
	}
}
