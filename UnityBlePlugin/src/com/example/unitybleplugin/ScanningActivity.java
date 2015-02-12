package com.example.unitybleplugin;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.widget.Toast;

import com.unity3d.player.UnityPlayerActivity;



public class ScanningActivity extends UnityPlayerActivity {
	public static ScanningActivity instance;
	private static final long SCANNING_TIMEOUT = 5 * 1000; /* 5 seconds */
	private static final int ENABLE_BT_REQUEST_ID = 1;
	private static final String MSGEVENT = "OnMessage";
	private DeviceListAdapter mDevicesListAdapter = null;
	private boolean mScanning = false;
	private Handler mHandler = new Handler();
	
	private BleWrapper mBleWrapper = null;
	public Peripheral peripheral;  
	
	private String mlevelStr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ScanningActivity.instance=this;
        initialize();
        //Util.sendMessage(" ScanningActivity ", "onCreate");
    };
    
    public void initialize()
    {
    	//Util.sendMessage(" ScanningActivity ", "initialize");
    	Util.showToast("initialize");
    	// create BleWrapper with empty callback object except uiDeficeFound function (we need only that here) 
        mBleWrapper = new BleWrapper(this, new BleWrapperUiCallbacks.Null() {
        	@Override
        	public void uiDeviceFound(final BluetoothDevice device, final int rssi, final byte[] record) {
        		handleFoundDevice(device, rssi, record);
        	}
        });
        
        // check if we have BT and BLE on board
        if(mBleWrapper.checkBleHardwareAvailable() == false) {
        	bleMissing();
        }

    };

    @Override
    protected void onResume() {
    	super.onResume();
    	onresume("","");
    };
    
    
    
    
    public static void showToast(final String msg)
    {
    	Util.showToast(msg);
    }
    
    
    @Override
    protected void onPause() {
    	super.onPause();
    	if(peripheral!=null)
    		peripheral.onPause();
    	mScanning = false;    	
    	mBleWrapper.stopScanning();
    	invalidateOptionsMenu();
    	
    	mDevicesListAdapter.clearList();
    };
    

    public void HelloWorld(final String title,final String content)
    {
    	runOnUiThread(new Runnable()
    	{
    		public void run()
    		{
    			MakeDialog(title,content);
    		}
    	});
    }

    public void MakeDialog(String title,String content)
    {
    	AlertDialog.Builder builder = new AlertDialog.Builder(ScanningActivity.this);
    	builder.setTitle(title).setMessage(content).setCancelable(false).setPositiveButton("OK", null);
    	builder.show();
    }

    
    /*
     * 		Unity callable functions
     */
    public void onresume(final String title,final String content)
    {
    	// on every Resume check if BT is enabled (user could turn it off while app was in background etc.)
    	if(mBleWrapper.isBtEnabled() == false) {
			// BT is not turned on - ask user to make it enabled
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		    startActivityForResult(enableBtIntent, ENABLE_BT_REQUEST_ID);
		    // see onActivityResult to check what is the status of our request
		}
    	if(peripheral!=null)
    		peripheral.onResume();
    	
    	// initialize BleWrapper object
        mBleWrapper.initialize();
    	
    	mDevicesListAdapter = new DeviceListAdapter(this);
        //setListAdapter(mDevicesListAdapter);
    	
    	
    	// 開始啟動掃描藍芽裝置
    	if(title.length()>0)
    	{
    		//Util.showToast("onResume , called from unity");
            // Automatically start scanning for devices
        	mScanning = true;
    		// remember to add timeout for scanning to not run it forever and drain the battery
    		addScanningTimeout();    	
    		mBleWrapper.startScanning();
    		
    	}
        //invalidateOptionsMenu();
        
    };
    
    public void sendUnityMessage(String msg)
    {
    	Util.sendMessage(MSGEVENT,msg);
    }
    
    public void writeAlertLevel(String level)
    {
    	mlevelStr = level;
    	Thread t = new Thread(new Runnable() {
	        @Override
	        public void run() 
	        {
	        	if(peripheral!=null)
	        		peripheral.writeAlertLevel( Integer.parseInt(mlevelStr) );
	        }
	    });
	    t.start();
    };
    public void writeVibrateToggle( String level )
    {
    	Thread t = new Thread(new Runnable() {
	        @Override
	        public void run() 
	        {
	        	if(peripheral!=null)
	        		peripheral.writeVibrateToggle();
	        }
	    });
	    t.start();
    };
    public void writeLightLevel( String level )
    {
    	mlevelStr = level;
    	Thread t = new Thread(new Runnable() {
	        @Override
	        public void run() 
	        {
	        	if(peripheral!=null)
	        		peripheral.writeLightLevel( Integer.parseInt(mlevelStr) );
	        }
	    });
	    t.start();
    };
    
    public void writeAccToggle( String level )
    {
    	mlevelStr = level;
    	Thread t = new Thread(new Runnable() {
	        @Override
	        public void run() 
	        {
	        	if(peripheral!=null)
	        		peripheral.writeAccToggle(true);
	        }
	    });
	    t.start();
    };
    
    public void writeZAccToggle( String level )
    {
    	mlevelStr = level;
    	Thread t = new Thread(new Runnable() {
	        @Override
	        public void run() 
	        {
	        	if(peripheral!=null)
	        		peripheral.writeZAccToggle(true);
	        }
	    });
	    t.start();
    };
    
    /* check if user agreed to enable BT */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // user didn't want to turn on BT
        if (requestCode == ENABLE_BT_REQUEST_ID) {
        	if(resultCode == Activity.RESULT_CANCELED) {
		    	btDisabled();
		        return;
		    }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

	/* make sure that potential scanning will take no longer
	 * than <SCANNING_TIMEOUT> seconds from now on */
	private void addScanningTimeout() {
		Runnable timeout = new Runnable() {
            @Override
            public void run() {
            	if(mBleWrapper == null) return;
                mScanning = false;
                mBleWrapper.stopScanning();
                invalidateOptionsMenu();
            }
        };
        mHandler.postDelayed(timeout, SCANNING_TIMEOUT);
	}    

	/* add device to the current list of devices */
    private void handleFoundDevice(final BluetoothDevice device,
            final int rssi,
            final byte[] scanRecord)
	{

		// adding to the UI have to happen in UI thread
		runOnUiThread(new Runnable() 
		{
			@Override
			public void run() 
			{
				mDevicesListAdapter.addDevice(device, rssi, scanRecord);
				//mDevicesListAdapter.notifyDataSetChanged();
			}
		});
		
		if (device.getName().contains("FUCK__NMA!") )//
	    {
			peripheral = new Peripheral(device.getName(),device.getAddress(), Integer.toString(mDevicesListAdapter.getRssi(0))  );
			if((mScanning)&&(peripheral != null))
			{
				mScanning = false;
				invalidateOptionsMenu();
				mBleWrapper.stopScanning();
				
				// 重啟 peripheral
				peripheral.mBleWrapper = mBleWrapper; 
				mBleWrapper.mUiCallback = peripheral;
				peripheral.onResume();
				
			}
			Util.sendMessage(MSGEVENT,("found device" + device.getName() ));
    	}
	}	

    private void btDisabled() {
    	Toast.makeText(this, "Sorry, BT has to be turned ON for us to work!", Toast.LENGTH_LONG).show();
        finish();    	
    }
    
    private void bleMissing() {
    	Toast.makeText(this, "BLE Hardware is required but not available!", Toast.LENGTH_LONG).show();
        finish();    	
    }
}
