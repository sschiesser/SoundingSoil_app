package com.couchbase.emptyapp;

import android.app.Activity;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.widget.Button;

public class StartActivity extends Activity {
	
	Button button;

	@SuppressWarnings("unused")
	private ServiceConnection couchServiceConnection;

	protected static final String TAG = "SoundingSoil";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {  	
    	 
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.start_layout);

        Thread welcomeThread = new Thread() { 

            @Override
            public void run() { 
                try {
                    super.run();
                    sleep(2150);
                } catch (Exception e) {  
                } finally {
                    Intent i = new Intent(StartActivity.this,
                             MainActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        };
        
        welcomeThread.start();
    }
     
    @Override
    public void onBackPressed() {
        // Do Here what ever you want do on back press;
    	}
}