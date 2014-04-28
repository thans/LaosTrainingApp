package com.example.laostrainingapp;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

public class MyViewAdapter extends BaseAdapter {

	private static final String TAG = MyViewAdapter.class.getSimpleName();
		//	MainActivity.class.getSimpleName();
	
	private Context mContext;
	private String[] files;
	
	
	public MyViewAdapter(Context c, String[] s){
		mContext = c;
		files = s;
		
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
		if (convertView == null) {  // if it's not recycled, initialize some attributes
           	view = new TextView(mContext);
            view.setLayoutParams(new GridView.LayoutParams(85, 85));
            //view.setScaleType(view.ScaleType.CENTER_CROP);
            view.setPadding(8, 8, 8, 8);
        } else {
            view = (TextView) convertView;
        }
		view.setText(files[position]);
		//view.setText(files[position]);
		Log.e(TAG, "Files at: " + position +  " = " + files[position]);
		view.setBackgroundColor(Color.parseColor("#4169e1"));
		return view;
		 
	}
	
	
}
