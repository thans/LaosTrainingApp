package com.uwcse.morepractice;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SearchView.OnQueryTextListener;

public class TrainingPackageNavigation extends Activity {
	
	private static final String TAG = TrainingPackageNavigation.class.getSimpleName();
	public static final String INTENT_KEY_NAME = "packageName";
	private static File[] FILES;
	private String[] fileNames;
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
        fileNames = new String[FILES.length];
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
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.package_navigation, menu);
 
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setQueryHint(getString(R.string.search_packages));
        
        searchView.setOnQueryTextListener(new OnQueryTextListener() {    
            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return false;
            }
            
            @Override
            public boolean onQueryTextSubmit(String query) { 
                hideKeyboard();
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }
    
    /**
     * Performs the search by filtering package names
     * @param search, the text to search for
     */
    public void performSearch(String search) {
        List<String> filteredList = new ArrayList<String>();
        for (String name : fileNames) {
            if (name.toLowerCase().startsWith(search.toLowerCase())) {
                filteredList.add(name);
            }
        }
        // the new array to give to the adapter
        String[] filteredArray = filteredList.toArray(new String[filteredList.size()]);
        adapter = null;
        adapter = new MyViewAdapter(this, filteredArray, packageName, R.layout.row_grid );
        
        // Construct the gridView, sending in the files and the absolute path where the files reside
        gridview.setAdapter(adapter);
    }

    /**
     * Hides the keyboard
     * @param view, the view that brought up the keyboard
     */
    private void hideKeyboard() {
        InputMethodManager inputManager = (InputMethodManager) this
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        //check if no view has focus:
        View v=this.getCurrentFocus();
        if(v != null)
            inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }
    
    
    @Override
    protected void onPause() {
      super.onPause();
      hideKeyboard();
    }
}
