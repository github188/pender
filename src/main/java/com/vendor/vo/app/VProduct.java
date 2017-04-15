package com.vendor.vo.app;

/**
 * 出货通知用商品对象
 */
public class VProduct {
	
	/**
	 * 商品ID
	 */
	private String produceId;
	
	/**
	 * 商品数量
	 */
	private Integer count;
	
	/**
	 * 商品类型  1：自有  2：平台供货
	 */
	private Integer type;

	public String getProduceId() {
		return produceId;
	}

	public void setProduceId(String produceId) {
		this.produceId = produceId;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	
}
