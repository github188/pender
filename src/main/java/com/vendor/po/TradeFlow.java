package com.vendor.po;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.vendor.util.Commons;

/**
 * 交易流水表
 * @author liujia on 2016年4月8日
 */
@Entity
@Table(name = "T_TRADE_FLOW")
@JsonFilter("com.vendor.po.TradeFlow")
public class TradeFlow implements Serializable {

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
	@Column(name = "USER_ID", length = 8)
	private Long userId;
	
	/**
	 * 交易类型 1：提现  2：充值
	 */
	@Column(name = "TRADE_TYPE", length = 2, nullable = false)
	private Integer tradeType;

	/**
	 * 交易状态 1：待处理  2：交易成功 3：交易失败
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
	 * 可用金额
	 */
	@Column(name = "BALANCE", length = 8, nullable = false)
	private Double balance;
	
	/**
	 * 真实名称
	 */
	@Transient
	private String realName;
	
	/**
	 * 机构名称
	 */
	@Transient
	private String orgName;
	
	/**
	 * 银行卡类型 1：工商  2：建设 ...
	 */
	@Transient
	private Integer cardType;
	
	/**
	 * 持卡人
	 */
	@Transient
	private String cardOwner;
	
	/**
	 * 银行卡号
	 */
	@Transient
	private String cardNo;
			
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

	public Double getBalance() {
		return balance;
	}

	public void setBalance(Double balance) {
		this.balance = balance;
	}
	
	/**
	 * 初始化默认值
	 */
	public void initDefaultValue() {
		if (this.tradeStatus == null)
			this.tradeStatus = Commons.TRADE_STATUS_INIT;
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
		TradeFlow other = (TradeFlow) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public Integer getCardType() {
		return cardType;
	}

	public void setCardType(Integer cardType) {
		this.cardType = cardType;
	}

	public String getCardOwner() {
		return cardOwner;
	}

	public void setCardOwner(String cardOwner) {
		this.cardOwner = cardOwner;
	}

	public String getCardNo() {
		return cardNo;
	}

	public void setCardNo(String cardNo) {
		this.cardNo = cardNo;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}
}