using UnityEngine;
using System.Collections;
using System.Collections.Generic;

/// <summary>
/// GCM receiver.
/// </summary>
public class BleReceiver : MonoBehaviour {
	
	public static System.Action<string> _onError = null;
	//public static System.Action<Dictionary<string, object>> _onMessage = null;
	public static System.Action<string> _onMessage = null;
	public static System.Action<string> _onConnected = null;
	public static System.Action<string> _onDisconnected = null;
	
	
	void Awake() {
		// This receiver must not be destroyed on loading level
		DontDestroyOnLoad(transform.gameObject);
	}
	

	
	void OnMessage (string message) {
		Debug.Log ("Message: " + message);
		if (_onMessage != null) {
			//Dictionary<string, object> table = MiniJSON.Json.Deserialize (message) as Dictionary<string, object>;
			_onMessage (message);
		}
	}

}
