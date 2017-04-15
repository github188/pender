package com.vendor.service;

public interface IObserverService {
	
	/**
	 * 更新设备日志信息
	 * 
	 * @param devNo 设备编号
	 * @param isOnline 设备是否在线 true:在线 false:离线
	 */
	 void saveDeviceLog(String devNo, boolean isOnline);
}
