package com.uwcse.morepractice;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class Topic extends Activity {
	private static final String TAG = TrainingPackageActivity.class.getSimpleName();
	public static final String INTENT_KEY_NAME = "packageName";
	
	public static final String POSITION = "0";
	private static File[] FILES;
	private int currentFile;
	private String packageName;
	
	private GridView gridview;
	private TopicsAdapter adapter;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//gestureDetector = new GestureDetector(this, new MyGestureDetector(this)); no swiping for now - make sure to uncomment dispatchTouchEvent
		
		packageName = getIntent().getExtras().getString(INTENT_KEY_NAME);
		
		//showFiles(retrievedName);
		//this.setTitle(getNameFromPath(packageName)); 
		
		
		Bundle bundle = getIntent().getExtras();
		ArrayList<String> files = bundle.getStringArrayList("files");
		
		final String[] fileNames = new String[files.size()];
		
		for(int i = 0; i < files.size(); i++) {
			fileNames[i] = files.get(i);
		}
		
		gridview = (GridView) findViewById(R.id.gridview);
		if(gridview == null){
			Log.e("TOPIC", "Gridview is null");
			
		}
        // Get the short names of the files to populate the grid view
        
        //adapter = new MyViewAdapter(this, files, appRoot.getAbsolutePath() + "/", R.layout.row_grid );
		Log.e("TOPIC", "NUMBER OF FILES : " + files.size());
		Log.e("TOPIC", "package name: " + packageName);
		adapter = new TopicsAdapter(this, fileNames, R.layout.toic);
		
        // Construct the gridView, sending in the files and the absolute path where the files reside
        gridview.setAdapter(adapter);

        gridview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                Intent intent = new Intent(Topic.this, Single.class);

                intent.putExtra(Single.INTENT_KEY_NAME, packageName + "/" + fileNames[position]);
                
                startActivity(intent);
            }
        });
        
	}
}
