package com.vendor.po;

import java.io.Serializable;
import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonFilter;


/**
 * The persistent class for the t_dev_combination database table.
 * 
 */
@Entity
@Table(name="t_dev_combination")
@JsonFilter("com.vendor.po.DevCombination")
public class DevCombination implements Serializable, Comparable{	

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;

	@Column(name="cabinet_no")
	private Integer cabinetNo;

	@Column(name="combination_no")
	private Integer combinationNo;

	@Column(name="model")
	private Integer model;

	@Column(name="aisle_count")
	private Integer aisleCount;
	
	@Transient
	private String identity;
	
	@Transient
	private Long deviceId;
	
	@Transient
	private String factoryDevNo;
	
	@Transient
	private Integer bindState;
	
	public DevCombination() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getCabinetNo() {
		return this.cabinetNo;
	}

	public void setCabinetNo(Integer cabinetNo) {
		this.cabinetNo = cabinetNo;
	}

	public Integer getCombinationNo() {
		return this.combinationNo;
	}

	public void setCombinationNo(Integer combinationNo) {
		this.combinationNo = combinationNo;
	}

	public Integer getModel() {
		return this.model;
	}

	public void setModel(Integer model) {
		this.model = model;
	}

	public Integer getAisleCount() {
		return aisleCount;
	}

	public void setAisleCount(Integer aisleCount) {
		this.aisleCount = aisleCount;
	}

	public String getIdentity() {
		return this.identity;
	}

	public void setIdentity(String identity) {
		this.identity = identity;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DevCombination other = (DevCombination) obj;
		if (cabinetNo == null) {
			if (other.cabinetNo != null)
				return false;
		} else if (!cabinetNo.equals(other.cabinetNo))
			return false;
		//不比id了
//		if (id == null) {
//			if (other.id != null)
//				return false;
//		} else if (!id.equals(other.id))
//			return false;
		if(model == null) {
			if(other.model != null)
				return false;
		} else if(!model.equals(other.model)) {
			return false;
		}	
		
		return true;
	}
	
	@Override
	public int compareTo(Object obj) {
		if(this.cabinetNo > ((DevCombination)obj).cabinetNo)
		{
			return -1;
		}
		else if(this.cabinetNo == ((DevCombination)obj).cabinetNo && this.model == ((DevCombination)obj).model)
		{
			return 0;
		}
		else
		{
			return 1;
		}	
	}

	public Long getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(Long deviceId) {
		this.deviceId = deviceId;
	}

	public String getFactoryDevNo() {
		return factoryDevNo;
	}

	public void setFactoryDevNo(String factoryDevNo) {
		this.factoryDevNo = factoryDevNo;
	}

	public Integer getBindState() {
		return bindState;
	}

	public void setBindState(Integer bindState) {
		this.bindState = bindState;
	}

}