package com.uwcse.morepractice;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ChooseLanguage extends Activity {
	private String[] colors = {"#A4C400", "#60A917", "#008A00", "#00ABA9","#1BA1E2", "#0050EF","#6A00FF", "#AA00FF", 
			   "#F472D0", "#D80073", "#A20025", "#E51400", "#FA6800", "#F0A30A", "#E3C800"};
	private static final int TEXT_SIZE = 30;
	private static final String TAG = TrainingPackageActivity.class.getSimpleName();
	
	private LanguageViewAdapter adapter;
	
	public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_language);
       // setContentView(R.layout.language);
	}
	
	public void onResume() {
		super.onResume();
		//LinearLayout layout = (LinearLayout) this.findViewById(R.id.language_layout);
		LinearLayout layout = (LinearLayout) this.findViewById(R.id.lang_layout);
		//layout.removeAllViews();
		/*if(layout.findViewById(R.id.language_button) != null){
			layout.removeViewAt(R.id.language_button);
		}*/
		
        File[] files = setupFileSystem();
        showLanguages(files, layout);
	}
	
	private File[] setupFileSystem() {
		String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();

        // for first design iteration
        String laosFilePath = baseDir + "/" + getString(R.string.local_storage_folder);
        final File appRoot = new File(laosFilePath);
        if (!appRoot.exists()) {
        	if (!appRoot.mkdir()) {
        		// uh-oh. Failed
        	}
        }
        
        return appRoot.listFiles();
	}
	
	private void showLanguages(File[] files, LinearLayout layout) {
		if (files.length == 0) {
		
			  final Button button = (Button) findViewById(R.id.language_button);
		      button.setOnClickListener(new View.OnClickListener() {
		            public void onClick(View v) {
		            	Intent intent = new Intent(ChooseLanguage.this, DownloadActivity.class);
	                    startActivity(intent);
		                
		            }
		        });
			/*	Button btn = new Button(this);
        	btn.setText("Download Files");
        	//LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        	//LinearLayout layout = (LinearLayout) findViewById(R.id.language_layout);
        	//btn.setLayoutParams(params);
        	btn.setWidth(400);
        	btn.setHeight(250);
        	btn.setTextSize(TEXT_SIZE * 2);
            btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Perform action on click
            		Intent intent = new Intent(ChooseLanguage.this, DownloadActivity.class);
                    startActivity(intent);
                }
            });
            
        	layout.addView(btn);*/
		      
		} else {
			// Get the short names of the files to populate the grid view
			
			
					if(layout.findViewById(R.id.language_button ) != null){
						Button button = (Button)layout.findViewById(R.id.language_button);
						ViewGroup parent = (ViewGroup) button.getParent();
						parent.removeView(button);
					}
			
			
			String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            final String laosFilePath = baseDir + "/" + getString(R.string.local_storage_folder);
			
			
	        String[] fileNames = new String[files.length];
	        for(int i = 0; i < files.length; i++){
	            fileNames[i] = files[i].getName();
	        }
	        
	       // LanguageViewAdapter adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1, fileNames);
	        adapter = new LanguageViewAdapter(this, fileNames, baseDir + "/", R.layout.list_view );
	        
	        ListView listView = (ListView) findViewById(R.id.listview);
	        listView.setAdapter(adapter);
	        
	        Log.e(TAG, "IN CHOOSE LANGUAGE");
	        Log.e(TAG, "File 1" + fileNames[0]);
	        Log.e(TAG, "File 1" + fileNames[1]);
	        
	        listView.setOnItemClickListener(new OnItemClickListener() {
	            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	                Intent intent = new Intent(ChooseLanguage.this, MainActivity.class);
	                final String f = (String) parent.getItemAtPosition(position);
	                
	                TextView tv = (TextView) v.findViewById(R.id.language_text);
	                String name = laosFilePath + "/" + tv.getText();
	                
	                // sets device configurations so that lao script appears
	                setLanguage(name);
	                
	               // TextView tv = (TextView) v.findViewById(R.id.item_text);
	                //String fullname = laosFilePath + "/" + f;
	                intent.putExtra(MainActivity.LANGUAGE_KEY, name);
                    startActivity(intent);
	            }
	        });
	        /*int i = 0;
			for (final File f : files) {
	        	Button btn = new Button(this);
	        	String[] parts = f.getPath().split("/");
	        	btn.setText(parts[parts.length - 1]);
	        	//LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, .5f);
	        	//RelativeLayout rlayout = (RelativeLayout) findViewById(R.id.language_relative);
	        	btn.setWidth(100);
	        	btn.setHeight(250);
	        	//btn.setLayoutParams(params);
	        	btn.setBackgroundColor(Color	.parseColor(colors[i % colors.length]));
	        	btn.setTextSize(TEXT_SIZE);
	        	

	            btn.setOnClickListener(new View.OnClickListener() {
	                public void onClick(View v) {
	                    // Perform action on click
	            		Intent intent = new Intent(ChooseLanguage.this, MainActivity.class);
	                    intent.putExtra(MainActivity.LANGUAGE_KEY, f.getAbsolutePath());
	                    startActivity(intent);
	                }
	            });
	            
	        	layout.addView(btn);
	        	i++;
	        }*/
		}
	}

	/**
	 * Determines which language to use in the app
	 * @param path, the file path of the language folder in local storage
	 */
	private void setLanguage(String path) {
	    String name = getName(path);
	    String langCode = "";
	    
	    // finds which language button the user clicked
	    if (name.equalsIgnoreCase(getString(R.string.english))) {
	        langCode = "en";
	    } else if (name.equalsIgnoreCase(getString(R.string.lao))) {
	        langCode = "lo";
	    }
	    
	    // changes the device's locale
	    Locale locale = new Locale(langCode); 
	    Locale.setDefault(locale);
	    Configuration config = new Configuration();
	    config.locale = locale;
	    getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

	    // resets the content after resetting the locale
	    setContentView(R.layout.activity_language);

	}

	/**
	 * Gets the name of the folder
	 * @param path, the filepath where the folder to return is located
	 * @return the name of the folder
	 */
	private String getName(String path) {
	    String[] names = path.split("\\/");
	    return names[names.length - 1];
	}
}
