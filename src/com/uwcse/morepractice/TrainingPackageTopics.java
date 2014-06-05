package com.uwcse.morepractice;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class TrainingPackageTopics extends Activity {

	private static final String TAG = TrainingPackageActivity.class.getSimpleName();
	public static final String INTENT_KEY_NAME = "packageName";
	public static final String POSITION = "0";
	private static File[] FILES;
	private int currentFile;
	private String packageName;
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_training_topics);
		//gestureDetector = new GestureDetector(this, new MyGestureDetector(this)); no swiping for now - make sure to uncomment dispatchTouchEvent
		
		packageName = getIntent().getExtras().getString(INTENT_KEY_NAME);
		
		//showFiles(retrievedName);
		this.setTitle(getNameFromPath(packageName)); //fileNameParts[fileNameParts.length - 1].split("\\.")[0]); // set the title to the title of the training package
		
		//FILES = getOrderedFiles(packageName);
		Log.e(TAG, "in on create");
		//currentFile = 0;
		
		//currentFile = getIntent().getExtras().getInt(POSITION);
		packageName = getIntent().getExtras().getString(INTENT_KEY_NAME);
		// set up the back and next buttons
		ImageButton backButton = (ImageButton) this.findViewById(R.id.back_button);
		final TrainingPackageTopics activity = this;
		
		Button learning = (Button) this.findViewById(R.id.button_learning);
		
		learning.setOnClickListener(new View.OnClickListener() {
	    	public void onClick(View v) {
	    		 Intent intent = new Intent(TrainingPackageTopics.this, TrainingPackageActivity.class);
	             intent.putExtra(TrainingPackageActivity.INTENT_KEY_NAME, packageName);
	             intent.putExtra(TrainingPackageActivity.POSITION, 0);
	             startActivity(intent);
	        }
	    });
		
		
		File currentDir = new File(packageName);
        FILES = currentDir.listFiles();
		
        
        final ArrayList<String> quizzes = new ArrayList<String>();
        final ArrayList<String> videos = new ArrayList<String>();
        final ArrayList<String> refs = new ArrayList<String>();
        
        for(File f: FILES) {
        	String extension = getExtension(f.getName());
        	if (extension.equals("mp4")) {
    			videos.add(f.getName());
    		} else if (extension.equals("csv")) {
    			quizzes.add(f.getName());
    		} else if (!f.isDirectory()) {
    			refs.add(f.getName());
    		}
        }

		Button quiz = (Button) this.findViewById(R.id.button_quizzes);
		
		quiz.setOnClickListener(new View.OnClickListener() {
	    	public void onClick(View v) {
	    		 Intent intent = new Intent(TrainingPackageTopics.this, Topic.class);
	             intent.putStringArrayListExtra("files", quizzes);
	    		 intent.putExtra(TrainingPackageActivity.INTENT_KEY_NAME, packageName);
	             //intent.putExtra(TrainingPackageActivity.POSITION, 0);
	             startActivity(intent);
	        }
	    });
		
		Button video = (Button) this.findViewById(R.id.button_videos);
		
		video.setOnClickListener(new View.OnClickListener() {
	    	public void onClick(View v) {
	    		 Intent intent = new Intent(TrainingPackageTopics.this, Topic.class);
	             intent.putStringArrayListExtra("files", videos);
	    		 intent.putExtra(TrainingPackageActivity.INTENT_KEY_NAME, packageName);
	             //intent.putExtra(TrainingPackageActivity.POSITION, 0);
	             startActivity(intent);
	        }
	    });
		
		Button references = (Button) this.findViewById(R.id.button_ref);
		
		references.setOnClickListener(new View.OnClickListener() {
	    	public void onClick(View v) {
	    		 Intent intent = new Intent(TrainingPackageTopics.this, Topic.class);
	             intent.putStringArrayListExtra("files", refs);
	    		 intent.putExtra(TrainingPackageActivity.INTENT_KEY_NAME, packageName);
	             //intent.putExtra(TrainingPackageActivity.POSITION, 0);
	             startActivity(intent);
	        }
	    });
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
	
	private String getNameFromPath(String path) {
		String[] parts = path.split("/");
		return parts[parts.length - 1].split("\\.")[0];
	}
}
