var cordova = require('cordova'),
    exec = require('cordova/exec');

var nlog = nlog || {};

/**
 * 执行命令
 * @param{String} cmd 命令行
 * @param{Object} params 参数
 */
function command(cmd, params) {
  	exec(null, function () {
  		
  	}, 'NLog', 'command', [cmd, params]);
};

/**
 * 初始化
 * @param{Object} options 配置项
 * @param{Function} callback 回调
 */
function init(options, callback) {
	exec(callback, function() {

	}, 'NLog', 'init', [options]);
};

nlog.command = command;
nlog.init = init;

module.exports = nlog;