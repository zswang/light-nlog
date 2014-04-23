package com.baidu.light.nlog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.baidu.nlog.NLog;

import android.content.Context;
import android.util.Log;

public class NLogLauncher extends CordovaPlugin {
	private static final String LOG_TAG = "NLogLauncher";
	private static final boolean DEBUG = true;

	private CallbackContext mNLogCallbackContext = null;
	private Context mContext;
	private boolean mResumed = true;

	@SuppressWarnings("rawtypes")
	private void json2Array(JSONObject json, ArrayList<Object> list) {
		Iterator it = json.keys();
		while (it.hasNext()) {
			String key = (String) it.next();
			Object o = json.opt(key);
			list.add(key);
			if (json.isNull(key)) {
				list.add(null);
			} else if (o instanceof JSONObject) {
				list.add(json2Map(json.optJSONObject(key)));
			} else if (o instanceof JSONArray) {
				jarr2Array(json.optJSONArray(key), list, 0);
			} else if (o instanceof Boolean) {
				list.add(json.optBoolean(key));
			} else {
				list.add(o);
			}
		}
	}

	private void jarr2Array(JSONArray jarr, ArrayList<Object> list, int offset) {
		for (int i = offset; i < jarr.length(); i++) {
			Object o = jarr.opt(i);
			if (jarr.isNull(i)) {
				list.add(null);
			} else if (o instanceof JSONObject) {
				json2Array(jarr.optJSONObject(i), list);
			} else if (o instanceof JSONArray) {
				jarr2Array(jarr.optJSONArray(i), list, 0);
			} else if (o instanceof Boolean) {
				list.add(jarr.optBoolean(i));
			} else {
				list.add(o);
			}
		}
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
										result.put("event", "createSession");
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
										result.put("event", "destorySession");
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
										result.put("event", "upgrade");
										result.put("param", map2Json(map));
									} catch (JSONException e) {
									}
									mNLogCallbackContext.success(result);
								}
							}), json2Map(args.optJSONObject(0))));
			if (mResumed) {
				NLog.onResume(mContext); // 插件加载之前已经是 resumen 状态
			}
		} else if ("command".equals(action)) {
			if (!NLog.getInitCompleted()) { // 未初始化
				callbackContext.error("Can't repeat initialization.");
				return true;
			}
			ArrayList<Object> list = new ArrayList<Object>();
			jarr2Array(args, list, 1);
			NLog.cmd(args.optString(0, ""), list.toArray());
		}
		return true;
	}

	/**
	 * Called when the system is about to start resuming a previous activity.
	 *
	 * @param multitasking		Flag indicating if multitasking is turned on for app
	 */
	public void onPause(boolean multitasking) {
		super.onPause(multitasking);
		NLog.onPause(webView.getContext());
		mResumed = false;
	}

	/**
	 * Called when the activity will start interacting with the user.
	 *
	 * @param multitasking		Flag indicating if multitasking is turned on for app
	 */
	public void onResume(boolean multitasking) {
		super.onResume(multitasking);
		NLog.onResume(mContext);
		mResumed = true;
	}
	
    /**
     * @param cordova The context of the main Activity.
     * @param webView The associated CordovaWebView.
     */
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    	super.initialize(cordova, webView);
    	mContext = webView.getContext();
    }

	/**
	 * The final call you receive before your activity is destroyed.
	 */
	public void onDestroy() {
		super.onDestroy();
		NLog.exit();
	}
}
