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

/*
 * ControlBlinkyActivity
 *
 * @use This Activity controls the bluetooth connection between the device and the app.
 * There are two Action Record and Monitoring.
 *
 * @author Patrick Lüthi
 *
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
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.codetroopers.betterpickers.radialtimepicker.RadialTimePickerDialogFragment;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ak.sh.ay.musicwave.MusicWave;
import no.nordicsemi.android.blinky.profile.BleProfileService;
import no.nordicsemi.android.blinky.service.BlinkyService;

import static android.os.Build.VERSION_CODES.N;

public class ControlBlinkyActivity extends AppCompatActivity implements RadialTimePickerDialogFragment.OnTimeSetListener{

	public static final String TAG = "ControlBlinkyActivity";

	private BlinkyService.BlinkyBinder mBlinkyDevice;
	private Button mActionOnOff, mActionConnect, mActionOnOff2;
	private View mParentView;
	private View mBackgroundView;
	private View mBackgroundView2;
	private LinearLayout LinearLayout;
	private int height;
	private TextView recordfor;
	private TextView pausefor;
	private MusicWave musicWave;
	private Visualizer mVisualizer;
	private boolean mActionOnOff2_b = false;
	private boolean mActionOnOff_b = false;
	private String audioFilePath;
	private String RECORD_WAV_PATH = Environment.getExternalStorageDirectory() + File.separator + "AudioRecord";
	private static final int UART_PROFILE_DISCONNECTED = 21;
	private final BroadcastReceiver UARTStatusChangeReceiver = new C00593();
	private BluetoothAdapter mBtAdapter = null;
	private BluetoothDevice mDevice = null;
	private Handler mHandler = new C00552();
	private UartService mService = null;
	private ServiceConnection mServiceConnection2 = new C00541();
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new C00501();

	public boolean bulb = false;
	public boolean bulb2 = false;
	public boolean timePicker = true;
	public String deviceAddress = "";
	public byte[] byteArray = new byte[5];
	public byte[] even = null;
	public byte[] odd = null;
	public List<String> permissions = new ArrayList<String>();
	public Handler handler = new Handler();
	public int zaehler = 1;

	@SuppressLint({"ClickableViewAccessibility", "WrongConstant"})
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_control_device);

		Intent i = getIntent();
		final String deviceName = i.getStringExtra(UartService.EXTRA_DEVICE_NAME);
		deviceAddress = i.getStringExtra(UartService.EXTRA_DEVICE_ADDRESS);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setTitle(deviceName);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		this.mBtAdapter = BluetoothAdapter.getDefaultAdapter();

		ActivityCompat.requestPermissions(ControlBlinkyActivity.this,
				new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
				1);

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
		mBackgroundView = findViewById(R.id.background_view);
		mBackgroundView2 = findViewById(R.id.background_view2);
		mParentView = findViewById(R.id.relative_layout_control);
		LinearLayout = (android.widget.LinearLayout) findViewById(R.id.linearlayout_button);
		musicWave = (MusicWave) findViewById(R.id.musicWave);

		musicWave.setEnabled(true);
		musicWave.setActivated(true);

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

		final Intent intent = new Intent(this, BlinkyService.class);
		intent.putExtra(BlinkyService.EXTRA_DEVICE_ADDRESS, deviceAddress);
		startService(intent);
		bindService(intent, mServiceConnection, 0);

		mActionConnect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mActionConnect.getText().equals("CONNECT")){
					startService(intent);
					bindService(intent, mServiceConnection, 0);
					service_init();
				} else if (mActionConnect.getText().equals("DISCONNECT")){
					mBlinkyDevice.disconnect();
				}
			}
		});

		ViewTreeObserver vto = LinearLayout.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
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

		//If Pre Settings disabled
		/*recordfor.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				RadialTimePickerDialogFragment rtpd = new RadialTimePickerDialogFragment()
						.setOnTimeSetListener(ControlBlinkyActivity.this)
						.setStartTime(10, 10)
						.setDoneText("Done")
						.setCancelText("Cancel")
						.setThemeCustom(R.style.RadialPicker);
				rtpd.show(getSupportFragmentManager(), "timePickerDialogFragment");

				timePicker = true;
			}
		});*/

		pausefor = (TextView) findViewById(R.id.PauseFor);

		//If Pre Settings disabled
		/*pausefor.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				RadialTimePickerDialogFragment rtpd = new RadialTimePickerDialogFragment()
						.setOnTimeSetListener(ControlBlinkyActivity.this)
						.setStartTime(10, 10)
						.setDoneText("Done")
						.setCancelText("Cancel")
						.setThemeCustom(R.style.RadialPicker);
				rtpd.show(getSupportFragmentManager(), "timePickerDialogFragment");

				timePicker = false;
			}
		});*/

		final EditText et1 = (EditText) findViewById(R.id.editText);
		//Pre Settings
		et1.setFocusable(false);
		//et1.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "200")}); // Max and Min Filter
		final TextView tv1 = (TextView) findViewById(R.id.textView);

		RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio_group);

		// Pre Settings
		RadioButton rb1 = (RadioButton) findViewById(R.id.radio_ninjas);
		RadioButton rb2 = (RadioButton) findViewById(R.id.radio_pirates);
		RadioButton rb3 = (RadioButton) findViewById(R.id.radio_warriors);

		rb1.setChecked(true);
		rb2.setClickable(false);
		rb3.setClickable(false);
		// Pre Settings

		/* If Pre Settings are disabled
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
		});*/

		/* Permission
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 9976);
		} else {
			initialise();
		}*/

		service_init();
	}

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
            ControlBlinkyActivity.this.mService = ((UartService.LocalBinder) rawBinder).getService();

			mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);

			mService.initialize();
			mService.connect(deviceAddress);

            if (!ControlBlinkyActivity.this.mService.initialize()) {
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
                Log.d(TAG, "UART_CONNECT_MSG");
            }
        }

        class C00572 implements Runnable {
            C00572() {
            }

            public void run() {
                String currentDateTimeString = DateFormat.getTimeInstance().format(new Date());
                Log.d(TAG, "UART_DISCONNECT_MSG");
                ControlBlinkyActivity.this.mService.close();
            }
        }

        C00593() {
        	Log.d(TAG, "Create BroadCastRec UART");
        }

        public void onReceive(final Context context, Intent intent) {
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

				// Saving Array of 200 Times
            	if(zaehler <= 200){

					short[] test = convert(txValue);

					//short
					/*if (zaehler == 1) {
						arrayF = test;
					} else{
						short[] c = new short[arrayF.length + test.length];
						System.arraycopy(arrayF, 0, c, 0, arrayF.length);
						System.arraycopy(test, 0, c, arrayF.length, test.length);
						arrayF = c;
					}*/

					//byte
					if (zaehler == 1) {
						odd = txValue;
					} else{
						byte[] c = new byte[odd.length + txValue.length];
						System.arraycopy(odd, 0, c, 0, odd.length);
						System.arraycopy(txValue, 0, c, odd.length, txValue.length);
						odd = c;
					}

            		zaehler+=1;
				}

				if(zaehler == 200){

            		//Swap bytes
            		final byte[] oddswap = new byte[odd.length];

            		for(int q = 0; q < odd.length; q+=2){
            			if(q < odd.length) {
							oddswap[q] = odd[q + 1];
							oddswap[q + 1] = odd[q];
						}
					}

            		zaehler = 2000;

					handler = new Handler();

					final Runnable r = new Runnable() {
						public void run() {

							try {
								String filePath = rawToWave(oddswap);

								final MediaPlayer mediaPlayer = new MediaPlayer();
								mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
								mediaPlayer.setDataSource(filePath);
								mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
									@Override
									public void onPrepared(MediaPlayer mp) {
										mp.start();
									}
								});

								// Check if Android Version is high enough for SoundWave
								if (android.os.Build.VERSION.SDK_INT >= N) {

									mVisualizer = new Visualizer(mediaPlayer.getAudioSessionId());
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

								mediaPlayer.prepareAsync();

								mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
									@Override
									public void onCompletion(MediaPlayer mp) {
										SystemClock.sleep(2000); // 2 Sec
										mp.start();
									}
								});

							} catch (IOException e) {
								String s = e.toString();
								Log.d(TAG, "EXCEPTION: " + s);
							}
						}
					};
					r.run();
				}
            }
            if (action.equals(UartService.DEVICE_DOES_NOT_SUPPORT_UART)) {
                ControlBlinkyActivity.this.mService.disconnect();
            }
        }
    }

	public String rawToWave(byte[] rawArray) throws IOException {

		String wav = "wav";
		String filePath = getExternalFilesDir(wav).getPath().toString() + "/vocal.wav";

		byte[] rawData = rawArray;
		DataInputStream input = null;
		try {
			InputStream inFromServer;
			DataInputStream in;

			inFromServer = new InputStream() {
				@Override
				public int read() throws IOException {
					return 0;
				}
			};
			in = new DataInputStream(inFromServer);

			byte[] buffer = rawArray;

		} finally {
			if (input != null) {
				input.close();
			}
		}

		DataOutputStream output = null;//following block is converting raw to wav.
		try {
			output = new DataOutputStream(new FileOutputStream(filePath));

			long mySubChunk1Size = 16;
			int myBitsPerSample= 16;
			int myFormat = 1;
			long myChannels = 2;
			long mySampleRate = 7000;
			long myByteRate = mySampleRate * myChannels * myBitsPerSample/16; // 8
			int myBlockAlign = (int) (myChannels * myBitsPerSample/16); //8

			long myDataSize = rawArray.length;
			long myChunk2Size =  myDataSize * myChannels * myBitsPerSample/32;//16
			long myChunkSize = 36 + myChunk2Size;

			output.writeBytes("RIFF");                 // 00 - RIFF
			output.write(intToByteArray((int) myChunkSize), 0, 4);     // 04 - how big is the rest of this file?
			output.writeBytes("WAVE");                 // 08 - WAVE
			output.writeBytes("fmt ");                 // 12 - fmt
			output.write(intToByteArray((int) mySubChunk1Size), 0, 4); // 16 - size of this chunk
			output.write(shortToByteArray((short) myFormat), 0, 2);        // 20 - what is the audio format? 1 for PCM = Pulse Code Modulation
			output.write(shortToByteArray((short) myChannels), 0, 2);  // 22 - mono or stereo? 1 or 2?  (or 5 or ???)
			output.write(intToByteArray((int) mySampleRate), 0, 4);        // 24 - samples per second (numbers per second)
			output.write(intToByteArray((int) myByteRate), 0, 4);      // 28 - bytes per second
			output.write(shortToByteArray((short) myBlockAlign), 0, 2);    // 32 - # of bytes in one sample, for all channels
			output.write(shortToByteArray((short) myBitsPerSample), 0, 2); // 34 - how many bits in a sample(number)?  usually 16 or 24
			output.writeBytes("data");                 // 36 - data
			output.write(intToByteArray((int) myDataSize), 0, 4);      // 40 - how big is this data chunk
			//output.write(ShortToByte_ByteBuffer_Method(rawArray));
			output.write(rawArray);

		} catch (Exception e) {
			String s = e.toString();
			Log.d(TAG, "ERROR: " + s);
		} finally {
			if (output != null) {
				output.close();
			}
		}

		return filePath;
	}

	private static byte[] intToByteArray(int i)
	{
		byte[] b = new byte[4];
		b[0] = (byte) (i & 0x00FF);
		b[1] = (byte) ((i >> 8) & 0x000000FF);
		b[2] = (byte) ((i >> 16) & 0x000000FF);
		b[3] = (byte) ((i >> 24) & 0x000000FF);

		return b;
	}

	// convert a short to a byte array
	public static byte[] shortToByteArray(short data)
	{
        /*
         * NB have also tried:
         * return new byte[]{(byte)(data & 0xff),(byte)((data >> 8) & 0xff)};
         *
         */

        byte[] b = new byte[]{(byte)(data & 0xff),(byte)((data >>> 8) & 0xff)};

		return b;
	}

    public short[] convert(byte[] array){

		short[]converted = new short[array.length/2];

		int a = 0;
		int b = 0;

		while(a < array.length){

			short int16 = (short) (((array[a+1] & 0xFF) << 8) | (array[a] & 0xFF));

			converted[b] = int16;

			a+=2;
			b+=1;

			if(a >= 243){
				a = 1321313213;
			}
		}

		return converted;

	}

	@SuppressLint("WrongConstant")
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case 1:
				if (resultCode == -1 && data != null) {
					this.mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
					Log.d(TAG, "... onActivityResultdevice.address==" + this.mDevice + "mserviceValue" + this.mService);
					//this.mService.connect(deviceAddress);
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

	@SuppressLint("WrongConstant")
	private void service_init() {
		bindService(new Intent(this, UartService.class), this.mServiceConnection2, 1);
		LocalBroadcastManager.getInstance(this).registerReceiver(this.UARTStatusChangeReceiver, makeGattUpdateIntent2Filter());
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
		unbindService(mServiceConnection2);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mBlinkyUpdateReceiver);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(UARTStatusChangeReceiver);

		mServiceConnection = null;
		mServiceConnection2 = null;
		mBlinkyDevice = null;
	}

	private BroadcastReceiver mBlinkyUpdateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			switch (action) {
				case BlinkyService.BROADCAST_LED_STATE_CHANGED: {
					final boolean flag = intent.getBooleanExtra(BlinkyService.EXTRA_DATA, false);
					if (flag) {
						mActionOnOff.setBackgroundResource(R.drawable.b_cancel_1);
						bulb = true;
					} else {
						mActionOnOff.setBackgroundResource(R.drawable.b_stop_3);
						bulb = false;
					}
					break;
				}
                case BlinkyService.BROADCAST_LED2_STATE_CHANGED: {
                    final boolean flag = intent.getBooleanExtra(BlinkyService.EXTRA_DATA, false);
                    if (flag) {
						mActionOnOff2.setBackgroundResource(R.drawable.b_pause_3);
                        bulb2 = true;
                    } else {
						mActionOnOff2.setBackgroundResource(R.drawable.b_play_1);
                        bulb2 = false;
                    }
                    break;
                }
				case BlinkyService.BROADCAST_BUTTON_STATE_CHANGED: {
					final boolean flag = intent.getBooleanExtra(BlinkyService.EXTRA_DATA, false);
						//Empfangen
					break;
				}
                case BlinkyService.BROADCAST_BUTTON2_STATE_CHANGED: {
                    final boolean flag = intent.getBooleanExtra(BlinkyService.EXTRA_DATA, false);
						//Empfangen
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

        if(timePicker) {
            recordfor.setText(formattedhour + ":" + formattedminute);
        }else{
            pausefor.setText(formattedhour + ":" + formattedminute);
        }
    }
}