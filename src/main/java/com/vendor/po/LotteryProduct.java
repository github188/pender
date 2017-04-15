package com.vendor.po;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
* @ClassName: 抽奖活动内容
* @Description: TODO
* @author: duanyx
* @date: 2016年12月17日 上午11:48:57
*/
@Entity
@Table(name = "T_LOTTERY_PRODUCT")
@JsonFilter(value = "com.vendor.po.LotteryProduct")
public class LotteryProduct implements Serializable{
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;
	
	/** 抽奖活动内容名称 */ 
	@Column(name = "PRODUCT_NAME")
	private String productName;
	
	/** 抽奖活动内容零售价 */ 
	@Column(name = "PRODUCT_PRICE")
	private Double productPrice;
	
	/** 抽奖活动内容规格 */ 
	@Column(name = "PRODUCT_NORMS")
	private String productNorms;
	
	/** 商品编号 */ 
	@Column(name = "PRODUCT_NO")
	private String productNo;
	
	/** 所属抽奖活动 */ 
	@Column(name = "LOTTERY_ID")
	private Long lotteryId;
	
	@Transient
	private List<Device> deviceList = new ArrayList<Device>();
	
	@Transient
	private String images; 
	
	@Transient
	private String picUrl;
	
	@Transient
	private String picDetailUrl;
	
	@Transient
	private String desc;
	
	@Transient
	private Long key; 
	
	@Transient
	private Long[] fileIds;
	
	@Transient
	private String isSucessState;
	
	@Transient
	private String factoryDevNo;
	
	@Transient
	private Long lotteryProductId;
	
	@Transient
	private Integer deviceState;
	
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public Double getProductPrice() {
		return productPrice;
	}

	public void setProductPrice(Double productPrice) {
		this.productPrice = productPrice;
	}

	public String getProductNorms() {
		return productNorms;
	}

	public void setProductNorms(String productNorms) {
		this.productNorms = productNorms;
	}

	public Long getLotteryId() {
		return lotteryId;
	}

	public void setLotteryId(Long lotteryId) {
		this.lotteryId = lotteryId;
	}

	public List<Device> getDeviceList() {
		return deviceList;
	}

	public void setDeviceList(List<Device> deviceList) {
		this.deviceList = deviceList;
	}

	public String getImages() {
		return images;
	}

	public void setImages(String images) {
		this.images = images;
	}

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public Long[] getFileIds() {
		return fileIds;
	}

	public void setFileIds(Long[] fileIds) {
		this.fileIds = fileIds;
	}

	public String getPicUrl() {
		return picUrl;
	}

	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}

	public String getPicDetailUrl() {
		return picDetailUrl;
	}

	public void setPicDetailUrl(String picDetailUrl) {
		this.picDetailUrl = picDetailUrl;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getProductNo() {
		return productNo;
	}

	public void setProductNo(String productNo) {
		this.productNo = productNo;
	}

	public String getIsSucessState() {
		return isSucessState;
	}

	public void setIsSucessState(String isSucessState) {
		this.isSucessState = isSucessState;
	}

	public String getFactoryDevNo() {
		return factoryDevNo;
	}

	public void setFactoryDevNo(String factoryDevNo) {
		this.factoryDevNo = factoryDevNo;
	}

	public Long getLotteryProductId() {
		return lotteryProductId;
	}

	public void setLotteryProductId(Long lotteryProductId) {
		this.lotteryProductId = lotteryProductId;
	}

	public Integer getDeviceState() {
		return deviceState;
	}

	public void setDeviceState(Integer deviceState) {
		this.deviceState = deviceState;
	}
	
	
}
