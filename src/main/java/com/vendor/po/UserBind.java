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
 * 用户第三方绑定表
 * @author dranson on 2015年12月14日*
 */
@Entity
@Table(name = "T_USER_BIND")
@JsonFilter("com.vendor.po.UserBind")
public class UserBind implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;
	/**
	 * 第三方KEY值
	 */
	@Column(name = "THIRD_KEY", length = 64, nullable = false)
	private String thirdKey;
	/**
	 * 第三方类型
	 */
	@Column(name = "THIRD_TYPE", length = 8, nullable = false)
	private String thirdType;
	/**
	 * 用户ID
	 */
	@Column(name = "USER_ID", length = 8, nullable = false)
	private Long userId;

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getThirdKey() {
		return this.thirdKey;
	}

	public void setThirdKey(String thirdKey) {
		this.thirdKey = thirdKey;
	}

	public String getThirdType() {
		return this.thirdType;
	}

	public void setThirdType(String thirdType) {
		this.thirdType = thirdType;
	}

	public Long getUserId() {
		return this.userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
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
		UserBind other = (UserBind) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}