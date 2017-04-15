package com.vendor.vo.app;

import java.io.Serializable;

/**
 * 扫码支付请求对象
 */
@Deprecated
public class QRCodeRequestInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 设备编号
	 */
	private String deviceNumber;
	
	/**
	 * 商品编号
	 */
	private String productNo;

	/**
	 * 商品数量
	 */
	private Integer productCount;

	/**
	 * 支付类型
	 */
	private String payType;
	
	/**
	 * 货柜编号
	 */
	private String cabinetNo;

	/**
	 * 货道编号
	 */
	private String roadNo;

	public String getDeviceNumber() {
		return deviceNumber;
	}

	public void setDeviceNumber(String deviceNumber) {
		this.deviceNumber = deviceNumber;
	}

	public String getProductNo() {
		return productNo;
	}

	public void setProductNo(String productNo) {
		this.productNo = productNo;
	}

	public Integer getProductCount() {
		return productCount;
	}

	public void setProductCount(Integer productCount) {
		this.productCount = productCount;
	}

	public String getPayType() {
		return payType;
	}

	public void setPayType(String payType) {
		this.payType = payType;
	}

	public String getCabinetNo() {
		return cabinetNo;
	}

	public void setCabinetNo(String cabinetNo) {
		this.cabinetNo = cabinetNo;
	}

	public String getRoadNo() {
		return roadNo;
	}

	public void setRoadNo(String roadNo) {
		this.roadNo = roadNo;
	}

}