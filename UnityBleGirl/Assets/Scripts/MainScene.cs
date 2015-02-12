using UnityEngine;
using System.Collections;
using System.Collections.Generic;

[ExecuteInEditMode]
public class MainScene : MonoBehaviour {
	
	// Project Number on Google API Console
	private string[] SENDER_IDS = {"136450804985"};
	private string _text = "(null)";

	private const int numLightLevel = 3;
	private int curLightLevel = 0;

	
	// Use this for initialization
	void Start () {
		
		// Create receiver game object
		BLE.Initialize ();
		
		// Set callbacks

		
		BLE.SetMessageCallback ((string msg) => {
			//Debug.Log ("<======== On Message ==========>");

			if( msg.Contains("connected") )
			{
				BLE.ShowToast ("Message : " + msg);
			}
			else if( msg.Contains("zaccel:") )
			{
				string[] splled = msg.Split(':');
				Debug.Log ("<======== z Accel ==========> " + splled[1].ToString()); 
			}
			else if( msg.Contains("ffa1setcharacc") )
			{
				string[] args=new string[1];
				args[0]="1";
				BLE.scanningActivity.Call("writeZAccToggle",args);

			}

			//_text = "Message: " + System.Environment.NewLine;
		});

	}
	
	// Update is called once per frame
	void Update () 
	{
	
	}
	
	void OnGUI () 
	{
		float x = 50.0f;
		float y = 340.0f;
		float width = Screen.width / 2 - x - 25.0f;
		float height = 100.0f;
		float margin = 25.0f;
		
		if (GUI.Button (new Rect(x, y, width, height), "Connect Devices")) 
		{
			#if UNITY_ANDROID
			// && !UNITY_EDITOR
			string[] args=new string[2];
			args[0]="Hello";
			args[1]="World";
			BLE.scanningActivity.Call("onresume",args);
			#endif
		}
		x += width + margin * 2;
		if (GUI.Button (new Rect(x, y, width, height), "Alert Toggle")) 
		{
			#if UNITY_ANDROID
			// && !UNITY_EDITOR
			
			string[] args=new string[1];
			args[0]="1";
			BLE.scanningActivity.Call("writeAlertLevel",args);
			#endif
		}
		x -= width + margin * 2;
		y += height + margin;
		if (GUI.Button (new Rect(x, y, width, height), "Light Toggle")) 
		{
			#if UNITY_ANDROID
			string[] args=new string[1];

			curLightLevel = (++curLightLevel) % numLightLevel;
			args[0]=curLightLevel.ToString();
			BLE.scanningActivity.Call("writeLightLevel",args);
			#endif
		}

		x += width + margin * 2;
		if (GUI.Button (new Rect(x, y, width, height), "Vibrate Toggle")) 
		{
			#if UNITY_ANDROID
			string[] args=new string[1];
			args[0]="1";
			BLE.scanningActivity.Call("writeVibrateToggle",args);
			#endif
		}

		x -= width + margin * 2;
		y += height + margin;
		if (GUI.Button (new Rect(x, y, width, height), "Accel Notify on")) 
		{
			#if UNITY_ANDROID
			string[] args=new string[1];
			args[0]="true";
			BLE.scanningActivity.Call("writeAccToggle",args);
			#endif
		}

		x += width + margin * 2;
		if (GUI.Button (new Rect(x, y, width, height), "Accel Notify Z on")) 
		{
			#if UNITY_ANDROID
			string[] args=new string[1];
			args[0]="1";
			BLE.scanningActivity.Call("writeZAccToggle",args);
			#endif
		}
	}
}
