package com.vendor.po;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

/**
 * Created by Chris on 2017/2/28.
 * 扩展默认的角色，增加了角色下用户计数
 */
@Entity
@Table(name = "SYS_ROLE")
@JsonFilter("com.vendor.po.Role")
public class Role {
    @Transient
    private Long userCount;
    @Transient
    private List<User> users;
    @Transient
    private Long user_id;
    public Long getUser_id() {
        return user_id;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }


    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    /**
	 * 角色类型 0：自定义 1：管理者 2：经营者 3：超级管理员 4：管理员 5：运营 6：财务 7：客服 8：补货员
	 */
	@Column(name = "TYPE", length = 32, nullable = true)
	private Integer type;

    public Long getUserCount() {
        return userCount;
    }

    public void setUserCount(Long userCount) {
        this.userCount = userCount;
    }

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}
	@Column(name = "ID")
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	/**
	 * 角色名称
	 */
	@Column(name = "NAME", length = 32, nullable = false)
	private String name;
	/**
	 * 所属机构
	 */
	@Column(name = "ORG_ID", length = 8, nullable = false)
	private Long orgId;

	@Column(name = "SYS_TYPE", length = 2, nullable = false)
	private Integer sysType;

	@Column(name="EDITABLE", nullable = false)
	private Boolean editable;
	/**
	 * 备注
	 */
	@Column(name = "REMARK", length = 256)
	private String remark;

	@Column(name = "CREATE_USER", length = 32)
	private String createUser;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
	@Column(name="CREATE_TIME")
	private Timestamp createTime;

	@Column(name = "UPDATE_USER", length = 32)
	private String updateUser;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "UPDATE_TIME")
	private Timestamp updateTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getOrgId() {
		return orgId;
	}

	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}

	public Integer getSysType() {
		return sysType;
	}

	public boolean isEditabled() {
		return editable != null && editable.booleanValue();
	}

	public Boolean getEditable() {
		return editable;
	}

	public void setEditable(Boolean editable) {
		this.editable = editable;
	}

	public void setSysType(Integer sysType) {
		this.sysType = sysType;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getCreateUser() {
		return createUser;
	}

	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public String getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Role other = (Role) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
