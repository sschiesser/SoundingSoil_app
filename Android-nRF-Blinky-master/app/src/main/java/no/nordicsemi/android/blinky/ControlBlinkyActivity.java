/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *  Neither the name of copyright holder nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.blinky;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.codetroopers.betterpickers.radialtimepicker.RadialTimePickerDialogFragment;

import ak.sh.ay.musicwave.MusicWave;
import no.nordicsemi.android.blinky.profile.BleProfileService;
import no.nordicsemi.android.blinky.service.BlinkyService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import no.nordicsemi.android.blinky.MainActivity;

public class ControlBlinkyActivity extends AppCompatActivity implements RadialTimePickerDialogFragment.OnTimeSetListener{

	private BlinkyService.BlinkyBinder mBlinkyDevice;
	private UartService.LocalBinder mBlinkyDevice2;
	private Button mActionOnOff, mActionConnect, mActionOnOff2;
	private ImageView mImageBulb;
	private ImageView mImageBulb2;
	private View mParentView;
	private View mBackgroundView;
	private View mBackgroundView2;
	private LinearLayout LinearLayout;
	private LinearLayout LinearLayoutWave;
	private int height;
	private TextView recordfor;
	private TextView pausefor;

	private MusicWave musicWave;
	private MediaPlayer mMediaPlayer;
	private Visualizer mVisualizer;
	private boolean mMediaPlayerMute = false;

	private boolean mActionOnOff2_b = false;
	private boolean mActionOnOff_b = false;
	public boolean bulb = false;
	public boolean bulb2 = false;
	public boolean timepicker = true;
	List<String> permissions = new ArrayList<String>();

	public MainActivity ma;
	public MediaPlayer m = new MediaPlayer();

    ////////////////////////////////////////////////////////////////////////////////////////////////
    private static final int REQUEST_ENABLE_BT = 2;
    private static final int REQUEST_SELECT_DEVICE = 1;
    private static final int STATE_OFF = 10;
    public static final String TAG = "YEAH";
    private static final int UART_PROFILE_CONNECTED = 20;
    private static final int UART_PROFILE_DISCONNECTED = 21;
    private static final int UART_PROFILE_READY = 10;
    private final BroadcastReceiver UARTStatusChangeReceiver = new C00593();
    private ArrayAdapter<String> listAdapter;
    private BluetoothAdapter mBtAdapter = null;
    private BluetoothDevice mDevice = null;
    private Handler mHandler = new C00552();
    TextView mRemoteRssiVal;
    RadioGroup mRg;
    private UartService mService = null;
    private ServiceConnection mServiceConnection2 = new C00541();
    private int mState = UART_PROFILE_DISCONNECTED;
    private BluetoothAdapter mBluetoothAdapter;
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new C00501();

	public String deviceAddress = "";

	class C00501 implements BluetoothAdapter.LeScanCallback {
		C00501() {
		}

