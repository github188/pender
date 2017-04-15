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
 * 推送离线消息存储实体
 * 
 * @author Administrator
 *
 */
@Entity
@Table(name = "T_OFFLINE_MESSAGE")
@JsonFilter("com.vendor.po.OfflineMessage")
public class OfflineMessage implements Serializable{

	/**
	 * 推送离线消息ID
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;

	/**
	 *  推送离线消息
	 */
	@Column(name = "OFF_LINES")
	private String offlines;
//	
//	/** 离线创建时间 */
//	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
//	@Column(name = "CREATE_TIME")
//	private Timestamp createTime;
//	
//	/** 离线更新时间 */
//	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
//	@Column(name = "UPDATE_TIME")
//	private Timestamp updateTime;
//	
//	/** 离线详细的状态:0未删除,1已删除 */ 
//	@Column(name = "STATE")
//	private Integer state;
	
	/**
	 * 设备号
	 */
	@Column(name = "DEV_NOS")
	private String devNos;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getOfflines() {
		return offlines;
	}

	public void setOfflines(String offlines) {
		this.offlines = offlines;
	}

	public String getDevNos() {
		return devNos;
	}

	public void setDevNos(String devNos) {
		this.devNos = devNos;
	}

//	public Timestamp getCreateTime() {
//		return createTime;
//	}
//
//	public void setCreateTime(Timestamp createTime) {
//		this.createTime = createTime;
//	}
//
//	public Timestamp getUpdateTime() {
//		return updateTime;
//	}
//
//	public void setUpdateTime(Timestamp updateTime) {
//		this.updateTime = updateTime;
//	}
//
//	public Integer getState() {
//		return state;
//	}
//
//	public void setState(Integer state) {
//		this.state = state;
//	}

}
