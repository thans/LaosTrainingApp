package com.example.laostrainingapp;

import java.io.File;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.os.Build;

public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.getSimpleName();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
		addTextProgrammatically(baseDir);
		addImage(baseDir);
	}

	public String addTextProgrammatically(String baseDir) {
	    LinearLayout layout = 
	        (LinearLayout) this.findViewById(R.id.activity_main_linear_layout);
	    
		File appRoot = new File(baseDir + "/LaosTrainingApp/");
		File[] files = appRoot.listFiles();
		Log.e(TAG, appRoot.getAbsolutePath());
//		String data = "Files currently in " + appRoot.getAbsolutePath();
		TextView text = new TextView(this);
		text.setText("Files in the LaosTrainingApp directory:");
		layout.addView(text);
		for (File f : files) {
			TextView tv = new TextView(this);
			tv.setText(f.getPath());
			layout.addView(tv);
			Log.e(TAG, f.getPath());
		}
	    
	    return baseDir;
	}
	
	public void addImage(String baseDir) {
	    LinearLayout layout = 
		        (LinearLayout) this.findViewById(R.id.activity_main_linear_layout);
		
		File appRoot = new File(baseDir + "/LaosTrainingApp/");
		File[] files = appRoot.listFiles();
		for (File f : files) {
			Log.e(TAG, f.getPath());
			
			ImageView image = new ImageView(this);
			String fileName = f.getAbsolutePath();
			File imgFile = new  File(fileName);
		
			if(imgFile.exists()){
			    Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
			    image.setImageBitmap(myBitmap);
			} else {
				Log.e(TAG, imgFile.getAbsolutePath() + " permission to read " + imgFile.canRead());
			}
			
			layout.addView(image);
		}
		
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
