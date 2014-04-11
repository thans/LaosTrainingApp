package com.example.laostrainingapp;

import android.app.Activity;
import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoActivity extends Activity{
	
	protected static String videoName;

	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Get the layout from video_main.xml
        setContentView(R.layout.videoview_main);
        
        
		final VideoView videoview = (VideoView) findViewById(R.id.VideoView);
		final ProgressDialog pDialog = new ProgressDialog(VideoActivity.this);
		videoName = this.getIntent().getExtras().getString("VIDEO_NAME");
		pDialog.setTitle("SMS Video" + videoName);
		pDialog.setMessage("Buffering...");
		pDialog.setIndeterminate(false);
		pDialog.setCancelable(false);
		pDialog.show();
		
		try{
			MediaController mediacontroller = new MediaController(VideoActivity.this);
			mediacontroller.setAnchorView(videoview);
			videoview.setMediaController(mediacontroller);
			videoview.setVideoPath(videoName);
		} catch (Exception e) {
			Log.e("Error", e.getMessage());
			e.printStackTrace();
		}
		
		videoview.requestFocus();
		videoview.setOnPreparedListener(new OnPreparedListener() {
			public void onPrepared(MediaPlayer mp){
				pDialog.dismiss();
				videoview.start();
			}
		});
	}
}
