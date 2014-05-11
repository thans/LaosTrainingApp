package com.uwcse.morepractice;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class ActionBarFunctions {
    public void downloadsActivity(Context c) {
        Intent intent = new Intent(c, DownloadActivity.class);
        c.startActivity(intent);
    }
    
    public void quizActivity(Context context) {
		Intent intent = new Intent(context, QuizActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("QuizFileName", "fridge_tag_quiz.csv");
		intent.putExtras(bundle);
		context.startActivity(intent);
    }
    
    public void refresh(Context c) {
    	Intent intent = new Intent(c, DownloadActivity.class);
    	((Activity) c).finish();
    	c.startActivity(intent);
    }
}
