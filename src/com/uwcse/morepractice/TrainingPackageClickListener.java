package com.uwcse.morepractice;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
/**
 * A custom {@link OnClickListener} that launches an activity and passes it an intent
 * @author toreh
 *
 */
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