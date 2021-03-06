package com.uwcse.morepractice;
import android.net.Uri;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

public class TrainingPackageActivity extends Activity {
	private static final String TAG = TrainingPackageActivity.class.getSimpleName();
	public static final String INTENT_KEY_NAME = "packageName";
	public static final String POSITION = "0";
	private static File[] FILES;
	private int currentFile;
	private String packageName;
	
	private GestureDetector gestureDetector;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_training_package);
		
		packageName = getIntent().getExtras().getString(INTENT_KEY_NAME);
		
		//showFiles(retrievedName);
		this.setTitle(getNameFromPath(packageName)); //fileNameParts[fileNameParts.length - 1].split("\\.")[0]); // set the title to the title of the training package
		final RelativeLayout layout = 
		        (RelativeLayout) this.findViewById(R.id.activity_training_package_layout);
		
		FILES = getOrderedFiles(packageName);

		currentFile = getIntent().getExtras().getInt(POSITION);
		
		// set up the back and next buttons
		ImageButton backButton = (ImageButton) this.findViewById(R.id.back_button);
		final TrainingPackageActivity activity = this;
		backButton.setOnClickListener(new OnClickListener() {
			@Override
            public void onClick(View arg0) {
                activity.showPreviousFile();
            }
        });
		ImageButton nextButton = (ImageButton) this.findViewById(R.id.next_button);
		nextButton.setOnClickListener(new OnClickListener() {
			@Override
            public void onClick(View arg0) {
                activity.showNextFile();
            }
        });
		
		activity.navigateTo(currentFile);
	}
	
	/**
	 * Show the next file in the current package, or close if the activity if there are no more
	 */
	public void showNextFile() {
		RelativeLayout layout = 
		        (RelativeLayout) this.findViewById(R.id.activity_training_package_layout);
		currentFile++;
		if (currentFile < FILES.length) {
			addToActivity(FILES[currentFile], layout);
		} else {
			this.finish();
		}
	}
	
	/**
	 * Show the previous file in the current package.  Do not go past the beginning!
	 */
	public void showPreviousFile() {
		RelativeLayout layout = 
		        (RelativeLayout) this.findViewById(R.id.activity_training_package_layout);
		currentFile--;
		if (currentFile >= 0) {
			addToActivity(FILES[currentFile], layout);
		} else {
			currentFile = 0;
		}
	}
	
	/**
	 * Refresh the current file in the current package.
	 */
	public void refreshFile() {
		RelativeLayout layout = 
		        (RelativeLayout) this.findViewById(R.id.activity_training_package_layout);
		addToActivity(FILES[currentFile], layout);
	}
	
	/**
	 * Navigate to the file at the given position
	 * @param pos the file to navigate to
	 */
	private void navigateTo(int pos) {
		RelativeLayout layout = 
		        (RelativeLayout) this.findViewById(R.id.activity_training_package_layout);
		currentFile = pos;
		if (currentFile < FILES.length) {
			addToActivity(FILES[currentFile], layout);
		} else {
			this.finish();
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
	
	private String getNameWithoutExtension(String filename) {
	    String[] parts = filename.split("\\.");
	    return parts[0].toLowerCase();
	}
	
	public File[] getOrderedFiles(String directory) {
        File currentDir = new File(directory);
        File[] files = currentDir.listFiles();
        List<File> list = new ArrayList<File>();
        // remove all folders in package directory
        for (File f : files) {
            String name = f.getName();
            if (!f.isDirectory() && !getNameWithoutExtension(f.getName()).equals("img")) {
                list.add(f);
                System.out.println("/////////////////ADDED in activity " + name);
            } else {
                System.out.println("/////////////////REMOVED in activity" + name);
            }
        }
        
        // finds and parses the text file for order;
        // if text file is found, the files array will be ordered;
        // if not found, the array will remain the same
        return getSortedFiles(list.toArray(new File[list.size()]));
	}

	private File[] getSortedFiles(File[] files) {
		boolean textFileFound = false;
		for (File f : files) {
            String name = f.getName();
            Filetype type = getType(name);
            String path = f.getAbsolutePath();
            if (type == Filetype.TEXT) {
                textFileFound = true;
                TextParser parser = new TextParser(path, files);
                files = parser.getOrderedFiles();
                break;
            } else {
                continue;
            }
        }
		return files;
	}

	private void addToActivity(File f, RelativeLayout layout) {
		// params unused
		Filetype type = getType(f.getName());
		String path = f.getAbsolutePath();
		layout.removeAllViews();
		switch (type)  {
	        case IMAGE:
	            ImageView image = new ImageView(this);  
	            Bitmap myBitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
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
	        	System.out.println("File path: " +  path);
	        	Uri uriPath = Uri.fromFile(new File(path));
	        	Intent intent = new Intent(Intent.ACTION_VIEW);
	        	intent.setDataAndType(uriPath, "application/pdf");
	            startActivity(intent);
	            finish();
	            break;
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
				TrainingPackageActivity.this.refreshFile();
			}
    	});
    	group.addView(button);
    	
    	// Add the linear layout to the activity layout
    	RelativeLayout layout = (RelativeLayout) findViewById(R.id.activity_training_package_layout);
    	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
    			LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    	params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
    	params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
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
    	RelativeLayout layout = (RelativeLayout) findViewById(R.id.activity_training_package_layout);
    	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
    			LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    	params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
    	params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
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
				TrainingPackageActivity.this.refreshFile();
			}
    	});
    	RelativeLayout layout = (RelativeLayout) findViewById(R.id.activity_training_package_layout);
    	RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
    			LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    	params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
    	params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
    	layout.addView(button, params);
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.training_package, menu);
        return true;
    }
	
	private String getNameFromPath(String path) {
		String[] parts = path.split("/");
		return parts[parts.length - 1].split("\\.")[0];
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
