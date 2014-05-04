package com.example.laostrainingapp;

import java.io.File;

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
		// TODO Auto-generated method stub
		
		
		TextView view;
		
		ViewHolder holder = null;
		
		
		View row = convertView;
		//File file = null;
		
		if (row == null) {  // if it's not recycled, initialize some attributes
			
           	LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
           	row = inflater.inflate(LayoutResourceId, parent, false); 

           	holder = new ViewHolder();	
           	
            holder.text = (TextView) row.findViewById(R.id.item_text);
            //TextView tv = (TextView) row.findViewById(R.id.item_text);
            //tv.setTextSize(50);
           // ImageView iv = (ImageView) row.findViewById(R.id.item_image);
           // iv.getLayoutParams().width = 120;
            //iv.getLayoutParams().height = 120;
            holder.img = (ImageView) row.findViewById(R.id.item_image);
            row.setTag(holder);
			
            //view = new TextView(mContext);
            //view.setLayoutParams(new GridView.LayoutParams(85, 85));
            //view.setScaleType(view.ScaleType.CENTER_CROP);
            //view.setPadding(8, 8, 8, 8);
        } else {
            //view = (TextView) convertView;
        	holder = (ViewHolder) row.getTag();
        }
		
		//view.setText(files[position]);
		
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
		//view.setText(files[position]);
		
		
		LayoutParams lpText = holder.text.getLayoutParams();
		LayoutParams lpImg = holder.img.getLayoutParams();
		
		/*view.setBackgroundColor(Color.parseColor("#4169e1"));
		lp.height = 200;
		lp.width = 500;
		view.setTextSize(70);*/
		
		//holder.text.setTextSize(50);
		
		holder.text.setBackgroundColor(Color.parseColor("#4169e1"));
		holder.img.setBackgroundColor(Color.parseColor("#4169e1"));
		
		//return view;
		/*LayoutParams lp = row.getLayoutParams();
		lp.height = 300;
		lp.width = 600;
		*/
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
