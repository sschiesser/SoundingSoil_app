/*
 * Copyright (c) 2015, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.blinky.service;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.text.style.LineBackgroundSpan;
import android.util.Log;

import java.sql.ClientInfoStatus;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import no.nordicsemi.android.blinky.profile.BleManager;

public class BlinkyManager extends BleManager<BlinkyManagerCallbacks> {
	/** Nordic Blinky Service UUID */
	private final static UUID LBS_UUID_SERVICE = UUID.fromString("00001523-1212-efde-1523-785feabcd123");
	/** BUTTON characteristic UUID */
	private final static UUID LBS_UUID_BUTTON_CHAR = UUID.fromString("00001524-1212-efde-1523-785feabcd123");
	/** LED characteristic UUID LED 3*/
	private final static UUID LBS_UUID_LED_CHAR = UUID.fromString("00001525-1212-efde-1523-785feabcd123");
	/** BUTTON 2 characteristic UUID */
	private final static UUID LBS_UUID_BUTTON2_CHAR = UUID.fromString("00001526-1212-efde-1523-785feabcd123");
	/** LED characteristic UUID LED 4 **/
	private final static UUID LBS_UUID_LED2_CHAR = UUID.fromString("00001527-1212-efde-1523-785feabcd123");

	private BluetoothGattCharacteristic mButtonCharacteristic, mLedCharacteristic, mLed2Characteristic, mButtonCharacteristic2;

	public BlinkyManager(final Context context) {
		super(context);
	}

	@Override
	protected BleManagerGattCallback getGattCallback() {
		return mGattCallback;
	}

	/**
	 * BluetoothGatt callbacks for connection/disconnection, service discovery, receiving indication, etc
	 */
	private final BleManagerGattCallback mGattCallback = new BleManagerGattCallback() {

		@Override
		protected Queue<Request> initGatt(final BluetoothGatt gatt) {
			final LinkedList<Request> requests = new LinkedList<>();
			requests.push(Request.newEnableNotificationsRequest(mButtonCharacteristic));
			requests.push(Request.newEnableNotificationsRequest(mButtonCharacteristic2));
			return requests;
		}

		@Override
		public boolean isRequiredServiceSupported(final BluetoothGatt gatt) {
			final BluetoothGattService service = gatt.getService(LBS_UUID_SERVICE);
			if (service != null) {
				mButtonCharacteristic = service.getCharacteristic(LBS_UUID_BUTTON_CHAR);
				mButtonCharacteristic2 = service.getCharacteristic(LBS_UUID_BUTTON2_CHAR);
				mLedCharacteristic = service.getCharacteristic(LBS_UUID_LED_CHAR);
				mLed2Characteristic = service.getCharacteristic(LBS_UUID_LED2_CHAR);
			}

			boolean writeRequest = false;
			if (mLedCharacteristic != null) {
				final int rxProperties = mLedCharacteristic.getProperties();
				writeRequest = (rxProperties & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0;
			}

			if (mLed2Characteristic != null) {
				final int rxProperties = mLed2Characteristic.getProperties();
				writeRequest = (rxProperties & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0;
			}

			return mButtonCharacteristic != null && mButtonCharacteristic2 != null && mLedCharacteristic != null && mLed2Characteristic != null && writeRequest;
		}

		@Override
		protected void onDeviceDisconnected() {
			mButtonCharacteristic = null;
			mButtonCharacteristic2 = null;
			mLedCharacteristic = null;
			mLed2Characteristic = null;
		}

		@Override
		public void onCharacteristicWrite(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
			String text = characteristic.getUuid().toString();
			String text1 = LBS_UUID_LED_CHAR.toString();
			String text2 = LBS_UUID_LED2_CHAR.toString();

			if (text.equals(text1)){
				mCallbacks.onDataSent(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) == 0x01);
			} else if (text.equals(text2)) {
				mCallbacks.onDataSent2(characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0) == 0x01);
			}
		}

		@Override
		public void onCharacteristicNotified(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
			int data = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 0);

			String text = characteristic.getUuid().toString();
			String text1 = LBS_UUID_BUTTON_CHAR.toString();
			String text2 = LBS_UUID_BUTTON2_CHAR.toString();

			if (text.equals(text1)){
				mCallbacks.onDataReceived(data == 0x01);
			} else if (text.equals(text2)) {
				mCallbacks.onDataReceived2(data == 0x01);
			}
		}
	};

	public void send(final boolean onOff) {
		// Are we connected?
		if (mLedCharacteristic == null)
			return;

		byte [] command;
		if (onOff){
			command = new byte [] {1};
		} else {
			command = new byte [] {0};
		}
		mLedCharacteristic.setValue(command);
		writeCharacteristic(mLedCharacteristic);
	}

	public void send2(final boolean onOff2) {
		// Are we connected?
		if (mLed2Characteristic == null)
			return;

		byte [] command;
		if (onOff2){
			command = new byte [] {1};
		} else {
			command = new byte [] {0};
		}
		mLed2Characteristic.setValue(command);
		writeCharacteristic(mLed2Characteristic);
	}
}
