package com.vendor.po;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
* @ClassName: 打折时间段表
* @Description: TODO
* @author: Administrator
* @date: 2016年11月17日 下午6:43:17
*/
@Entity
@Table(name = "T_DISCOUNT_PERIOD")
@JsonFilter("com.vendor.po.DiscountPeriod")
public class DiscountPeriod implements Serializable {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;
	
	/** 打折开始时间 */
	@Column(name = "DISCOUNT_START")
	private String discount_start;
	
	/** 打折结束时间 */
	@Column(name = "DISCOUNT_END")
	private String discount_end;
	
	/** 折扣值 */
	@Column(name = "DISCOUNT_VALUE")
	private double discountValue;
	
	/** 所属打折活动 */
	@Column(name = "DISCOUNT_ID")
	private Long discountId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
	public String getDiscount_start() {
		return discount_start;
	}

	public void setDiscount_start(String discount_start) {
		this.discount_start = discount_start;
	}

	public String getDiscount_end() {
		return discount_end;
	}

	public void setDiscount_end(String discount_end) {
		this.discount_end = discount_end;
	}

	public double getDiscountValue() {
		return discountValue;
	}

	public void setDiscountValue(double discountValue) {
		this.discountValue = discountValue;
	}

	public Long getDiscountId() {
		return discountId;
	}

	public void setDiscountId(Long discountId) {
		this.discountId = discountId;
	}

	
}
