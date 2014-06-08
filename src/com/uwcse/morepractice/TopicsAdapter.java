package com.uwcse.morepractice;

import java.io.File;
import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.uwcse.morepractice.NavigationAdapter.ViewHolder;

public class TopicsAdapter extends BaseAdapter {
		private static final String TAG = NavigationAdapter.class.getSimpleName();
		//	MainActivity.class.getSimpleName();
	
	private Context mContext;
	private String[] files;
	private String[] colors = {"#A4C400", "#60A917", "#008A00", "#00ABA9","#1BA1E2", "#0050EF","#6A00FF", "#AA00FF", 
							   "#F472D0", "#D80073", "#A20025", "#E51400", "#FA6800", "#F0A30A", "#E3C800"};
	
	public LayoutInflater inflater;
	public int LayoutResourceId;
	
	public TopicsAdapter(Context c){
		mContext = c;
	}
	
	public TopicsAdapter(Context c, String[] f, int id ){
		mContext = c;
		files = f;
		LayoutResourceId = id;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return files.length;
	}
	
	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@SuppressWarnings("null")
	@Override
	 public View getView(int position, View convertView, ViewGroup parent) {
		
		TextView view;
		
		View row = convertView;
	
		if (convertView == null) {  // if it's not recycled, initialize some attributes
			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
	       	row = inflater.inflate(LayoutResourceId, parent, false); 
	       	view = (TextView) row.findViewById(R.id.topic_text);
	       	row.setTag(view);
	    } else {
	    	view = (TextView) row.getTag();
	    }
		
		String[] parts = files[position].split("\\.");
		
		view.setText(parts[0].toLowerCase());
		
		view.setBackgroundColor(Color.parseColor(colors[position % colors.length]));
		
		return row;
	}
	
	// Special view that populates the gridView
	// Has a text view and an image view
	public static class ViewHolder {
		TextView text;
		ImageView img;
	}
	
	public File getFileImg(File dir) {
		File file = null;		
		if (dir.isDirectory()) {
			File[] fs = dir.listFiles();
			for (File f : fs) {
				if (f.getName().equals("img.jpg")) {
					file = f;
				}	
			}
		}
		return file;
	}
	
	/**
	 * Returns the extension of the given filename or null if there is no extension
	 * @param filename the file name to parse
	 * @return the extension of the given filename, or null if there is no extension
	 */
	private String getExtension(String filename) {
		String[] parts = filename.split("\\.");
		return parts[parts.length - 1].toLowerCase();
	}
}
