/**
 * 
 */
package com.vendor.vo.app;

import java.io.Serializable;

/**
 * 下载补货上架信息接口对象
 * 
 * @author liujia on 2016年6月27日
 */
public class SyncProductShelveInfo implements Serializable {

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
	 * 库存数量
	 */
	private Integer inventory_amount;

	/**
	 * 补货数量
	 */
	private Integer shelve_amount;
	
	/**
	 * 设备DB用货柜号
	 */
	private String dbCabinetNo;

	/**
	 * 设备DB用货道号
	 */
	private String dbRoadNo;

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

	public Integer getInventory_amount() {
		return inventory_amount;
	}

	public void setInventory_amount(Integer inventory_amount) {
		this.inventory_amount = inventory_amount;
	}

	public Integer getShelve_amount() {
		return shelve_amount;
	}

	public void setShelve_amount(Integer shelve_amount) {
		this.shelve_amount = shelve_amount;
	}

	public String getCabinetNo() {
		return cabinetNo;
	}

	public void setCabinetNo(String cabinetNo) {
		this.cabinetNo = cabinetNo;
	}

	public String getDbCabinetNo() {
		return dbCabinetNo;
	}

	public void setDbCabinetNo(String dbCabinetNo) {
		this.dbCabinetNo = dbCabinetNo;
	}

	public String getDbRoadNo() {
		return dbRoadNo;
	}

	public void setDbRoadNo(String dbRoadNo) {
		this.dbRoadNo = dbRoadNo;
	}

}
