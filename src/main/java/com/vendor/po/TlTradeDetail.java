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
 * 通联交易详情表
 * @author liujia on 2016年4月12日
 */
@Entity
@Table(name = "T_TL_TRADE_DETAIL")
@JsonFilter("com.vendor.po.TlTradeDetail")
public class TlTradeDetail implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;
	
	/**
	 * 通联交易信息ID
	 */
	@Column(name = "TL_TRADE_ID", length = 8, nullable = false)
	private Long tlTradeId;

	/**
	 * 商品ID
	 */
	@Column(name = "PRODUCT_ID", length = 8, nullable = false)
	private Long productId;
	
	/**
	 * 补货类型 1：代理商品  2：自营商品
	 */
	@Column(name = "REPLENISH_TYPE", length = 2, nullable = false)
	private Integer replenishType;
	
	/**
	 * 补货数
	 */
	@Column(name = "REPLENISH_NUM", length = 4, nullable = false)
	private Integer replenishNum;
	
	/**
	 * 交易时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "TRADE_TIME", nullable = false)
	private Timestamp tradeTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Timestamp getTradeTime() {
		return tradeTime;
	}

	public void setTradeTime(Timestamp tradeTime) {
		this.tradeTime = tradeTime;
	}

	/**
	 * @return the tlTradeId
	 */
	public Long getTlTradeId() {
		return tlTradeId;
	}

	/**
	 * @param tlTradeId the tlTradeId to set
	 */
	public void setTlTradeId(Long tlTradeId) {
		this.tlTradeId = tlTradeId;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public Integer getReplenishType() {
		return replenishType;
	}

	public void setReplenishType(Integer replenishType) {
		this.replenishType = replenishType;
	}

	public Integer getReplenishNum() {
		return replenishNum;
	}

	public void setReplenishNum(Integer replenishNum) {
		this.replenishNum = replenishNum;
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
		TlTradeDetail other = (TlTradeDetail) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}