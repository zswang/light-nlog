var cordova = require('cordova'),
    exec = require('cordova/exec');

var nlog = nlog || {};

/**
 * 执行命令
 * @param successCallback
 * @param errorCallback
 */
function command(successCallback, errorCallback, params) {
  	exec(successCallback, errorCallback, 'NLogLauncher', 'command', [params]);
};

/**
 * 初始化
 * @param successCallback
 * @param errorCallback
 */
function init(successCallback, errorCallback) {
	exec(successCallback, errorCallback, 'NLogLauncher', 'init', []);
};

nlog.command = command;
nlog.init = init;

module.exports = nlog;