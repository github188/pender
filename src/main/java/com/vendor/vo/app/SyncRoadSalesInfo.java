/**
 * 
 */
package com.vendor.vo.app;

import javax.persistence.Transient;
import java.io.Serializable;

/**
 * 下载货道商品信息接口对象
 * 
 * @author liujia on 2016年6月27日
 */
public class SyncRoadSalesInfo implements Serializable {

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
	 * 打折后的价格
	 */
	private Double price;

	/**
	 * 商品标准价格
	 */
	private Double basePrice;
	
	/**
	 * 商品可售状态 0：不可售  1：可售
	 */
	private Integer state;
	
	/**
	 * 设备DB用货柜号
	 */
	private String dbCabinetNo;

	/**
	 * 设备DB用货道号
	 */
	private String dbRoadNo;
	
	/**
	 * 商品类型  1：自有  2：平台供货
	 */
	private Integer type;
	
	/**
	 * 商品名称
	 */
	private String productName;

	/**
	 * 商品图片
	 */
	private String picUrl;
	
	/**
	 * 货道容量
	 */
	private Integer shelf_amount;
	
	/**
	 * 产地
	 */
	private String original;

	/**
	 * 规格
	 */
	private String spec;
	
	/**
	 * 热销指数
	 */
	private Long hots;
	
	/**
	 * 商品详细介绍
	 */
	private String desc;
	
	/**
	 * 商品图片
	 */
	private String pic;

	/**
	 * 商品详情页主图
	 */
	private String picDetailUrl;
	
	/**
	 * 商品类别  1饮料 2小吃
	 */
	private Integer cagetory_type;
	
	/**
	 * 设备型号
	 */
	private String model;
	
	/**
	 * 折扣值
	 */
	private Double zhekou_num;

	/**
	 * 零售价
	 */
	private Double deletePrice;

	/**
	 * 	商品聚合code
	 */
	private String spuCode;

	/**
	 * 是否是显示的主商品 0不是 1 是
	 */
	private Integer wetherSpuMainGoods;
	/**
	 * 商品排序
	 */
	private Integer goodsSorting;


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

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
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

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
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

	public Integer getShelf_amount() {
		return shelf_amount;
	}

	public void setShelf_amount(Integer shelf_amount) {
		this.shelf_amount = shelf_amount;
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
		return this.picUrl;
	}

	public Integer getCagetory_type() {
		return cagetory_type;
	}

	public void setCagetory_type(Integer cagetory_type) {
		this.cagetory_type = cagetory_type;
	}

	public Double getBasePrice() {
		return basePrice;
	}

	public void setBasePrice(Double basePrice) {
		this.basePrice = basePrice;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public String getPicDetailUrl() {
		return picDetailUrl;
	}

	public void setPicDetailUrl(String picDetailUrl) {
		this.picDetailUrl = picDetailUrl;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public Double getZhekou_num() {
		return zhekou_num;
	}

	public void setZhekou_num(Double zhekou_num) {
		this.zhekou_num = zhekou_num;
	}

	public Double getDeletePrice() {
		return deletePrice;
	}

	public void setDeletePrice(Double deletePrice) {
		this.deletePrice = deletePrice;
	}

	public String getSpuCode() {
		return spuCode;
	}

	public void setSpuCode(String spuCode) {
		this.spuCode = spuCode;
	}

	public Integer getWetherSpuMainGoods() {
		return wetherSpuMainGoods;
	}

	public void setWetherSpuMainGoods(Integer wetherSpuMainGoods) {
		this.wetherSpuMainGoods = wetherSpuMainGoods;
	}

	public Integer getGoodsSorting() {
		return goodsSorting;
	}

	public void setGoodsSorting(Integer goodsSorting) {
		this.goodsSorting = goodsSorting;
	}
}
