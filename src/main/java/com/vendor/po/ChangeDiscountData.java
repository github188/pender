package com.vendor.po;

/**
 * 打折对象推送对象
 * 
 * @author duanyx
 *
 */
public class ChangeDiscountData {

	private Long Id;
	private String productNo;// : 商品编码
	private Double zhekou_num;// : 商品时折扣
	private Double deletePrice;// : 零售价
	private Double price;// 单个商品打折后价格
	private Integer state; // 0 :下线  1：上线

	public String getProductNo() {
		return productNo;
	}

	public void setProductNo(String productNo) {
		this.productNo = productNo;
	}

	public Double getZhekou_num() {
		return zhekou_num;
	}

	public void setZhekou_num(Double zhekou_num) {
		this.zhekou_num = zhekou_num;
	}

	public Long getId() {
		return Id;
	}

	public void setId(Long id) {
		Id = id;
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

	public Double getDeletePrice() {
		return deletePrice;
	}

	public void setDeletePrice(Double deletePrice) {
		this.deletePrice = deletePrice;
	}

}
