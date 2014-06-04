package com.uwcse.morepractice;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;
import android.widget.AdapterView.OnItemClickListener;

public class Single extends Activity {
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
		setContentView(R.layout.single);
		//gestureDetector = new GestureDetector(this, new MyGestureDetector(this)); no swiping for now - make sure to uncomment dispatchTouchEvent
		
		packageName = getIntent().getExtras().getString(INTENT_KEY_NAME);
		
		//showFiles(retrievedName);
		//this.setTitle(getNameFromPath(packageName)); 
		
		LinearLayout layout = (LinearLayout) findViewById(R.id.single_layout);
		File file = new File(packageName);
		
		String path;
		
		Filetype type = getType(file.getName());
		
		path = file.getAbsolutePath();
		
		//String path = packageName.getAbsolutePath();
		//Log.v(TAG, "Adding a " + type.name() + " file to the activity. File: " + path);
		//layout.removeAllViews();
		switch (type)  {
	        case IMAGE:
	            ImageView image = new ImageView(this);  
	            Bitmap myBitmap = BitmapFactory.decodeFile(packageName);
	            image.setImageBitmap(myBitmap);
	            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
	            params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
	            image.setLayoutParams(params);
	            image.setBackgroundColor(getResources().getColor(android.R.color.background_dark));
	            layout.addView(image);
	            image.requestFocus();
	            break;
	        case VIDEO:
	        	Log.e(TAG, "showing video");
				// TODO - add the video in a VideoView to the page
				final VideoView video = new VideoView(this); //(VideoView) findViewById(R.id.VideoView);
				
				MediaController mediacontroller = new MediaController(this);
				mediacontroller.setAnchorView(video);
				video.setMediaController(mediacontroller);
				RelativeLayout.LayoutParams parameters = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
				video.setLayoutParams(parameters);
				video.setVideoPath(path);
				
				final ProgressDialog pDialog = new ProgressDialog(this);
				pDialog.setTitle("Video " + path);
				pDialog.setMessage("Buffering...");
				pDialog.setIndeterminate(false);
				pDialog.setCancelable(false);
				pDialog.show();
				//video.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
				layout.addView(video);
				video.requestFocus();
				video.setOnPreparedListener(new OnPreparedListener() {
					public void onPrepared(MediaPlayer mp){
						try {
							Thread.sleep(2000); // pause for a second before resuming
						} catch (InterruptedException e) {
							
						} finally {
							pDialog.dismiss();
							video.start();
						}
						
					}
				}); 
				break;
	        case PDF:
	        	Log.e(TAG, "Display PDF");
	        	System.out.println("File path: " +  path);
	        	Uri uriPath = Uri.fromFile(new File(path));
	        	Intent intent = new Intent(Intent.ACTION_VIEW);
	        	intent.setDataAndType(uriPath, "application/pdf");
	        	
	        	//intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	        	
	            startActivity(intent);
	            finish();
	            break;
	        	//startActivity(intent);
	        case TEXT:
	            // do nothing
	            break;
	        case CSV:
	        	Log.v(TAG, "CSV file encountered.");
	    		Intent cvsintent = new Intent(this, QuizActivity.class);
	    		Bundle bundle = new Bundle();
	    		bundle.putString(QuizActivity.QUIZ_FILE_FULL_PATH_KEY, path);
	    		cvsintent.putExtras(bundle);
	    		this.startActivityForResult(cvsintent, QuizActivity.GET_QUIZ_SCORE_REQUEST);
	            break;
	        case UNSUPPORTED:
	            break;
	    }
		
		
	}
	
	
	/**
	 * Determines and returns the type of the file based on its extension
	 * mp4 for videos
	 * jpg, png, and gif for images
	 * csv for quiz files
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
		} else if (extension.equals("csv")) {
			return Filetype.CSV;
		} else if (extension.equals("pdf")) {
			return Filetype.PDF;
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
}
