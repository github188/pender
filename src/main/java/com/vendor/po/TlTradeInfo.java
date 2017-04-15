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
import com.vendor.util.Commons;

/**
 * 通联交易信息表
 * @author liujia on 2016年4月12日
 */
@Entity
@Table(name = "T_TL_TRADE_INFO")
@JsonFilter("com.vendor.po.TlTradeInfo")
public class TlTradeInfo implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;
	
	/**
	 * 所属机构
	 */
	@Column(name = "ORG_ID", length = 8, nullable = false)
	private Long orgId;

	/**
	 * 用户ID
	 */
	@Column(name = "USER_ID", length = 8, nullable = false)
	private Long userId;
	
	/**
	 * 交易类型 0：扫码支付  1：js支付  3：微信刷卡支付
	 */
	@Column(name = "TRADE_TYPE", length = 2, nullable = false)
	private Integer tradeType;

	/**
	 * 交易状态 1：初始化  2：交易成功 3：交易失败
	 */
	@Column(name = "TRADE_STATUS", length = 2, nullable = false)
	private Integer tradeStatus;
	
	/**
	 * 交易金额
	 */
	@Column(name = "TRADE_AMOUNT", length = 8, nullable = false)
	private Double tradeAmount;
	
	
	/**
	 * 交易时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "TRADE_TIME", nullable = false)
	private Timestamp tradeTime;

	/**
	 * 商户交易单号，唯一
	 */
	@Column(name = "TRADE_NO", length = 64, nullable = false)
	private String tradeNo;
	
	/**
	 * 微信交易单号，唯一
	 */
	@Column(name = "WX_TRADE_NO", length = 128)
	private String wxTradeNo;

	/**
	 * 通联交易单号，唯一
	 */
	@Column(name = "TL_TRADE_NO", length = 128)
	private String tlTradeNo;

	/**
	 * 应用ID
	 */
	@Column(name = "APP_ID", length = 64, nullable = false)
	private String appId;

	/**
	 * 备注
	 */
	@Column(name = "REMARK", length = 64)
	private String remark;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getOrgId() {
		return orgId;
	}

	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Integer getTradeType() {
		return tradeType;
	}

	public void setTradeType(Integer tradeType) {
		this.tradeType = tradeType;
	}

	public Integer getTradeStatus() {
		return tradeStatus;
	}

	public void setTradeStatus(Integer tradeStatus) {
		this.tradeStatus = tradeStatus;
	}

	public Double getTradeAmount() {
		return tradeAmount;
	}

	public void setTradeAmount(Double tradeAmount) {
		this.tradeAmount = tradeAmount;
	}

	public Timestamp getTradeTime() {
		return tradeTime;
	}

	public void setTradeTime(Timestamp tradeTime) {
		this.tradeTime = tradeTime;
	}
	
	public String getTradeNo() {
		return tradeNo;
	}

	public void setTradeNo(String tradeNo) {
		this.tradeNo = tradeNo;
	}

	public String getWxTradeNo() {
		return wxTradeNo;
	}

	public void setWxTradeNo(String wxTradeNo) {
		this.wxTradeNo = wxTradeNo;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getTlTradeNo() {
		return tlTradeNo;
	}

	public void setTlTradeNo(String tlTradeNo) {
		this.tlTradeNo = tlTradeNo;
	}

	public String getAppId() {
		return appId;
	}

	public void setAppId(String appId) {
		this.appId = appId;
	}

	/**
	 * 初始化默认值
	 */
	public void initDefaultValue() {
		if (this.tradeStatus == null)
			this.tradeStatus = Commons.TL_TRADE_STATUS_INIT;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TlTradeInfo other = (TlTradeInfo) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}