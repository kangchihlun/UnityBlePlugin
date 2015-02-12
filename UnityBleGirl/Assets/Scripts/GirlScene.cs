using UnityEngine;
using System.Collections;
using System.Collections.Generic;

[ExecuteInEditMode]
public class GirlScene : MonoBehaviour {
	
	// Project Number on Google API Console
	private string[] SENDER_IDS = {"136450804985"};
	private string _text = "(null)";

	private const int numLightLevel = 3;
	private int curLightLevel = 0;

	private float head_rotZ = 0;

	public Transform HeadRotator;
	private Quaternion prevRot = Quaternion.identity;
	public float smoothAmt =  1.5f;

	// Use this for initialization
	void Start () {
		
		// Create receiver game object
		BLE.Initialize ();
		
		// Set callbacks

		
		BLE.SetMessageCallback ((string msg) => 
		{
			//Debug.Log ("<======== On Message ==========>");

			if( msg.Contains("connected") )
			{
				BLE.ShowToast ("Message : " + msg);
			}
			else if( msg.Contains("zaccel:") )
			{
				string[] splled = msg.Split(':');
				try{ head_rotZ = float.Parse(splled[1]);}catch{;}
				Debug.Log ("<======== y Accel ==========> " + splled[1].ToString()); 
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
		float headRot = Mathf.Clamp ( head_rotZ , -30,30);
		Quaternion qrot = Quaternion.Euler(headRot ,0 ,0 );
		qrot = Quaternion.Slerp(prevRot , qrot, smoothAmt * Time.deltaTime);

		HeadRotator.transform.localRotation = qrot;
		prevRot = qrot;
	}
	
	void OnGUI () 
	{
		float x = 50.0f;
		float y = 0;
		float width = 160.0f;//Screen.width / 2 - x - 25.0f;
		float height = 50.0f;
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


		y += height + margin;
		if (GUI.Button (new Rect(x, y, width, height), "Accel Notify on")) 
		{
			#if UNITY_ANDROID
			string[] args=new string[1];
			args[0]="true";
			BLE.scanningActivity.Call("writeAccToggle",args);
			#endif
		}

	}
}
