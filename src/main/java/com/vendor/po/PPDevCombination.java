package com.vendor.po;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFilter;


/**
 * The persistent class for the T_PP_DEV_COMBINATION database table.
 * 
 */
@Entity
@Table(name="T_PP_DEV_COMBINATION")
@JsonFilter("com.vendor.po.PPDevCombination")
public class PPDevCombination implements Serializable {	
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")	
	private Long id;

	@Column(name="IDENTITY", length = 64)
	private String identity;
	
	@Column(name="COMBINATION_NO", length = 32)
	private Integer combinationNo;

	@Column(name="PP_ID")
	private Long ppId;

	@Column(name="DEVICE_ID")
	private Long deviceId;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getCombinationNo() {
		return combinationNo;
	}

	public void setCombinationNo(Integer combinationNo) {
		this.combinationNo = combinationNo;
	}

	public Long getPpId() {
		return ppId;
	}

	public void setPpId(Long ppId) {
		this.ppId = ppId;
	}

	public String getIdentity() {
		return identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	public Long getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(Long deviceId) {
		this.deviceId = deviceId;
	}

	
	
}