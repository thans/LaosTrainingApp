package com.uwcse.morepractice;

import java.io.File;

import com.uwcse.morepractice.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
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
		//gestureDetector = new GestureDetector(this, new MyGestureDetector(this)); no swiping for now - make sure to uncomment dispatchTouchEvent
		
		packageName = getIntent().getExtras().getString(INTENT_KEY_NAME);
		
		//showFiles(retrievedName);
		this.setTitle(getNameFromPath(packageName)); //fileNameParts[fileNameParts.length - 1].split("\\.")[0]); // set the title to the title of the training package
		final RelativeLayout layout = 
		        (RelativeLayout) this.findViewById(R.id.activity_training_package_layout);
		
		FILES = getOrderedFiles(packageName);
		Log.e(TAG, "in on create");
		//currentFile = 0;
		
		currentFile = getIntent().getExtras().getInt(POSITION);
		
		// set up the back and next buttons
		Button backButton = (Button) this.findViewById(R.id.back_button);
		final TrainingPackageActivity activity = this;
		backButton.setOnClickListener(new OnClickListener() {
			@Override
            public void onClick(View arg0) {
                activity.showPreviousFile();
            }
        });
		Button nextButton = (Button) this.findViewById(R.id.next_button);
		nextButton.setOnClickListener(new OnClickListener() {
			@Override
            public void onClick(View arg0) {
                activity.showNextFile();
            }
        });
		
		// let them choose where in the package to start
		//showPackageContents();
		
		activity.navigateTo(currentFile);
		
	}


	private void showPackageContents() {
		String names[] = new String[FILES.length]; //{"A","B","C","D"};
		for (int i = 0; i < FILES.length; i++) {
			names[i] = getNameFromPath(FILES[i].getPath());
		}
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        LayoutInflater inflater = getLayoutInflater();
        View convertView = (View) inflater.inflate(R.layout.list, null);
        alertDialog.setView(convertView);
        alertDialog.setTitle(getNameFromPath(packageName));
        ListView lv = (ListView) convertView.findViewById(R.id.navigate_list);
        final TrainingPackageActivity activity = this;
        lv.setOnItemClickListener(new OnItemClickListener() {
        	@Override
        	public void onItemClick(AdapterView<?> adapter, View v, int pos, long arg4) {
        		Log.e(TAG, "Position clicked = " + pos);
        		activity.navigateTo(pos);
        		alertDialog.dismiss();
        	}
        });
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, names);
        lv.setAdapter(adapter);
        alertDialog.show();
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
	
	public File[] getOrderedFiles(String directory) {
        File currentDir = new File(directory);
        File[] files = currentDir.listFiles();
        
        // finds and parses the text file for order;
        // if text file is found, the files array will be ordered;
        // if not found, the array will remain the same
        return getSortedFiles(files);
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
                // gets the ordered files
                if (parser.getNumInconsistency() > 0) {
                    showToast("Inconsistency between text file and directory.");
                }
                files = parser.getOrderedFiles();
                break;
            } else {
                continue;
            }
        }
		if (!textFileFound) {
            showToast("text file not found;  order is random");
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
		Log.d("ID Clicked ", id + "");
		if (id == R.id.action_settings) {
			return true;
		} else if (id == R.id.action_navigate) {
			showPackageContents();
		}
		return super.onOptionsItemSelected(item);
	}
	


	public void showToast(String text) { 
    	Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
    
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
	}
	
//    @Override
//    public boolean dispatchTouchEvent(MotionEvent e) {
//        super.dispatchTouchEvent(e);
//        return gestureDetector.onTouchEvent(e);
//    }
	
	private static final int SWIPE_MIN_DISTANCE = 10;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	
	private class MyGestureDetector extends SimpleOnGestureListener {
		
		private TrainingPackageActivity activity;
		
		public MyGestureDetector(TrainingPackageActivity currentActivity) {
			super();
			this.activity = currentActivity;
		}
		
		public boolean onTouchEvent(MotionEvent e) {
			return true;
		}
		
	    @Override
	    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
	    	//showToast("fling");
	        try {
	            if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
	                return false;
	            if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
	                showToast("leftSwipe");
	                gotoNext();
	            }  else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
	            	showToast("rightSwipe");
	            	gotoPrevious();
	            }
	        } catch (Exception e) {
	            // nothing
	        }
	        return false;
	    }

		private void gotoPrevious() {
			activity.showPreviousFile();
			
		}

		private void gotoNext() {
			activity.showNextFile();
		}

	}
}
