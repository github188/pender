package com.vendor.po;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFilter;

@Entity
@Table(name = "T_DISCOUNT_PRODUCT_POINTPLACE")
@JsonFilter("com.vendor.po.DiscountProductPointPlace")
public class DiscountProductPointPlace implements Serializable{
	

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;
	
	/** 类型(1:店铺,2:商品) */
	@Column(name = "TYPE")
	private Integer type;
	
	/** 店铺编码(彼此用,分隔开来) */
	@Column(name = "POINTPLACE_NO")
	private String pointplaceNo;
	
	/** 商品ID(彼此用,分隔开来) */
	@Column(name = "PRODUCT_NO")
	private Long productNo;
	
	/** 折扣值 */ 
	@Column(name = "DISCOUNT_VALUE")
	private Double discountValue;
	
	/** 所属打折活动 */
	@Column(name = "DISCOUNT_ID")
	private Long discountId;
	
	/** 是否选中1:选中,0:未选中 */
	@Column(name = "SORT")
	private Integer sort;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getPointplaceNo() {
		return pointplaceNo;
	}

	public void setPointplaceNo(String pointplaceNo) {
		this.pointplaceNo = pointplaceNo;
	}

	public Long getProductNo() {
		return productNo;
	}

	public void setProductNo(Long productNo) {
		this.productNo = productNo;
	}

	public Long getDiscountId() {
		return discountId;
	}

	public void setDiscountId(Long discountId) {
		this.discountId = discountId;
	}

	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}

	public Double getDiscountValue() {
		return discountValue;
	}

	public void setDiscountValue(Double discountValue) {
		this.discountValue = discountValue;
	}

}
