/**
 * 
 */
package com.vendor.vo.app;

import java.io.Serializable;
import java.util.List;

/**
 * 【补货APP】店铺信息
 * 
 * @author liujia on 2016年6月27日
 */
public class StoreInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 店铺编号
	 */
	private String storeNo;
	/**
	 * 店铺名称
	 */
	private String storeName;
	/**
	 * 店铺地址
	 */
	private String storeAddress;
	
	/**
	 * 省
	 */
	private String prov;
	
	/**
	 * 市
	 */
	private String city;
	
	/**
	 * 区
	 */
	private String dist;
	
	/**
	 * 建议补货时间
	 */
	private List<StoreReplenishTimeInfo> storeReplenishTimeInfos;
	
	/**
	 * 设备信息
	 */
	private List<DeviceInfo> deviceInfos;

	public String getStoreNo() {
		return storeNo;
	}

	public void setStoreNo(String storeNo) {
		this.storeNo = storeNo;
	}

	public String getStoreName() {
		return storeName;
	}

	public void setStoreName(String storeName) {
		this.storeName = storeName;
	}

	public String getStoreAddress() {
		return storeAddress;
	}

	public void setStoreAddress(String storeAddress) {
		this.storeAddress = storeAddress;
	}

	public List<StoreReplenishTimeInfo> getStoreReplenishTimeInfos() {
		return storeReplenishTimeInfos;
	}

	public void setStoreReplenishTimeInfos(List<StoreReplenishTimeInfo> storeReplenishTimeInfos) {
		this.storeReplenishTimeInfos = storeReplenishTimeInfos;
	}

	public List<DeviceInfo> getDeviceInfos() {
		return deviceInfos;
	}

	public void setDeviceInfos(List<DeviceInfo> deviceInfos) {
		this.deviceInfos = deviceInfos;
	}

	public String getProv() {
		return prov;
	}

	public void setProv(String prov) {
		this.prov = prov;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getDist() {
		return dist;
	}

	public void setDist(String dist) {
		this.dist = dist;
	}

}