		public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
			ControlBlinkyActivity.this.runOnUiThread(new Runnable() {
				public void run() {
					ControlBlinkyActivity access$0 = ControlBlinkyActivity.this;
					final BluetoothDevice bluetoothDevice = device;
					final int i = rssi;
					access$0.runOnUiThread(new Runnable() {
						public void run() {
							//ControlBlinkyActivity.this.addDevice(bluetoothDevice, i);
						}
					});
				}
			});
		}
	}

    class C00541 implements ServiceConnection {
        C00541() {
        }

        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
        	Log.d("YEAH", "WHAT");
            ControlBlinkyActivity.this.mService = ((UartService.LocalBinder) rawBinder).getService();
            Log.d("YEAH", "onServiceConnected mService = " + ControlBlinkyActivity.this.mService);
            Log.d("YEAH", "Address" + deviceAddress);

			Log.d("YEAH", "Address" + deviceAddress);
			mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
			Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);

			mService.initialize();
			mService.connect(deviceAddress);

            if (!ControlBlinkyActivity.this.mService.initialize()) {
                Log.e("YEAH", "Unable to initialize Bluetooth");
                ControlBlinkyActivity.this.finish();
            }
        }

        public void onServiceDisconnected(ComponentName classname) {
            ControlBlinkyActivity.this.mService = null;
        }
    }

    class C00552 extends Handler {
        C00552() {
        }

        public void handleMessage(Message msg) {
        }
    }

    class C00593 extends BroadcastReceiver {

        class C00561 implements Runnable {
            C00561() {
            }

            public void run() {
                String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                Log.d("YEAH", "UART_CONNECT_MSG");
            }
        }

        class C00572 implements Runnable {
            C00572() {
            }

            public void run() {
                String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                Log.d("YEAH", "UART_DISCONNECT_MSG");
                ControlBlinkyActivity.this.mService.close();
            }
        }

        C00593() {
        	Log.d("YEAH", "Create BroadCastRec UART");
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            Intent mIntent = intent;
            if (action.equals(UartService.ACTION_GATT_CONNECTED)) {
                ControlBlinkyActivity.this.runOnUiThread(new C00561());
            }
            if (action.equals(UartService.ACTION_GATT_DISCONNECTED)) {
                ControlBlinkyActivity.this.runOnUiThread(new C00572());
            }
            if (action.equals(UartService.ACTION_GATT_SERVICES_DISCOVERED)) {
                ControlBlinkyActivity.this.mService.enableTXNotification();
            }
            if (action.equals(UartService.ACTION_DATA_AVAILABLE)) {
                final byte[] txValue = intent.getByteArrayExtra(UartService.EXTRA_DATA);
                ControlBlinkyActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        try {
                            //Log.d("YEAH", "TX" + new String(txValue, "UTF-8"));
                            //playSound(new String(txValue, "UTF-8"));
							playSound(txValue);
                        } catch (Exception e) {
                            Log.e("YEAH", e.toString());
                        }
                    }
                });
            }
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)) {
                ControlBlinkyActivity.this.mService.disconnect();
            }
        }
    }

    public void playSound(byte[] input) throws IOException {
		//Log.d("YEAH", "Input: " + input);

		//if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
		//
		//	mMediaPlayer = MediaPlayer.create(this, R.raw.music_example);
		//	askPermission();
		//	prepareVisualizer();
		//	mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
		//		@Override
		//		public void onCompletion(MediaPlayer mediaPlayer) {
		//			mVisualizer.setEnabled(true);
		//			mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
		//				public void onPrepared(MediaPlayer player) {
		//					player.start();
		//				}
		//			});
		//		}
		//	});
		//	mVisualizer.setEnabled(true);
		//	mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
		//		public void onPrepared(MediaPlayer player) {
		//			player.start();
		//		}
		//	});
		//	mMediaPlayer.start();
		//	mMediaPlayer.setVolume(0, 0);
//
		//	LinearLayoutWave = (android.widget.LinearLayout) findViewById(R.id.wave);
		//	LinearLayoutWave.setOnClickListener(new View.OnClickListener() {
		//		@Override
		//		public void onClick(View view) {
		//			if (mMediaPlayerMute) {
		//				mMediaPlayer.setVolume(0, 0);
		//				mMediaPlayerMute = false;
		//			} else {
		//				mMediaPlayer.setVolume(1, 1);
		//				mMediaPlayerMute = true;
		//			}
		//		}
		//	});
		//}

		Log.d("YEAH", "1" + input[1]);
		Log.d("YEAH", "2" + input[2]);

		playByteArray(input);

	}

	private void playByteArray(byte[] mp3SoundByteArray) throws IOException {

		byte[] bytearray = mp3SoundByteArray;

		try {
			File file = File.createTempFile("UTF-8",".mp3");
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(bytearray);
			

			m.reset();
			m.setDataSource(String.valueOf(file));
			m.prepare();
			m.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
						public void onPrepared(MediaPlayer player) {
							player.start();
							Log.d("YEAH", "START");
						}
			});
			m.setVolume(1,1);
			m.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mediaPlayer) {
					Log.d("YEAH", "FINISH");
				}
			});

		} catch (IOException e) {
			String s = e.toString();
			Log.d("YEAH", "Error" + s);
		}

	}

	@SuppressLint("WrongConstant")
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("YEAH", "$$$" + requestCode);
		switch (requestCode) {
			case 1:
				if (resultCode == -1 && data != null) {
					Log.d("YEAH", "Address" + deviceAddress);
					this.mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
					Log.d(TAG, "... onActivityResultdevice.address==" + this.mDevice + "mserviceValue" + this.mService);
					this.mService.connect(deviceAddress);
					return;
				}
				return;
			case 2:
				if (resultCode == -1) {
					Toast.makeText(this, "Bluetooth has turned on ", 0).show();
					return;
				}
				Log.d(TAG, "BT not enabled");
				Toast.makeText(this, "Problem in BT Turning ON ", 0).show();
				finish();
				return;
			default:
				Log.e(TAG, "wrong request code");
				return;
		}
	}

    ////////////////////////////////////////////////////////////////////////////////////////////////

	private ServiceConnection mServiceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBlinkyDevice = (BlinkyService.BlinkyBinder) service;

			if (mBlinkyDevice.isConnected()) {
                    mActionConnect.setText(getString(R.string.action_disconnect));

				if (mBlinkyDevice.isButtonPressed()) {
					mBackgroundView.setVisibility(View.VISIBLE);
				} else {
					mBackgroundView.setVisibility(View.INVISIBLE);
				}

			} else {
				mActionConnect.setText(getString(R.string.action_connect));
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mBlinkyDevice = null;
		}
	};

	protected void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
	}

	protected void onPause() {
		Log.d(TAG, "onPause");
		super.onPause();
	}

	protected void onRestart() {
		super.onRestart();
		Log.d(TAG, "onRestart");
	}

	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		if (!this.mBtAdapter.isEnabled()) {
			Log.i(TAG, "onResume - BT not enabled yet");
			startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 2);
		}
	}

	@SuppressLint({"ClickableViewAccessibility", "WrongConstant"})
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_control_device);

		Intent i = getIntent();
		final String deviceName = i.getStringExtra(UartService.EXTRA_DEVICE_NAME);
		deviceAddress = i.getStringExtra(UartService.EXTRA_DEVICE_ADDRESS);
		Log.d("YEAH", "AddressI " + deviceAddress);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setTitle(deviceName);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		this.mBtAdapter = BluetoothAdapter.getDefaultAdapter();

		if (this.mBtAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available", 1).show();
			finish();
			return;
		}

        ActivityCompat.requestPermissions(ControlBlinkyActivity.this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                1);

		mActionOnOff = (Button) findViewById(R.id.button_blinky);
		mActionOnOff2 = (Button) findViewById(R.id.button_switch);
		mActionConnect = (Button) findViewById(R.id.action_connect);
		//mImageBulb = (ImageView) findViewById(R.id.img_bulb);
		//mImageBulb2 = (ImageView) findViewById(R.id.img_bulb2);
		mBackgroundView = findViewById(R.id.background_view);
		mBackgroundView2 = findViewById(R.id.background_view2);
		mParentView = findViewById(R.id.relative_layout_control);
		LinearLayout = (android.widget.LinearLayout) findViewById(R.id.linearlayout_button);

		mActionOnOff2.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mBlinkyDevice != null && mBlinkyDevice.isConnected()) {
					if(mActionOnOff2_b){
						mBlinkyDevice.send2(false);
						mActionOnOff2_b = false;
					} else {
						mBlinkyDevice.send2(true);
						mActionOnOff2_b = true;
					}
				} else {
					showError(getString(R.string.please_connect));
				}
			}
		});

		mActionOnOff2.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if(mActionOnOff2_b){
					mActionOnOff2.setBackgroundResource(R.drawable.b_pause_4);
				} else {
					mActionOnOff2.setBackgroundResource(R.drawable.b_play_2);
				}
				return false;
			}
		});

		mActionOnOff.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mBlinkyDevice != null && mBlinkyDevice.isConnected()) {
					if(mActionOnOff_b){
						mBlinkyDevice.send(false);
						mActionOnOff_b = false;
					} else {
						mBlinkyDevice.send(true);
						mActionOnOff_b = true;
					}
				} else {
					showError(getString(R.string.please_connect));
				}
			}
		});

		mActionOnOff.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				if(mActionOnOff_b){
					mActionOnOff.setBackgroundResource(R.drawable.b_cancel_2);
				} else {
					mActionOnOff.setBackgroundResource(R.drawable.b_stop_4);
				}
				return false;
			}
		});

		this.mBluetoothAdapter = ((BluetoothManager) getSystemService("bluetooth")).getAdapter();
		if (this.mBluetoothAdapter == null) {
			Toast.makeText(this, R.string.ble_not_supported, 0).show();
			finish();
			return;
		}

		LocalBroadcastManager.getInstance(this).registerReceiver(mBlinkyUpdateReceiver, makeGattUpdateIntentFilter());
        //LocalBroadcastManager.getInstance(this).registerReceiver(UARTStatusChangeReceiver, makeGattUpdateIntent2Filter());

        //final Intent intent2 = new Intent(this, UartService.class);
        //intent2.putExtra("android.bluetooth.device.extra.DEVICE", deviceAddress);
        //startService(intent2);
        //Log.d("YEAH", "START SERVICE");
        //bindService(intent2, mServiceConnection2, 0);

		final Intent intent = new Intent(this, BlinkyService.class);
		intent.putExtra(BlinkyService.EXTRA_DEVICE_ADDRESS, deviceAddress);
		startService(intent);
		bindService(intent, mServiceConnection, 0);

		//ControlBlinkyActivity.this.mBluetoothAdapter.stopLeScan(ControlBlinkyActivity.this.mLeScanCallback);
		//Bundle b = new Bundle();
		//b.putString("android.bluetooth.device.extra.DEVICE", deviceAddress);
		//Intent result = new Intent();
		//result.putExtras(b);
		//ControlBlinkyActivity.this.setResult(-1, result);
		//ControlBlinkyActivity.this.finish();

		mActionConnect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				/*if (mBlinkyDevice != null && mBlinkyDevice.isConnected()) {
					mBlinkyDevice.disconnect();
				} else {*/
					startService(intent);
					//startService(intent2);
					bindService(intent, mServiceConnection, 0);
					//bindService(intent2, mServiceConnection2, -1);
					service_init();
				//}
			}
		});

		ViewTreeObserver vto = LinearLayout.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				height = LinearLayout.getMeasuredHeight();

				android.view.ViewGroup.LayoutParams params1 = mActionOnOff.getLayoutParams();
				android.view.ViewGroup.LayoutParams params2 = mActionOnOff2.getLayoutParams();

				height*=0.75;
				params1.height = height;
				params1.width = height;
				params2.height = height;
				params2.width = height;

				mActionOnOff.setLayoutParams(params1);
				mActionOnOff2.setLayoutParams(params2);
			}
		});

		recordfor = (TextView) findViewById(R.id.RecordFor);

		recordfor.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				RadialTimePickerDialogFragment rtpd = new RadialTimePickerDialogFragment()
						.setOnTimeSetListener(ControlBlinkyActivity.this)
						.setStartTime(10, 10)
						.setDoneText("Done")
						.setCancelText("Cancel")
						.setThemeCustom(R.style.RadialPicker);
				rtpd.show(getSupportFragmentManager(), "timePickerDialogFragment");

				timepicker = true;
			}
		});

		pausefor = (TextView) findViewById(R.id.PauseFor);

		pausefor.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				RadialTimePickerDialogFragment rtpd = new RadialTimePickerDialogFragment()
						.setOnTimeSetListener(ControlBlinkyActivity.this)
						.setStartTime(10, 10)
						.setDoneText("Done")
						.setCancelText("Cancel")
						.setThemeCustom(R.style.RadialPicker);
				rtpd.show(getSupportFragmentManager(), "timePickerDialogFragment");

				timepicker = false;
			}
		});

		final EditText et1 = (EditText) findViewById(R.id.editText);
		//et1.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "200")});
		final TextView tv1 = (TextView) findViewById(R.id.textView);

		RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio_group);
		radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.radio_pirates) {
					et1.setEnabled(true);
					tv1.setText("hours");
					tv1.setVisibility(View.VISIBLE);
					et1.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_CLASS_NUMBER);
					et1.setText("");
					pausefor.setEnabled(true);
					recordfor.setEnabled(true);
					et1.setHint("1.5               ");
				} else if (checkedId == R.id.radio_warriors){
					et1.setEnabled(true);
					tv1.setText("times");
					tv1.setVisibility(View.VISIBLE);
					et1.setInputType(InputType.TYPE_CLASS_NUMBER);
					et1.setText("");
					pausefor.setEnabled(true);
					recordfor.setEnabled(true);
					et1.setHint("1                  ");
				} else if (checkedId == R.id.radio_ninjas) {
					et1.setEnabled(false);
					pausefor.setEnabled(false);
					recordfor.setEnabled(false);
                    et1.setHint("-                  ");
				}
			}
		});

		/* Permission
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 9976);
		} else {
			initialise();
		}*/

		musicWave = (MusicWave) findViewById(R.id.musicWave);

		service_init();
    }

	@SuppressLint("WrongConstant")
	private void service_init() {
		bindService(new Intent(this, UartService.class), this.mServiceConnection2, 1);
		LocalBroadcastManager.getInstance(this).registerReceiver(this.UARTStatusChangeReceiver, makeGattUpdateIntent2Filter());
		Log.d("YEAH", "service_init");
	}

	private void prepareVisualizer() {
		mVisualizer = new Visualizer(mMediaPlayer.getAudioSessionId());
		mVisualizer.setCaptureSize(Visualizer.getCaptureSizeRange()[1]);
		mVisualizer.setDataCaptureListener(
				new Visualizer.OnDataCaptureListener() {
					public void onWaveFormDataCapture(Visualizer visualizer,
													  byte[] bytes, int samplingRate) {
						musicWave.updateVisualizer(bytes);
					}

					public void onFftDataCapture(Visualizer visualizer,
												 byte[] bytes, int samplingRate) {
					}
				}, Visualizer.getMaxCaptureRate() / 2, true, false);
		mVisualizer.setEnabled(true);
	}

	private boolean askPermission() {


		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

			int RECORD_AUDIO = checkSelfPermission(Manifest.permission.RECORD_AUDIO);

			if (RECORD_AUDIO != PackageManager.PERMISSION_GRANTED) {
				permissions.add(Manifest.permission.RECORD_AUDIO);
			}

			if (!permissions.isEmpty()) {
				requestPermissions(permissions.toArray(new String[permissions.size()]), 1);
			} else {
				return false;
			}
		} else {
			return false;
		}

		return true;

	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
		}
		return true;
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		if (mBlinkyDevice != null && mBlinkyDevice.isConnected())
			mBlinkyDevice.disconnect();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(mServiceConnection);
		//unbindService(mServiceConnection2);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mBlinkyUpdateReceiver);
		//LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);

		mServiceConnection = null;
		//mServiceConnection2 = null;
		mBlinkyDevice = null;
	}

	private BroadcastReceiver mBlinkyUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
            Log.d("YEAH", "IN" + action);
			switch (action) {
				case BlinkyService.BROADCAST_LED_STATE_CHANGED: {
					final boolean flag = intent.getBooleanExtra(BlinkyService.EXTRA_DATA, false);
					if (flag) {
						//mImageBulb.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.bulb_on));
						mActionOnOff.setBackgroundResource(R.drawable.b_cancel_1);
						bulb = true;
					} else {
						//mImageBulb.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.bulb_off));
						mActionOnOff.setBackgroundResource(R.drawable.b_stop_3);
						bulb = false;
					}
					break;
				}
                case BlinkyService.BROADCAST_LED2_STATE_CHANGED: {
                    final boolean flag = intent.getBooleanExtra(BlinkyService.EXTRA_DATA, false);
                    if (flag) {
                        //mImageBulb2.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.bulb_on));
						mActionOnOff2.setBackgroundResource(R.drawable.b_pause_3);
                        bulb2 = true;
                    } else {
                        //mImageBulb2.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.bulb_off));
						mActionOnOff2.setBackgroundResource(R.drawable.b_play_1);
                        bulb2 = false;
                    }
                    break;
                }
				case BlinkyService.BROADCAST_BUTTON_STATE_CHANGED: {
					final boolean flag = intent.getBooleanExtra(BlinkyService.EXTRA_DATA, false);

                    //mBackgroundView.setVisibility(View.VISIBLE);
                    //Handler handler = new Handler();
                    //handler.postDelayed(new Runnable() {
                    //    @Override
                    //    public void run() {
                    //        mBackgroundView.setVisibility(View.INVISIBLE);
                    //    }
                    //}, 400);

					/* If Button.pressed activated *//*
					if(bulb == false && bulb2 == false){
						mBackgroundView.setVisibility(View.INVISIBLE);
						mBackgroundView2.setVisibility(View.INVISIBLE);
					} else if(bulb == true){
						mBackgroundView.setVisibility(View.VISIBLE);
						mBackgroundView2.setVisibility(View.INVISIBLE);
					}*/
					break;
				}
                case BlinkyService.BROADCAST_BUTTON2_STATE_CHANGED: {
                    final boolean flag = intent.getBooleanExtra(BlinkyService.EXTRA_DATA, false);

                    //mBackgroundView2.setVisibility(View.VISIBLE);
                    //Handler handler = new Handler();
                    //handler.postDelayed(new Runnable() {
					//    @Override
                    //    public void run() {
                    //        mBackgroundView2.setVisibility(View.INVISIBLE);
                    //    }
                    //}, 400);

                    /* If Button.pressed activated *//*
                    if(bulb == false && bulb2 == false){
                        mBackgroundView2.setVisibility(View.INVISIBLE);
                        mBackgroundView.setVisibility(View.INVISIBLE);
                    } else if (bulb2 == true){
                        mBackgroundView2.setVisibility(View.VISIBLE);
                        mBackgroundView.setVisibility(View.INVISIBLE);
                    }*/
                    break;
                }
				case BlinkyService.BROADCAST_CONNECTION_STATE: {
					final int value = intent.getIntExtra(BlinkyService.EXTRA_CONNECTION_STATE, BlinkyService.STATE_DISCONNECTED);
					switch (value) {
						case BleProfileService.STATE_CONNECTED:
							mActionConnect.setText(getString(R.string.action_disconnect));
							break;
						case BleProfileService.STATE_DISCONNECTED:
							mActionConnect.setText(getString(R.string.action_connect));
							//mImageBulb.setImageDrawable(ContextCompat.getDrawable(ControlBlinkyActivity.this, R.drawable.bulb_off));
							//mImageBulb2.setImageDrawable(ContextCompat.getDrawable(ControlBlinkyActivity.this, R.drawable.bulb_off));
							mActionOnOff2.setBackgroundResource(R.drawable.b_play_1);
							mActionOnOff.setBackgroundResource(R.drawable.b_stop_3);
							break;
					}
					break;
				}
				case BlinkyService.BROADCAST_ERROR: {
					final String message = intent.getStringExtra(BlinkyService.EXTRA_ERROR_MESSAGE);
					final int code = intent.getIntExtra(BlinkyService.EXTRA_ERROR_CODE, 0);
					showError(getString(R.string.error_msg, message, code));
					break;
				}
			}
		}
	};

	private static IntentFilter makeGattUpdateIntentFilter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(BlinkyService.BROADCAST_LED_STATE_CHANGED);
        intentFilter.addAction(BlinkyService.BROADCAST_LED2_STATE_CHANGED);
		intentFilter.addAction(BlinkyService.BROADCAST_BUTTON_STATE_CHANGED);
		intentFilter.addAction(BlinkyService.BROADCAST_BUTTON2_STATE_CHANGED);
		intentFilter.addAction(BlinkyService.BROADCAST_CONNECTION_STATE);
		intentFilter.addAction(BlinkyService.BROADCAST_ERROR);

		return intentFilter;
	}

	private static IntentFilter makeGattUpdateIntent2Filter() {
		final IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
		intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
		intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
		intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
		intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);

		return intentFilter;
	}

	private void showError(final String error) {
		Snackbar.make(mParentView, error, Snackbar.LENGTH_LONG).show();
	}

	public void onStart() {
		super.onStart();
		IntentFilter filter = new IntentFilter("android.bluetooth.device.action.FOUND");
		filter.addAction("android.bluetooth.adapter.action.DISCOVERY_FINISHED");
		filter.addAction("android.bluetooth.adapter.action.STATE_CHANGED");
	}

	private void scanLeDevice(boolean enable) {
		if (enable) {
			this.mHandler.postDelayed(new Runnable() {
				public void run() {
					ControlBlinkyActivity.this.mBluetoothAdapter.stopLeScan(ControlBlinkyActivity.this.mLeScanCallback);
				}
			}, 10000);
			this.mBluetoothAdapter.startLeScan(this.mLeScanCallback);
			return;
		}
		this.mBluetoothAdapter.stopLeScan(this.mLeScanCallback);
	}

    @Override
    public void onTimeSet(RadialTimePickerDialogFragment dialog, int hourOfDay, int minute) {
        // Time Picked

        String formattedhour = String.format("%02d", hourOfDay);
        String formattedminute = String.format("%02d", minute);

        if(timepicker) {
            recordfor.setText(formattedhour + ":" + formattedminute);
        }else{
            pausefor.setText(formattedhour + ":" + formattedminute);
        }
    }
}