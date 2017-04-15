package com.vendor.vo.web;

import java.sql.Timestamp;

public class Store {
	/**
	 * 店铺编号
	 */
	private String storeNo;
	
	/**
	 * 店铺地址
	 */
	private String storeAddress;
	
	/**
	 * 创建时间
	 */
	private Timestamp createTime;

	public String getStoreNo() {
		return storeNo;
	}

	public void setStoreNo(String storeNo) {
		this.storeNo = storeNo;
	}

	public String getStoreAddress() {
		return storeAddress;
	}

	public void setStoreAddress(String storeAddress) {
		this.storeAddress = storeAddress;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	
}
