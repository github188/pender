package com.vendor.vo.web;

import javax.persistence.Transient;

import com.ecarry.core.util.MathUtil;

/**
 * 商品销售信息
 */
public class ProductSales {
	/**
	 * 日期，格式：yyyy-MM-dd
	 */
	@Transient
	private String date;
	
	/**
	 * 销售额
	 */
	@Transient
	private Double salesAmount = 0.0;
	
	/**
	 * 销售量
	 */
	@Transient
	private Integer salesVolume = 0;
	
	/**
	 * 商品名称
	 */
	@Transient
	private String productName;

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public Double getSalesAmount() {
		return MathUtil.round(null == salesAmount ? 0.0 : salesAmount, 2);
	}

	public void setSalesAmount(Double salesAmount) {
		this.salesAmount = salesAmount;
	}

	public Integer getSalesVolume() {
		return salesVolume;
	}

	public void setSalesVolume(Integer salesVolume) {
		this.salesVolume = salesVolume;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}
	
}
