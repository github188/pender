package com.vendor.vo.app;

import java.io.Serializable;

/**
 * 上传本机库存数据接收对象
 * 
 * @author liujia on 2016年6月27日
 */
public class MachineStock implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 设备的厂家编号
	 */
	private String deviceNumber;
	
	/**
	 * 库存数据
	 */
	private String stocks;

	public String getDeviceNumber() {
		return deviceNumber;
	}

	public void setDeviceNumber(String deviceNumber) {
		this.deviceNumber = deviceNumber;
	}

	public String getStocks() {
		return stocks;
	}

	public void setStocks(String stocks) {
		this.stocks = stocks;
	}

}
