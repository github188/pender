package com.vendor.vo.app;

import java.util.List;

public class QRCodeOrders {

	/**
	 * 多个商品，包含一个商品的情况
	 */
	private List<Product> products;
	
	/**
	 * 设备组号
	 */
	private String machineNum;
	
	/**
	 * 订单号
	 */
	private String orderNo;
	
	/**
	 * 商品编号
	 */
	private String productNo;

	/**
	 * 商品数量
	 */
	private Integer productCount;

	/**
	 * 货柜编号
	 */
	private String cabinetNo;

	/**
	 * 货道编号
	 */
	private String roadNo;
	
	/**
	 * 商品类型  1:自有  2：平台供货
	 */
	private Integer type;
	
	
	/** 订单类型  0：普通订单  1：限时打折  2：抽奖活动 */
	private String orderType;
	
	public List<Product> getProducts() {
		return products;
	}

	public void setProducts(List<Product> products) {
		this.products = products;
	}

	public String getMachineNum() {
		return machineNum;
	}

	public void setMachineNum(String machineNum) {
		this.machineNum = machineNum;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
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

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

}
