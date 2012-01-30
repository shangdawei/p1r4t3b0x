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
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.piratebox.server.ServerConfiguration;
import com.piratebox.utils.PreferencesKeys;

/**
 * This class describes an {@link Activity} that allows the user to browse to a folder.
 * The root folder is either the preference stored in {@link PreferencesKeys.SELECT_DIR} if set, or
 * {@link ServerConfiguration.DEFAULT_ROOT_DIR}.
 * 
 * @author Aylatan
 */
/**
 * @author Aylatan
 *
 */
/**
 * @author Aylatan
 *
 */
public class DirectoryChooserActivity extends ListActivity {

	private File currentFolder;
	private List<String> directories;
	
	// Need a Context variable to be accessed in sub-classes.
	private Context ctx;
	
	/**
	 * Initialises the {@link Activity} and display content of the default folder.
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		ctx = this;

		setContentView(R.layout.browse);

		Button validBtn = (Button) findViewById(R.id.validBtn);
		validBtn.setOnClickListener(validBtnListener);

		Button newBtn = (Button) findViewById(R.id.newBtn);
		newBtn.setOnClickListener(newBtnListener);

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
		String folder = settings.getString(PreferencesKeys.SELECT_DIR, ServerConfiguration.DEFAULT_ROOT_DIR);
		
		goInDir(new File(folder));
	}
	
	/**
	 * Listener for the "Validate" button.
	 * Returns the path to the selected folder to the calling Activity.
	 */
	private OnClickListener validBtnListener = new OnClickListener() {
		public void onClick(View v) {
			setResult(RESULT_OK, new Intent(currentFolder.getAbsolutePath()));
			finish();
		}
	};
	
	/**
	 * Listener for the "New" button.
	 * Creates a dialog to ask the user for the name of the new folder, and then create it.
	 */
	private OnClickListener newBtnListener = new OnClickListener() {
		public void onClick(View v) {
			
		    //Prompt the user for the name of the new folder
			AlertDialog.Builder alert = new AlertDialog.Builder(ctx);
	        alert.setTitle(getResources().getString(R.string.new_folder));
	        alert.setMessage(getResources().getString(R.string.new_folder_dialog));
	        
	        final EditText inputName = new EditText(ctx);  
	        alert.setView(inputName);

	        alert.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {  
	        	public void onClick(DialogInterface dialog, int whichButton) {  
	        		String newFolder = inputName.getText().toString();
	        		File folder = new File(currentFolder, newFolder);
	        		//Try to create the folder
	        		if (folder.mkdirs()) {
	        			goInDir(folder);
	        		} else {
	        		    // If fail, it may be because we don't have sufficient rights, so try with root rights
						try {
						    Runtime.getRuntime().exec("su -c mkdir " + folder.getAbsolutePath()).waitFor();
	                        goInDir(folder);
						} catch (Exception e) {
							Log.e(this.getClass().getName(), e.toString());
						}
	        		}
	        	}
	        });
	        
	        alert.setCancelable(true);
	        alert.show();

		}
	};
	
	/**
	 * On list item click, display content of the click folder.
	 * 
	 * @see android.app.ListActivity#onListItemClick(android.widget.ListView, android.view.View, int, long)
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String folder = directories.get(position);
		if ("..".equals(folder)) {
		    goInDir(currentFolder.getParentFile());
		} else {
		    goInDir(new File(currentFolder, folder));
		}
	}
	
	/**
	 * Goes into a folder and display all folders inside it, plus the ".." folder.
	 * @param folder The folder to go into
	 */
	private void goInDir(File folder) {
		currentFolder = folder;

		TextView currentFolderTxt = (TextView) findViewById(R.id.currentFolder);
		currentFolderTxt.setText(currentFolder.getAbsolutePath());

		//Show only directories
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				return new File(dir, filename).isDirectory();
			}
		};
		
		String[] dirs = currentFolder.list(filter);
    	directories = new ArrayList<String>();
    	
    	//If it is not the root, add the ".." folder
    	if (currentFolder.getParent() != null) {
    	    directories.add("..");
    	}
    	if (dirs != null) {
    	    for (String dir : dirs) {
    	        directories.add(dir);
    	    }
    	}
        setListAdapter(new ArrayAdapter<String>(this, R.layout.folder_item, directories));
    }
}
