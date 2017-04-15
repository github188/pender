package com.vendor.vo.app;

import java.io.Serializable;

public class Product implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		
	/**
	 * 商品价格
	 */
	private Double price;
	
	/**
	 * 商品编号
	 */
	private String productNo;

	/**
	 * 商品名称
	 */
	private String productName;

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
	
	/**
	 * 订单类型  0：普通订单  1：限时打折  2：抽奖活动
	 */
	private Integer orderType;
	
	/**
	 * 折扣数，无折扣时传1
	 */
	private Double discount;

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

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Integer getOrderType() {
		return orderType;
	}

	public void setOrderType(Integer orderType) {
		this.orderType = orderType;
	}

	public Double getDiscount() {
		return discount;
	}

	public void setDiscount(Double discount) {
		this.discount = discount;
	}

}
