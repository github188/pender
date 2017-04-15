package com.vendor.thirdparty.et;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * 易触同步订单对象
 * 
 * @author liujia on 2016年5月30日
 */
public class ETSyncOrder implements Serializable {

	private static final long serialVersionUID = 1L;

	// 外部订单号
	private String outOrderNo;

	// 设备编号(资产编号)
	private String assetNo;

	// 货道编号
	private String aisleNo;

	// 微信openId
	private String openId;

	// 订单金额
	private Double amount;

	// 支付时间
	private Timestamp payTime;

	// 交易单号
	private String payCode;

	// 支付类型
	private Integer payType;

	// 货品编号
	private String productsNo;

	// 货品名称
	private String productsName;

	public ETSyncOrder() {

	}

	/**
	 * @return the outOrderNo
	 */
	public String getOutOrderNo() {
		return outOrderNo;
	}

	/**
	 * @param outOrderNo the outOrderNo to set
	 */
	public void setOutOrderNo(String outOrderNo) {
		this.outOrderNo = outOrderNo;
	}

	/**
	 * @return the assetNo
	 */
	public String getAssetNo() {
		return assetNo;
	}

	/**
	 * @param assetNo the assetNo to set
	 */
	public void setAssetNo(String assetNo) {
		this.assetNo = assetNo;
	}

	/**
	 * @return the aisleNo
	 */
	public String getAisleNo() {
		return aisleNo;
	}

	/**
	 * @param aisleNo the aisleNo to set
	 */
	public void setAisleNo(String aisleNo) {
		this.aisleNo = aisleNo;
	}

	/**
	 * @return the openId
	 */
	public String getOpenId() {
		return openId;
	}

	/**
	 * @param openId the openId to set
	 */
	public void setOpenId(String openId) {
		this.openId = openId;
	}

	/**
	 * @return the singlePrice
	 */
	public Double getAmount() {
		return amount;
	}

	/**
	 * @param singlePrice the singlePrice to set
	 */
	public void setAmount(Double amount) {
		this.amount = amount;
	}

	/**
	 * @return the payTime
	 */
	public Timestamp getPayTime() {
		return payTime;
	}

	/**
	 * @param payTime the payTime to set
	 */
	public void setPayTime(Timestamp payTime) {
		this.payTime = payTime;
	}

	/**
	 * @return the payCode
	 */
	public String getPayCode() {
		return payCode;
	}

	/**
	 * @param payCode the payCode to set
	 */
	public void setPayCode(String payCode) {
		this.payCode = payCode;
	}

	/**
	 * @return the payType
	 */
	public Integer getPayType() {
		return payType;
	}

	/**
	 * @param payType the payType to set
	 */
	public void setPayType(Integer payType) {
		this.payType = payType;
	}

	/**
	 * @return the productsNo
	 */
	public String getProductsNo() {
		return productsNo;
	}

	/**
	 * @param productsNo the productsNo to set
	 */
	public void setProductsNo(String productsNo) {
		this.productsNo = productsNo;
	}

	/**
	 * @return the productsName
	 */
	public String getProductsName() {
		return productsName;
	}

	/**
	 * @param productsName the productsName to set
	 */
	public void setProductsName(String productsName) {
		this.productsName = productsName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ETSyncOrder [outOrderNo=" + outOrderNo + ", assetNo=" + assetNo + ", aisleNo=" + aisleNo + ", openId=" + openId + ", amount=" + amount + ", payTime="
				+ payTime + ", payCode=" + payCode + ", payType=" + payType + ", productsNo=" + productsNo + ", productsName=" + productsName + "]";
	}

}
