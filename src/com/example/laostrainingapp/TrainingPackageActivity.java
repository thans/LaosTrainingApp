package com.example.laostrainingapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

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
		//showFiles(retrievedName);
            showOrderedFilesFromText(retrievedName);
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
		} else if (extension.equals("txt")) {
		    return Filetype.TEXT;
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
	    showToast("showfiles");
	    LinearLayout layout = 
		        (LinearLayout) this.findViewById(R.id.activity_training_package_linear_layout);
	    File currentDir = new File(directory);
	    File[] files = currentDir.listFiles();
	    
	    // for debugging
        for (File f : files) {
            showToast(f.getName());
        }
        
		for (File f : files) {
			String name = f.getName();
			Filetype type = getType(name);
			final String path = f.getAbsolutePath();
			switch (type)  {
				case IMAGE:
					ImageView image = new ImageView(this);	
					Bitmap myBitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
					image.setImageBitmap(myBitmap);
					layout.addView(image);
					break;
				case VIDEO:
					// TODO - add the video in a VideoView to the page
					
					Button toVideo = new Button(this);
					toVideo.setText(path);
					toVideo.setOnClickListener(new OnClickListener() {
						public void onClick(View arg0) {
							Intent myIntent = new Intent(TrainingPackageActivity.this, VideoActivity.class);
							
							myIntent.putExtra("VIDEO_NAME", path);
							startActivity(myIntent);
						}
						
					});
					layout.addView(toVideo);
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
	
	public void showOrderedFilesFromText(String directory) {
	    showToast("show ordered files from text");
        LinearLayout layout = 
                (LinearLayout) this.findViewById(R.id.activity_training_package_linear_layout);
        File currentDir = new File(directory);
        File[] files = currentDir.listFiles();
        
        List<File> fileList = findTextFileAndParse(files);
        //List<String> fileNames = new ArrayList<String>();
        
        // shows ordered files
        for (File f : fileList) {
            String name = f.getName();
            Filetype type = getType(name);
            final String path = f.getAbsolutePath();
            switch (type)  {
                case IMAGE:
                    ImageView image = new ImageView(this);  
                    Bitmap myBitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
                    image.setImageBitmap(myBitmap);
                    layout.addView(image);
                    break;
                case VIDEO:
                    // TODO - add the video in a VideoView to the page
                    
                    Button toVideo = new Button(this);
                    toVideo.setText(path);
                    toVideo.setOnClickListener(new OnClickListener() {
                        public void onClick(View arg0) {
                            Intent myIntent = new Intent(TrainingPackageActivity.this, VideoActivity.class);
                            
                            myIntent.putExtra("VIDEO_NAME", path);
                            startActivity(myIntent);
                        }
                        
                    });
                    layout.addView(toVideo);
                case TEXT:
                    // do nothing
                    break;
                case CSV:
                    // TODO - parse the quiz
                    break;
                case UNSUPPORTED:
                    break;
            }
        }
    }
	
	// finds and parses the text file that contains the order of the packages
	private List<File> findTextFileAndParse(File[] files) {
	    List<File> fileList = new ArrayList<File>();
	    for (File f : files) {
            String name = f.getName();
            Filetype type = getType(name);
            String path = f.getAbsolutePath();
            if (type == Filetype.TEXT) {
                TextParser parser = new TextParser(path, files);
                // gets the ordered files, minus the text file
                if (parser.getNumInconsistency() > 0) {
                    showToast("Inconsistency between text file and directory.");
                }
                fileList = parser.getOrderedFiles();
                //fileNames = parser.getOrderedFileNames();
                break;
            } else {
                continue;
            }
        }
	    return fileList;
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
	
	public void showToast(String text) { 
    	Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
    
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
	}
}
