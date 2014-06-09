package com.uwcse.morepractice;

import java.io.File;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.VideoView;

public class Single extends Activity {
	private static final String TAG = TrainingPackageActivity.class.getSimpleName();
	public static final String INTENT_KEY_NAME = "packageName";
	public static final String POSITION = "0";
	private String packageName;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.single);
		
		packageName = getIntent().getExtras().getString(INTENT_KEY_NAME);
		
		LinearLayout layout = (LinearLayout) findViewById(R.id.single_layout);
		File file = new File(packageName);
		
		String path;
		
		Filetype type = getType(file.getName());
		
		path = file.getAbsolutePath();
		
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
				final VideoView video = new VideoView(this);
				
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
	        	
	            startActivity(intent);
	            finish();
	            break;
	        	//startActivity(intent);
	        case TEXT:
	            // do nothing
	            break;
	        case CSV:
	        	Log.v(TAG, "CSV file encountered.");
	    		Intent csvIntent = new Intent(this, QuizActivity.class);
	    		Bundle bundle = new Bundle();
	    		bundle.putString(QuizActivity.QUIZ_FILE_FULL_PATH_KEY, path);
	    		csvIntent.putExtras(bundle);
	    		this.startActivityForResult(csvIntent, QuizActivity.GET_QUIZ_SCORE_REQUEST);
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (requestCode == QuizActivity.GET_QUIZ_SCORE_REQUEST) {
	        if (resultCode == RESULT_OK) {
	        	// If the activity result was successful, display the quiz score and a "Try again?"
	        	// button in the UI
	        	String quizScore = data.getExtras().getString(QuizActivity.QUIZ_SCORE_KEY);
	        	int numStars = data.getExtras().getInt(QuizActivity.QUIZ_SCORE_STAR_KEY);
	        	showQuizScore(quizScore, numStars);
	        } else if (resultCode == RESULT_CANCELED) {
	        	// If the activity result was not successful, check to see if an error message
	        	// was returned
	        	if (data == null) {
	        		// The user must have pressed the back button to close the activity
	        		showQuizButton();
	        	} else if (data.hasExtra(QuizActivity.QUIZ_ERROR_MSG_KEY)) {
		        	String errorMsg = data.getExtras().getString(QuizActivity.QUIZ_ERROR_MSG_KEY);
	        		showQuizErrorMsg(errorMsg);
	        	}
	        }
	    }
	}

	/**
	 * Add the user's quiz score and the star score-feedback icons to the UI.
	 * @param quizScore The user's quiz score.
	 * @param numStars The number of star icons to display.
	 */
	private void showQuizScore(String quizScore, int numStars) {
    	// Create a new linear layout
		LinearLayout group = new LinearLayout(this);
    	group.setOrientation(LinearLayout.VERTICAL);

    	// Get the star score-feedback icons
		LinearLayout stars = getQuizStarIcons(numStars);
    	group.addView(stars);
    	
    	// Create a text view to display the quiz score
    	TextView textView = new TextView(this);
    	textView.setText(getResources().getString(R.string.score) + "\n" + quizScore);
    	textView.setTextSize(40);
    	textView.setGravity(Gravity.CENTER);
    	group.addView(textView);
    	
    	// Create the "Try again?" button
    	Button button = new Button(this);
    	button.setText(R.string.try_again);
    	button.setTextSize(32);
    	button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Single.this.recreate();
			}
    	});
    	group.addView(button);
    	
    	// Add the linear layout to the activity layout
    	LinearLayout layout = (LinearLayout) findViewById(R.id.single_layout);
    	layout.setGravity(Gravity.CENTER);
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
    			LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    	layout.addView(group, params);
	}
	
	/**
	 * Returns a linear layout with star icons to display for quiz score feedback.
	 * @param numStars The number of star icons to display.
	 * @return The linear layout containing the icons.
	 */
	private LinearLayout getQuizStarIcons(int numStars) {
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.HORIZONTAL);
		int numEmptyStars = 5 - numStars;
		for (int i = 0; i < numStars; i++) {
			ImageView image = new ImageView(this);
			image.setImageResource(R.drawable.star_filled);
			ll.addView(image);
		}
		for (int i = 0; i < numEmptyStars; i++) {
			ImageView image = new ImageView(this);
			image.setImageResource(R.drawable.star_empty);
			ll.addView(image);
		}
		return ll;
	}

	/**
	 * Add an error message to the UI describing why the quiz activity was not successful.
	 * @param errorMsg The error message to display.
	 */
	private void showQuizErrorMsg(String errorMsg) {
    	TextView textView = new TextView(this);
    	textView.setText(errorMsg);
    	textView.setTextSize(30);
    	textView.setTypeface(null, Typeface.ITALIC);
    	LinearLayout layout = (LinearLayout) findViewById(R.id.single_layout);
    	layout.setGravity(Gravity.CENTER);
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
    			LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    	layout.addView(textView, params);
	}
	
	/**
	 * Add a "Take quiz" button to the UI.
	 */
	private void showQuizButton() {
    	Button button = new Button(this);
    	button.setText(R.string.take_quiz);
    	button.setTextSize(32);
    	button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Single.this.recreate();
			}
    	});
    	LinearLayout layout = (LinearLayout) findViewById(R.id.single_layout);
    	layout.setGravity(Gravity.CENTER);
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
    			LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    	layout.addView(button, params);
	}
}
