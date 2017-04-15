package com.vendor.vo.app;

import java.io.Serializable;
import java.util.List;

/**
 * 购物车
 * @author all
 *
 */
public class ShoppingCart implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 订单号
	 */
	private String orderNo;
	
	/**
	 * 多个商品，包含一个商品的情况
	 */
	private List<Product> products;
	
	/**
	 * 设备编号
	 */
	private String deviceNumber;
	/**
	 * 支付类型
	 */
	
	private String payType;
	
	/**
	 * 总金额
	 */
	private Double amount;
	
	/**
	 * 订单状态  1：未付款  8：已付款
	 */
	private Integer orderState;
	
	public String getDeviceNumber() {
		return deviceNumber;
	}

	public void setDeviceNumber(String deviceNumber) {
		this.deviceNumber = deviceNumber;
	}
	
	public String getPayType() {
		return payType;
	}

	public void setPayType(String payType) {
		this.payType = payType;
	}

	public List<Product> getProducts() {
		return products;
	}

	public void setProducts(List<Product> products) {
		this.products = products;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public Integer getOrderState() {
		return orderState;
	}

	public void setOrderState(Integer orderState) {
		this.orderState = orderState;
	}
}
