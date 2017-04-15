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
 * 设备规则
 * 
 */
@Entity
@Table(name = "T_DEVICE_ROAD_EXCEPT")
@JsonFilter("com.vendor.po.DeviceRoadExcept")
public class DeviceRoadExcept implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;
	/**
	 * 设备厂商编码
	 */
	@Column(name = "FACTORY_CODE", length = 16)
	private String factoryCode;

	/**
	 * 设备型号
	 */
	@Column(name = "MODEL", length = 64)
	private String model;

	/**
	 * 除外货道号
	 */
	@Column(name = "EXCEPT_ROAD_NO", length = 64)
	private Integer exceptRoadNo;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFactoryCode() {
		return factoryCode;
	}

	public void setFactoryCode(String factoryCode) {
		this.factoryCode = factoryCode;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public Integer getExceptRoadNo() {
		return exceptRoadNo;
	}

	public void setExceptRoadNo(Integer exceptRoadNo) {
		this.exceptRoadNo = exceptRoadNo;
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
		DeviceRoadExcept other = (DeviceRoadExcept) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}