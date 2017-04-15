/**
 * 
 */
package com.vendor.vo.app;

import java.io.Serializable;

/**
 * 【补货APP】设备信息
 * @author liujia on 2016年6月27日
 */
public class DeviceInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 设备出厂编号
	 */
	private String factoryDevNo;
	
	/**
	 * 设备类型
	 */
	private String typeStr;
	
	/**
	 * 是否在线  1在线  2 离线 
	 */
	private Integer isOnOffLine;

	public String getFactoryDevNo() {
		return factoryDevNo;
	}

	public void setFactoryDevNo(String factoryDevNo) {
		this.factoryDevNo = factoryDevNo;
	}

	public String getTypeStr() {
		return typeStr;
	}

	public void setTypeStr(String typeStr) {
		this.typeStr = typeStr;
	}

	public Integer getIsOnOffLine() {
		return isOnOffLine;
	}

	public void setIsOnOffLine(Integer isOnOffLine) {
		this.isOnOffLine = isOnOffLine;
	}

}
