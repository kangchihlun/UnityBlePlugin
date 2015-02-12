package com.example.unitybleplugin;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.unity3d.player.UnityPlayerActivity;

public class Peripheral implements BleWrapperUiCallbacks  //extends UnityPlayerActivity 
{
	private final static String TAG = Peripheral.class.getSimpleName();
    public static final String EXTRAS_DEVICE_NAME    = "BLE_DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "BLE_DEVICE_ADDRESS";
    public static final String EXTRAS_DEVICE_RSSI    = "BLE_DEVICE_RSSI";
    
    
    public enum ListType 
    {
    	GATT_SERVICES,
    	GATT_CHARACTERISTICS,
    	GATT_CHARACTERISTIC_DETAILS
    }
    
    private ListType mListType = ListType.GATT_SERVICES;
    private String mDeviceName;
    private String mDeviceAddress;
    private String mDeviceRSSI;

    public BleWrapper mBleWrapper;
    
    // 警報器等級
    private int m_alertLvl=0;
    
    // 震動開關
    private boolean m_bVibrate=false;
    
    private Button bnLeftHand,bnRightHand,bnHead;
    
    private ServicesListAdapter mServicesListAdapter = null;
    private CharacteristicsListAdapter mCharacteristicsListAdapter = null; 
    private CharacteristicDetailsAdapter mCharDetailsAdapter = null;  
    
    public void uiDeviceConnected(final BluetoothGatt gatt,
			                      final BluetoothDevice device)
    {
    	Thread t = new Thread(new Runnable() {
			@Override
			public void run() 
			{
				//mDeviceStatus.setText("connected");
				//invalidateOptionsMenu();
			}
    	});
    	t.start();
    	/*
        for(BluetoothGattService service : mBleWrapper.getCachedServices())
        {
        	Log.d(TAG, service.getUuid().toString());
    		
    	}*/
    }
    
