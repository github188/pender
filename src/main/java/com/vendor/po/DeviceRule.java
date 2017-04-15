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
@Table(name = "T_DEVICE_RULE")
@JsonFilter("com.vendor.po.DeviceRule")
public class DeviceRule implements Serializable {

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
	 * 设备厂商名称
	 */
	@Column(name = "FACTORY_NAME", length = 64)
	private String factoryName;

	/**
	 * 设备型号
	 */
	@Column(name = "MODEL", length = 64)
	private String model;

	/**
	 * 货道组合，多个的话以逗号分割
	 */
	@Column(name = "ROAD_COMBO", length = 256)
	private String roadCombo;
	/**
	 * 货道长度，单位mm
	 */
	@Column(name = "ROAD_LENGTH", length = 16)
	private Integer roadLength;

	/**
	 * 货道容量
	 */
	@Column(name = "ROAD_CAPACITY", length = 16)
	private Integer roadCapacity;

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

	public String getFactoryName() {
		return factoryName;
	}

	public void setFactoryName(String factoryName) {
		this.factoryName = factoryName;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getRoadCombo() {
		return roadCombo;
	}

	public void setRoadCombo(String roadCombo) {
		this.roadCombo = roadCombo;
	}

	public Integer getRoadLength() {
		return roadLength;
	}

	public void setRoadLength(Integer roadLength) {
		this.roadLength = roadLength;
	}

	public Integer getRoadCapacity() {
		return roadCapacity;
	}

	public void setRoadCapacity(Integer roadCapacity) {
		this.roadCapacity = roadCapacity;
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
		DeviceRule other = (DeviceRule) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}