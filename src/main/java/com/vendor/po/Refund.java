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

/**
 * 退款信息表
 * @author dranson on 2015年12月11日
 */
@Entity
@Table(name = "T_REFUND")
@JsonFilter("com.vendor.po.Refund")
public class Refund implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;
	/**
	 * 原始金额
	 */
	@Column(name = "AMOUNT", length = 8, nullable = false)
	private Double amount;
	/**
	 * 退款编号
	 */
	@Column(name = "CODE", length = 32, nullable = false)
	private String code;
	/**
	 * 申请退款时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "CREATE_TIME", nullable = false)
	private Timestamp createTime;

	/**
	 * 到账时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "UPDATE_TIME")
	private Timestamp updateTime;
	/**
	 * 创建人
	 */
	@Column(name = "CREATE_USER", length = 8, nullable = false)
	private Long createUser;
	/**
	 * 退款金额
	 */
	@Column(name = "FEE_REFUND", length = 8, nullable = false)
	private Double feeRefund;
	/**
	 * 订单号
	 */
	@Column(name = "ORDER_NO", length = 32, nullable = false)
	private String orderNo;
	/**
	 * 支付订单号
	 */
	@Column(name = "PAY_NO", length = 32, nullable = false)
	private String payNo;
	/**
	 * 退款原因
	 */
	@Column(name = "REASON", length = 256)
	private String reason;
	/**
	 * 退款类别   6：微信  7：支付宝
	 */
	@Column(name = "TYPE", length = 4, nullable = false)
	private Integer type;

	/**
	 * 退款状态  0：初始化  1：成功  -1：失败  2：退款中
	 */
	@Column(name = "STATE", length = 4, nullable = false)
	private Integer state;
	/**
	 * 所属商家
	 */
	@Column(name = "ORG_ID", length = 8, nullable = false)
	private Long orgId;
	/**
	 * 备注
	 */
	@Column(name = "REMARK", length = 256)
	private String remark;
	
	/**
	 * 商品ID
	 */
	@Column(name = "SKU_ID", length = 8)
	private Long skuId;
	
	/**
	 * 退款数量
	 */
	@Column(name = "REFUND_QTY", length = 4)
	private Integer refundQty;
	
	/**
	 * 抽奖活动对应的真实商品编码
	 */
	@Transient
	private String productNo;

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Double getAmount() {
		return this.amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public String getCode() {
		return this.code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Timestamp getCreateTime() {
		return this.createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public Long getCreateUser() {
		return this.createUser;
	}

	public void setCreateUser(Long createUser) {
		this.createUser = createUser;
	}

	public Double getFeeRefund() {
		return this.feeRefund;
	}

	public void setFeeRefund(Double feeRefund) {
		this.feeRefund = feeRefund;
	}

	public String getOrderNo() {
		return this.orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public String getPayNo() {
		return this.payNo;
	}

	public void setPayNo(String payNo) {
		this.payNo = payNo;
	}

	public String getReason() {
		return this.reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Long getOrgId() {
		return orgId;
	}

	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	public Long getSkuId() {
		return skuId;
	}

	public void setSkuId(Long skuId) {
		this.skuId = skuId;
	}

	public Integer getRefundQty() {
		return refundQty;
	}

	public void setRefundQty(Integer refundQty) {
		this.refundQty = refundQty;
	}

	public String getProductNo() {
		return productNo;
	}

	public void setProductNo(String productNo) {
		this.productNo = productNo;
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
		Refund other = (Refund) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}