    public void uiDeviceDisconnected(final BluetoothGatt gatt,
			                         final BluetoothDevice device)
    {
    	Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				//mDeviceStatus.setText("disconnected");

				
				mServicesListAdapter.clearList();
				mCharacteristicsListAdapter.clearList();
				mCharDetailsAdapter.clearCharacteristic();
				
				//invalidateOptionsMenu();
				
				//mHeaderTitle.setText("");
				//mHeaderBackButton.setVisibility(View.INVISIBLE);
				mListType = ListType.GATT_SERVICES;
				//mListView.setAdapter(mServicesListAdapter);
			}
    	});    
    	t.start();
    }
    
    public void uiNewRssiAvailable(final BluetoothGatt gatt,
    							   final BluetoothDevice device,
    							   final int rssi)
    {
    	Thread t = new Thread(new Runnable() {
	    	@Override
			public void run() 
	    	{
				mDeviceRSSI = rssi + " db";
				//mDeviceRssiView.setText(mDeviceRSSI);
	    		Log.d(TAG,mDeviceRSSI);

			}
		});    	
    	t.start();
    }
    
    public void uiAvailableServices(final BluetoothGatt gatt,
    						        final BluetoothDevice device,
    							    final List<BluetoothGattService> services)
    {
    	Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				mServicesListAdapter.clearList();
				mListType = ListType.GATT_SERVICES;
				//mListView.setAdapter(mServicesListAdapter);
				//mHeaderTitle.setText(mDeviceName + "\'s services:");
				//mHeaderBackButton.setVisibility(View.INVISIBLE);
				
				// 打訊息回去給unity，通知已經連線
				Util.sendMessage("onMessage", "connected");
				Util.showToast("Service Found");
    			for(BluetoothGattService service : mBleWrapper.getCachedServices()) {
            		mServicesListAdapter.addService(service);
            	}				
    			mServicesListAdapter.notifyDataSetChanged();
			}    		
    	});
    	t.start();
    }
   
    public void uiCharacteristicForService(final BluetoothGatt gatt,
    				 					   final BluetoothDevice device,
    									   final BluetoothGattService service,
    									   final List<BluetoothGattCharacteristic> chars)
    {
    	Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				mCharacteristicsListAdapter.clearList();
		    	mListType = ListType.GATT_CHARACTERISTICS;
		    	//mListView.setAdapter(mCharacteristicsListAdapter);
		    	//mHeaderTitle.setText(BleNamesResolver.resolveServiceName(service.getUuid().toString().toLowerCase(Locale.getDefault())) + "\'s characteristics:");
		    	//mHeaderBackButton.setVisibility(View.VISIBLE);
		    	
		    	for(BluetoothGattCharacteristic ch : chars) {
		    		mCharacteristicsListAdapter.addCharacteristic(ch);
		    	}
		    	mCharacteristicsListAdapter.notifyDataSetChanged();
			}
    	});
    	t.start();
    }
    
    public void uiCharacteristicsDetails(final BluetoothGatt gatt,
					 					 final BluetoothDevice device,
										 final BluetoothGattService service,
										 final BluetoothGattCharacteristic characteristic)
    {
    	Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				mListType = ListType.GATT_CHARACTERISTIC_DETAILS;
				//mListView.setAdapter(mCharDetailsAdapter);
		    	//mHeaderTitle.setText(BleNamesResolver.resolveCharacteristicName(characteristic.getUuid().toString().toLowerCase(Locale.getDefault())) + "\'s details:");
		    	//mHeaderBackButton.setVisibility(View.VISIBLE);
		    	
		    	mCharDetailsAdapter.setCharacteristic(characteristic);
		    	mCharDetailsAdapter.notifyDataSetChanged();
			}
    	});
    	t.start();
    }

    public void uiNewValueForCharacteristic(final BluetoothGatt gatt,
											final BluetoothDevice device,
											final BluetoothGattService service,
											final BluetoothGattCharacteristic characteristic,
											final String strValue,
											final int intValue,
											final byte[] rawValue,
											final String timestamp)
    {
    	if(mCharDetailsAdapter == null || mCharDetailsAdapter.getCharacteristic(0) == null) return;
    	Thread t = new Thread(new Runnable() 
    	{
			@Override
			public void run() 
			{
				// 數值改變
				//mCharDetailsAdapter.newValueForCharacteristic(characteristic, strValue, intValue, rawValue, timestamp);
				//mCharDetailsAdapter.notifyDataSetChanged();
				
				UUID uuid = characteristic.getUuid();
				/*
				if(uuid.equals( UUID.fromString("0000ffa3-0000-1000-8000-00805f9b34fb")))
				{
					Util.sendMessage("OnMessage", Integer.toString(intValue));
				}*/
				Util.sendMessage("OnMessage", uuid + "_" +Integer.toString(intValue));
			}
    	});
    	t.start();
    }
 
	public void uiSuccessfulWrite(final BluetoothGatt gatt,
            					  final BluetoothDevice device,
            					  final BluetoothGattService service,
            					  final BluetoothGattCharacteristic ch,
            					  final String description)
	{
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() 
			{
				
				UUID uuid = ch.getUuid();
				Util.showToast(uuid.toString() + "  SuccessfulWrite ");
				
				if(uuid.equals(UUID.fromString("0000ffa1-0000-1000-8000-00805f9b34fb")) )
				{
					ScanningActivity.instance.sendUnityMessage( "ffa1setcharacc");	
				}
			}
		});
		t.start();
	}
	
	public void uiFailedWrite(final BluetoothGatt gatt,
							  final BluetoothDevice device,
							  final BluetoothGattService service,
							  final BluetoothGattCharacteristic ch,
							  final String description)
	{
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() 
			{
				Util.showToast(description + "  FailedWrite");
			}
		});	
		t.start();
	}

	
	public void uiGotNotification(final BluetoothGatt gatt,
								  final BluetoothDevice device,
								  final BluetoothGattService service,
								  final BluetoothGattCharacteristic ch)
	{
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() 
			{
				// 數值改變通知打回來於此
				
				// at this moment we only need to send this "signal" do characteristic's details view
				//mCharDetailsAdapter.setNotificationEnabledForService(ch);
			}			
		});
		t.start();
	}

	@Override
	public void uiDeviceFound(BluetoothDevice device, int rssi, byte[] record) {
		// no need to handle that in this Activity (here, we are not scanning)
	}  	
	
    
	public byte[] parseHexStringToBytes(final String hex) {
		String tmp = hex.substring(2).replaceAll("[^[0-9][a-f]]", "");
		byte[] bytes = new byte[tmp.length() / 2]; // every two letters in the string are one byte finally
		
		String part = "";
		
		for(int i = 0; i < bytes.length; ++i) {
			part = "0x" + tmp.substring(i*2, i*2+2);
			bytes[i] = Long.decode(part).byteValue();
		}
		
		return bytes;
	}
	
	public void writeAlertLevel(int level) 
	{
		if (mDeviceAddress != null && mBleWrapper != null)
		{
			final UUID IMMEDIATE_ALERT_UUID = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
			final UUID ALERT_LEVEL_UUID = UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb");
			BluetoothGattService alertService = mBleWrapper.getService(IMMEDIATE_ALERT_UUID);
			if(alertService == null) 
			{
				Log.d(TAG, "Immediate Alert service not found!");
				return;
			}
			BluetoothGattCharacteristic alertLevel = alertService.getCharacteristic(ALERT_LEVEL_UUID);
			if(alertLevel == null) 
			{
				Log.d(TAG, "Alert Level charateristic not found!");
				return;
			}
			//alertLevel.setValue(level, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
			//mBleWrapper.writeCharacteristic(alertLevel);
			byte[] dataToWrite = parseHexStringToBytes("0x0"+ Integer.toString( level ));
			mBleWrapper.writeDataToCharacteristic(alertLevel, dataToWrite);
		}	
	}
	
	public void writeVibrateToggle()
	{
		if (mDeviceAddress != null && mBleWrapper != null)
		{
			final UUID VIBRATE_SERVICE_UUID = UUID.fromString("0000ffc0-0000-1000-8000-00805f9b34fb");
			final UUID VIBRATE_CHAR_UUID = UUID.fromString("0000ffc1-0000-1000-8000-00805f9b34fb");
			
			BluetoothGattService vbService = mBleWrapper.getService(VIBRATE_SERVICE_UUID);
			if(vbService == null) 
			{
				Log.d(TAG, "Vibrate service not found!");
				return;
			}
			BluetoothGattCharacteristic vbChr = vbService.getCharacteristic(VIBRATE_CHAR_UUID);
			if(vbChr == null) 
			{
				Log.d(TAG, "Vibrate charateristic not found!");
				return;
			}
			//
			m_bVibrate = !m_bVibrate;
			int vbVal = 0;
			vbVal = m_bVibrate?1:0;
			byte[] dataToWrite = parseHexStringToBytes("0x0"+ Integer.toString( vbVal ));
			mBleWrapper.writeDataToCharacteristic(vbChr, dataToWrite);
		}	
		
	}
	
	public void writeLightLevel(int level )
	{
		if (mDeviceAddress != null && mBleWrapper != null)
		{
			// light 
			final UUID LIGHT_SERVICE_UUID = UUID.fromString("0000ffb0-0000-1000-8000-00805f9b34fb");
			final UUID LIGHT_RED_CHAR_UUID = UUID.fromString("0000ffb1-0000-1000-8000-00805f9b34fb");
			final UUID LIGHT_GREEN_CHAR_UUID = UUID.fromString("0000ffb2-0000-1000-8000-00805f9b34fb");
			
			BluetoothGattService lightService = mBleWrapper.getService(LIGHT_SERVICE_UUID);
			if(lightService == null) 
			{
				Log.d(TAG, "Immediate Alert service not found!");
				return;
			}
			
			if(level==1)
			{
				BluetoothGattCharacteristic lightRed = lightService.getCharacteristic(LIGHT_RED_CHAR_UUID);
				if(lightRed == null) 
				{
					Log.d(TAG, "Alert Level charateristic not found!");
					return;
				}
				byte[] dataToWrite = parseHexStringToBytes("0x0"+ Integer.toString( level ));
				mBleWrapper.writeDataToCharacteristic(lightRed, dataToWrite);
				
				
				
			}
			else if (level==2)
			{
				BluetoothGattCharacteristic lightGreen = lightService.getCharacteristic(LIGHT_GREEN_CHAR_UUID);
				if(lightGreen == null) 
				{
					Log.d(TAG, "Alert Level charateristic not found!");
					return;
				}
				byte[] dataToWrite = parseHexStringToBytes("0x0"+ Integer.toString( level ));
				mBleWrapper.writeDataToCharacteristic(lightGreen, dataToWrite);
				
			}
		}
	}
	
	public void writeAccToggle(boolean bNotifyAccel)
	{
		if (mDeviceAddress != null && mBleWrapper != null)
		{
			final UUID ACCEL_SERVICE_UUID = UUID.fromString("0000ffa0-0000-1000-8000-00805f9b34fb");
			final UUID ACCEL_CHAR_UUID = UUID.fromString("0000ffa1-0000-1000-8000-00805f9b34fb");
			final UUID ACCEL_X_UUID = UUID.fromString("0000ffa3-0000-1000-8000-00805f9b34fb");
			final UUID ACCEL_Y_UUID = UUID.fromString("0000ffa4-0000-1000-8000-00805f9b34fb");
			final UUID ACCEL_Z_UUID = UUID.fromString("0000ffa5-0000-1000-8000-00805f9b34fb");
			
			BluetoothGattService accService = mBleWrapper.getService(ACCEL_SERVICE_UUID);
			if(accService == null) 
			{
				Log.d(TAG, "accel service not found!");
				return;
			}
			BluetoothGattCharacteristic accChr = accService.getCharacteristic(ACCEL_CHAR_UUID);
			if(accChr == null) 
			{
				Log.d(TAG, "accelmometor charateristic not found!");
				return;
			}
			//
			// 收 Notify
			int acVal = 0;
			acVal = bNotifyAccel?1:0;
			byte[] dataToWrite = parseHexStringToBytes("0x0"+ Integer.toString( acVal ));
			
			mBleWrapper.writeDataToCharacteristic(accChr, dataToWrite);
			
			
			BluetoothGattCharacteristic accChrz = accService.getCharacteristic(ACCEL_Z_UUID);
			if(accChrz == null) 
			{
				Log.d(TAG, "accelmometorZ charateristic not found!");
				return;
			}
			mBleWrapper.setNotificationForCharacteristic(accChrz, bNotifyAccel);
		}	
		
	}
	
	public void writeZAccToggle(boolean bNotifyAccel)
	{
		if (mDeviceAddress != null && mBleWrapper != null)
		{
			final UUID ACCEL_SERVICE_UUID = UUID.fromString("0000ffa0-0000-1000-8000-00805f9b34fb");
			
			final UUID ACCEL_X_UUID = UUID.fromString("0000ffa3-0000-1000-8000-00805f9b34fb");
			final UUID ACCEL_Y_UUID = UUID.fromString("0000ffa4-0000-1000-8000-00805f9b34fb");
			final UUID ACCEL_Z_UUID = UUID.fromString("0000ffa5-0000-1000-8000-00805f9b34fb");
			
			BluetoothGattService accService = mBleWrapper.getService(ACCEL_SERVICE_UUID);
			if(accService == null) 
			{
				Log.d(TAG, "accel service not found!");
				return;
			}
			
			BluetoothGattCharacteristic accChrz = accService.getCharacteristic(ACCEL_Y_UUID);
			if(accChrz == null) 
			{
				Log.d(TAG, "accelmometorZ charateristic not found!");
				return;
			}
			mBleWrapper.setNotificationForCharacteristic(accChrz, bNotifyAccel);
		}	
		
	}
	
	public Peripheral(String deviceName,String deviceAddress, String deviceRSSI) 
	{
		//setContentView(R.layout.activity_peripheral);
		//setContentView(R.layout.activity_touching);
		//getActionBar().setDisplayHomeAsUpEnabled(true);
		//mListViewHeader = (View) getLayoutInflater().inflate(R.layout.peripheral_list_services_header, null, false);
		
		//connectViewsVariables();
		
        //final Intent intent = getIntent();
        mDeviceName = deviceName;
        mDeviceAddress = deviceAddress;
        mDeviceRSSI = deviceRSSI;
        //mDeviceNameView.setText(mDeviceName);
        //mDeviceAddressView.setText(mDeviceAddress);
        //mDeviceRssiView.setText(mDeviceRSSI);
        //getActionBar().setTitle(mDeviceName);
        
        //mListView.addHeaderView(mListViewHeader);
        //mListView.setOnItemClickListener(listClickListener);
        
        
       
	}
	
	
	public void onResume() 
	{
		//super.onResume();
		Util.showToast("mBleWrapper : " + mBleWrapper.toString());
		
		if(mBleWrapper == null) mBleWrapper = new BleWrapper(ScanningActivity.instance, this);
		
		if(mBleWrapper.initialize() == false) 
		{
			ScanningActivity.instance.finish();
		}
		
		if(mServicesListAdapter == null) mServicesListAdapter = new ServicesListAdapter();
		if(mCharacteristicsListAdapter == null) mCharacteristicsListAdapter = new CharacteristicsListAdapter();
		if(mCharDetailsAdapter == null) mCharDetailsAdapter = new CharacteristicDetailsAdapter(this, mBleWrapper);
		
		//mListView.setAdapter(mServicesListAdapter);
		mListType = ListType.GATT_SERVICES;
    	mBleWrapper.connect(mDeviceAddress);
	};
	
	
	public void onPause() 
	{
		mServicesListAdapter.clearList();
		mCharacteristicsListAdapter.clearList();
		mCharDetailsAdapter.clearCharacteristic();
		
		
		mBleWrapper.stopMonitoringRssiValue();
		mBleWrapper.diconnect();
		mBleWrapper.close();
	};

	
	
}
