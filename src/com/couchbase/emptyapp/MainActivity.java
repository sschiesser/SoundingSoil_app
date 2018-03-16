package com.couchbase.emptyapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.ServiceConnection;

public class MainActivity extends Activity {
	Button button;
	public int x;
	public int y;
	
	@SuppressWarnings("unused")
	private ServiceConnection couchServiceConnection;
	
	protected static final String TAG = "SoundingSoil";
	
    @SuppressLint({ "CutPasteId", "InflateParams", "InlinedApi" })
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        
        button = (Button) findViewById(R.id.btn);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {   
            	
            	Button mLinearLayout = (Button) findViewById(R.id.btn);
            	
            	if(x != 1){
            		mLinearLayout.setBackgroundResource(R.drawable.b_pause);
            		x= 1;
            	}else {
            		mLinearLayout.setBackgroundResource(R.drawable.b_play);
            		x = 2;
            	}
            
            }
        });
        
        button = (Button) findViewById(R.id.button_id2);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	
            	Button mLinearLayout = (Button) findViewById(R.id.button_id2);
            	
            	if(y != 1){
            		mLinearLayout.setBackgroundResource(R.drawable.b_stop);
            		y= 1;
            	}else {
            		mLinearLayout.setBackgroundResource(R.drawable.b_record);
            		y = 2;
            	}
            	
            	BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();    
            	if (mBluetoothAdapter.isEnabled()) {
            	    setBluetooth(true); 
            	} else {
            		setBluetooth(false);
            	}
            
            }
        });
    }
    
    public static boolean setBluetooth(boolean enable) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (enable) {
        	bluetoothAdapter.disable();
        }
        else if(!enable) {
        	bluetoothAdapter.enable();
        }
        return true;
    }
    
    @Override
    public void onBackPressed() {
    	
    }
}