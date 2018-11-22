package com.chengxiaoxiao.buletoothmanager;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.*;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

@SuppressLint("NewApi")
public class MainActivity extends Activity {

	private ListView mListView;
	private ArrayList<String> datas = new ArrayList<String>();
	private ArrayList<BluetoothDevice> devices = new ArrayList<BluetoothDevice>();
	private BluetoothAdapter mBluetoothAdapter;

	protected static final UUID MY_UUID = UUID
			.fromString("00001101-0000-1000-8000-00805F9B34FB");

	private boolean mScanning;
	private Handler mHandler = new Handler();
	private static final long SCAN_PERIOD = 10000;

	public BluetoothGatt bluetoothGatt;
	private BluetoothGattService bluetoothGattService;
	private BluetoothGattCharacteristic bluetoothGattCharacteristic;
	private String str_receive;
	
	private Button btnConn;
	private ProgressDialog dialog;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		btnConn = (Button) findViewById(R.id.btnConn);
		// �����������ϰ�������
		openServices();

		if (!getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, "Sorry!�����ֻ���ʱ��֧�ִ��豸.", Toast.LENGTH_SHORT)
					.show();
			finish();
		}
		
		
		 
        
		
		

	}

	/*
	 * ��ʼ��������ֱ�Ӵ�����
	 */
	public void connect(View v) {
		
		String text = btnConn.getText().toString().trim();
		if(text.equals("�����豸"))
		{
			btnConn.setText("�Ͽ��豸");
			
			// ��ʼ������
			initData();
			
			dialog = ProgressDialog.show(MainActivity.this, "��������", "���������豸...���Ժ�"); 

			// ��������
			scanLeDevice(true);
		}
		else
		{
			btnConn.setText("�����豸");
			disconnect();
		}
		
		
	}

	/*
	 * �ر��豸
	 */
	public void disconnect() {

		// ��������رգ�����ʾ������
		if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
			mBluetoothAdapter.disable();
		}
		else
		{
			Toast.makeText(MainActivity.this, "����û�п����豸������ر�", 0).show();
		}
	}

	// ��������
	public void openServices() {

		String name = "com.chengxiaoxiao.buletoothmanager/com.chengxiaoxiao.buletoothmanager.RobServices";

		if (checkStealFeature(name)) {
			// Toast.makeText(this, "������", 0).show();
		} else {
			Toast.makeText(this, "��������[���ϰ�]���棬��ѡ��[�쳵������������]���񲢿���",
					Toast.LENGTH_LONG).show();

			try {
				// ��תϵͳ�Դ����� �������ܽ���
				Intent intent = new Intent(
						android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);

				startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	/*
	 * �������Ƿ���
	 */
	private boolean checkStealFeature(String service) {
		int ok = 0;
		try {
			ok = Settings.Secure.getInt(getApplicationContext()
					.getContentResolver(),
					Settings.Secure.ACCESSIBILITY_ENABLED);
		} catch (Settings.SettingNotFoundException e) {
		}

		TextUtils.SimpleStringSplitter ms = new TextUtils.SimpleStringSplitter(
				':');
		if (ok == 1) {
			String settingValue = Settings.Secure.getString(
					getApplicationContext().getContentResolver(),
					Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);
			if (settingValue != null) {
				ms.setString(settingValue);
				while (ms.hasNext()) {
					String accessibilityService = ms.next();
					if (accessibilityService.equalsIgnoreCase(service)) {
						return true;
					}

				}
			}
		}
		return false;
	}

	/*
	 * ������
	 */
	private void initData() {

		// ��ȡ����
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		// ��������رգ�����ʾ������
		if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, 1);
		}

	}

	/*
	 * 
	 * �����򿪵ķ��ط�ʽ
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			Toast.makeText(this, "�����򿪳ɹ�", Toast.LENGTH_SHORT).show();

		} else if (resultCode == RESULT_CANCELED) {
			Toast.makeText(this, "������ʧ��", Toast.LENGTH_SHORT).show();
		}
	}

	/*
	 * 
	 * ɨ��ʮ���ӣ�����
	 */
	@SuppressLint("NewApi")
	private void scanLeDevice(final boolean enable) {
		if (enable) {
			// ����Ԥ��ɨ���ں�ֹͣɨ��
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					
					if(isDiscover)
					{
						mScanning = false;
						mBluetoothAdapter.stopLeScan(mLeScanCallback);
					}
					else
					{
						discoverNum++;
						
						if(discoverNum>=3)
						{
							Toast.makeText(MainActivity.this, "��û�з����豸���뱣���豸���ڿ���״̬", 0).show();
						}
						else
						{
							scanLeDevice(true);
						}

					}
					
				}
			}, SCAN_PERIOD);
			mScanning = true;
			mBluetoothAdapter.startLeScan(mLeScanCallback);

		} else {
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
		}

	}
	
	private boolean isDiscover = false;
	private int discoverNum =0;

	@SuppressLint("NewApi")
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(final BluetoothDevice device, int rssi,
				byte[] scanRecord) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {

					
					String deviceName = device.getName().trim();

					deviceName = deviceName.trim().toLowerCase();

					if (deviceName.equals("itag")) {

						isDiscover = true;
						dialog.dismiss();
						
						connectAndCheck(device);

					}

				}
			});
		}
	};
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
	}

	/*
	 * �����豸������
	 */
	private void connectAndCheck(BluetoothDevice mDevice) {

		if (mDevice == null) {
			Toast.makeText(MainActivity.this, "��������ʧ�ܣ������������", 0).show();
			return;
		}

		if (mScanning) {
			scanLeDevice(false);
		}

		if (bluetoothGatt != null)
			bluetoothGatt.disconnect();

		// ����״̬�ı�
		bluetoothGatt = mDevice.connectGatt(MainActivity.this, false,
				new BluetoothGattCallback() {

					@Override
					public void onConnectionStateChange(BluetoothGatt gatt,
							int status, int newState) {
						super.onConnectionStateChange(gatt, status, newState);
						if (newState == BluetoothProfile.STATE_CONNECTED) {

							mHandler.post(new Runnable() {

								@Override
								public void run() {
									// TODO Auto-generated method stub
									Toast.makeText(MainActivity.this, "�豸������",
											0).show();
								}

							});
							// ���ӳɹ���������
							gatt.discoverServices();
						}
						if (newState == BluetoothProfile.STATE_DISCONNECTED) {
							mHandler.post(new Runnable() {

								@Override
								public void run() {
									// TODO Auto-generated method stub
									Toast.makeText(MainActivity.this, "�豸�ѶϿ�",
											0).show();
								}
							});
							return;

						}

					}

					@Override
					public void onServicesDiscovered(BluetoothGatt gatt,
							int status) {

						super.onServicesDiscovered(gatt, status);

						if (status == gatt.GATT_SUCCESS) { // ���ַ���ɹ�
							String service_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";
							String characteristic_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb";
							bluetoothGattService = gatt.getService(UUID
									.fromString(service_UUID));
							bluetoothGattCharacteristic = bluetoothGattService
									.getCharacteristic(UUID
											.fromString(characteristic_UUID));

							if (bluetoothGattCharacteristic != null) {

								// �ɹ���ȡ����
								gatt.setCharacteristicNotification(
										bluetoothGattCharacteristic, true);

							} else {
								if (gatt != null)
									gatt.disconnect();

								Toast.makeText(MainActivity.this,
										"�豸���������Դ����Ѿ��޷�ʹ����", 0).show();
							}
						} else {

							if (gatt != null)
								gatt.disconnect();

							Toast.makeText(MainActivity.this,
									"�豸���������Դ����Ѿ��޷�ʹ����", 0).show();
						}
					}

					@Override
					public void onCharacteristicChanged(BluetoothGatt gatt,
							BluetoothGattCharacteristic characteristic) {
						// TODO Auto-generated method stub
						super.onCharacteristicChanged(gatt, characteristic);

						byte[] bytesreceive = characteristic.getValue();

						if (bytesreceive.length != 0) {
							str_receive = new String();
							for (int i = 0; i < bytesreceive.length; i++) {
								String str_hex = (Integer
										.toHexString((int) bytesreceive[i] & 0x000000ff) + "")
										.toUpperCase();
								if (str_hex.length() == 1)
									str_hex = "0" + str_hex;
								str_receive += str_hex + " ";
							}
							mHandler.post(new Runnable() {

								@Override
								public void run() {
									// TODO Auto-generated method stub

									str_receive = str_receive.trim();

									if (str_receive.equals("01")) {
										Toast.makeText(MainActivity.this,
												"�ε�������", 0).show();
									}

								}
							});

						}
					}
				});

	}

}
