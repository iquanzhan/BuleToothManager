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
		// 开启服务【无障碍】辅助
		openServices();

		if (!getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, "Sorry!您的手机暂时不支持此设备.", Toast.LENGTH_SHORT)
					.show();
			finish();
		}
		
		
		 
        
		
		

	}

	/*
	 * 初始化蓝牙，直接打开蓝牙
	 */
	public void connect(View v) {
		
		String text = btnConn.getText().toString().trim();
		if(text.equals("连接设备"))
		{
			btnConn.setText("断开设备");
			
			// 初始化蓝牙
			initData();
			
			dialog = ProgressDialog.show(MainActivity.this, "蓝牙连接", "正在连接设备...请稍候"); 

			// 搜索蓝牙
			scanLeDevice(true);
		}
		else
		{
			btnConn.setText("连接设备");
			disconnect();
		}
		
		
	}

	/*
	 * 关闭设备
	 */
	public void disconnect() {

		// 如果蓝牙关闭，则提示打开蓝牙
		if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
			mBluetoothAdapter.disable();
		}
		else
		{
			Toast.makeText(MainActivity.this, "您并没有开启设备，无需关闭", 0).show();
		}
	}

	// 开启服务
	public void openServices() {

		String name = "com.chengxiaoxiao.buletoothmanager/com.chengxiaoxiao.buletoothmanager.RobServices";

		if (checkStealFeature(name)) {
			// Toast.makeText(this, "开启了", 0).show();
		} else {
			Toast.makeText(this, "即将跳到[无障碍]界面，请选择[快车车主抢单辅助]服务并开启",
					Toast.LENGTH_LONG).show();

			try {
				// 跳转系统自带界面 辅助功能界面
				Intent intent = new Intent(
						android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);

				startActivity(intent);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	/*
	 * 检测服务是否开启
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
	 * 打开蓝牙
	 */
	private void initData() {

		// 获取蓝牙
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();

		// 如果蓝牙关闭，则提示打开蓝牙
		if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, 1);
		}

	}

	/*
	 * 
	 * 蓝牙打开的返回方式
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			Toast.makeText(this, "蓝牙打开成功", Toast.LENGTH_SHORT).show();

		} else if (resultCode == RESULT_CANCELED) {
			Toast.makeText(this, "蓝牙打开失败", Toast.LENGTH_SHORT).show();
		}
	}

	/*
	 * 
	 * 扫描十秒钟，蓝牙
	 */
	@SuppressLint("NewApi")
	private void scanLeDevice(final boolean enable) {
		if (enable) {
			// 经过预定扫描期后停止扫描
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
							Toast.makeText(MainActivity.this, "并没有发现设备，请保持设备处于开机状态", 0).show();
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
	 * 连接设备并处理
	 */
	private void connectAndCheck(BluetoothDevice mDevice) {

		if (mDevice == null) {
			Toast.makeText(MainActivity.this, "连接蓝牙失败，请重启本软件", 0).show();
			return;
		}

		if (mScanning) {
			scanLeDevice(false);
		}

		if (bluetoothGatt != null)
			bluetoothGatt.disconnect();

		// 监听状态改变
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
									Toast.makeText(MainActivity.this, "设备已连接",
											0).show();
								}

							});
							// 连接成功搜索服务
							gatt.discoverServices();
						}
						if (newState == BluetoothProfile.STATE_DISCONNECTED) {
							mHandler.post(new Runnable() {

								@Override
								public void run() {
									// TODO Auto-generated method stub
									Toast.makeText(MainActivity.this, "设备已断开",
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

						if (status == gatt.GATT_SUCCESS) { // 发现服务成功
							String service_UUID = "0000ffe0-0000-1000-8000-00805f9b34fb";
							String characteristic_UUID = "0000ffe1-0000-1000-8000-00805f9b34fb";
							bluetoothGattService = gatt.getService(UUID
									.fromString(service_UUID));
							bluetoothGattCharacteristic = bluetoothGattService
									.getCharacteristic(UUID
											.fromString(characteristic_UUID));

							if (bluetoothGattCharacteristic != null) {

								// 成功获取特征
								gatt.setCharacteristicNotification(
										bluetoothGattCharacteristic, true);

							} else {
								if (gatt != null)
									gatt.disconnect();

								Toast.makeText(MainActivity.this,
										"设备出现致命性错误，已经无法使用了", 0).show();
							}
						} else {

							if (gatt != null)
								gatt.disconnect();

							Toast.makeText(MainActivity.this,
									"设备出现致命性错误，已经无法使用了", 0).show();
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
												"滴滴抢单中", 0).show();
									}

								}
							});

						}
					}
				});

	}

}
