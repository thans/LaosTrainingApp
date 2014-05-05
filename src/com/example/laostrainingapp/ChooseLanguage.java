package com.example.laostrainingapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ChooseLanguage extends Activity {
	public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.language);
        
        
        String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();

        // for first design iteration
        String laosFilePath = baseDir + "/" + getString(R.string.local_storage_folder);
        File baseFolder = new File(baseDir);
        File downloadFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        try {
            File zipFile = new File(downloadFolder, "LaosTrainingApp.zip");
            System.out.println("unzipping " + baseFolder);
            downloadZip(zipFile, baseFolder, laosFilePath);
        } catch (ZipException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        final File appRoot = new File(laosFilePath);
        LinearLayout layout = 
                (LinearLayout) this.findViewById(R.id.language_layout);
        File[] files = appRoot.listFiles();
        
        for (final File f : files) {
        	Button btn = new Button(this);
        	btn.setText(f.getPath());
        	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, .5f);
        	btn.setLayoutParams(params);
        	
        	final ChooseLanguage thisActivity = this;

            btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    // Perform action on click
            		Intent intent = new Intent(thisActivity, MainActivity.class);
                    intent.putExtra(MainActivity.LANGUAGE_KEY, f.getAbsolutePath());
                    startActivity(intent);
                    finish();
                }
            });
            
        	layout.addView(btn);
        }
	}
	
    public void showToast(String text) { 
        Context context = getApplicationContext();
        int duration = Toast.LENGTH_SHORT;
    
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
    }
	
    // for first design iteration
    public void downloadZip(File zipFile, File baseFolder, String laosFilePath) throws ZipException, IOException {
        System.out.println("zipFile: " + zipFile);
        System.out.println("baseFolder: " + baseFolder);
        System.out.println("laosFilePath: " + laosFilePath);
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
