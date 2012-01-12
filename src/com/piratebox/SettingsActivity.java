package com.piratebox;

import java.io.File;
import java.io.FilenameFilter;

import com.piratebox.server.ServerConfiguration;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class SettingsActivity extends ListActivity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setListAdapter((new ArrayAdapter<String>(this, R.layout.setting_item, getResources().getStringArray(R.array.settings_items))));

		ListView lv = getListView();
		lv.setTextFilterEnabled(true);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (position == 0) {
					setSharedFolder();
				}
			}
		});
	}
	
	private void setSharedFolder() {
		
		Toast.makeText(getApplicationContext(), R.string.restart_server, Toast.LENGTH_LONG).show();
	}
	
	
	private String[] mFileList;
	private File mPath = new File(ServerConfiguration.rootDir);
	private String mChosenFile;
	private static final String FTYPE = ".txt";    
	private static final int DIALOG_LOAD_FILE = 1000;

	private void loadFileList(){
		try{
			mPath.mkdirs();
		}
		catch(SecurityException e){
			e.printStackTrace();
		}
		if(mPath.exists()){
			FilenameFilter filter = new FilenameFilter(){
				public boolean accept(File dir, String filename){
					File sel = new File(dir, filename);
					return filename.contains(FTYPE) || sel.isDirectory();
				}
			};
			mFileList = mPath.list(filter);
		}
		else{
			mFileList= new String[0];
		}
	}

	protected Dialog onCreateDialog(int id){
		 Dialog dialog = null;
		 AlertDialog.Builder builder = new Builder(this);

		 switch(id){
		 case DIALOG_LOAD_FILE:
			 builder.setTitle("Choose your file");
			 if(mFileList == null) {
				 dialog = builder.create();
				 return dialog;
			 }
			 builder.setItems(mFileList, new DialogInterface.OnClickListener() {
				 public void onClick(DialogInterface dialog, int which) {
					 ServerConfiguration.rootDir = mFileList[which];
				 }
			 });
			 break;
		 }
		 dialog = builder.show();
		 return dialog;
	} 
}
