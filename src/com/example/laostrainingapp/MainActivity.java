package com.example.laostrainingapp;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final String TAG = MainActivity.class.getSimpleName();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
		addTrainingPackageButtons(baseDir);
		//addTextProgrammatically(baseDir);
		//addImage(baseDir);
	}
	
	public void addTrainingPackageButtons(String baseDir) {
	    LinearLayout layout = 
		        (LinearLayout) this.findViewById(R.id.activity_main_linear_layout);
	    Log.e(TAG, "checking in: " + R.string.local_storage_folder);
	    File appRoot = new File(baseDir + "/" + getString(R.string.local_storage_folder));
	    File[] files = appRoot.listFiles();
		for (File f : files) {
			Button toTrainingPackage = new Button(this);
			Log.e(TAG, f.getName());
			Log.e(TAG, f.getPath());
			
			// Gets the size of the current window and stores it using a Point
			Display display = getWindowManager().getDefaultDisplay(); 
			Point size = new Point();
			
			display.getSize(size);
			toTrainingPackage.setWidth(size.x / 3);

			toTrainingPackage.setHeight(size.y / 3);
			
			toTrainingPackage.setTextSize(50);
			
			toTrainingPackage.setBackgroundColor(Color.parseColor("#4169e1"));
			
			
			
			toTrainingPackage.setText(f.getName());
			toTrainingPackage.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
			
			Intent intent = new Intent(MainActivity.this, TrainingPackageActivity.class);
	        intent.putExtra(TrainingPackageActivity.INTENT_KEY_NAME, f.getAbsolutePath());
			toTrainingPackage.setOnClickListener(new TrainingPackageClickListener((Activity) this, intent));
			layout.addView(toTrainingPackage);
		}
	}

	public void addTextProgrammatically(String baseDir) {
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
			File imgFile = new File(fileName);
		
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
