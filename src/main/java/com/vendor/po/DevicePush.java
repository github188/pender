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
 * 设备推送关联信息
 */
@Entity
@Table(name = "T_DEVICE_PUSH")
@JsonFilter("com.vendor.po.DevicePush")
public class DevicePush implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;
	
	/**
	 * 设备组号
	 */
	@Column(name = "FACTORY_DEV_NO", length = 16)
	private String factoryDevNo;

	/**
	 * 推送设备ID
	 */
	@Column(name = "PUSH_DEVICE_ID", length = 64)
	private String pushDeviceId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFactoryDevNo() {
		return factoryDevNo;
	}

	public void setFactoryDevNo(String factoryDevNo) {
		this.factoryDevNo = factoryDevNo;
	}

	public String getPushDeviceId() {
		return pushDeviceId;
	}

	public void setPushDeviceId(String pushDeviceId) {
		this.pushDeviceId = pushDeviceId;
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
		DevicePush other = (DevicePush) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
	
}
