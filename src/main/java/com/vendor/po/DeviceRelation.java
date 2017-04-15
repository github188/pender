package com.vendor.po;

import java.io.Serializable;
import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonFilter;


/**
 * The persistent class for the t_dev_combination database table.
 * 
 */
@Entity
@Table(name="t_device_relation")
@JsonFilter("com.vendor.po.DeviceRelation")
public class DeviceRelation implements Serializable {	

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;

	@Column(name="DEV_NO")
	private String devNo;

	@Column(name="FACTORY_DEV_NO")
	private String factoryDevNo;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDevNo() {
		return devNo;
	}

	public void setDevNo(String devNo) {
		this.devNo = devNo;
	}

	public String getFactoryDevNo() {
		return factoryDevNo;
	}

	public void setFactoryDevNo(String factoryDevNo) {
		this.factoryDevNo = factoryDevNo;
	}

}