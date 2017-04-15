/**
 * 
 */
package com.vendor.vo.app;

import java.io.Serializable;

/**
 * 【补货APP】商品补货信息
 * 
 * @author liujia on 2016年6月27日
 */
public class ProductReplenishInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 商品编号
	 */
	private String productNo;
	
	/**
	 * 商品名称
	 */
	private String productName;
	
	/**
	 * 商品图片url
	 */
	private String picUrl;

	/**
	 * 应补货数量
	 */
	private Integer totalSupplementNo;

	public String getProductNo() {
		return productNo;
	}

	public void setProductNo(String productNo) {
		this.productNo = productNo;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getPicUrl() {
		return picUrl;
	}

	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}

	public Integer getTotalSupplementNo() {
		return totalSupplementNo;
	}

	public void setTotalSupplementNo(Integer totalSupplementNo) {
		this.totalSupplementNo = totalSupplementNo;
	}

}
