package com.uwcse.morepractice;

import java.util.Random;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CustomTags extends Activity {
	private static int stepNumber;
	private static String[] stepInstructions;
	
	private static final int HIGH_ALARM_MARGIN_TOP = 107;
	private static final int LOW_ALARM_MARGIN_TOP = 120;
	private static final int TODAY_ALARM_LEFT = 790;
	private static final int DAILY_WIDTH = 23;
	
	private static final String[] TEMPS = {"06.1",  "04.7",  "09.7",  "03.7",  "06.4",  "-02.3", "09.6"};
	private static final String[] TIMES = {"00:00", "00:00", "04:41", "00:00", "00:00", "03:12", "12:52"};
	private static String[] COMMENTS;
	
	private static final int LOW_ALARM_INDEX = 5;
	private static final int HIGH_ALARM_INDEX = 6;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_custom_tags);

		final CustomTags act = this;

        ImageButton next = (ImageButton) this.findViewById(R.id.next_instruction);
        
        stepNumber = -1;
        stepInstructions = getResources().getStringArray(R.array.custom_tags_instructions);
        COMMENTS = getResources().getStringArray(R.array.custom_tags_history_instructions);
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
        		act.next();
            }
        });
	}

	protected void next() {
		stepNumber++;
		if (stepNumber >= stepInstructions.length) {
			Button read = (Button) this.findViewById(R.id.read_button);
			final CustomTags act = this;
	        read.setOnClickListener(new View.OnClickListener() {
	            public void onClick(View v) {
	        		act.goToHistoryMode();
	            }
	        });
	        
			TextView instructions = (TextView) this.findViewById(R.id.custom_tags_instructions);
			instructions.setText(R.string.custom_tags_history_intro);
			ImageButton next = (ImageButton) this.findViewById(R.id.next_instruction);
			if (next != null) {
				RelativeLayout nextLayout = (RelativeLayout) this.findViewById(R.id.next_button_layout);
				nextLayout.setVisibility(RelativeLayout.INVISIBLE);
			}
			return;
		}
		
		TextView instructions = (TextView) this.findViewById(R.id.custom_tags_instructions);
		instructions.setText(stepInstructions[stepNumber]);
	}

	private void goToHistoryMode() {
		RelativeLayout relativeLayout = (RelativeLayout) this.findViewById(R.id.custom_tags_relative_layout);
		
		final TextView highAlarm = new TextView(this);//(TextView) this.findViewById(R.id.alarm_up);
		highAlarm.setText(getResources().getString(R.string.up_arrow));
		highAlarm.setTextSize(24);
		RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		layout.addRule(RelativeLayout.ALIGN_LEFT, R.id.fridge_tag);
		layout.addRule(RelativeLayout.ALIGN_TOP, R.id.fridge_tag);
		layout.leftMargin = TODAY_ALARM_LEFT;
		layout.topMargin = HIGH_ALARM_MARGIN_TOP;
		relativeLayout.addView(highAlarm, layout);
		
		final Animation anim = new AlphaAnimation(0.0f, 1.0f);
		anim.setDuration(400); //You can manage the time of the blink with this parameter
		anim.setStartOffset(20);
		anim.setRepeatMode(Animation.REVERSE);
		anim.setRepeatCount(Animation.INFINITE);
		highAlarm.startAnimation(anim);
		stepNumber = 0;
		
		TextView instructions = (TextView) this.findViewById(R.id.custom_tags_instructions);
		instructions.setText(COMMENTS[stepNumber]);
		
		TextView duration = (TextView) this.findViewById(R.id.time_text);
		duration.setText(TIMES[stepNumber]);
		
		TextView temp = (TextView) this.findViewById(R.id.temp_text);
		
		temp.setText(showNegative(TEMPS[stepNumber]));
	
        Button read = (Button) this.findViewById(R.id.read_button);
        final CustomTags act = this;
        
        read.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
        		act.nextHistory(anim, highAlarm);
            }
        });
	}
	
	private String showNegative(String temperature) {
		TextView negative = (TextView) this.findViewById(R.id.negative_temp);
		
		if (Double.parseDouble(temperature) < 0) {
			negative.setVisibility(TextView.VISIBLE);
			return temperature.substring(1);
		} else {
			negative.setVisibility(TextView.INVISIBLE);
			return temperature;
		}
	}

	

	protected void nextHistory(Animation anim, final TextView alarm) {
		anim.cancel();

		stepNumber++;
		if (stepNumber >= TEMPS.length) {
			completeTraining();
			return;
		}
		
		TextView lowAlarm = (TextView) this.findViewById(R.id.alarm_down);
		
		if (stepNumber == LOW_ALARM_INDEX) {
			lowAlarm.setVisibility(TextView.INVISIBLE);
		} else {
			lowAlarm.setVisibility(TextView.VISIBLE);
		}
		
		TextView highAlarm = (TextView) this.findViewById(R.id.alarm_up);
		if (stepNumber == HIGH_ALARM_INDEX) {
			highAlarm.setVisibility(TextView.INVISIBLE);
		} else {
			highAlarm.setVisibility(TextView.VISIBLE);
		}
		
		RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
		layout.addRule(RelativeLayout.ALIGN_LEFT, R.id.fridge_tag);
		layout.addRule(RelativeLayout.ALIGN_TOP, R.id.fridge_tag);
		layout.leftMargin = TODAY_ALARM_LEFT - DAILY_WIDTH * ((stepNumber / 2));
		
		// Set the comment
		TextView instructions = (TextView) this.findViewById(R.id.custom_tags_instructions);
		instructions.setText(COMMENTS[stepNumber]);
		
		// Set the temp
		TextView temp = (TextView) this.findViewById(R.id.temp_text);
		temp.setText(showNegative(TEMPS[stepNumber]));
		
		// Set the time
		TextView time = (TextView) this.findViewById(R.id.time_text);
		time.setText(TIMES[stepNumber]);
		
		// Set the alarm status
		TextView okayStatus = (TextView) this.findViewById(R.id.alarm_ok_text);
		TextView alarmStatus = (TextView) this.findViewById(R.id.alarm_text);
		
		if (alarm()) {
			alarmStatus.setVisibility(TextView.VISIBLE);
			okayStatus.setVisibility(TextView.INVISIBLE);
		} else {
			okayStatus.setVisibility(TextView.VISIBLE);
			alarmStatus.setVisibility(TextView.INVISIBLE);
		}
		
		if (stepNumber % 2 == 1) { // Low alarms
			alarm.setText(getResources().getString(R.string.down_arrow));
			layout.topMargin = LOW_ALARM_MARGIN_TOP;
		} else {
			alarm.setText(getResources().getString(R.string.up_arrow));
			layout.topMargin = HIGH_ALARM_MARGIN_TOP;
		}
		alarm.setLayoutParams(layout);
		
		final Animation animate = new AlphaAnimation(0.0f, 1.0f);
		animate.setDuration(400); //You can manage the time of the blink with this parameter
		animate.setStartOffset(20);
		animate.setRepeatMode(Animation.REVERSE);
		animate.setRepeatCount(Animation.INFINITE);
		alarm.startAnimation(animate);
	}

	private void completeTraining() {

		TextView highAlarm = (TextView) this.findViewById(R.id.alarm_up);
		if (stepNumber == HIGH_ALARM_INDEX) {
			highAlarm.setVisibility(TextView.INVISIBLE);
		} else {
			highAlarm.setVisibility(TextView.VISIBLE);
		}
		
		// Set the alarm status
		TextView okayStatus = (TextView) this.findViewById(R.id.alarm_ok_text);
		TextView alarmStatus = (TextView) this.findViewById(R.id.alarm_text);
		okayStatus.setVisibility(TextView.VISIBLE);
		alarmStatus.setVisibility(TextView.INVISIBLE);
		
		TextView instructions = (TextView) this.findViewById(R.id.custom_tags_instructions);
		instructions.setText(R.string.complete_custom_tags);
		
        Button read = (Button) this.findViewById(R.id.read_button);
        final CustomTags act = this;
        
        read.setOnClickListener(null);
        
        ImageButton next = (ImageButton) this.findViewById(R.id.next_instruction);
		if (next != null) {
			RelativeLayout nextLayout = (RelativeLayout) this.findViewById(R.id.next_button_layout);
			nextLayout.setVisibility(RelativeLayout.VISIBLE);
		}
		
		next.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				act.finish();
			}
		});
	}

	private boolean alarm() {
		String time = TIMES[stepNumber];
		double temp = Double.parseDouble(TEMPS[stepNumber]);
		int hour = Integer.parseInt(time.split(":")[0]);
		return ((temp >= 8 && hour >= 10) || (temp < -.05 && hour >= 1));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.custom_tags, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}


}
