package com.uwcse.morepractice;

import java.io.File;

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
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.AdapterView.OnItemClickListener;

public class TrainingPackageNavigation extends Activity {
	
	private static final String TAG = TrainingPackageNavigation.class.getSimpleName();
	public static final String INTENT_KEY_NAME = "packageName";
	private static File[] FILES;
	private int currentFile;
	private String packageName;
	private GridView gridview;
	private MyViewAdapter adapter;
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_training_navigation);
		
		packageName = getIntent().getExtras().getString(INTENT_KEY_NAME);
		
		// Start activity button
		final Button button = (Button) findViewById(R.id.button_start);
	    button.setOnClickListener(new View.OnClickListener() {
	    	public void onClick(View v) {
	    		 Intent intent = new Intent(TrainingPackageNavigation.this, TrainingPackageActivity.class);
	             intent.putExtra(TrainingPackageActivity.INTENT_KEY_NAME, packageName);
	             intent.putExtra(TrainingPackageActivity.POSITION, 0);
	             startActivity(intent);
	        }
	    });
		
		this.setTitle(getNameFromPath(packageName)); //fileNameParts[fileNameParts.length - 1].split("\\.")[0]); // set the title to the title of the training package
		
		FILES = getOrderedFiles(packageName);
		
		currentFile = 0;
        // GridView for layout
        gridview = (GridView) findViewById(R.id.gridview);

        // Get the short names of the files to populate the grid view
        String[] fileNames = new String[FILES.length];
        for(int i = 0; i < FILES.length; i++){
            fileNames[i] = FILES[i].getName();
        }
        //adapter = new MyViewAdapter(this, fileNames, appRoot.getAbsolutePath() + "/", R.layout.row_grid );
        adapter = new MyViewAdapter(this, fileNames, packageName, R.layout.row_grid );
        
        // Construct the gridView, sending in the files and the absolute path where the files reside
        gridview.setAdapter(adapter);

        //gridview.setVerticalSpacing(100);

        // Connect each grid to a new activity with a listener
        gridview.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int pos, long id) {
                Intent intent = new Intent(TrainingPackageNavigation.this, TrainingPackageActivity.class);
               
                // Reconstruct the full path of the file to send to the new activity
                TextView tv = (TextView) v.findViewById(R.id.item_text);
                //String name = appRoot.getAbsolutePath() + "/" + tv.getText();
                String name = packageName + "/" + tv.getText();
                Log.e(TAG, "packageName " + packageName);
                Log.e(TAG, "text " + tv.getText());
                // intent.putExtra(TrainingPackageActivity.INTENT_KEY_NAME, name);
                intent.putExtra(TrainingPackageActivity.INTENT_KEY_NAME, packageName);
                intent.putExtra(TrainingPackageActivity.POSITION, pos);
                startActivity(intent);
                
            		
            }
        });
        
      
	}
	
	private String getNameFromPath(String path) {
		String[] parts = path.split("/");
		return parts[parts.length - 1].split("\\.")[0];
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
	
	private String getExtension(String filename) {
		String[] parts = filename.split("\\.");
		return parts[parts.length - 1].toLowerCase();
	}
	
	public void showToast(String text) { 
    	Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
    
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
	}
}
