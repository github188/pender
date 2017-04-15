package com.vendor.po;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * 打折对象推送对象
 * 
 * @author duanyx
 *
 */
public class ChangeLotteryStateData {

	private String notifyFlag;// 通知标识，用于区分表示本次推送是商品状态变更后的推送。
	private Timestamp time;// 随机时间戳
	private Long messageId;// 消息ID
	private Integer state;// 0 :下线  1：上线
	private String productNo;
	private Double price;//商品价格
	private String productName;//商品名称
	
	private String picUrl;
	private String picDetailUrl;
	private String desc;
	private String version = "16";// 版本号

	private List<ChangeLotteryData> list = new ArrayList<ChangeLotteryData>();

	public String getNotifyFlag() {
		return notifyFlag;
	}

	public void setNotifyFlag(String notifyFlag) {
		this.notifyFlag = notifyFlag;
	}

	public Timestamp getTime() {
		return time;
	}

	public void setTime(Timestamp time) {
		this.time = time;
	}

	public Long getMessageId() {
		return messageId;
	}

	public void setMessageId(Long messageId) {
		this.messageId = messageId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public List<ChangeLotteryData> getList() {
		return list;
	}

	public void setList(List<ChangeLotteryData> list) {
		this.list = list;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
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

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public String getProductNo() {
		return productNo;
	}

	public void setProductNo(String productNo) {
		this.productNo = productNo;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}


}
