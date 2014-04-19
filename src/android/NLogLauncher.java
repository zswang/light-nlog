package com.baidu.light.nlog;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NLogLauncher extends CordovaPlugin {
	public static final String TAG = "NLogLauncher";

	/**
	 * Executes the request and returns PluginResult.
	 *
	 * @param action            The action to execute.
	 * @param args              JSONArry of arguments for the plugin.
	 * @param callbackContext   The callback id used when calling back into JavaScript.
	 * @return                  True if the action was valid, false if not.
	 */
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {
		return true;
	}

	/**
	 * The final call you receive before your activity is destroyed.
	 */
	public void onDestroy() {
	}
}
