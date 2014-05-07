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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

public class DownloadActivity extends Activity {
    static final int                REQUEST_ACCOUNT_PICKER = 1;
    static final int                REQUEST_AUTHORIZATION = 2;
    private static Drive            mService;
    private GoogleAccountCredential mCredential;
    private Context                 mContext;
    private List<File>              languageList;
    private java.io.File[]          localLanguages;
    private ArrayAdapter<String>    mAdapter;
    private SearchView              search;
    
    private java.io.File            targetDir;
    private EditText                inputSearch;
    
    // persistent data that stores the last time the device updated files
    private SharedPreferences 		sp;  
    public static final String 		UPDATE = "update"; 
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
        localLanguages = targetDir.listFiles();

        final Button button = (Button) findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		getDriveContents();
        	}
        });
    }

    /**
     * Gets the list of language folders in drive (in the top level directory)
     */
    private void getDriveContents() {
        readPref();
        writePref();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                languageList = new ArrayList<File>();
                Files f1 = mService.files();
                Files.List request = null;
        
                do {
                    try { 
                        request = f1.list();
                        // get the language folders from drive
                        request.setQ("'root' in parents and trashed=false and mimeType = 'application/vnd.google-apps.folder'");
                        FileList fileList = request.execute();
                        
                        languageList.addAll(fileList.getItems());
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
                
                processLanguages();
            }
        });
        t.start();
    }
    
    
    /**
     * Figures out if a language folder needs to downloaded, pruned, or checked
     */
    private void processLanguages() {
        List<String> googleDriveLanguages = new ArrayList<String>(); // list of language names in drive
        // process each drive folder to see if it needs to be updated;
        for (File f : languageList) {
            String folderName = f.getTitle();
            googleDriveLanguages.add(folderName);
            java.io.File targetFolder = new java.io.File(targetDir, folderName);
            
            if (!targetFolder.exists()) {
                targetFolder.mkdirs();
            } else if (targetFolder.exists() && checkTimeStamp(f)) {
                // folder has been modified in drive since last update
                DateTime date = f.getModifiedDate();
                System.out.println("Last modified date of package " + folderName + " is " + date.getValue());

                // delete any local files that have the same name: for package overwrite
                deleteFile(targetFolder);
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
                List<File> packageList = new ArrayList<File>();
                Files f1 = mService.files();
                Files.List request = null;
        
                do {
                    try { 
                        request = f1.list();
                        // get the packages in the given language folder in drive
                        request.setQ("'" + languageFolder.getId() + "' in parents and trashed=false and mimeType = 'application/vnd.google-apps.folder'");
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
                
                processPackages(packageList, localLanguageFolder);
            }
        });
        t.start();
    }
    
    /**
     * Figures out if a package from the given language folder needs to be downloaded, pruned, or checked
     * @param packageList; the list of packages to check
     * @param parentFolder; the language folder of the package list (in local storage)
     */
    private void processPackages(List<File> packageList, java.io.File parentFolder) {
        List<String> googleDrivePackages = new ArrayList<String>(); // list of package names in drive
        
        // process each package folder to see if it needs to be updated;
        for (File f : packageList) {
            String folderName = f.getTitle();
            googleDrivePackages.add(folderName);
            java.io.File packageFolder = new java.io.File(parentFolder, folderName);
            
            if (!packageFolder.exists()) {
                packageFolder.mkdirs();
                getPackageContents(f, packageFolder, true);
            } else if (packageFolder.exists() && checkTimeStamp(f)) {
                // folder has been modified in drive since last update
                DateTime date = f.getModifiedDate();
                System.out.println("Last modified date of package " + folderName + " is " + date.getValue());

                // delete any local files that have the same name: for package overwrite
                deleteFile(packageFolder);
                // guaranteed at this point that there is no file in local storage of the same name
                packageFolder.mkdirs();
                getPackageContents(f, packageFolder, true);
            } else {
                // f is existing folder since last update: check last modified date of its contents
                getPackageContents(f, packageFolder, false);
            }
        }
        prune(googleDrivePackages, parentFolder.listFiles());
    }
    
    
    /**
     * Gets the contents of a package
     * @param f, the package folder from google drive
     * @param targetFolder, the package folder in local storage
     */
    private void getPackageContents(final File f, final java.io.File targetFolder, final boolean download) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                // list of contents in f
                List<File> mFileList = new ArrayList<File>();
                Files f1 = mService.files();
                Files.List request = null;
                do {
                    try { 
                        request = f1.list();
                
                        // get only the files from the given folder 
                        request.setQ("'" + f.getId() + "' in parents and trashed=false and mimeType != 'application/vnd.google-apps.folder'");
                        FileList fileList = request.execute();
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
                downloadPackage(item, localFolder);
            } else if (temp.exists() && checkTimeStamp(driveContent)) {
                // needs to be updated
                deleteFile(temp);
                // download
                item.add(driveContent);
                downloadPackage(item, localFolder);
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
    }
    
    
    /**
     * Deletes the files in local storage if their names are not in the given list
     * @param names, the list of file names to check against
     * @param localFiles, the list of local files to prune
     */
    private void prune(List<String> names, java.io.File[] localFiles) {
        for (java.io.File localFile : localFiles) {
            if (!names.contains(localFile.getName())) {
                deleteFile(localFile);
                System.out.println("Deleting Language from pruning " + localFile.getName());
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
        long currentTime = 0;//System.currentTimeMillis();
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
