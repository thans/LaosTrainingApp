package com.uwcse.morepractice;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SearchView.OnQueryTextListener;

public class Topic extends Activity {
	private static final String TAG = TrainingPackageActivity.class.getSimpleName();
	public static final String INTENT_KEY_NAME = "packageName";
	public static final String TOPIC_NAME = "topicName";
	
	public static final String POSITION = "0";
	private static File[] FILES;
	private int currentFile;
	private String packageName;
	private String topicName;
	private String[] fileNames;
	
	private GridView gridview;
	private NavigationAdapter adapter;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		packageName = getIntent().getExtras().getString(INTENT_KEY_NAME);
		topicName = getIntent().getExtras().getString(TOPIC_NAME);
		setTitle(getFolderName(packageName) + " - " + topicName);
		
		//showFiles(retrievedName);
		//this.setTitle(getNameFromPath(packageName)); 
		
		
		Bundle bundle = getIntent().getExtras();
		ArrayList<String> files = bundle.getStringArrayList("files");
		
		List<String> names = new ArrayList<String>();
		
		for(int i = 0; i < files.size(); i++) {
		    String name = files.get(i);
		    if (!getNameWithoutExtension(name).equals("img") && !name.equals("order.txt")) {
		        names.add(files.get(i));
		    } 
		}
		
		fileNames = names.toArray(new String[names.size()]);
		
		gridview = (GridView) findViewById(R.id.gridview);
		
        // Get the short names of the files to populate the grid view
        
        //adapter = new MyViewAdapter(this, files, appRoot.getAbsolutePath() + "/", R.layout.row_grid );
		Log.e("TOPIC", "NUMBER OF FILES : " + fileNames.length);
		Log.e("TOPIC", "package name: " + packageName);
		
		//adapter = new TopicsAdapter(this, fileNames, R.layout.toic);
		adapter = new NavigationAdapter(this, fileNames, R.layout.row_grid );
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
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.topic, menu);

        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setQueryHint(getString(R.string.search_packages));
        
        searchView.setOnQueryTextListener(new OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return false;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                hideKeyboard();
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }
	
	private String getNameWithoutExtension(String filename) {
	    String[] parts = filename.split("\\.");
	    return parts[0].toLowerCase();
	}
	
	private String getFolderName(String filename) {
	    String[] parts = filename.split("/");
	    return parts[parts.length - 1];
	}
	
    /**
     * Performs the search by filtering package names
     * @param search, the text to search for
     */
    public void performSearch(String search) {
        List<String> filteredList = new ArrayList<String>();
        for (String name : fileNames) {
            if (name.toLowerCase().startsWith(search.toLowerCase())) {
                filteredList.add(name);
            }
        }
        // the new array to give to the adapter
        String[] filteredArray = filteredList.toArray(new String[filteredList.size()]);
        adapter = null;
        adapter = new NavigationAdapter(this, filteredArray, R.layout.row_grid );
        
        // Construct the gridView, sending in the files and the absolute path where the files reside
        gridview.setAdapter(adapter);
    }
	
    /**
     * Hides the keyboard
     * @param view, the view that brought up the keyboard
     */
    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) this
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        //check if no view has focus:
        View v=this.getCurrentFocus();
        if(v != null)
            inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }


    @Override
    protected void onPause() {
      super.onPause();
      hideKeyboard();
    }

}
