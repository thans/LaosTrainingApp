package com.example.laostrainingapp;

import java.io.*;
import java.util.*;
import java.util.zip.*;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	public static final String LANGUAGE_KEY = "language";
    private static final String TAG = MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        ActionBar actionBar = getActionBar();
        actionBar.show();
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();

    	
        // for first design iteration
        String laosFilePath = getIntent().getExtras().getString(LANGUAGE_KEY); //baseDir + "/" + getString(R.string.local_storage_folder);
        File baseFolder = new File(baseDir);
        File downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        /*
        try {
            File zipFile = new File(downloadFolder, "LaosTrainingApp.zip");
            downloadZip(zipFile, baseFolder, laosFilePath);
        } catch (ZipException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/
        
    	
    	 final File appRoot = new File(laosFilePath);
         File[] files = appRoot.listFiles();
         
         // GridView for layout
         GridView gridview = (GridView) findViewById(R.id.gridview);
         
         // Get the short names of the files to populate the grid view
         String[] fileNames = new String[files.length];
         
         for(int i = 0; i < files.length; i++){
        	 fileNames[i] = files[i].getName();
         }
         
         // Construct the gridView, sending in the files and the absolute path where the files reside
         gridview.setAdapter(new MyViewAdapter(this, fileNames, appRoot.getAbsolutePath() + "/", R.layout.row_grid ));
         
         //gridview.setVerticalSpacing(100);
         
    	 // Connect each grid to a new activity with a listener
         gridview.setOnItemClickListener(new OnItemClickListener() {
    	        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
    	        	Intent intent = new Intent(MainActivity.this, TrainingPackageActivity.class);
    	        	
    	        	// Reconstruct the full path of the file to send to the new activity
    	        	TextView tv = (TextView) v.findViewById(R.id.item_text);
    	        	String name = appRoot.getAbsolutePath() + "/" + tv.getText();
    	        	intent.putExtra(TrainingPackageActivity.INTENT_KEY_NAME, name);
    	    		startActivity(intent);
    	        }
    	});
        
    }
		
    public void addTrainingPackageButtons(String baseDir) {
        //LinearLayout layout = 
    	    //    (LinearLayout) this.findViewById(R.id.activity_main_linear_layout);
        Log.e(TAG, "checking in: " + R.string.local_storage_folder);
        File appRoot = new File(baseDir + "/" + getString(R.string.local_storage_folder));
        File[] files = appRoot.listFiles();

        for (File f : files) {
        
        	Button toTrainingPackage = new Button(this);
    		Log.e(TAG, f.getName());
    		Log.e(TAG, f.getPath());
    		
    		// Gets the size of the current window and stores it using a Point
    		Display display = getWindowManager().getDefaultDisplay(); 
    		Point size = new Point();
    		
    		display.getSize(size);
    		toTrainingPackage.setWidth(size.x / 3);
    
    		toTrainingPackage.setHeight(size.y / 3);
    		
    		toTrainingPackage.setTextSize(50);
    		
    		toTrainingPackage.setBackgroundColor(Color.parseColor("#4169e1"));
    		
    		
    		
    		toTrainingPackage.setText(f.getName());
    		toTrainingPackage.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    		
    		Intent intent = new Intent(MainActivity.this, TrainingPackageActivity.class);
            intent.putExtra(TrainingPackageActivity.INTENT_KEY_NAME, f.getAbsolutePath());
    		toTrainingPackage.setOnClickListener(new TrainingPackageClickListener((Activity) this, intent));
    		//layout.addView(toTrainingPackage);
    	}
    }

   /* public void addTextProgrammatically(String baseDir) {
        LinearLayout layout = 
            (LinearLayout) this.findViewById(R.id.activity_main_linear_layout);
        
    	File appRoot = new File(baseDir + "/LaosTrainingApp/");
    	File[] files = appRoot.listFiles();
    	Log.e(TAG, appRoot.getAbsolutePath());
    //		String data = "Files currently in " + appRoot.getAbsolutePath();
    	TextView text = new TextView(this);
    	text.setText("Files in the LaosTrainingApp directory:");
    	layout.addView(text);
    	for (File f : files) {
    		TextView tv = new TextView(this);
    		tv.setText(f.getPath());
    		layout.addView(tv);
    		Log.e(TAG, f.getPath());
    	}
    }
	
    public void addImage(String baseDir) {
        LinearLayout layout = 
                (LinearLayout) this.findViewById(R.id.activity_main_linear_layout);
    
        File appRoot = new File(baseDir + "/LaosTrainingApp/");
        File[] files = appRoot.listFiles();
        for (File f : files) {
            Log.e(TAG, f.getPath());
    
            ImageView image = new ImageView(this);
            String fileName = f.getAbsolutePath();
            File imgFile = new File(fileName);
    
            if(imgFile.exists()){
                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                image.setImageBitmap(myBitmap);
            } else {
                Log.e(TAG, imgFile.getAbsolutePath() + " permission to read " + imgFile.canRead());
            }
    
            layout.addView(image);
        }
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
        if (id == R.id.action_download) {
            new ActionBarFunctions().downloadsActivity(this);
            return true;
        }
        if (id == R.id.action_quiz) {
        	new ActionBarFunctions().quizActivity(this);
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
    
    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            return rootView;
        }
    }
   
    // for first design iteration
    public void downloadZip(File zipFile, File baseFolder, String laosFilePath) throws ZipException, IOException {
        
        File oldLaosFolder = new File(laosFilePath);
        
        // make sure to unzip zipfile once
        boolean zipFileExists = zipFile.exists();
        boolean folderExists = oldLaosFolder.exists();
        
        if (zipFileExists && folderExists) {
            delete(oldLaosFolder);
            unzip(zipFile, baseFolder);
        } else if (zipFileExists && !folderExists) {
            unzip(zipFile, baseFolder);
        } else if (!zipFileExists && !folderExists) {
            showToast("LaosTrainingApp.zip not found.");
        }
    }
    
    // delete the given directly by recursively deleting its contents
    public void delete(File file) throws IOException {
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                delete(f);
                System.out.println("deleted " + f.getName());
            }
        }
        if (!file.delete())
            throw new FileNotFoundException("Failed to delete file: " + file.getName());
    }

    // unzips the given file into the target directory
    public void unzip(File file, File targetDir) throws ZipException, IOException {
        ZipFile zipFile = new ZipFile(file);
        try {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                File targetFile = new File(targetDir, entry.getName());
                if (entry.isDirectory()) {
                    targetFile.mkdirs();
                } else {
                    InputStream input = zipFile.getInputStream(entry);
                    try {
                        OutputStream output = new FileOutputStream(targetFile);
                        try {
                            copyContents(input, output);
                        } finally {
                            output.close();
                        }
                    } finally {
                        input.close();
                    }
                }
            }
        } finally {
            zipFile.close();
        }
        
        // to make sure that the zip file gets unzipped only once, 
        // delete as soon as it's unzipped
        file.delete();
        System.out.println("deleted zipfile");
    }

    // copy the contents of a file to the given output
    private static void copyContents(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[4096];
        int size;
        while ((size = input.read(buffer)) != -1)
            output.write(buffer, 0, size);
    }
}
