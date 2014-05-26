package com.uwcse.morepractice;

import com.uwcse.morepractice.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

public class Splash extends Activity {

    private final int SPLASH_DISPLAY_LENGTH = 3000;
    private final int DELAY = SPLASH_DISPLAY_LENGTH / 6;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.splash);
        
        // sets a font to better accommodate Lao script
        ChangeFont.setDefaultFont(this, "MONOSPACE", "fonts/Phetsarath_OT.ttf");
        
        final ImageView splash = (ImageView) this.findViewById(R.id.splashscreen);
        // Delay, then animate the image
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // stop the animation
                RotateAnimation anim = new RotateAnimation(0, 360,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                anim.setInterpolator(new LinearInterpolator());
                anim.setDuration((SPLASH_DISPLAY_LENGTH - DELAY) / 2);
                anim.setRepeatCount(Animation.INFINITE);

                // Start animating the image
                splash.startAnimation(anim);
            }
        }, DELAY);  

        /* New Handler to start the Menu-Activity 
         * and close this Splash-Screen after some seconds.*/
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // stop the animation
                splash.setAnimation(null);
                Intent openMainActivity =  new Intent(Splash.this, ChooseLanguage.class);
                startActivity(openMainActivity);
                overridePendingTransition(R.animator.slide_no_move, R.animator.fade);
                finish();

            }
        }, SPLASH_DISPLAY_LENGTH);    
    }
    
    public void onFinishInflate() {
    	
    }
}