package com.uwcse.morepractice;

import java.io.File;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ChooseLanguage extends Activity {
	private String[] colors = {"#A4C400", "#60A917", "#008A00", "#00ABA9","#1BA1E2", "#0050EF","#6A00FF", "#AA00FF", 
			   "#F472D0", "#D80073", "#A20025", "#E51400", "#FA6800", "#F0A30A", "#E3C800"};
	private static final int TEXT_SIZE = 30;	
	private LanguageViewAdapter adapter;
	
	public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //setContentView(R.layout.activity_language);
        setContentView(R.layout.language_download);
        //setContentView(R.layout.language);
	}
	
	public void onResume() {
		super.onResume();

		LinearLayout layout = (LinearLayout) this.findViewById(R.id.lang_layout);
		
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
	
	/**
	 * Determines if content needs to be downloaded or if the initial Language options should be shown
	 * @param files, the empty or full file path of the language folder in local storage
	 * @param layout, the layout used by this language page
	 */
	private void showLanguages(File[] files, LinearLayout layout) {
		if (files.length == 0) { // if no content has been loaded, show the download button
			final Button button = (Button) findViewById(R.id.language_button);
		    button.setOnClickListener(new View.OnClickListener() {
		            public void onClick(View v) {
		            	Intent intent = new Intent(ChooseLanguage.this, DownloadActivity.class);
	                    startActivity(intent);
		                
		            }
		    });
		      
		} else { 
			setContentView(R.layout.language_list);		
					
			String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            final String laosFilePath = baseDir + "/" + getString(R.string.local_storage_folder);
			
	        String[] fileNames = new String[files.length];
	        for(int i = 0; i < files.length; i++){
	            fileNames[i] = files[i].getName();
	        }
	        
	        adapter = new LanguageViewAdapter(this, fileNames, R.layout.list_view );
	        
	        ListView listView = (ListView) findViewById(R.id.listview);
	        listView.setAdapter(adapter);
	        
	        listView.setOnItemClickListener(new OnItemClickListener() {
	            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	                Intent intent = new Intent(ChooseLanguage.this, MainActivity.class);
	                
	                TextView tv = (TextView) v.findViewById(R.id.language_text);
	                String name = laosFilePath + "/" + tv.getText();
	                
	                // sets device configurations so that lao script appears
	                setLanguage(name);
	                
	                intent.putExtra(MainActivity.LANGUAGE_KEY, name); 
                    startActivity(intent);
	            }
	        });
		}
	}

	/**
	 * Determines which language to use in the app
	 * @param path, the file path of the language folder in local storage
	 */
	private void setLanguage(String path) {
	    String name = getName(path);
	     
	    // English is default
	    String langCode = "en";
	    
	    // finds which language button the user clicked
	    if (name.equalsIgnoreCase(getString(R.string.lao))) {
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
