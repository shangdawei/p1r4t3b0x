package com.piratebox;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;

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
			GoToDonateVersion();
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
	
	private void openSelectDir() {
		startActivityForResult(new Intent(this, DirectoryChooserActivity.class), DIRECTORY_CHOOSE_ACTIVITY_CODE);
	}
	
	private void resetStats() {
		// TODO dialog confirm + reset data
	}
	
	private void openHelp() {
		// TODO open help dialog
	}
	
	private void GoToDonateVersion() {
		// TODO open market to donate version
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
