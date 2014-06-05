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
    private static final String FOLDER          = "mimeType='application/vnd.google-apps.folder'";
    private static final String NOT_FOLDER      = "mimeType!='application/vnd.google-apps.folder'";
    private static final String TOP_LEVEL       = "'root' in parents";
    private static final String NOT_TRASHED     = "trashed=false";
    private static final String NOT_XLS         = "mimeType!='application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'";
    private static final String NOT_XLSX        = "mimeType!='application/vnd.ms-excel'";
    private static final String NOT_SPREADSHEET = NOT_XLS + " and " + NOT_XLSX;
    
    private static final String MOVIE           = "mimeType='video/mp4'";
    private static final String IMAGE           = "mimeType='image/jpeg' or mimeType='image/png' or mimeType='image/gif'";
    private static final String TEXT            = "mimeType='text/plain'";
    private static final String CSV             = "mimeType='text/csv'";
    private static final String PDF             = "mimeType='application/pdf'";
    private static final String SUPPORTED_FILES = "(" + MOVIE + " or " + IMAGE + " or " + TEXT + " or " + CSV + " or " + PDF + ")";       
    
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
            // alert user that wifi needs to be checked
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DownloadActivity.this);
            alertDialogBuilder
                .setTitle(getString(R.string.check_wifi))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.exit), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, close current activity
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
            }
        }).start();
    }

    
    /**
     * Sets up progress bars and starts the download process
     */
    private void update() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                List<File> list = getContents(NOT_TRASHED + " and " + NOT_FOLDER + " and " + SUPPORTED_FILES);
                List<File> filteredList = new ArrayList<File>();
                
                // filters out shared drive files
                for (File f : list) {
                    if(!f.getShared()) {
                        filteredList.add(f);
                    }
                }

                numDownloading = filteredList.size();
                Log.e("START NUM", "" + numDownloading);
                updateMax = numDownloading;
                
                // notification progress bar
                String note = getResources().getString(R.string.notification_message);
                nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mBuilder = new NotificationCompat.Builder(mContext);
                mBuilder.setContentTitle(note)
                        .setSmallIcon(android.R.drawable.stat_sys_download)
                        .setTicker(note);

                // main progress bar
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
                                        back();//finish();
                                    }
                                }    
                            });
                        }
                    }
                }).start();

                if (!targetDir.exists())
                    targetDir.mkdirs();
                getDriveContents();
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
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                // get only the folders in the root directory of drive account
                List<File> files = getContents(TOP_LEVEL + " and " + NOT_TRASHED + " and " + FOLDER + " and " + NOT_SPREADSHEET);
                
                prune(targetDir, files);
                processLanguages(files);
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
            if (!localLangFolder.exists())
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
                String whichFiles = "'" + languageFolder.getId() + "' in parents and " + NOT_TRASHED + " and " + FOLDER + " and " + NOT_SPREADSHEET;
                List<File> files = getContents(whichFiles);
                
                prune(localLanguageFolder, files);
                processFolders(files, localLanguageFolder);
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
            if (!localFolder.exists())
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
                // list of contents in f that are directories
                List<File> mFolderList = getContents("'" + f.getId() + "' in parents and " + NOT_TRASHED + " and " + FOLDER + " and " + NOT_SPREADSHEET);
                // check if there is a folder
                if (!mFolderList.isEmpty()) {
                    prune(targetFolder, mFolderList, false);
                    processFolders(mFolderList, targetFolder);
                }
                
                // list of contents in f that are files
                List<File> mFileList = getContents("'" + f.getId() + "' in parents and " + NOT_TRASHED + " and " + NOT_FOLDER + " and " + SUPPORTED_FILES);
                
                prune(targetFolder, mFileList, true);
                processFiles(mFileList, targetFolder);
            }
        });
        t.start();
    }
    
    
    /**
     * Processes each file in a google drive folder
     * @param mFileList, the list of files in a google drive folder
     * @param targetFolder, the local folder where mFileList will be downloaded into
     */
    private void processFiles(List<File> mFileList, java.io.File targetFolder) {
        // check if the local file exists
        for (File file : mFileList) {
            java.io.File local = new java.io.File(targetFolder, file.getTitle());
            if (!local.exists()) {
                downloadFile(file, targetFolder);
            } else {
                if (checkTimeStamp(file)) {
                    Log.e("DELETE TIME", local.getParentFile().getName() + "/" + local.getName());
                    deleteFile(local);
                    downloadFile(file, targetFolder);
                } else {
                    check = 1;
                    Log.e("DL NOT NEEDED", local.getParentFile().getName() + "/" + local.getName());
                    numDownloading--;
                    Log.e("STATUS NOT DL","numDownloading is at " + numDownloading);
                    
                    setNotification();
                    if (numDownloading <= 0) {
                        setFinalNotification();
                    }
                }
            }
        }
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
        check = 1;
        System.out.println("Downloaded file " + file.getAbsolutePath());
        Log.e("STATUS","numDownloading is at " + numDownloading);
        if (--numDownloading <= 0) {
            //finished downloading all files
            Log.e("STATUS","numDownloading is at " + numDownloading);
            setFinalNotification();
            back();
        } else {
            setNotification();
        }
    }
    
    
    /**
     * Deletes files in local storage if they are not in drive account;
     * For a directory with only folders
     * @param localDir, the local folder that is the parent folder of the files to prune
     * @param driveFiles, the list of files in drive to compare against
     */
    private void prune(java.io.File localDir, List<File> driveFiles) {
        java.io.File[] localArray = localDir.listFiles();
        for (java.io.File f : localArray) {
            boolean found = false;
            String lname = f.getName();
            for (File gf : driveFiles) {
                String gname = gf.getTitle();
                if (lname.equals(gname))
                    found = true;
            }
            if (!found) {
                Log.e("DELETE PRUNE", f.getParentFile().getName() + "/" + f.getName());
                deleteFile(f);
            }
        }
    }
    
    
    /**
     * Deletes files in local storage if they are not in drive account; 
     * For a directory with both folders and files
     * @param localDir, the local folder that is the parent folder of the files to prune
     * @param driveFiles, the list of files in drive to compare against
     * @param dir, true if only considering files, false if only considering folders
     */
    private void prune(java.io.File localDir, List<File> driveFiles, boolean dir) {
        java.io.File[] localArray = localDir.listFiles();
        for (java.io.File f : localArray) {
            if (!dir) {
                // only consider folders
                if (!f.isDirectory())
                    continue;
            } else {
                // only consider files
                if (f.isDirectory())
                    continue;
            }
            boolean found = false;
            String lname = f.getName();
            for (File gf : driveFiles) {
                String gname = gf.getTitle();
                if (lname.equals(gname))
                    found = true;
            }
            if (!found) {
                Log.e("DELETE PRUNE", f.getParentFile().getName() + "/" + f.getName());
                deleteFile(f);
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
     * Updates the notification progress bar
     */
    private void setNotification() {
        mBuilder.setProgress(updateMax, ++updateProgress, false)
        .setContentTitle(getResources().getString(R.string.notification_message));
        // Issues the notification
        nm.notify(0, mBuilder.build());
    }
    
    
    /**
     * Finishes the notification progress bar
     */
    private void setFinalNotification() {
      //finished downloading all files
        // update notification
        String note = getResources().getString(R.string.update_complete);
        mBuilder.setContentTitle(note)
                .setContentText("")
                .setSmallIcon(R.drawable.ic_launcher_mp)
                .setTicker(note)
                .setProgress(0, 0, false);
        nm.notify(0, mBuilder.build());
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
    
    private void back() {
        Intent intent = new Intent(this, ChooseLanguage.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
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
}
