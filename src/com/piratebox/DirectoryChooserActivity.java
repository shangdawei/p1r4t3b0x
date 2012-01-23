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

public class DirectoryChooserActivity extends ListActivity {

	private File currentFolder;
	private List<String> directories;
	private Context ctx;
	
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
	
	private OnClickListener validBtnListener = new OnClickListener() {
		public void onClick(View v) {
			setResult(RESULT_OK, new Intent(currentFolder.getAbsolutePath()));
			finish();
		}
	};
	
	private OnClickListener newBtnListener = new OnClickListener() {
		public void onClick(View v) {
			
			AlertDialog.Builder alert = new AlertDialog.Builder(ctx);
	        alert.setTitle("New folder");
	        alert.setMessage("Type the name of the folder you want to create:");
	        
	        final EditText inputName = new EditText(ctx);  
	        alert.setView(inputName);

	        alert.setPositiveButton("Create", new DialogInterface.OnClickListener() {  
	        	public void onClick(DialogInterface dialog, int whichButton) {  
	        		String newFolder = inputName.getText().toString();
	        		File folder = new File(currentFolder, newFolder);
	        		if (folder.mkdir()) {
	        			goInDir(folder);
	        		} else {
						try {
						    Runtime.getRuntime().exec("mkdir " + folder.getAbsolutePath()).waitFor();
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
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String folder = directories.get(position);
		if ("..".equals(folder)) {
		    goInDir(currentFolder.getParentFile());
		} else {
		    goInDir(new File(currentFolder, folder));
		}
	}
	
	private void goInDir(File folder) {
		currentFolder = folder;

		TextView currentFolderTxt = (TextView) findViewById(R.id.currentFolder);
		currentFolderTxt.setText(currentFolder.getAbsolutePath());
		
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String filename) {
				return new File(dir, filename).isDirectory();
			}
		};
		
		String[] dirs = currentFolder.list(filter);
    	directories = new ArrayList<String>();
    	if (currentFolder.getParent() != null) {
    	    directories.add(0, "..");
    	}
    	if (dirs != null) {
    	    for (String dir : dirs) {
    	        directories.add(dir);
    	    }
    	}
        setListAdapter(new ArrayAdapter<String>(this, R.layout.folder_item, directories));
    }
}
