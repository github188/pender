package com.vendor.vo.common;

import java.io.Serializable;

/**
 * 库存数据
 * 
 * @author liujia on 2016年6月27日
 */
public class Stock implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * 货柜号
	 */
	private String cabinetNo;

	/**
	 * 货道号
	 */
	private String roadNo;

	/**
	 * 商品编号
	 */
	private String productNo;

	/**
	 * 库存
	 */
	private Integer numbers;

	public String getRoadNo() {
		return roadNo;
	}

	public void setRoadNo(String roadNo) {
		this.roadNo = roadNo;
	}

	public String getProductNo() {
		return productNo;
	}

	public void setProductNo(String productNo) {
		this.productNo = productNo;
	}

	public Integer getNumbers() {
		return numbers;
	}

	public void setNumbers(Integer numbers) {
		this.numbers = numbers;
	}

	public String getCabinetNo() {
		return cabinetNo;
	}

	public void setCabinetNo(String cabinetNo) {
		this.cabinetNo = cabinetNo;
	}

}
