package com.uwcse.morepractice;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.example.laostrainingapp.R;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.os.Build;

public class DownloadActivity extends Activity {
    static final int                REQUEST_ACCOUNT_PICKER = 1;
    static final int                REQUEST_AUTHORIZATION = 2;
    static final int                REQUEST_DOWNLOAD_FILE = 3;
    static final int                RESULT_STORE_FILE = 4;
    private static Uri              mFileUri;
    private static Drive            mService;
    private GoogleAccountCredential mCredential;
    private Context                 mContext;
    private List<File>              packageList;
    private ListView                mListView;
    private java.io.File[]          localPackages;
    private String[]                mFileArray;
    private String                  mDLVal;
    private ArrayAdapter<String>    mAdapter;
    private SearchView              search;
    
    private boolean                 deleteFirst;
    private java.io.File            targetDir;
    private EditText                inputSearch;
    
    SharedPreferences sp;  // persistent data that stores the last time the device updated files
    public static final String UPDATE = "update"; 
    private long                    lastUpdate;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_download);

    	getActionBar().setDisplayHomeAsUpEnabled(true);

        // setup for credentials for connecting to the Google Drive account
        mCredential = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(DriveScopes.DRIVE));
	    
        // start activity that prompts the user for their google drive account
        startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
	    
        mContext = getApplicationContext();
        
        sp = getPreferences(Context.MODE_PRIVATE);
    
        targetDir = new java.io.File(Environment.getExternalStorageDirectory(), 
                getString(R.string.local_storage_folder));
        localPackages = targetDir.listFiles();
        
        //setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.listView1);

        final Button button = (Button) findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		getDriveContents();
        	}
        });
    }

    /**
     * Gets the folders in the google drive account
     */
    private void getDriveContents() {
    	readPref();
        writePref();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                packageList = new ArrayList<File>();
                Files f1 = mService.files();
                Files.List request = null;
        
                do {
                    try { 
                        request = f1.list();
                
                        // get only zip files from drive 
                        //request.setQ("trashed=false and mimeType = 'application/zip'");
                        request.setQ("trashed=false and mimeType = 'application/vnd.google-apps.folder'");
                        //request.setQ("trashed=false and title = 'simple'");
                        //request.setQ("trashed=false"); // for getting all the files, recursively looking at all the folders
                        //request.setQ("'root' in parents and trashed=false"); // for getting all the files in root
                        FileList fileList = request.execute();
                        
                        packageList.addAll(fileList.getItems());
                        request.setPageToken(fileList.getNextPageToken());
                    } catch (UserRecoverableAuthIOException e) {
                        startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
                    } catch (IOException e) {
                        e.printStackTrace();
                        if (request != null) 
                            request.setPageToken(null);
                    }
                } while ((request.getPageToken() != null) 
                      && (request.getPageToken().length() > 0));
                
                processPackages();
            }
        });
        t.start();
    }
    
    
    /**
     * Looks through each drive package to see if it needs to be updated or pruned
     */
    private void processPackages() {
    	// list of package names in drive
        List<String> googleDrivePackages = new ArrayList<String>();
        
        // process each drive folder to see if it needs to be updated;
        // if so, get the folderId of the folder and add to map
        for (File f : packageList) {
            String folderName = f.getTitle();
            googleDrivePackages.add(folderName);
            java.io.File targetFolder = new java.io.File(targetDir, folderName);
            
            if (!targetFolder.exists()) {
                showToast("Package " + folderName + " does not exist locally; must download");
                System.out.println("Package " + folderName + " does not exist locally; must download");
                targetFolder.mkdirs();
                getPackageContents(f, targetFolder, true);
            } else if (targetFolder.exists() && checkTimeStamp(f)) {
                // folder has been modified in drive since last update
                DateTime date = f.getModifiedDate();
                System.out.println("Last modified date of package " + folderName + " is " + date.getValue());

                // delete any local files that have the same name: for package overwrite
                try {
                    deleteFile(targetFolder);
                    showToast("Deleting package folder " + folderName);
                    System.out.println("Deleting package folder" + folderName);
                    // guaranteed at this point that there is no file in local storage of the same name
                    targetFolder.mkdirs();
                    getPackageContents(f, targetFolder, true);
                } catch (IOException e) {
                    System.err.println("Delete of folder " + folderName + " failed");
                    e.printStackTrace();
                }
            } else {
                // f is an existing folder since last update: check last modified date of its contents
                getPackageContents(f, targetFolder, false);
            }
        }
        
        // prune unwanted packages from local storage
        pruneLocalPackages(googleDrivePackages);
    }

    
    /**
     * Deletes any package in local storage that is not in drive
     * @param googleDrivePackages, the list of package names in drive
     */
    private void pruneLocalPackages(List<String> googleDrivePackages) {
        if (!googleDrivePackages.isEmpty()) {
            // delete any package in local storage that is not also in the google drive account
            for (java.io.File localFile : localPackages) {
                if (!googleDrivePackages.contains(localFile.getName())) {
                    try {
                        deleteFile(localFile);
                        System.out.println("Deleting " + localFile.getName());
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    
    /**
     * Compares the time stamps on the given folder 
     * @return true if the last modified date of folder in drive account 
     *         is later than that of the time of last update
     */
    private boolean checkTimeStamp(File file) {
        // uses sharedpreferences
        return (lastUpdate < file.getModifiedDate().getValue());
    }
    
    
    /**
     * Gets the contents of a package
     * @param f, the package folder 
     * @param targetFolder, the package folder in local storage, currently empty
     */
    private void getPackageContents(final File f, final java.io.File targetFolder, final boolean download) {
    	Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                // list of contents in f
                List<File> mFileList = new ArrayList<File>();
                Files f1 = mService.files();
                Files.List request = null;
                int count = 0;
                do {
                    try { 
                        request = f1.list();
                
                        // get only the files from the given folder 
                        request.setQ("'" + f.getId() + "' in parents and trashed=false");
                        FileList fileList = request.execute();
                        
                        count += fileList.getItems().size();
                        
                        mFileList.addAll(fileList.getItems());
                        request.setPageToken(fileList.getNextPageToken());
                    } catch (UserRecoverableAuthIOException e) {
                        startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
                    } catch (IOException e) {
                        e.printStackTrace();
                        if (request != null) 
                            request.setPageToken(null);
                    }
                } while ((request.getPageToken() != null) 
                      && (request.getPageToken().length() > 0));
                
                //System.out.println("Package " + f.getTitle() + " has this many items: " + count);
                
                // populates the list view with all the zip files in the specified google drive account
                //populateListView(f, mFileList);
                
                if (download) {
                    // downloads the contents of the package within a folder of the same name
                    downloadPackage(mFileList, targetFolder);
                } else {
                    // compares with the contents of the local package of the same name
                    checkContents(mFileList, targetFolder);
                }
            }
        });
        t.start();
    }
    
    /**
     * Begins the downloading process of the given files from a google drive account package
     * @param mFileList, the package contents
     */
    private void downloadPackage(final List<File> mFileList, final java.io.File targetFolder) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                for(File tmp : mFileList) {
                    if (tmp.getDownloadUrl() != null && tmp.getDownloadUrl().length() > 0) {
                        try {
                            com.google.api.client.http.HttpResponse resp =
                                    mService.getRequestFactory()
                                    .buildGetRequest(new GenericUrl(tmp.getDownloadUrl()))
                                    .execute();
                            
                            // gets the file's contents
                            InputStream inputStream = resp.getContent();

                            // stores the contents to the device's external storage
                            try {
                                final java.io.File file = new java.io.File(targetFolder, tmp.getTitle());
                                System.out.println("Downloading: " + tmp.getTitle() + " to " + file.getPath());
                                storeFile(file, inputStream);
                            } finally {
                                inputStream.close();
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        t.start();
    }
    
    
    /**
     * Checks that the contents in drive package and the contents of local package are in sync;
     * If not in sync, prune or update
     * @param mFileList, the contents of the google drive package folder
     * @param localFolder, the package folder of the same name in local storage
     */
    private void checkContents(List<File> mFileList, java.io.File localFolder) {
        java.io.File[] localContents = localFolder.listFiles();
        List<String> driveContentNames = new ArrayList<String>();
        for (File driveContent : mFileList ) {
            driveContentNames.add(driveContent.getTitle());
            java.io.File temp = new java.io.File(localFolder, driveContent.getTitle());
            List<File> item = new ArrayList<File>();
            if (!temp.exists()) {
                // download 
                item.add(driveContent);
                showToast(driveContent.getTitle() + "does not exist; must download");
                System.out.println(driveContent.getTitle() + "does not exist; must download");
                downloadPackage(item, localFolder);
            } else if (temp.exists() && checkTimeStamp(driveContent)) {
                // needs to be updated
                showToast(driveContent.getTitle() + "does exist; but must be updated");
                System.out.println(driveContent.getTitle() + "does exist; but must be updated");
                try {
                    showToast("Deleting file " + temp.getName());
                    System.out.println("Deleting file " + temp.getName());
                    deleteFile(temp);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                // download
                item.add(driveContent);
                downloadPackage(item, localFolder);
            }
        }
        
        // prune unwanted files in the local package
        prunePackageContent(localContents, driveContentNames);
    }
    
    
    /**
     * Deletes any files in a local package folder
     * @param localContents, the contents of the local package folder to prune
     * @param driveContentNames, the contents of the drive package folder to compare against
     */
    private void prunePackageContent(java.io.File[] localContents, List<String> driveContentNames) {
        // for the case where the local package has something that the drive package doesn't
        for (java.io.File f : localContents) {
            if (!driveContentNames.contains(f.getName())) {
                try {
                    showToast("Deleting file " + f.getName());
                    System.out.println("Deleting file " + f.getName());
                    deleteFile(f);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }
    
    
    /**
     * Stores the file into local storage
     * @param file, the package content to be stored
     * @param inputStream 
     */
    private void storeFile(java.io.File file, InputStream inputStream) {
        try {
            final OutputStream oStream = new FileOutputStream(file);
            try {
                try {
                    final byte[] buffer = new byte[1024];
                    int read;
                    while ((read = inputStream.read(buffer)) != -1) {
                        oStream.write(buffer, 0, read);
                    }
                    oStream.flush();
                } finally {
                    oStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Deletes the given file by recursively deleting its contents
     * @param file, the file that gets deleted
     * @throws IOException
     */
    private void deleteFile(java.io.File file) throws IOException {
        if (file.isDirectory()) {
            for (java.io.File f : file.listFiles()) 
                deleteFile(f);
        }
        
        if (!file.delete())
            throw new FileNotFoundException("Failed to delete file: " + file.getName());
    }
    
    /**
     * Converts milliseconds to the corresponding date in the form dd/MM/yy
     * @param msec, the time in milliseconds to convert
     * @return the String date
     */
    private String getDateFromLong(Long msec) {
        Date date = new Date(msec);
        SimpleDateFormat df2 = new SimpleDateFormat("dd/MM/yy");
        return df2.format(date);
    }
    
    
    /**
     * Reads from SharedPreferences the date/time of last update
     */
    private void readPref() {
        // get the last update; if not set, lastUpdate gets 31/12/69
        lastUpdate = sp.getLong(UPDATE, 0);    
        System.out.println("Time of last update: " + getDateFromLong(lastUpdate));
    }
    
    /**
     * Writes to SharedPreferences the current date/time for the latest update
     */
    private void writePref() {
        SharedPreferences.Editor editor = sp.edit();
        long currentTime = 0;//System.currentTimeMillis();
        editor.putLong(UPDATE, currentTime);
        editor.commit();
        System.out.println("Time of current update: " + getDateFromLong(currentTime));
    }
    
    private void populateListView(final File f, final List<File> mFileList) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String[] FileArray = new String[mFileList.size()];
                //System.out.println("Number of files in folder " + f.getTitle() + " is " + mFileList.size());
                int i = 0;
                for(File tmp : mFileList) {
                    FileArray[i] = f.getTitle() + " : " + tmp.getTitle();
                    //System.out.println(FileArray[i]);
                    i++;
                }
                mAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, FileArray) {

                    @Override
                    public View getView(int position, View convertView,
                            ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);

                        TextView textView = (TextView) view.findViewById(android.R.id.text1);

                        textView.setTextColor(Color.GRAY);
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, 28);
                        textView.setHeight(85);
                        return view;
                    }
                };
                mListView.setAdapter(mAdapter);
            }
        });
    }
    
    
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        mCredential.setSelectedAccountName(accountName);
                        mService = getDriveService(mCredential);
                    }
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == Activity.RESULT_OK) {
                    //account already picked
                } else {
                    startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
                }
                break;
            case RESULT_STORE_FILE:
                mFileUri = data.getData();
                // Save the file to Google Drive
                //saveFileToDrive();
                break;
        }
    }
  
    private Drive getDriveService(GoogleAccountCredential credential) {
        return new Drive.Builder(AndroidHttp.newCompatibleTransport(), new GsonFactory(), credential)
            .build();
    }
    
    public void showToast(final String toast) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), toast, Toast.LENGTH_SHORT).show();
            }
        });
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
   
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.download, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        
        return super.onOptionsItemSelected(item);
    }
}
