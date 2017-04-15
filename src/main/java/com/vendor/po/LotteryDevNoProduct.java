package com.vendor.po;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonFilter;

/**
* @ClassName: 抽奖活动与商品设备关联表
* @Description: TODO
* @author: duanyx
* @date: 2016年12月17日 上午11:49:20
*/
@Entity
@Table(name="T_LOTTERY_DEVNO_PRODUCT")
@JsonFilter(value="com.vendor.po.LotteryDevNoProduct")
public class LotteryDevNoProduct implements Serializable{

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name="ID")
	private Long id;
	
	/** 设备出厂编号 */
	@Column(name="FACTORY_DEV_NO")
	private String factoryDevNo;
	
	@Transient
	private String productCode;
	
	@Transient
	private Integer sellable;
	
	/** 商品ID */ 
	@Column(name="PRODUCT_ID")
	private Long productId;
	
	/** 所属活动 */ 
	@Column(name="LOTTERY_ID")
	private Long lotteryId;
	
	/** 所属抽奖活动商品 */ 
	@Column(name="LOTTERY_PRODUCT_ID")
	private Long lotteryProductId;
	
	/** 是否选中1:选中,0:未选中 */
	@Column(name = "SORT")
	private Integer sort;
	
	/** 数量 */
	@Column(name = "NUM")
	private Integer num;
	
	/** 活动内容是否执行成功（活动执行状态:0:发布成功、1:发布失败、2:执行中、3:未发布、4:预热成功、5:预热失败、6:活动成功、7:活动失败、8:结束成功、9:结束失败）*/
	@Column(name = "IS_SUCESSSTATE")
	private String isSucessState;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFactoryDevNo() {
		return factoryDevNo;
	}

	public void setFactoryDevNo(String factoryDevNo) {
		this.factoryDevNo = factoryDevNo;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public Long getLotteryId() {
		return lotteryId;
	}

	public void setLotteryId(Long lotteryId) {
		this.lotteryId = lotteryId;
	}

	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}

	public Long getLotteryProductId() {
		return lotteryProductId;
	}

	public void setLotteryProductId(Long lotteryProductId) {
		this.lotteryProductId = lotteryProductId;
	}

	public Integer getNum() {
		return num;
	}

	public void setNum(Integer num) {
		this.num = num;
	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public Integer getSellable() {
		return sellable;
	}

	public void setSellable(Integer sellable) {
		this.sellable = sellable;
	}

	public String getIsSucessState() {
		return isSucessState;
	}

	public void setIsSucessState(String isSucessState) {
		this.isSucessState = isSucessState;
	}

}
