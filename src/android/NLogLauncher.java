package com.baidu.light.nlog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.nlog.NLog;

import android.util.Log;

public class NLogLauncher extends CordovaPlugin {
	private static final String LOG_TAG = "NLogLauncher";
	private static final boolean DEBUG = true;

	private CallbackContext mNLogCallbackContext = null;

	private Object[] json2Array(JSONObject json, int offset) {
		return null;
	}
	private Object[] json2Array(JSONObject json) {
		return json2Array(json, 0);
	}

	private Object[] json2Array(JSONArray json, int offset) {
		ArrayList<Object> list = new ArrayList<Object>();
		for (int i = offset; i < json.length(); i++) {
			JSONObject a = json.optJSONObject(i);
			JSONArray b = json.optJSONArray(i);
			if (a != null) {
				list.add(json2Array(a));
			} else if (b != null) {
				list.add(json2Array(b));
			} else {
				list.add(json.optString(i));
			}
		}
		
		return list.toArray();
	}
	private Object[] json2Array(JSONArray json) {
		return json2Array(json, 0);
	}

	@SuppressWarnings("rawtypes")
	private Map<String, Object> json2Map(JSONObject json) {
		Map<String, Object> result = new HashMap<String, Object>();
		Iterator it = json.keys();
		while (it.hasNext()) {
			String key = (String) it.next();
			Object o = json.opt(key);
			if (json.isNull(key)) {
				result.put(key, null);
			} else if (o instanceof JSONObject) {
				result.put(key, json2Map(json.optJSONObject(key)));
			} else if (o instanceof JSONArray) {
				result.put(key, json2Array(json.optJSONArray(key)));
			} else if (o instanceof Boolean) {
				result.put(key, json.optBoolean(key));
			} else {
				result.put(key, o);
			}
		}
		return result;
	}
	
	private JSONObject map2Json(Map<String, Object> params) {
		JSONObject result = new JSONObject();
        for (String key : params.keySet()) {
            Object item = params.get(key);
        	try {
				result.putOpt(key, item);
			} catch (JSONException e) {
			}
        }
		return result;
	}

	/**
	 * Executes the request and returns PluginResult.
	 *
	 * @param action            The action to execute.
	 * @param args              JSONArry of arguments for the plugin.
	 * @param callbackContext   The callback id used when calling back into JavaScript.
	 * @return                  True if the action was valid, false if not.
	 */
	@SuppressWarnings("unchecked")
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {
		if (DEBUG) {
			Log.d(LOG_TAG, String.format("execute({ action: %s, args: %s })",
					action, args));
		}
		if ("init".equals(action)) {
			if (NLog.getInitCompleted()) { // 已经初始化过
				callbackContext.error("Can't repeat initialization.");
				return true;
			}
			mNLogCallbackContext = callbackContext;
			NLog.init(webView.getContext(), NLog.mergeMap(
					NLog.buildMap("onCreateSession=", new NLog.EventListener() { // 重新创建session时触发
								@Override
								public void onHandler(Map<String, Object> map) {
									JSONObject result = new JSONObject();
									try {
										result.put("event", "onCreateSession");
										result.put("param", map2Json(map));
									} catch (JSONException e) {
									}
									mNLogCallbackContext.success(result);
								}
							}, "onDestorySession=", new NLog.EventListener() {
								@Override
								public void onHandler(Map<String, Object> map) {
									JSONObject result = new JSONObject();
									try {
										result.put("event", "onDestorySession");
										result.put("param", map2Json(map));
									} catch (JSONException e) {
									}
									mNLogCallbackContext.success(result);
								}
							}, "onUpgrade=", new NLog.EventListener() { // 软件升级
								@Override
								public void onHandler(Map<String, Object> map) {
									JSONObject result = new JSONObject();
									try {
										result.put("event", "onUpgrade");
										result.put("param", map2Json(map));
									} catch (JSONException e) {
									}
									mNLogCallbackContext.success(result);
								}
							}), json2Map(args.optJSONObject(0))));

		} else if ("command".equals(action)) {
			if (!NLog.getInitCompleted()) { // 未初始化
				callbackContext.error("Can't repeat initialization.");
				return true;
			}
			NLog.cmd(args.optString(0, ""), json2Array(args, 1));
		}
		return true;
	}

	/**
	 * Called when the system is about to start resuming a previous activity.
	 *
	 * @param multitasking		Flag indicating if multitasking is turned on for app
	 */
	public void onPause(boolean multitasking) {
		NLog.onPause(webView.getContext());
	}

	/**
	 * Called when the activity will start interacting with the user.
	 *
	 * @param multitasking		Flag indicating if multitasking is turned on for app
	 */
	public void onResume(boolean multitasking) {
		NLog.onResume(webView.getContext());
	}

	/**
	 * The final call you receive before your activity is destroyed.
	 */
	public void onDestroy() {
		NLog.exit();
	}
}
