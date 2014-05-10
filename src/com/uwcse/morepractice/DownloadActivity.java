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

import com.uwcse.morepractice.R;
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
    private java.io.File[]          	localLanguages;
    
    private java.io.File            	targetDir;
    
    // notification of update progress
    private int                         numDownloading = 0;
    private int                         updateMax = 0;
    private                             int updateProgress = 0;
    private NotificationManager     	nm;
    private NotificationCompat.Builder  mBuilder;
    
    // persistent data that stores the last time the device updated files
    private SharedPreferences           sp;  
    public static final String          UPDATE = "update"; 
    private long                        lastUpdate;
    
    private static final String FOLDER = "mimeType='application/vnd.google-apps.folder'";
    private static final String NOT_FOLDER = "mimeType!='application/vnd.google-apps.folder'";
    private static final String TOP_LEVEL = "'root' in parents";
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
        localLanguages = targetDir.listFiles();

        final Button button = (Button) findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mBuilder = new NotificationCompat.Builder(mContext);
                mBuilder.setContentTitle("Package Update")
                        .setContentText("Update in progress")
                        .setSmallIcon(android.R.drawable.stat_sys_download)
                        .setTicker("Starting update");
                
                readPref();
                getNumFilesToDownload();
        		getDriveContents();
        	}
        });
    }
    
    /**
     * Gets the max number of files that need to be downloaded; used for determinate progress bar
     */
    private void getNumFilesToDownload() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                List<File> list = getContents("modifiedDate > '" + lastUpdate + "'");
                for (File f : list) {
                    if (!f.getShared()) {
                        // not shared with me
                        updateMax++;
                        Log.e("File " + updateMax, f.getTitle());
                    }
                }
                Log.e("GOOGLE","files need downloading: " + updateMax);
                if (updateMax == 0)
                    showToast("Packages are already updated");
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
     * Figures out if a language folder needs to downloaded, pruned, or checked;
     * 
     */
    private void processLanguages() {
        List<String> googleDriveLanguages = new ArrayList<String>(); // list of language names in drive
        // process each drive folder to see if it needs to be updated;
        for (File f : languageList) {
            String folderName = f.getTitle();
            googleDriveLanguages.add(folderName);
            java.io.File targetFolder = new java.io.File(targetDir, folderName);
            
            if (!targetFolder.exists()) {
                showToast("Language " + folderName + " does not exist locally; must download");
                System.out.println("Language " + folderName + " does not exist locally; must download");
                targetFolder.mkdirs();
            } else if (targetFolder.exists() && checkTimeStamp(f)) {
                // folder has been modified in drive since last update
                DateTime date = f.getModifiedDate();
                System.out.println("Last modified date of package " + folderName + " is " + date.getValue());

                // delete any local files that have the same name: for package overwrite
                deleteFile(targetFolder);
                showToast("Deleting Language folder " + folderName);
                System.out.println("Deleting Language folder " + folderName);
                // guaranteed at this point that there is no file in local storage of the same name
                targetFolder.mkdirs();
            } 
            // f is existing folder since last update: check last modified date of its contents
            getLanguageContents(f, targetFolder);
        }
        prune(googleDriveLanguages, localLanguages);
    }
    
    
    /**
     * Gets the packages from the given folder in drive account
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
     * Figures out if a folder from the given parent folder needs to be downloaded, pruned, or checked
     * @param folderList; the list of folders to check
     * @param parentFolder; the parent folder of the list of folders (in local storage)
     */
    private void processFolders(List<File> folderList, java.io.File parentFolder) {
        List<String> googleDriveFolders = new ArrayList<String>(); // list of folder names in drive with same parent
        
        // process each package folder to see if it needs to be updated;
        for (File f : folderList) {
            String folderName = f.getTitle();
            googleDriveFolders.add(folderName);
            java.io.File localFolder = new java.io.File(parentFolder, folderName);
            
            if (!localFolder.exists()) {
                showToast("Folder " + folderName + "in folder " + parentFolder.getAbsolutePath() + " does not exist locally; must download");
                System.out.println("Folder " + folderName + "in folder " + parentFolder.getAbsolutePath() + " does not exist locally; must download");
                localFolder.mkdirs();
                getFolderContents(f, localFolder, true);
            } else if (localFolder.exists() && checkTimeStamp(f)) {
                // folder has been modified in drive since last update
                DateTime date = f.getModifiedDate();
                System.out.println("Last modified date of folder " + folderName + " is " + date.getValue());

                // delete any local files that have the same name: for package overwrite
                deleteFile(localFolder);
                showToast("Deleting folder " + folderName);
                System.out.println("Deleting folder " + folderName);
                // guaranteed at this point that there is no file in local storage of the same name
                localFolder.mkdirs();
                getFolderContents(f, localFolder, true);
            } else {
                // f is existing folder since last update: check last modified date of its contents
            	getFolderContents(f, localFolder, false);
            }
        }
        prune(googleDriveFolders, parentFolder.listFiles());
    }
    
    
    /**
     * Gets the contents of a folder
     * @param f, the folder from google drive
     * @param targetFolder, the corresponding folder in local storage
     * @param download, true if contents of File f needs download
     */
    private void getFolderContents(final File f, final java.io.File targetFolder, final boolean download) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                // list of contents in f 
                List<File> mFileList = getContents("'" + f.getId() + "' in parents and " + NOT_TRASHED);
                
                // process each file
                for (File file : mFileList) {
                    //Log.e("FOLDER", file.getMimeType());
                    if (!file.getMimeType().equals("application/vnd.google-apps.folder")) {
                        // is a file
                        if (download) {
                            // downloads the contents of the drive folder within a local folder of the same name
                        	downloadFile(file, targetFolder);
                        } else {
                            // compares with the contents of the local folder of the same name
                            checkContents(mFileList, targetFolder);
                        }
                    } else {
                        // is a directory
                        List<File> directory = new ArrayList<File>();
                        directory.add(file);
                        processFolders(directory, targetFolder);
                    }
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
     * Checks that the contents in a folder and the contents of the corresponding 
     * folder in local storage are in sync: download, prune or update
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
                showToast(driveContent.getTitle() + " does not exist; must download");
                System.out.println(driveContent.getTitle() + " does not exist; must download");
                downloadFile(driveContent, localFolder);
            } else if (temp.exists() && checkTimeStamp(driveContent)) {
                // needs to be updated
                showToast(driveContent.getTitle() + " does exist; but must be updated");
                System.out.println(driveContent.getTitle() + " does exist; but must be updated");
                showToast("Deleting file " + temp.getName());
                System.out.println("Deleting file " + temp.getName());
                deleteFile(temp);
                // download
                item.add(driveContent);
                downloadFile(driveContent, localFolder);
            }
        }
        
        prune(driveContentNames, localContents);
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
        } else {
            // Sets an activity indicator for an operation of indeterminate length
            mBuilder.setProgress(updateMax, ++updateProgress, false)
            .setContentTitle("Updating....");
            // Issues the notification
            nm.notify(0, mBuilder.build());
        }
    }
    
    
    /**
     * Deletes the files in local storage if their names are not in the given list
     * @param names, the list of file names to check against
     * @param localFiles, the list of local files to prune
     */
    private void prune(List<String> names, java.io.File[] localFiles) {
        if (updateMax > 0) {
            for (java.io.File localFile : localFiles) {
                if (!names.contains(localFile.getName())) {
                    deleteFile(localFile);
                    System.out.println("Deleting Language from pruning " + localFile.getName());
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
