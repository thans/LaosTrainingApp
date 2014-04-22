package com.example.laostrainingapp;

import java.io.File;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

public class TrainingPackageActivity extends Activity {
	private static final String TAG = TrainingPackageActivity.class.getSimpleName();
	public static final String INTENT_KEY_NAME = "packageName";
	private static File[] FILES;
	private int currentFile;
	
	private GestureDetector gestureDetector;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_training_package);
		gestureDetector = new GestureDetector(this, new MyGestureDetector(this));
		Log.e(TAG, "in on create");
		String retrievedName = getIntent().getExtras().getString(INTENT_KEY_NAME);
		addAppIdentifier(retrievedName);
		//showFiles(retrievedName);
		final LinearLayout layout = 
		        (LinearLayout) this.findViewById(R.id.activity_training_package_layout);
		FILES = getOrderedFiles(retrievedName);

		
		currentFile = 0;
		if (FILES.length > 0) {
			addToActivity(FILES[currentFile], layout);
		} else {
			showToast("No files to show");
		}
		
		//addNextButton(currentFile, layout);
		
        //showOrderedFilesFromText(retrievedName);
	}
	
	public void showNextFile() {
		LinearLayout layout = 
		        (LinearLayout) this.findViewById(R.id.activity_training_package_layout);
		currentFile++;
		if (currentFile < FILES.length) {
			addToActivity(FILES[currentFile], layout);
		} else {
			this.finish();
		}
		//addNextButton(currentFile, layout);
	}
	
	public void showPreviousFile() {
		LinearLayout layout = 
		        (LinearLayout) this.findViewById(R.id.activity_training_package_layout);
		currentFile--;
		if (currentFile >= 0) {
			addToActivity(FILES[currentFile], layout);
		}
	}
	
	public void addAppIdentifier(String data) {
		Log.e(TAG, "adding app identifier");
		LinearLayout layout = 
		        (LinearLayout) this.findViewById(R.id.activity_training_package_layout);
		TextView text = new TextView(this);
		text.setText(data);
		layout.addView(text);
	}
	
	/*
	public void addNextButton(final int currentFile, final LinearLayout layout) {
		Button next = new Button(this);
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		next.setWidth(100);
		
		next.setLayoutParams(params);
		if (currentFile + 1 < FILES.length) {
			
			next.setText("NEXT >>");
			next.setOnClickListener(new OnClickListener() {
	            public void onClick(View arg0) {
	                showNextFile(currentFile, layout);
	            }
	            
	        });
			//layout.setLayoutParams(lay);
			layout.addView(next);
		} else {
			final Activity act = this;
			next.setText("FINISH");
			next.setOnClickListener(new OnClickListener() {
	            public void onClick(View arg0) {
	                act.finish();
	            }
	            
	        });
			layout.addView(next);
		}
	} */
	
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
		        (LinearLayout) this.findViewById(R.id.activity_training_package_layout);
	    File currentDir = new File(directory);
	    File[] files = currentDir.listFiles();
        
        for (File f : files) {
            String name = f.getName();
            Filetype type = getType(name);
            final String path = f.getAbsolutePath();
            addToActivity(f, layout);
        }
	}
	
	public File[] getOrderedFiles(String directory) {
        File currentDir = new File(directory);
        File[] files = currentDir.listFiles();
        
        // finds and parses the text file for order;
        // if text file is found, the files array will be ordered;
        // if not found, the array will remain the same
        return getSortedFiles(files);
        
        // if text file is not found, alert user
	}
	/*
	public void showOrderedFilesFromText(String directory) {
        
        // shows ordered files
        for (File f : files) {
            String name = f.getName();
            Filetype type = getType(name);
            final String path = f.getAbsolutePath();
            addToActivity(type, path, layout, f);
        }
    }*/

	private File[] getSortedFiles(File[] files) {
		// TODO Auto-generated method stub
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

	private void addToActivity(File f, LinearLayout layout) {
		// params unused
		Filetype type = getType(f.getName());
		String path = f.getAbsolutePath();
		layout.removeAllViews();
		switch (type)  {
	        case IMAGE:
	            ImageView image = new ImageView(this);  
	            Bitmap myBitmap = BitmapFactory.decodeFile(f.getAbsolutePath());
	            image.setImageBitmap(myBitmap);
	            image.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));//new LinearLayout.LayoutParams(900, 600));
	            image.setBackgroundColor(getResources().getColor(android.R.color.background_dark));
	            layout.addView(image);
	            break;
	        case VIDEO:
	        	Log.e(TAG, "showing video");
				// TODO - add the video in a VideoView to the page
				final VideoView video = new VideoView(this); //(VideoView) findViewById(R.id.VideoView);
				MediaController mediacontroller = new MediaController(this);
				mediacontroller.setAnchorView(video);
				video.setMediaController(mediacontroller);
				video.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));//new LinearLayout.LayoutParams(900, 600));
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
	
    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        super.dispatchTouchEvent(e);
        return gestureDetector.onTouchEvent(e);
    }
	
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
