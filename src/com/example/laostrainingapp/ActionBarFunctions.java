package com.example.laostrainingapp;

import android.content.Context;
import android.content.Intent;

public class ActionBarFunctions {
    public void downloadsActivity(Context c) {
        Intent intent = new Intent(c, DownloadActivity.class);
        c.startActivity(intent);
    }
}
