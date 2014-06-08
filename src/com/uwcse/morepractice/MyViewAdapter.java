package com.uwcse.morepractice;

import java.io.File;
import java.util.Random;

import com.uwcse.morepractice.R;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class MyViewAdapter extends BaseAdapter {

	private static final String TAG = MyViewAdapter.class.getSimpleName();
		//	MainActivity.class.getSimpleName();
	
	private Context mContext;
	private String[] files;
	private String directory;
	private String[] colors = {"#A4C400", "#60A917", "#008A00", "#00ABA9","#1BA1E2", "#0050EF","#6A00FF", "#AA00FF", 
							   "#F472D0", "#D80073", "#A20025", "#E51400", "#FA6800", "#F0A30A", "#E3C800"};
	
	public LayoutInflater inflater;
	public int LayoutResourceId;
	
	public MyViewAdapter(Context c, String[] s, String d, int id){
		mContext = c;
		files = s;
		directory = d;
		LayoutResourceId = id;
	}
	
	public MyViewAdapter(Context c){
		mContext = c;
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

	@Override
	 public View getView(int position, View convertView, ViewGroup parent) {
		
		ViewHolder holder = null;
		
		View row = convertView;
		
		if (row == null) {  // if it's not recycled, initialize some attributes
           	LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
           	row = inflater.inflate(LayoutResourceId, parent, false); 
           	holder = new ViewHolder();	
            holder.text = (TextView) row.findViewById(R.id.item_text);
            holder.img = (ImageView) row.findViewById(R.id.item_image);
            row.setTag(holder);
        } else {
        	holder = (ViewHolder) row.getTag();
        }
		
		holder.text.setText(files[position]);
		
		File file = this.getFileImg(new File(directory + files[position]));
		
		if(file != null){
			Log.e(TAG, "FILE:"  + file.getName());
	        Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
			holder.img.setImageBitmap(myBitmap);
			file = null;
		}else {
			holder.img.setImageBitmap(null);
		}
			
		holder.text.setBackgroundColor(Color.parseColor(colors[position % colors.length]));
		holder.img.setBackgroundColor(Color.parseColor(colors[position % colors.length]));
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
}
