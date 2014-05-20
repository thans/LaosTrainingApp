package com.uwcse.morepractice;

import java.util.Random;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.Build;

public class CustomTags extends Activity {
	private static Random r = new Random();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_custom_tags);

//		TextView timeText = (TextView) this.findViewById(R.id.time_text);
		Button set = (Button) this.findViewById(R.id.set_button);
		Button read = (Button) this.findViewById(R.id.read_button);
		
		final CustomTags act = this;
        set.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
        		act.setClick();
            }
        });
        
        read.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
        		act.readClick();
            }
        });
	}

	protected void readClick() {
		TextView tempText = (TextView) this.findViewById(R.id.temp_text);
		TextView alarmText = (TextView) this.findViewById(R.id.alarm_text);
		
		tempText.setText(String.format("%02d:%01d", (r.nextInt(10)), (r.nextInt(10))));
		
		if (r.nextBoolean()) {
			alarmText.setText("ALM");
		} else {
			alarmText.setText("OK");
		}
	}

	protected void setClick() {
		TextView timeText = (TextView) this.findViewById(R.id.time_text);
		
		timeText.setText(String.format("%02d:%02d", r.nextInt(24), r.nextInt(60)));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.custom_tags, menu);
		return true;
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


}
