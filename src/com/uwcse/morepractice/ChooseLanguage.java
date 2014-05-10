package com.uwcse.morepractice;

import java.io.File;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;

public class ChooseLanguage extends Activity {
	private int[] colors = {0xA4C400FF, 0x60A917FF, 0x008A00FF, 0x00ABA9FF, 0x1BA1E2FF, 0x0050EFFF,  0x6A00FFFF, 0xAA00FFFF, 
			0xF472D0FF, 0xD80073FF, 0xA20025FF, 0xE51400FF, 0xFA6800FF, 0xF0A30AFF, 0xE3C800FF};
	private static final int TEXT_SIZE = 30;
	public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.language);
	}
	
	public void onResume() {
		super.onResume();
		LinearLayout layout = (LinearLayout) this.findViewById(R.id.language_layout);
		layout.removeAllViews();
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
			Button btn = new Button(this);
        	btn.setText("Download Files");
        	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        	btn.setLayoutParams(params);
        	btn.setTextSize(TEXT_SIZE * 2);
            btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Perform action on click
            		Intent intent = new Intent(ChooseLanguage.this, DownloadActivity.class);
                    startActivity(intent);
                }
            });
            
        	layout.addView(btn);
		} else {
	        Random ran = new Random();
			for (final File f : files) {
	        	Button btn = new Button(this);
	        	String[] parts = f.getPath().split("/");
	        	btn.setText(parts[parts.length - 1]);
	        	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, .5f);
	        	btn.setLayoutParams(params);
	        	int colorNum = ran.nextInt(colors.length - 1);
	        	btn.setBackgroundColor(colors[colorNum]);
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
	        }
		}
	}
}
