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

public class LanguageViewAdapter extends BaseAdapter {

	private Context mContext;
	private String[] files;
	private String[] colors = {"#A4C400", "#60A917", "#008A00", "#00ABA9","#1BA1E2", "#0050EF","#6A00FF", "#AA00FF", 
							   "#F472D0", "#D80073", "#A20025", "#E51400", "#FA6800", "#F0A30A", "#E3C800"};
	
	public LayoutInflater inflater;
	public int LayoutResourceId;
	
	public LanguageViewAdapter(Context c, String[] s, int id){
		mContext = c;
		files = s;
		LayoutResourceId = id;
	}
	
	public LanguageViewAdapter(Context c){
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
		
		TextView view;
		
		View row = convertView;
		
		if (row == null) {  // if it's not recycled, initialize some attributes
           	LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
           	row = inflater.inflate(LayoutResourceId, parent, false); 
           	view = (TextView) row.findViewById(R.id.language_text);
            row.setTag(view);
        } else {
        	view = (TextView) row.getTag();
        }
		
		view.setText(files[position]);
			
		view.setBackgroundColor(Color.parseColor(colors[position % colors.length]));
		
		return row; 
	}
	
}
