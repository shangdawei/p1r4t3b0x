package com.piratebox;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.provider.MediaStore.Audio.Media;
import android.widget.Toast;

import com.piratebox.server.ServerConfiguration;
import com.piratebox.utils.PreferencesKeys;
import com.piratebox.utils.Utils;

public class SettingsActivity extends PreferenceActivity {
	
	private final int DIRECTORY_CHOOSE_ACTIVITY_CODE = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.settings);
		
		setSelectDirSummary();
		setLowBatSummary();
		setNotificationsFrequencySummary();
		setNotificationsRingtoneSummary();
		
		setNotificationMenusStates();
	}
	
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		
		if (preference.equals(getPreferenceScreen().findPreference(PreferencesKeys.SELECT_DIR))) {
			openSelectDir();
		} else if(preference.equals(getPreferenceScreen().findPreference(PreferencesKeys.RESET_STAT))) {
			resetStats();
		} else if(preference.equals(getPreferenceScreen().findPreference(PreferencesKeys.HELP))) {
			openHelp();
        } else if(preference.equals(getPreferenceScreen().findPreference(PreferencesKeys.BEER))) {
            goToDonateVersion();
        } else if(preference.equals(getPreferenceScreen().findPreference(PreferencesKeys.NOTIFICATION))) {
            setNotificationMenusStates();
		}
		
		return true;
	}
	
	private void setSelectDirSummary() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		
		String summary = getResources().getString(R.string.current);
		summary += " " + settings.getString(PreferencesKeys.SELECT_DIR, ServerConfiguration.DEFAULT_ROOT_DIR);
		getPreferenceScreen().findPreference(PreferencesKeys.SELECT_DIR).setSummary(summary);
	}
	
	private void setLowBatSummary() {
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		CharSequence[] array = getResources().getTextArray(R.array.battery_threshold);
		CharSequence[] arrayReadable = getResources().getTextArray(R.array.battery_threshold_readable);
		
		String value = settings.getString(PreferencesKeys.LOW_BAT, "0");
		int index = Utils.indexOf(value, array);
		CharSequence readable = arrayReadable[index];
		
		String summary = getResources().getString(R.string.current);
		summary += " " + readable;
		getPreferenceScreen().findPreference(PreferencesKeys.LOW_BAT).setSummary(summary);
	}
    
    private void setNotificationsFrequencySummary() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        CharSequence[] array = getResources().getTextArray(R.array.notification_frequency);
        CharSequence[] arrayReadable = getResources().getTextArray(R.array.notification_frequency_readable);
        
        String value = settings.getString(PreferencesKeys.NOTIFICATION_FREQUENCY, "0");
        int index = Utils.indexOf(value, array);
        CharSequence readable = arrayReadable[index];
        
        String summary = getResources().getString(R.string.current);
        summary += " " + readable;
        getPreferenceScreen().findPreference(PreferencesKeys.NOTIFICATION_FREQUENCY).setSummary(summary);
    }
    
    private void setNotificationsRingtoneSummary() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String ringtone = settings.getString(PreferencesKeys.NOTIFICATION_RINGTONE, getResources().getString(R.string.none));
        if ("".equals(ringtone)) {
            ringtone = getResources().getString(R.string.silent);
        } else if (!getResources().getString(R.string.none).equals(ringtone)) {
            ringtone = RingtoneManager.getRingtone(this, Media.getContentUri(ringtone)).getTitle(this);
        }
        
        
        String summary = getResources().getString(R.string.current);
        summary += " " + ringtone;
        getPreferenceScreen().findPreference(PreferencesKeys.NOTIFICATION_RINGTONE).setSummary(summary);
    }
	
	private void openSelectDir() {
		startActivityForResult(new Intent(this, DirectoryChooserActivity.class), DIRECTORY_CHOOSE_ACTIVITY_CODE);
	}
	
	private void setNotificationMenusStates() {
	    CheckBoxPreference pref = (CheckBoxPreference) getPreferenceScreen().findPreference(PreferencesKeys.NOTIFICATION);
	    boolean checked = pref.isChecked();

        getPreferenceScreen().findPreference(PreferencesKeys.NOTIFICATION_FREQUENCY).setEnabled(checked);
        getPreferenceScreen().findPreference(PreferencesKeys.NOTIFICATION_RINGTONE).setEnabled(checked);
        getPreferenceScreen().findPreference(PreferencesKeys.NOTIFICATION_VIBRATE).setEnabled(checked);
	}
	
	private void resetStats() {
        AlertDialog.Builder confirm = new AlertDialog.Builder(this);
        confirm.setTitle(getResources().getString(R.string.confirm_title))
        .setMessage(getResources().getString(R.string.reset_confirm))
        .setCancelable(true)
        .setNegativeButton(R.string.no, null)
        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            
            public void onClick(DialogInterface dialog, int which) {
                System.getInstance(SettingsActivity.this).resetAllStats();
                Toast.makeText(SettingsActivity.this, R.string.reset_done, Toast.LENGTH_SHORT).show();
            }
        })
        .show();
	}
	
	private void openHelp() {
	    AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(getResources().getString(R.string.help));
        alert.setMessage(getResources().getString(R.string.help_content));
        alert.setPositiveButton(R.string.close, null);
        alert.setCancelable(true);
        alert.show();
	}
	
	private void goToDonateVersion() {
	    Intent intent = new Intent(Intent.ACTION_VIEW);
	    intent.setData(Uri.parse(getResources().getString(R.string.donate_app)));
	    startActivity(intent);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if (requestCode == DIRECTORY_CHOOSE_ACTIVITY_CODE && resultCode == RESULT_OK) {
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
			Editor edit = settings.edit();
			edit.putString(PreferencesKeys.SELECT_DIR, data.getAction());
			edit.commit();
			setSelectDirSummary();
		}
	}
}