package com.example.laostrainingapp;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;

public class TrainingPackageClickListener implements OnClickListener {
	private Intent intent;
	private Activity act;
	public TrainingPackageClickListener(Activity act, Intent intent) {
		this.intent = intent;
		this.act = act;
	}
	
	@Override
	public void onClick(View v) {
		act.startActivity(intent);
	}
}