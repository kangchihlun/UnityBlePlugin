using UnityEngine;
using System.Collections;
using System.Collections.Generic;

/// <summary>
/// Android GCM Plugin
/// </summary>
public class BLE {
	
	//private const string CLASS_NAME = "com.kskkbys.unitygcmplugin.UnityGCMRegister";
	private const string CLASS_NAME = "com.example.unitybleplugin.UnityBleWrapper";

	private static GameObject _receiver = null;
	public static AndroidJavaObject scanningActivity;

	void Start()
	{

	}

	/// <summary>
	/// Initialize this plugin (Create receiver game object)
	/// </summary>
	public static void Initialize () {
		if (Application.platform == RuntimePlatform.Android) 
		{
			if (_receiver == null) 
			{
				_receiver = new GameObject ("BleReceiver");
				_receiver.AddComponent ("BleReceiver");
			}
			AndroidJavaClass scanningActivityCls = new AndroidJavaClass("com.example.unitybleplugin.ScanningActivity");
			scanningActivity = scanningActivityCls.GetStatic<AndroidJavaObject>("instance");
			/*
			string[] args=new string[1];
			args[0]="";
			scanningActivity.Call("initialize",args);
			*/
		}
	}
	
	public static void ShowToast (string message) 
	{
		if (Application.platform == RuntimePlatform.Android) 
		{
			scanningActivity.CallStatic("showToast", message);
		}
	}



	/// <summary>
	/// Sets the message callback.
	/// </summary>
	/// <param name='onMessage'>
	/// On message.
	/// </param>
	public static void SetMessageCallback (System.Action<string> onMessage) ///(System.Action<Dictionary<string, object>> onMessage) 
	{
		BleReceiver._onMessage = onMessage;
	}




}
