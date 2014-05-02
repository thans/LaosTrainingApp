package com.example.laostrainingapp;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
import android.app.ActionBar;
import android.app.Fragment;
import android.app.NotificationManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
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
    private List<File>              mResultList;
    private List<String>            googleDrivePackages;
    private ListView                mListView;
    private java.io.File[]          localPackages;
    private String[]                mFileArray;
    private String                  mDLVal;
    private ArrayAdapter<String>    mAdapter;
    private SearchView              search;
    
    private boolean                 deleteFirst;
    private java.io.File            targetDir;
    private EditText                inputSearch;
    
    
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
    
        targetDir = new java.io.File(Environment.getExternalStorageDirectory(), 
                getString(R.string.local_storage_folder));
        localPackages = targetDir.listFiles();
        
        //setContentView(R.layout.activity_main);
        mListView = (ListView) findViewById(R.id.listView1);
	    
//        OnItemClickListener mMessageClickedHandler = new OnItemClickListener() {
//            public void onItemClick(AdapterView parent, View v, int position, long id) {
//            	// set up notification of download
//            	NotificationManager nm =
//            		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//            	NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);
//            	mBuilder.setContentTitle("Package Download")
//                .setContentText("Download in progress")
//                .setSmallIcon(android.R.drawable.stat_sys_download)
//                .setTicker("Starting download");
//            	
//            	// download an item	from the list of packages
//            	downloadItemFromList(position, nm, mBuilder);
//        	}
//        };
//    
//        mListView.setOnItemClickListener(mMessageClickedHandler); 
        
        
        final Button button2 = (Button) findViewById(R.id.button2);
        button2.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		getDriveContents();
        	}
        });
     
    }

    /**
     * Gets the folders in the google drive account
     */
    private void getDriveContents() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                mResultList = new ArrayList<File>();
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
                        
                        mResultList.addAll(fileList.getItems());
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
                
                googleDrivePackages = new ArrayList<String>();
                
                // process each folder to see if it needs to be updated
                // and if so, get the folderId of the folder and add to map
                for (File f : mResultList) {
                    if (checkTimeStamp()) {
                        String folderName = f.getTitle();
                        googleDrivePackages.add(folderName);
                        java.io.File targetFolder = new java.io.File(targetDir, folderName);
                        
                        // delete any files that have been updated: for package overwrite
                        if (targetFolder.exists())
                            try {
                                deleteFile(targetFolder);
                                System.out.println("Deleting " + folderName);
                            } catch (IOException e) {
                                System.err.println("Delete of folder " + folderName + " failed");
                                e.printStackTrace();
                            }
                        System.out.println("Begin download of files from folder " + folderName);
                        // guaranteed at this point that there is no file in local storage of the same name
                        targetFolder.mkdirs();
                        getPackageContents(f, targetFolder);
                    }
                }
                
                if (!googleDrivePackages.isEmpty()) {
                	// delete any package in local storage that is not also in the google drive account
                	for (java.io.File localFile : localPackages) {
                		if (!googleDrivePackages.contains(localFile.getName()))
                			try {
                				deleteFile(localFile);
                				System.out.println("Deleting " + localFile.getName());
                			} catch (IOException e) {
                				// TODO Auto-generated catch block
                				e.printStackTrace();
                			}
                	}
                }
                
                // populates the list view with all the zip files in the specified google drive account
                //populateListView();
                
                
            }
        });
        t.start();
    }
    
    /**
     *  compares the time stamps on the given folder 
     * @return true if the last modified date of folder in drive account is later than that of the last time of update
     */
    private boolean checkTimeStamp() {
        // TODO: use sharedpreferences
        // may need to prune mResultList or return a new list of files to download
        return true;
    }
    
    /**
     * Gets the contents of a package
     * @param f, the package folder 
     * @param targetFolder, the package folder in local storage, currently empty
     */
    private void getPackageContents(final File f, final java.io.File targetFolder) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
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
                populateListView(f, mFileList);
                
                // downloads the contents of the package within a folder of the same name
                downloadPackage(mFileList, targetFolder);
                
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
     *  deletes the given file by recursively deleting its contents
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
    
    
    private void downloadItemFromList(int position, final NotificationManager nm, final NotificationCompat.Builder mBuilder) {
        mDLVal = (String) mListView.getItemAtPosition(position);
        showToast("You just pressed: " + mDLVal);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                for(File tmp : mResultList) {
                    if (tmp.getTitle().equalsIgnoreCase(mDLVal)) {
                        if (tmp.getDownloadUrl() != null && tmp.getDownloadUrl().length() > 0) {
                            Log.i("GoogleDriveProject", "running downloadItemFromList");
                            try { // for httpresponse
                                com.google.api.client.http.HttpResponse resp = 
                                        mService.getRequestFactory()
                                        .buildGetRequest(new GenericUrl(tmp.getDownloadUrl()))
                                        .execute();
                                
                                // gets the zip file's contents
                                InputStream inputStream = resp.getContent();
                                try {
                                    Log.i("GoogleDriveProject", "beginning storage of file");
    
                                    ZipInputStream zis = new ZipInputStream(inputStream);
                                    // the laos directory
                                    java.io.File targetDir = new java.io.File(Environment.getExternalStorageDirectory(), 
                                            getString(R.string.local_storage_folder));
                                    System.out.println("the target directory is " + targetDir.getAbsolutePath());
                                    
                                    int count = 0;
                                    ZipEntry ze;
                                    while ((ze = zis.getNextEntry()) != null) {
                                    	// Sets an activity indicator for an operation of indeterminate length
                                    	mBuilder.setProgress(0, 0, true);
                                    	// Issues the notification
                                    	nm.notify(0, mBuilder.build());
                                    	
                                        count++;
                                        Log.d("DEBUG", "Extracting: " + ze.getName() + "...");
                                        // Extracted file will be saved with same file name that's in the zip drive
                                        String fileName = ze.getName();
                                        System.out.println("file " + count + "'s name is " + fileName);
                                        java.io.File targetFile = new java.io.File(targetDir, fileName);
                                        System.out.println("file " + count + "'s path is " + targetFile.getAbsolutePath());
                                        
                                        showToast("Downloading: " + targetFile.getName() + " to " + targetFile.getPath());
                                        System.out.println("Downloading: " + targetFile.getName() + " to " + targetFile.getPath());
                                        new java.io.File(targetFile.getParent()).mkdirs();
                                        if (ze.isDirectory()) {
                                            System.out.println("entry is directory");
                                            targetFile.mkdirs();
                                        } else {
                                            System.out.println("entry is file");
                                            // reads/writes each file 
                                            FileOutputStream fos = new FileOutputStream(targetFile);
                                            try {
                                                copyContents(zis, fos);
                                                System.out.println("Just wrote file: " + targetFile.getName());
                                            } finally {
                                                fos.close();
                                            }
                                        }  // end if
                                        
                                        // finish the current zip entry
                                        zis.closeEntry();
                                    }  // end while
                                    System.out.println("zipentry count = " + count);
                                    // update notification
                                    mBuilder.setContentText("Download complete")
                                    	.setSmallIcon(R.drawable.ic_action_download)
                                    	.setProgress(0, 0, false);
                                    nm.notify(0, mBuilder.build());
                                    zis.close();
                                } finally {
                                    inputStream.close();
                                }
                            } catch (IOException e) {
                                System.err.println("the HttpResponse failed");
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
        t.start();
    }
    
    // copy the contents of a file to the given output
    private static void copyContents(ZipInputStream zis, OutputStream fos) {
        byte[] buffer = new byte[4096];
        // reads/writes each file 
        int len;
        try {
            while((len = zis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
        } catch (IOException e) {
            System.err.println("write failed");
            e.printStackTrace();
        }
    }
    
    private void populateListView() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFileArray = new String[mResultList.size()];
                int i = 0;
                for(File tmp : mResultList) {
                    mFileArray[i] = tmp.getTitle();
                    i++;
                }
                mAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, mFileArray){

                    @Override
                    public View getView(int position, View convertView,
                            ViewGroup parent) {
                        View view =super.getView(position, convertView, parent);

                        TextView textView=(TextView) view.findViewById(android.R.id.text1);

                        /*YOUR CHOICE OF COLOR*/
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
        int id = item.getItemId();
        if (id == R.id.action_download) {
        	getDriveContents();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
