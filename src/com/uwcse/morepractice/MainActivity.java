package com.uwcse.morepractice;

import java.io.*;
import java.util.*;

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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    public static final String LANGUAGE_KEY = "language";
    private static final String TAG = MainActivity.class.getSimpleName();
    private MyViewAdapter adapter;
    private String[] fileNames;
    private File appRoot;
    private GridView gridview;
    private Context context;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = getApplicationContext();
        
        ActionBar actionBar = getActionBar();
        actionBar.show();
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();

        // for first design iteration
        String laosFilePath = getIntent().getExtras().getString(LANGUAGE_KEY); 
        appRoot = new File(laosFilePath);
        File[] files = appRoot.listFiles();

        // GridView for layout
        gridview = (GridView) findViewById(R.id.gridview);

        // Get the short names of the files to populate the grid view
        fileNames = new String[files.length];
        for(int i = 0; i < files.length; i++){
            fileNames[i] = files[i].getName();
        }
        adapter = new MyViewAdapter(this, fileNames, appRoot.getAbsolutePath() + "/", R.layout.row_grid );
        // Construct the gridView, sending in the files and the absolute path where the files reside
        gridview.setAdapter(adapter);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
 
        final SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
        searchView.setQueryHint("Search for packages");
        
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
        adapter = new MyViewAdapter(this, filteredArray, appRoot.getAbsolutePath() + "/", R.layout.row_grid );
        
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
}
