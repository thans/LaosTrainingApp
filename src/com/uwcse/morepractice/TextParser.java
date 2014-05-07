package com.uwcse.morepractice;

import java.io.*;
import java.util.*;

import android.util.Log;

/**
 * This program parses a text file 
 *
 */

public class TextParser {
  
    private Scanner scanner;
    private List<File> orderedFiles;
    private List<String> orderedFileNames;
    
    // number of files listed in the text file but not found in the directory
    private int numFilesNotFound;
    
    /**
     * 
     * @param path the absolute path of the text file
     * @param dir the list of unordered files in the package directory
     * @throws FileNotFoundException 
     */
    public TextParser(String path, File[] dir) {
        File file = new File(path);
        try {
            this.scanner = new Scanner(file);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        orderedFiles = new ArrayList<File>();
        orderedFileNames = new ArrayList<String>();
        
        numFilesNotFound = 0;
        
        // read text file line by line
        while (scanner.hasNextLine()) {
            String fileName = scanner.nextLine();
            System.out.println("print file from scanner " + fileName);
            boolean fileFound = false;
            for (File f : dir) {
                String currname = getFileNameFromPath(fileName);
                if (f.getName().equals(currname)) {
                    orderedFiles.add(f);
                    orderedFileNames.add(fileName);
                    fileFound = true;
                    //System.out.println("added fileName: " + currname);
                    break;
                } else {
                    continue;
                }
            }
            if (!fileFound)
                numFilesNotFound++;
        }
    }
    
    public String[] getOrderedFileNames() {
        return orderedFileNames.toArray(new String[orderedFileNames.size()]);
    }
    
    /**
     * Assumes that there will be only 1 text file per package
     * @return the ordered list of files
     */
    public File[] getOrderedFiles() {
        return orderedFiles.toArray(new File[orderedFiles.size()]);
    }
    
    /**
     * 
     * @return the number of files listed in the text file but not found in the directory
     */
    public int getNumInconsistency() {
        return numFilesNotFound;
    }

    private String getFileNameFromPath(String filename) {
        String[] parts = filename.split("/");
        return parts[parts.length - 1];
    }
}
