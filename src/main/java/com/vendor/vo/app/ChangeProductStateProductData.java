package com.vendor.vo.app;

import com.ecarry.core.util.MathUtil;

public class ChangeProductStateProductData {

	/**
	 * 商品编码
	 */
	private String productNo;

	/**
	 * 单个商品打折后价格
	 */
	private Double price;

	/**
	 * 商品状态 0 :下线 1：上线
	 */
	private Integer state;

	/**
	 * 商品时折扣
	 */
	private Double zhekou_num;

	/**
	 * 零售价
	 */
	private Double deletePrice;

	public String getProductNo() {
		return productNo;
	}

	public void setProductNo(String productNo) {
		this.productNo = productNo;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public Double getZhekou_num() {
		return MathUtil.round(null == zhekou_num ? 0 : zhekou_num, 2);
	}

	public void setZhekou_num(Double zhekou_num) {
		this.zhekou_num = zhekou_num;
	}

	public Double getDeletePrice() {
		return MathUtil.round(null == deletePrice ? 0 : deletePrice, 2);
	}

	public void setDeletePrice(Double deletePrice) {
		this.deletePrice = deletePrice;
	}

}
