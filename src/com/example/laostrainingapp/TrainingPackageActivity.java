package com.example.laostrainingapp;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TrainingPackageActivity extends Activity {
	private static final String TAG = TrainingPackageActivity.class.getSimpleName();
	public static final String INTENT_KEY_NAME = "packageName";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_training_package);

		Log.e(TAG, "in on create");
		String retrievedName = getIntent().getExtras().getString(INTENT_KEY_NAME);
		addAppIdentifier(retrievedName);
		showFiles(retrievedName);
	}
	
	public void addAppIdentifier(String data) {
		Log.e(TAG, "adding app identifier");
	    LinearLayout layout = 
		        (LinearLayout) this.findViewById(R.id.activity_training_package_linear_layout);
		TextView text = new TextView(this);
		text.setText(data);
		layout.addView(text);
	}
	
	/**
	 * Determines and returns the type of the file based on its extension
	 * mp4 for videos
	 * jpg, png, and gif for images
	 * TODO all other file types are currently unsupported
	 * @param filename the filename to parse
	 * @return the {@link Filetype} associated with the filename
	 */
	private Filetype getType(String filename) {
		String extension = getExtension(filename);
		if (extension == null) {
			return Filetype.UNSUPPORTED;
		} else if (extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png") || extension.equals("gif")) {
			return Filetype.IMAGE;
		} else if (extension.equals("mp4")) {
			return Filetype.VIDEO;
		} else {
			return Filetype.UNSUPPORTED;
		}
	}
	
	/**
	 * Returns the extension of the given filename or null if there is no extension
	 * @param filename the file name to parse
	 * @return the extension of the given filename, or null if there is no extension
	 */
	private String getExtension(String filename) {
		String[] parts = filename.split("\\.");
		return parts[parts.length - 1].toLowerCase();
	}
	
	/**
	 * Inserts all of the files into the current activity
	 * TODO currently only inserts images
	 * TODO images are all thrown in, need to add back/forward functionality and separate out into separate activities
	 * @param directory the directory to show files for
	 */
	public void showFiles(String directory) {
	    LinearLayout layout = 
		        (LinearLayout) this.findViewById(R.id.activity_training_package_linear_layout);
	    File currentDir = new File(directory);
	    File[] files = currentDir.listFiles();
		for (File f : files) {
			String name = f.getName();
			Filetype type = getType(name);
			switch (type)  {
				case IMAGE:
					ImageView image = new ImageView(this);	
					Bitmap myBitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
					image.setImageBitmap(myBitmap);
					layout.addView(image);
					break;
				case VIDEO:
					// TODO - add the video in a VideoView to the page
					break;
				case TEXT:
					// TODO - parse the text file (though if this is our package_details, then we will actually do this first.
					break;
				case CSV:
					// TODO - parse the quiz
					break;
				case UNSUPPORTED:
					break;
			}
	    }
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.training_package, menu);
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
