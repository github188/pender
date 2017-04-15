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
 * 微商城引流表
 * @author zhaoss on 2016年4月15日*
 */
@Entity
@Table(name = "T_WE_USER")
@JsonFilter("com.vendor.po.WeUser")
public class WeUser implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;
	/**
	 * 微信KEY值
	 */
	@Column(name = "OPEN_ID", length = 64, nullable = false)
	private String openId;
	
	/**
	 * 标识符unionId
	 */
	@Column(name="UNION_ID",length=64,nullable=false)
	private String unionId;
	/**
	 * 所属设备
	 */
	@Column(name = "DEVICE_ID", length = 8, nullable = false)
	private Long deviceId;
	/**
	 * 所属机构
	 */
	@Column(name = "ORG_ID", length = 8, nullable = false)
	private Long orgId;
	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "CREATE_TIME", nullable = false)
	private Timestamp createTime;
	
	/**
	 * 微信昵称
	 */
	@Column(name="NICKNAME",length=255)
	private String nickname;
	
	/**
	 * 设备编号
	 */
	@Column(name="DEV_NO", length=16)
	private String devNo;
	
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}

	public Long getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(Long deviceId) {
		this.deviceId = deviceId;
	}

	public Long getOrgId() {
		return orgId;
	}

	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	
	

	/**
	 * @return the unionId
	 */
	public String getUnionId() {
		return unionId;
	}

	/**
	 * @param unionId the unionId to set
	 */
	public void setUnionId(String unionId) {
		this.unionId = unionId;
	}

	/**
	 * @return the nickname
	 */
	public String getNickname() {
		return nickname;
	}

	/**
	 * @param nickname the nickname to set
	 */
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	/**
	 * @return the devNo
	 */
	public String getDevNo() {
		return devNo;
	}

	/**
	 * @param devNo the devNo to set
	 */
	public void setDevNo(String devNo) {
		this.devNo = devNo;
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
		WeUser other = (WeUser) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}