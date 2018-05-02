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
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.audiofx.Visualizer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.Spinner;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;

import ak.sh.ay.musicwave.MusicWave;
import no.nordicsemi.android.blinky.profile.BleProfileService;
import no.nordicsemi.android.blinky.service.BlinkyService;

public class ControlBlinkyActivity extends AppCompatActivity{

	private BlinkyService.BlinkyBinder mBlinkyDevice;
	private Button mActionOnOff, mActionConnect, mActionOnOff2;
	private ImageView mImageBulb;
	private ImageView mImageBulb2;
	private View mParentView;
	private View mBackgroundView;
	private View mBackgroundView2;
	private LinearLayout LinearLayout;
	private LinearLayout LinearLayoutWave;
	private int height;
	private Spinner spinner;
	private Spinner spinner2;
	private Spinner spinner3;
	private Spinner spinner4;

	private MusicWave musicWave;
	private MediaPlayer mMediaPlayer;
	private Visualizer mVisualizer;
	private boolean mMediaPlayerMute = true;

	private boolean mActionOnOff2_b = false;
	private boolean mActionOnOff_b = false;
	private static final Integer[]stunden = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24};
	private static final Integer[]minuten = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24,
		25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59};
	public boolean bulb = false;
	public boolean bulb2 = false;

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_control_device);

		Intent i = getIntent();
		final String deviceName = i.getStringExtra(BlinkyService.EXTRA_DEVICE_NAME);
		final String deviceAddress = i.getStringExtra(BlinkyService.EXTRA_DEVICE_ADDRESS);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		getSupportActionBar().setTitle(deviceName);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mActionOnOff = (Button) findViewById(R.id.button_blinky);
		mActionOnOff2 = (Button) findViewById(R.id.button_switch);
		mActionConnect = (Button) findViewById(R.id.action_connect);
		mImageBulb = (ImageView) findViewById(R.id.img_bulb);
		mImageBulb2 = (ImageView) findViewById(R.id.img_bulb2);
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

		LocalBroadcastManager.getInstance(this).registerReceiver(mBlinkyUpdateReceiver, makeGattUpdateIntentFilter());

		final Intent intent = new Intent(this, BlinkyService.class);
		intent.putExtra(BlinkyService.EXTRA_DEVICE_ADDRESS, deviceAddress);
		startService(intent);
		bindService(intent, mServiceConnection, 0);

		mActionConnect.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mBlinkyDevice != null && mBlinkyDevice.isConnected()) {
					mBlinkyDevice.disconnect();
				} else {
					startService(intent);
					bindService(intent, mServiceConnection, 0);
				}
			}
		});

		ViewTreeObserver vto = LinearLayout.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				height = LinearLayout.getMeasuredHeight();

				android.view.ViewGroup.LayoutParams params1 = mActionOnOff.getLayoutParams();
				android.view.ViewGroup.LayoutParams params2 = mActionOnOff2.getLayoutParams();

				height*=0.7;
				params1.height = height;
				params1.width = height;
				params2.height = height;
				params2.width = height;

				mActionOnOff.setLayoutParams(params1);
				mActionOnOff2.setLayoutParams(params2);
			}
		});
		/*
		final EditText et1 = (EditText) findViewById(R.id.editText);
		et1.setFilters(new InputFilter[]{ new InputFilterMinMax("1", "200")});
		final TextView tv1 = (TextView) findViewById(R.id.textView);

		RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio_group);
		radioGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.radio_pirates) {
					et1.setVisibility(View.VISIBLE);
					tv1.setVisibility(View.VISIBLE);
				} else {
					et1.setVisibility(View.INVISIBLE);
					tv1.setVisibility(View.INVISIBLE);
				}
			}
		});

		spinner = (Spinner) findViewById(R.id.spinner);
		ArrayAdapter<Integer> adapter_stunden = new ArrayAdapter<Integer>(ControlBlinkyActivity.this,
				android.R.layout.simple_spinner_item, stunden);

		adapter_stunden.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(adapter_stunden);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

				switch (position) {
					case 0:
						break;
					case 1:
						break;
					case 2:
						break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		spinner2 = (Spinner) findViewById(R.id.spinner2);
		ArrayAdapter<Integer> adapter_minuten = new ArrayAdapter<Integer>(ControlBlinkyActivity.this,
				android.R.layout.simple_spinner_item, minuten);

		adapter_minuten.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner2.setAdapter(adapter_minuten);
		spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

				switch (position) {
					case 0:
						break;
					case 1:
						break;
					case 2:
						break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		spinner3 = (Spinner) findViewById(R.id.spinner3);
		adapter_stunden.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner3.setAdapter(adapter_stunden);
		spinner3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

				switch (position) {
					case 0:
						break;
					case 1:
						break;
					case 2:
						break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		spinner4 = (Spinner) findViewById(R.id.spinner4);
		adapter_minuten.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner4.setAdapter(adapter_minuten);
		spinner4.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

				switch (position) {
					case 0:
						break;
					case 1:
						break;
					case 2:
						break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		*//* Permission
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 9976);
		} else {
			initialise();
		}*/

		musicWave = (MusicWave) findViewById(R.id.musicWave);
		mMediaPlayer = MediaPlayer.create(this, R.raw.you_music);
		prepareVisualizer();
		mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mediaPlayer) {
				mVisualizer.setEnabled(true);
				mMediaPlayer.start();
			}
		});
		mVisualizer.setEnabled(true);
		mMediaPlayer.start();

		LinearLayoutWave = (android.widget.LinearLayout) findViewById(R.id.wave);
		LinearLayoutWave.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(mMediaPlayerMute) {
					mMediaPlayer.setVolume(0, 0);
					mMediaPlayerMute = false;
				}else{
					mMediaPlayer.setVolume(1,1);
					mMediaPlayerMute = true;
				}
			}
		});

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
		LocalBroadcastManager.getInstance(this).unregisterReceiver(mBlinkyUpdateReceiver);

		mServiceConnection = null;
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
						mImageBulb.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.bulb_on));
						bulb = true;
					} else {
						mImageBulb.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.bulb_off));
						bulb = false;
					}
					break;
				}
                case BlinkyService.BROADCAST_LED2_STATE_CHANGED: {
                    final boolean flag = intent.getBooleanExtra(BlinkyService.EXTRA_DATA, false);
                    if (flag) {
                        mImageBulb2.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.bulb_on));
                        bulb2 = true;
                    } else {
                        mImageBulb2.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.bulb_off));
                        bulb2 = false;
                    }
                    break;
                }
				case BlinkyService.BROADCAST_BUTTON_STATE_CHANGED: {
					final boolean flag = intent.getBooleanExtra(BlinkyService.EXTRA_DATA, false);

                    mBackgroundView.setVisibility(View.VISIBLE);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mBackgroundView.setVisibility(View.INVISIBLE);
                        }
                    }, 400);

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

                    mBackgroundView2.setVisibility(View.VISIBLE);
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mBackgroundView2.setVisibility(View.INVISIBLE);
                        }
                    }, 400);

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
							mImageBulb.setImageDrawable(ContextCompat.getDrawable(ControlBlinkyActivity.this, R.drawable.bulb_off));
							mImageBulb2.setImageDrawable(ContextCompat.getDrawable(ControlBlinkyActivity.this, R.drawable.bulb_off));
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

	private void showError(final String error) {
		Snackbar.make(mParentView, error, Snackbar.LENGTH_LONG).show();
	}
}