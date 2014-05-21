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
import java.util.LinkedList;
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
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;
import android.widget.Toast;

public class DownloadActivity extends Activity {
    // for accessing drive
    static final int                    REQUEST_ACCOUNT_PICKER = 1;
    static final int                    REQUEST_AUTHORIZATION = 2;
    private static Drive                mService;
    private GoogleAccountCredential     mCredential;
    private Context                     mContext;
    
    // pertaining to files
    private java.io.File                targetDir;
    private List<java.io.File>          localFiles;
    
    // for progress bar in notification bar
    private int                         numDownloading;
    private int                         updateMax;
    private int                         updateProgress;
    private NotificationManager         nm;
    private NotificationCompat.Builder  mBuilder;
    
    // for progress bar on screen
    private ProgressBar                 mProgress;
    private Handler                     mHandler = new Handler();
    
    // for pop up dialog while checking to see if update is necessary
    private ProgressDialog              checkProgress;
    private int                         check = 0;
    
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
        setTitle(getString(R.string.update));
        getActionBar().show();
        mContext = getApplicationContext();
        
        sp = getPreferences(Context.MODE_PRIVATE);
    
        targetDir = new java.io.File(Environment.getExternalStorageDirectory(), 
                getString(R.string.local_storage_folder));
        
        checkWIFI();
    }
    
    
    /**
     * Checks that wifi is available;
     * If so, proceeds to the account picker;
     * If not, alerts the user to check wifi
     */
    private void checkWIFI() {
        ConnectivityManager manager = (ConnectivityManager) 
                getSystemService(MainActivity.CONNECTIVITY_SERVICE);
        boolean isWifi = manager.getNetworkInfo(
                ConnectivityManager.TYPE_WIFI).isConnected();
        if (isWifi) {
            // setup for credentials for connecting to the Google Drive account
            mCredential = GoogleAccountCredential.usingOAuth2(this, Arrays.asList(DriveScopes.DRIVE));
            // start activity that prompts the user for their google drive account
            startActivityForResult(mCredential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
        } else {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DownloadActivity.this);
            alertDialogBuilder
                .setTitle(getString(R.string.check_wifi))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.exit), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, close
                        // current activity
                        DownloadActivity.this.finish();
                    }
                });

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }
    }
    
    
    /**
     * Starts the update as soon as user picks an account
     */
    private void startDownloadActivity() {
        checking();
        setUPForRecovery();
        numDownloading = 0;
        updateMax = 0;
        updateProgress = 0;
        readPref();
        writePref();
        update();
    }
    
    /**
     * Shows the indeterminate progress dialog for checking to see if 
     * an update is needed
     */
    private void checking() {
        checkProgress = new ProgressDialog(DownloadActivity.this);
        checkProgress.setCancelable(false);
        checkProgress.setMessage(getResources().getString(R.string.checking_message));
        checkProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        checkProgress.setIndeterminate(true);
        checkProgress.show();
        new Thread(new Runnable() {
            public void run() {
                while (check == 0) {}
                checkProgress.dismiss();
                if (check == 1)
                    DownloadActivity.this.finish();
            }
        }).start();

    }

    /**
     * Gets all the files in app directory, for recovery
     */
    private void setUPForRecovery() {
        localFiles = addFiles(null, targetDir);
    }
    
    /**
     * Helper method for recovery
     * @param files, the list of files to process
     * @param dir, if directory, recurse; if file add to files and return
     * @return the files in the directory
     */
    private List<java.io.File> addFiles(List<java.io.File> files, java.io.File dir) {
        if (files == null)
            files = new LinkedList<java.io.File>();

        if (!dir.isDirectory()) {
            files.add(dir);
            return files;
        }

        for (java.io.File file : dir.listFiles())
            addFiles(files, file);
        return files;
    }
    
    
    /**
     * Checks that the local app directory and the google drive account are consistent;
     * For seeing whether update is needed
     * @param list, the list of files from drive
     * @return true if consistent, false otherwise
     */
    private boolean isConsistent(List<File> list) {
        if (!targetDir.exists()) {
            Log.e("EMPTY","App folder does not exist");
            return false;
        }

        // checks whether there is a file locally that is not represented in drive
        for (java.io.File local : localFiles) {
            boolean flag = false;
            for (File drive : list) {
                if (local.getName().equals(drive.getTitle()))
                    flag = true;
            }
            if (!flag) {
                // not found, so not consistent
                Log.e("HERE","inconsistency local outer");
                return false;
            }
        }

        // checks whether there is file in drive that is not represented locally
        for (File drive : list) {
            boolean flag = false;
            for (java.io.File local : localFiles) {
                if (local.getName().equals(drive.getTitle()))
                    flag = true;
            }
            if (!flag) {
                // not found, so not consistent
                Log.e("HERE","inconsistency drive outer");
                return false;
            }
        }
        return true;
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
                List<File> list = getContents(NOT_TRASHED + " and " + NOT_FOLDER);
                List<File> filteredList = new ArrayList<File>();
                
                // filters out shared drive files and by timestamp
                boolean needsUpdate = false;
                for (File f : list) {
                    if(!f.getShared()) {
                        filteredList.add(f);
                        if (checkTimeStamp(f)) {
                            needsUpdate = true;
                            break;
                        }
                    }
                }

                // for showing progress to user

                numDownloading = filteredList.size();
                updateMax = numDownloading;
                if (!needsUpdate && isConsistent(filteredList)) {
                    //showToast("Update not needed");
                    check++;
                } else {
                    check += 2;
                    String note = getResources().getString(R.string.notification_message);
                    nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    mBuilder = new NotificationCompat.Builder(mContext);
                    mBuilder.setContentTitle(note)
                            .setSmallIcon(android.R.drawable.stat_sys_download)
                            .setTicker(note);
                    
                    mProgress = (ProgressBar) findViewById(R.id.progressBar1);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (numDownloading > 0) {
                                mHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        int progress = 100 * (updateMax - numDownloading) / updateMax; 
                                        mProgress.setProgress(progress);
                                        if (numDownloading <= 0) {
                                            mProgress.setVisibility(4);
                                        }
                                    }    
                                });
                            }
                        }
                    }).start();
                    
                    if (targetDir.exists())
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
                //startActivityForResult(e.getIntent(), REQUEST_AUTHORIZATION);
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
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                // get only the folders in the root directory of drive account
                List<File> files = getContents(TOP_LEVEL + " and " + NOT_TRASHED);
                List<File> languageList = new ArrayList<File>();
                for (File f : files) {
                    if (isFolder(f)) 
                        languageList.add(f);
                    else 
                        numDownloading--;
                }
                processLanguages(languageList);
            }
        });
        t.start();
    }
    
    
    /**
     * Processes the language folders downloaded from the drive
     */
    private void processLanguages(List<File> languageList) {
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
                String whichFiles = "'" + languageFolder.getId() + "' in parents and " + NOT_TRASHED;
                List<File> files = getContents(whichFiles);
                List<File> packageList = new ArrayList<File>();
                for (File f : files) {
                    if (isFolder(f)) 
                        packageList.add(f);
                    else 
                        numDownloading--;
                }
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
                            //numDownloading++;
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
        try {
            Thread.sleep(750);
        } catch (InterruptedException e) {
            e.printStackTrace();
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
        
        // updates notification and sees if all downloading is finished
        
        System.out.println("Downloaded file " + file.getAbsolutePath());
        Log.e("STATUS","numDownloading is at " + numDownloading);
        if (--numDownloading <= 0) {
            //finished downloading all files
            Log.e("STATUS","numDownloading is at " + numDownloading);
            // update notification
            String note = getResources().getString(R.string.update_complete);
            mBuilder.setContentTitle(note)
                    .setContentText("")
                    .setSmallIcon(R.drawable.ic_action_download)
                    .setTicker(note)
                    .setProgress(0, 0, false);
            nm.notify(0, mBuilder.build());
            finish();
        } else {
            mBuilder.setProgress(updateMax, ++updateProgress, false)
            .setContentTitle(getResources().getString(R.string.notification_message));
            // Issues the notification
            nm.notify(0, mBuilder.build());
        }
    }
    
    /**
     * Checks if the given file is a folder
     * @param f, the file to check
     * @return true if file is a folder, false otherwise
     */
    private boolean isFolder(File f) {
        return f.getMimeType() != null && f.getMimeType().equals("application/vnd.google-apps.folder");
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
     * Deletes the given file by recursively deleting its contents
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
                    // gets drive account information and proceeds to download packages
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        mCredential.setSelectedAccountName(accountName);
                        mService = getDriveService(mCredential);
                        startDownloadActivity();
                    }
                } else {
                    // exits the activity if user clicks "cancel"
                    DownloadActivity.this.finish();
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
        return super.onOptionsItemSelected(item);
    }
}
