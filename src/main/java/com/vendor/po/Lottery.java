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
 * @ClassName: LuckyDraw
 * @Description: TODO(抽奖活动实体)
 * @author duanyx
 * @date 2016年12月12日 下午5:20:31
 */
@Entity
@Table(name = "T_LOTTERY")
@JsonFilter(value = "com.vendor.po.Lottery")
public class Lottery implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;

	/** 活动名称 */
	@Column(name = "LOTTERY_NAME")
	private String lotteryName;

	/** 活动创建时间 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "CREATE_TIME")
	private Timestamp createTime;

	/** 活动预热时间 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "WARM_UP_TIME")
	private Timestamp warmUpTime;

	/** 活动开始时间 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "START_TIME")
	private Timestamp startTime;

	/** 活动结束时间 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "END_TIME")
	private Timestamp endTime;
	
	/** 活动状态(0:未开始,1：已开始,2:已结束,5:预热中) */
	@Column(name = "STATE")
	private String state;

	/** 所属机构 */
	@Column(name = "ORG_ID")
	private Long orgId;
	
	/** 是否可修改概率(0:默认不可以修改,1:可修改) */
	@Column(name = "IS_PROBABIL")
	private Integer isProbabil;
	
	/** 活动发布状态（0:未发布、1:已发布）*/
	@Column(name = "IS_PUBLISH")
	private Integer isPublish;
	
	@Transient
	private String pushState;
	
	/** 参与抽奖活动的商品ID/设备编号 */ 
	@Transient
	private List<LotteryDevNoProduct> lotteryDevNoProductList = new ArrayList<LotteryDevNoProduct>();
	
	
	@Transient
	private List<LotteryProduct> lotteryProductList = new ArrayList<LotteryProduct>();
	
	@Transient
	private List<Device> deviceList = new ArrayList<Device>();
	
	@Transient
	private Long lotteryProductId;
	
	@Transient
	private String productNo;
	
	@Transient
	private Integer num;
	
	@Transient
	private String factoryDevNo;
	
	
	

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLotteryName() {
		return lotteryName;
	}

	public void setLotteryName(String lotteryName) {
		this.lotteryName = lotteryName;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public Timestamp getWarmUpTime() {
		return warmUpTime;
	}

	public void setWarmUpTime(Timestamp warmUpTime) {
		this.warmUpTime = warmUpTime;
	}

	public Timestamp getStartTime() {
		return startTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

	public Timestamp getEndTime() {
		return endTime;
	}

	public void setEndTime(Timestamp endTime) {
		this.endTime = endTime;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public Long getOrgId() {
		return orgId;
	}

	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}

	public List<LotteryDevNoProduct> getLotteryDevNoProductList() {
		return lotteryDevNoProductList;
	}

	public void setLotteryDevNoProductList(List<LotteryDevNoProduct> lotteryDevNoProductList) {
		this.lotteryDevNoProductList = lotteryDevNoProductList;
	}

	public List<LotteryProduct> getLotteryProductList() {
		return lotteryProductList;
	}

	public void setLotteryProductList(List<LotteryProduct> lotteryProductList) {
		this.lotteryProductList = lotteryProductList;
	}

	public List<Device> getDeviceList() {
		return deviceList;
	}

	public void setDeviceList(List<Device> deviceList) {
		this.deviceList = deviceList;
	}

	public Integer getIsProbabil() {
		return isProbabil;
	}

	public void setIsProbabil(Integer isProbabil) {
		this.isProbabil = isProbabil;
	}

	public String getPushState() {
		return pushState;
	}

	public void setPushState(String pushState) {
		this.pushState = pushState;
	}

	public Integer getIsPublish() {
		return isPublish;
	}

	public void setIsPublish(Integer isPublish) {
		this.isPublish = isPublish;
	}

	public Long getLotteryProductId() {
		return lotteryProductId;
	}

	public void setLotteryProductId(Long lotteryProductId) {
		this.lotteryProductId = lotteryProductId;
	}

	public String getProductNo() {
		return productNo;
	}

	public void setProductNo(String productNo) {
		this.productNo = productNo;
	}

	public Integer getNum() {
		return num;
	}

	public void setNum(Integer num) {
		this.num = num;
	}

	public String getFactoryDevNo() {
		return factoryDevNo;
	}

	public void setFactoryDevNo(String factoryDevNo) {
		this.factoryDevNo = factoryDevNo;
	}


}
