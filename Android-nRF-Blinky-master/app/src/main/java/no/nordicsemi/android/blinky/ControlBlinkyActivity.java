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
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.audiofx.Visualizer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.widget.TextView;

import com.codetroopers.betterpickers.radialtimepicker.RadialTimePickerDialogFragment;

import ak.sh.ay.musicwave.MusicWave;
import no.nordicsemi.android.blinky.profile.BleProfileService;
import no.nordicsemi.android.blinky.service.BlinkyService;

import com.codetroopers.betterpickers.*;

import org.w3c.dom.Text;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class ControlBlinkyActivity extends AppCompatActivity implements RadialTimePickerDialogFragment.OnTimeSetListener{

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
	private Spinner spinner3;
	private Spinner spinner4;
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

	public File outputmediafile;

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

	@SuppressLint("ClickableViewAccessibility")
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
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
			musicWave = (MusicWave) findViewById(R.id.musicWave);
			mMediaPlayer = MediaPlayer.create(this, R.raw.music_example);
			askPermission();
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
			mMediaPlayer.setVolume(0, 0);

			LinearLayoutWave = (android.widget.LinearLayout) findViewById(R.id.wave);
			LinearLayoutWave.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					if (mMediaPlayerMute) {
						mMediaPlayer.setVolume(0, 0);
						mMediaPlayerMute = false;
					} else {
						mMediaPlayer.setVolume(1, 1);
						mMediaPlayerMute = true;
					}
				}
			});
		}

		byte[] bytearray = {-1, 0, 42, -115, -45, 0, 14, -12, 1, -2, 1, -2, 1, -2};

        try {
            File file = File.createTempFile("prefixx","suffixx");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytearray);

            final MediaPlayer m = new MediaPlayer();
            m.setDataSource(String.valueOf(file));
            m.start();
            m.setVolume(1,1);
            m.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    m.start();
                    m.setVolume(1,1);
                }
            });

        } catch (IOException e) {
            e.printStackTrace();
        }

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

	private void showError(final String error) {
		Snackbar.make(mParentView, error, Snackbar.LENGTH_LONG).show();
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