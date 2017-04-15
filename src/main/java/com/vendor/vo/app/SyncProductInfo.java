/**
 * 
 */
package com.vendor.vo.app;

import java.io.Serializable;

/**
 * 下载商品信息接口对象
 * @author liujia on 2016年6月27日
 */
public class SyncProductInfo implements Serializable {

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
	 * 原产地
	 */
	private String original;
	/**
	 * 商品规格
	 */
	private String spec;
	/**
	 * 热销指数
	 */
	private Long hots;
	/**
	 * 详细介绍
	 */
	private String desc;
	/**
	 * 商品图片
	 */
	private String pic;
	
	/**
	 * 商品价格
	 */
	private Double price;

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

	public String getOriginal() {
		return original;
	}

	public void setOriginal(String original) {
		this.original = original;
	}

	public String getSpec() {
		return spec;
	}

	public void setSpec(String spec) {
		this.spec = spec;
	}

	public Long getHots() {
		return hots;
	}

	public void setHots(Long hots) {
		this.hots = hots;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getPic() {
		return pic;
	}

	public void setPic(String pic) {
		this.pic = pic;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

}
