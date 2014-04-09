package com.example.laostrainingapp;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TrainingPackageActivity extends Activity {
	public static final String INTENT_KEY_NAME = "packageName";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_training_package);

		
		String retrievedName = getIntent().getExtras().getString(INTENT_KEY_NAME);
		
		addAppIdentifier(retrievedName);
	}
	
	public void addAppIdentifier(String data) {
	    LinearLayout layout = 
		        (LinearLayout) this.findViewById(R.id.activity_training_package_linear_layout);
		TextView text = new TextView(this);
		text.setText(data);
		layout.addView(text);
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.training_package, menu);
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
