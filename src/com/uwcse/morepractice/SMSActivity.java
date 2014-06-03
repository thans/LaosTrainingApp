package com.uwcse.morepractice;

import java.util.Random;

import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.Build;

public class SMSActivity extends Activity {
	
	private static int stepNumber;
	private static String[] stepInstructions;
	
	private int[] counters;
	private char[] text;
	private int last;
	private int screenCount;
	private int letterCount;
	boolean firstClick = true;
	
	private SharedPreferences sp;  
    public static final String VERSION = "version"; 
	
	private static final String[] answers = {"A32", "A70", "0"};
	private static final int[] images = {R.drawable.tag1, R.drawable.tag2, R.drawable.tag3};
	private int index;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sms);
		setTitle(getString(R.string.sms_training));
		RelativeLayout layout = (RelativeLayout) this.findViewById(R.id.sms_relative_layout);
		
		sp = getPreferences(Context.MODE_PRIVATE);
		// get the last version; if not set, version is 0
        index = sp.getInt(VERSION, 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(VERSION, (index + 1) % answers.length);
        editor.commit();
		
		ImageView imageView = (ImageView) findViewById(R.id.tag_img);
		imageView.setImageResource(images[index]);
		Log.e("ANSWER", "'" + answers[index] + "'");
		
		counters = new int[10];
		text = new char[48];
		screenCount = 0;
		letterCount = 0;
		last = 0;
		
		final Button back = (Button) findViewById(R.id.back_button);
		final Button one = (Button) findViewById(R.id.one_button);
		final Button two = (Button) findViewById(R.id.two_button);
		final Button three = (Button) findViewById(R.id.three_button);
		final Button four = (Button) findViewById(R.id.four_button);
		final Button five = (Button) findViewById(R.id.five_button);
		final Button six = (Button) findViewById(R.id.six_button);
		final Button seven = (Button) findViewById(R.id.seven_button);
		final Button eight = (Button) findViewById(R.id.eight_button);
		final Button nine = (Button) findViewById(R.id.nine_button);
		final Button zero = (Button) findViewById(R.id.zero_button);
		final Button next = (Button) findViewById(R.id.next_button);
		
		back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	TextView screen = (TextView) findViewById(R.id.phone_screen);
            	if (screenCount > 0) {	
	            	if (back.getId() == last) {
	                		screenCount--;	
	                	}
	            		last = back.getId();
	            		letterCount = 0;
		            	
		            	
		                screen.setText(text, 0, screenCount);
		                firstClick = true;	
	            } else {
	            	screen.setText(text,0,0);
	            	firstClick = true;
	            	letterCount = 0;
	            }
            }
        });
		
		next.setOnClickListener(new View.OnClickListener() {
		    public void onClick(View v) {
		        letterCount = 0;
		        last = 0;
		        //screenCount++;
		        sendMessage();
		    }
		});
		
		one.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if (firstClick) {
            		firstClick = false;
            		last = one.getId();
            	} else if (one.getId() != last) {
            		last = one.getId();
            		screenCount++;
            		letterCount = 0;
            	}
            	TextView screen = (TextView) findViewById(R.id.phone_screen);
                int c;
            	if(letterCount == 0){
                	c = 46; // "."
                	letterCount++;
                } else if(letterCount == 1){
                	c = 64; // "@"
                	letterCount++;
                }else if(letterCount == 2){
                	c = 44; // ","
                	letterCount++;
                }else if(letterCount == 3){
                	c = 45; // "-"
                	letterCount++;
                }else if(letterCount == 4){
                	c = 63; // "?"
                	letterCount++;
                } else if(letterCount == 5){
                	c = 33; // "!"
                	letterCount++;
                } else if(letterCount == 6){
                	c = 58; // ":"
                	letterCount++;
                }else if(letterCount == 7){
                	c = 47; // "/"
                	letterCount++;
                }else {
                	c = 49; // "1"
                	letterCount = 0;
                }
                text[screenCount] = (char) c;
                screen.setText(text, 0, screenCount + 1);
        		}
            
        });
		
		two.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if (screenCount < text.length - 1) {
	            	if (firstClick) {
	            		firstClick = false;
	            		last = two.getId();
	            	} else if (two.getId() != last) {
	            		last = two.getId();
	            		screenCount++;
	            		letterCount = 0;
	            	}
	            	TextView screen = (TextView) findViewById(R.id.phone_screen);
	                int c;
	            	if(letterCount == 3){
	                	c = 50;
	                	letterCount = 0;
	                } else {
	                	c = 'A' + letterCount;
	                	letterCount++;
	                }
	                text[screenCount] = (char) c;
	                screen.setText(text, 0, screenCount + 1);
            	}
            }
        });
		
		three.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (screenCount < text.length - 1) {
	                if (firstClick) {
	            		firstClick = false;
	            		last = three.getId();
	            	} else if (three.getId() != last) {
	            		last = three.getId();
	            		screenCount++;
	            		letterCount = 0;
	            	}
	                TextView screen = (TextView) findViewById(R.id.phone_screen);
	                int c;
	            	if(letterCount == 3){
	                	c = 51;
	                	letterCount = 0;
	                } else {
	                	c = 'D' + letterCount;
	                	letterCount++;
	                }
	                text[screenCount] = (char) c;
	                screen.setText(text, 0, screenCount + 1);

                }
            }
        });
		
		four.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (screenCount < text.length - 1) {
	                if (firstClick) {
	            		firstClick = false;
	            		last = four.getId();
	            	} else if (four.getId() != last) {
	            		last = four.getId();
	            		screenCount++;
	            		letterCount = 0;
	            	}
	                TextView screen = (TextView) findViewById(R.id.phone_screen);
	                int c;
	            	if(letterCount == 3){
	                	c = 52;
	                	letterCount = 0;
	                } else {
	                	c = 'G' + letterCount;
	                	letterCount++;
	                }
	                text[screenCount] = (char) c;
	                screen.setText(text, 0, screenCount + 1);

                }
            }
        });
		
		five.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (screenCount < text.length - 1) {
	                if (firstClick) {
	            		firstClick = false;
	            		last = five.getId();
	            	} else if (five.getId() != last) {
	            		last = five.getId();
	            		screenCount++;
	            		letterCount = 0;
	            	}
	                TextView screen = (TextView) findViewById(R.id.phone_screen);
	                int c;
	            	if(letterCount == 3){
	                	c = 53;
	                	letterCount = 0;
	                } else {
	                	c = 'J' + letterCount;
	                	letterCount++;
	                }
	                text[screenCount] = (char) c;
	                screen.setText(text, 0, screenCount + 1);

                }
            }
        });
		
		six.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (screenCount < text.length - 1) {
	                if (firstClick) {
	            		firstClick = false;
	            		last = six.getId();
	            	} else if (six.getId() != last) {
	            		last = six.getId();
	            		screenCount++;
	            		letterCount = 0;
	            	}
	                TextView screen = (TextView) findViewById(R.id.phone_screen);
	                int c;
	            	if(letterCount == 3){
	                	c = 54;
	                	letterCount = 0;
	                } else {
	                	c = 'M' + letterCount;
	                	letterCount++;
	                }
	                text[screenCount] = (char) c;
	                screen.setText(text, 0, screenCount + 1);

                }
            }
        });
		
		seven.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (screenCount < text.length - 1) {
	                if (firstClick) {
	            		firstClick = false;
	            		last = seven.getId();
	            	} else if (seven.getId() != last) {
	            		last = seven.getId();
	            		screenCount++;
	            		letterCount = 0;
	            	}
	                TextView screen = (TextView) findViewById(R.id.phone_screen);
	                int c;
	            	if(letterCount == 4){
	                	c = 55;
	                	letterCount = 0;
	                } else {
	                	c = 'P' + letterCount;
	                	letterCount++;
	                }
	                text[screenCount] = (char) c;
	                screen.setText(text, 0, screenCount + 1);

                }
            }
        });
		
		
		eight.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (screenCount < text.length - 1) {
	                if (firstClick) {
	            		firstClick = false;
	            		last = eight.getId();
	            	} else if (eight.getId() != last) {
	            		last = eight.getId();
	            		screenCount++;
	            		letterCount = 0;
	            	}
	                TextView screen = (TextView) findViewById(R.id.phone_screen);
	                int c;
	            	if(letterCount == 3){
	                	c = 56;
	                	letterCount = 0;
	                } else {
	                	c = 'T' + letterCount;
	                	letterCount++;
	                }
	                text[screenCount] = (char) c;
	                screen.setText(text, 0, screenCount + 1);

                }
            }
        });
		
		nine.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (screenCount < text.length - 1) {
	                if (firstClick) {
	            		firstClick = false;
	            		last = nine.getId();
	            	} else if (nine.getId() != last) {
	            		last = nine.getId();
	            		screenCount++;
	            		letterCount = 0;
	            	}
	                TextView screen = (TextView) findViewById(R.id.phone_screen);
	                int c;
	            	if(letterCount == 4){
	                	c = 57;
	                	letterCount = 0;
	                } else {
	                	c = 'W' + letterCount;
	                	letterCount++;
	                }
	                text[screenCount] = (char) c;
	                screen.setText(text, 0, screenCount + 1);

                }
            }
        });
		
		
		zero.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	if (screenCount < text.length - 1) {
	            	if (firstClick) {
	            		firstClick = false;
	            		last = zero.getId();
	            		screenCount--;
	            	}
            		last = zero.getId();
            		letterCount = 0;
	            	screenCount++;
	            	TextView screen = (TextView) findViewById(R.id.phone_screen);
	                int c = 48;
	                text[screenCount] = (char) c;
	                screen.setText(text, 0, screenCount + 1);
	                //screenCount++;
            	}
            }
        });
		
		Button send = (Button) findViewById(R.id.send_text);
		send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
	}
	
	
	private void sendMessage() {
	    TextView screen = (TextView) findViewById(R.id.phone_screen);
        String s = screen.getText().toString();
        
        // get the title for the alert
        String title = "";
        String answer = answers[index];
        Log.e("REAL ANSWER", answer);
        Log.e("USER ANSWER", s);
        if (s.equals(answer)) {
            title += getString(R.string.texting_correct) + "\n " + getString(R.string.try_again);
        } else {
            title += getString(R.string.texting_answer) + " " + answer + ". ";
            title += "\n " + getString(R.string.try_again);
        }
        
        // alert user if answer is correct
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SMSActivity.this);
        alertDialogBuilder
        .setTitle(title)
        .setCancelable(false)
        .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                // if this button is clicked, refresh activity
                Intent intent = new Intent(SMSActivity.this, SMSActivity.class);
                SMSActivity.this.finish();
                SMSActivity.this.startActivity(intent);
            }
        })
        .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                // if this button is clicked, close current activity
                SMSActivity.this.finish();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.sms, menu);
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
