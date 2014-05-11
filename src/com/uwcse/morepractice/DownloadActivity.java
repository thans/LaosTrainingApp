package com.uwcse.morepractice;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.uwcse.morepractice.R;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.Drive.Files;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;

import android.accounts.AccountManager;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class DownloadActivity extends Activity {
    static final int                	REQUEST_ACCOUNT_PICKER = 1;
    static final int                	REQUEST_AUTHORIZATION = 2;
    private static Drive            	mService;
    private GoogleAccountCredential 	mCredential;
    private Context                 	mContext;
    
    private List<File>              	languageList;
    
    private java.io.File            	targetDir;
    
    // notification of update progress
    private int                         numDownloading;
    private int                         updateMax;
    private                             int updateProgress;
    private NotificationManager     	nm;
    private NotificationCompat.Builder  mBuilder;
    
    // persistent data that stores the last time the device updated files
    private SharedPreferences           sp;  
    public static final String          UPDATE = "update"; 
    private long                        lastUpdate;
    
    // strings for getting files from drive
    private static final String FOLDER      = "mimeType='application/vnd.google-apps.folder'";
    private static final String NOT_FOLDER  = "mimeType!='application/vnd.google-apps.folder'";
    private static final String TOP_LEVEL   = "'root' in parents";
    private static final String NOT_TRASHED = "trashed=false";
    
    
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

        final Button button = (Button) findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mBuilder = new NotificationCompat.Builder(mContext);
                mBuilder.setContentTitle("Package Update")
                        .setContentText("Update in progress")
                        .setSmallIcon(android.R.drawable.stat_sys_download)
                        .setTicker("Starting update");
                numDownloading = 0;
                updateMax = 0;
                updateProgress = 0;
                readPref();
                update();
                
        	}
        });
    }
    
    
    /**
     * Sees if the app filesystem needs to be downloaded;
     * If at least one file in drive has been modified since that last 
     * time the device has updated, proceeds to download drive contents
     */
    private void update() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                List<File> list = getContents(NOT_TRASHED);
                boolean needsUpdate = false;
                for (File f : list) {
                    if (!f.getShared() && checkTimeStamp(f)) {
                        needsUpdate = true;
                        showToast("Update is necessary");
                        break;
                    }
                }
                if (!needsUpdate) {
                    showToast("Update not needed");
                } else {
                    if (!targetDir.exists())
                        deleteFile(targetDir);
                    targetDir.mkdirs();
                    
                    getDriveContents();
                }
           }
        });
        t.start();
    }
    
    /**
     * Gets the contents of a google drive folder
     * @param whichFiles, the string text that specifies what type of file to get
     * @return a list of files that it gets from the google drive folder
     */
    private List<File> getContents(String whichFiles) {
        List<File> result = new ArrayList<File>();
        Files f1 = mService.files();
        Files.List request = null;

        do {
            try { 
                request = f1.list();
                // get the language folders from drive
                request.setQ(whichFiles);
                FileList fileList = request.execute();
                
                result.addAll(fileList.getItems());
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
        
        return result;
    }


    /**
     * Gets the list of language folders in drive (in the top level directory)
     */
    private void getDriveContents() {
        writePref();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
            	// get only the folders in the root directory of drive account
                languageList = getContents(TOP_LEVEL + " and " + NOT_TRASHED + " and " + FOLDER);
                processLanguages();
            }
        });
        t.start();
    }
    
    
    /**
     * Processes the language folders downloaded from the drive
     */
    private void processLanguages() {
        // process each drive folder to download;
        for (File f : languageList) {
            String langName = f.getTitle();
            java.io.File localLangFolder = new java.io.File(targetDir, langName);
            localLangFolder.mkdirs();
            getLanguageContents(f, localLangFolder);
        }
    }
    
    
    /**
     * Gets the packages from the given language folder in drive
     * @param languageFolder, the parent folder in drive where the packages are retrieved from
     * @param localLanguageFolder, the language folder in local storage that the packages are downloaded into
     */
    private void getLanguageContents(final File languageFolder, final java.io.File localLanguageFolder) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
            	// gets only the folders in the language folder
            	String whichFiles = "'" + languageFolder.getId() + "' in parents and " + NOT_TRASHED + " and " + FOLDER;
                List<File> packageList = getContents(whichFiles);
                processFolders(packageList, localLanguageFolder);
            }
        });
        t.start();
    }
    
    
    /**
     * Processes a list of folders for download
     * @param folderList; the list of folders to download
     * @param parentFolder; the folder in local storage that is downloaded into
     */
    private void processFolders(List<File> folderList, java.io.File parentFolder) {
        // process each folder to see if it needs to be updated;
        for (File f : folderList) {
            String folderName = f.getTitle();
            java.io.File localFolder = new java.io.File(parentFolder, folderName);
            Log.e("folder",localFolder.getAbsolutePath());
            if (localFolder.exists()) {
                Log.e("EXISTS", folderName + " exists");
            }
            localFolder.mkdirs();
            getFolderContents(f, localFolder);
        }
    }
    
    
    /**
     * Gets the contents of a folder
     * @param f, the drive folder to get the contents from 
     * @param targetFolder, the corresponding folder in local storage
     * @param download, true if contents of File f needs download
     */
    private void getFolderContents(final File f, final java.io.File targetFolder) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                // list of contents in f that are files
                List<File> mFileList = getContents("'" + f.getId() + "' in parents and " + NOT_TRASHED+ " and " + NOT_FOLDER);
                for (File file : mFileList) {
                    downloadFile(file, targetFolder);
                }
                
                // list of contents in f that are directories
                List<File> mFolderList = getContents("'" + f.getId() + "' in parents and " + NOT_TRASHED + " and " + FOLDER);
                if (!mFolderList.isEmpty()) {
                    processFolders(mFolderList, targetFolder);
                }
            }
        });
        t.start();
    }
    
    
    /**
     * Begins the downloading process of the given file from a google drive account folder
     * @param mFile, the file to download
     * @param targetFolder, the parent folder in local storage to download into 
     */
    private void downloadFile(final File mFile, final java.io.File targetFolder) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                    if (mFile.getDownloadUrl() != null && mFile.getDownloadUrl().length() > 0) {
                        try {
                            com.google.api.client.http.HttpResponse resp =
                                    mService.getRequestFactory()
                                    .buildGetRequest(new GenericUrl(mFile.getDownloadUrl()))
                                    .execute();
                            
                            // gets the file's contents
                            InputStream inputStream = resp.getContent();

                            // stores the contents to the device's external storage
                            try {
                                final java.io.File file = new java.io.File(targetFolder, mFile.getTitle());
                                System.out.println("Downloading: " + mFile.getTitle() + " to " + file.getPath());
                                numDownloading++;
                                storeFile(file, inputStream);
                            } finally {
                                inputStream.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
            }
        });
        t.start();
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
        
        System.out.println("Downloaded file " + file.getAbsolutePath());
        if (--numDownloading <= 0) {
            //finished downloading all files
            Log.e("FINISHED", "all downloading should be finished by now");
            // update notification
            mBuilder.setContentTitle("Update complete")
                    .setContentText("")
                    .setSmallIcon(R.drawable.ic_action_download)
                    .setProgress(0, 0, false);
            nm.notify(0, mBuilder.build());
            finish();
        } else {
            // Sets an activity indicator for an operation of indeterminate length
            mBuilder.setProgress(updateMax, ++updateProgress, false)
            .setContentTitle("Updating....");
            // Issues the notification
            nm.notify(0, mBuilder.build());
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
     *  deletes the given file by recursively deleting its contents
     * @param file, the file that gets deleted
     * @throws IOException
     */
    private void deleteFile(java.io.File file) {
        if (file.isDirectory()) {
            for (java.io.File f : file.listFiles()) 
                deleteFile(f);
        }
        
        if (!file.delete())
            try {
                throw new FileNotFoundException("Failed to delete file: " + file.getName());
            } catch (FileNotFoundException e) {
                System.err.println("Delete of file " + file.getAbsolutePath() + " failed");
                e.printStackTrace();
            }
    }
    
    
    /**
     * Converts milliseconds to the corresponding date in the form dd/MM/yy
     * @param msec, the time in milliseconds to convert
     * @return the String date
     */
    private String getDateFromLong(Long msec) {
        DateFormat df = DateFormat.getDateTimeInstance();
        return df.format(new Date(msec));
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
        long currentTime = System.currentTimeMillis();
        editor.putLong(UPDATE, currentTime);
        editor.commit();
        System.out.println("Time of current update: " + getDateFromLong(currentTime));
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
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
        	new ActionBarFunctions().refresh(this);
        	return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
