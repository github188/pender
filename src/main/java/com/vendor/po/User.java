package com.vendor.po;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @author dranson on 2015年12月1日
 */
@Entity
@Table(name = "SYS_USER")
@JsonFilter("com.vendor.po.User")
public class User implements UserDetails {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;
	/**
	 * 所属公司
	 */
	@Column(name = "COMPANY_ID", length = 8, nullable = false)
	private Long companyId;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "CREATE_TIME", nullable = false)
	private Timestamp createTime;

	@Column(name = "CREATE_USER", nullable = false)
	private Long createUser;
	/**
	 * 允许编辑
	 */
	@Column(name = "EDITABLE", nullable = false)
	private Boolean editable;
	/**
	 * 电子邮箱
	 */
	@Column(name = "EMAIL", length = 64)
	private String email;
	/**
	 * 状态,0:禁用;1:启用
	 */
	@Column(name = "ENABLE", nullable = false)
	private Boolean enable;
	/**
	 * 最后登录设备
	 */
	@Column(name = "LAST_LOGIN_MACHINE", length = 32)
	private String lastLoginMachine;
	/**
	 * 最后登录时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "LAST_LOGIN_TIME")
	private Timestamp lastLoginTime;
	/**
	 * 手机
	 */
	@Column(name = "MOBILE", length = 16)
	private String mobile;
	/**
	 * 昵称
	 */
	@Column(name = "NICKNAME", length = 64, nullable = false)
	private String nickname;
	/**
	 * 所属机构
	 */
	@Column(name = "ORG_ID", length = 8, nullable = false)
	private Long orgId;

	@Column(name = "PASSWORD", length = 128, nullable = false)
	private String password;
	/**
	 * 头像
	 */
	@Column(name = "PORTRAIT", length = 128)
	private String portrait;
	/**
	 * 修改密码时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "PWD_UPDATE_TIME")
	private Timestamp pwdUpdateTime;
	/**
	 * 姓名
	 */
	@Column(name = "REAL_NAME", length = 64)
	private String realName;
	/**
	 * 密码重试次数
	 */
	@Column(name = "RETRY", length = 2, nullable = false)
	private Integer retry;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "UPDATE_TIME")
	private Timestamp updateTime;

	@Column(name = "UPDATE_USER", length = 8)
	private Long updateUser;

	@Column(name = "USERNAME", length = 128, nullable = false)
	private String username;

	@Column(name = "REMARK", length = 255)
	private String remark;
	
    @Transient
    private Integer userType;
    
	@Transient
	private String orgCode;

	@Transient
	private String orgName;

	@Transient
	private String companyCode;

	@Transient
	private String companyName;
	
	@Transient
	private String currency;

	@Transient
	private Double currencyRate;

	@Transient
	private Integer sysType;

	@Transient
	private Integer users;

	@Transient
	private Date expireDate;

	@Transient
	private boolean account;

	@Transient
	private boolean accountNonExpired = true;

	@Transient
	private List<GrantedAuthority> authorities;
	
	@Transient
	private List<String> bound = new ArrayList<>();

	@Transient
	private String roleName;

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    @Transient
    private Long roleId;

	/*
	 * (non-Javadoc)
	 * @see org.springframework.security.core.userdetails.UserDetails#getAuthorities()
	 */
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.security.core.userdetails.UserDetails#getPassword()
	 */
	@Override
	public String getPassword() {
		return password;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.security.core.userdetails.UserDetails#getUsername()
	 */
	@Override
	public String getUsername() {
		return username;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.security.core.userdetails.UserDetails#isAccountNonExpired()
	 */
	@Override
	public boolean isAccountNonExpired() {
		return accountNonExpired;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.security.core.userdetails.UserDetails#isAccountNonLocked()
	 */
	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.security.core.userdetails.UserDetails#isCredentialsNonExpired()
	 */
	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.security.core.userdetails.UserDetails#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		return enable != null && enable.booleanValue();
	}

	public void setAccountNonExpired(boolean accountNonExpired) {
		this.accountNonExpired = accountNonExpired;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCompanyId() {
		return this.companyId;
	}

	public void setCompanyId(Long companyId) {
		this.companyId = companyId;
	}

	public Timestamp getCreateTime() {
		return this.createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public Long getCreateUser() {
		return this.createUser;
	}

	public void setCreateUser(Long createUser) {
		this.createUser = createUser;
	}

	public boolean isEditabled() {
		return editable != null && editable.booleanValue();
	}

	public Boolean getEditable() {
		return this.editable;
	}

	public void setEditable(Boolean editable) {
		this.editable = editable;
	}

	public String getEmail() {
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public Boolean getEnable() {
		return this.enable;
	}

	public void setEnable(Boolean enable) {
		this.enable = enable;
	}

	public String getLastLoginMachine() {
		return this.lastLoginMachine;
	}

	public void setLastLoginMachine(String lastLoginMachine) {
		this.lastLoginMachine = lastLoginMachine;
	}

	public Timestamp getLastLoginTime() {
		return this.lastLoginTime;
	}

	public void setLastLoginTime(Timestamp lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	public String getMobile() {
		return this.mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getNickname() {
		return this.nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public Long getOrgId() {
		return this.orgId;
	}

	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}

	public String getPortrait() {
		return this.portrait;
	}

	public void setPortrait(String portrait) {
		this.portrait = portrait;
	}

	public Timestamp getPwdUpdateTime() {
		return this.pwdUpdateTime;
	}

	public void setPwdUpdateTime(Timestamp pwdUpdateTime) {
		this.pwdUpdateTime = pwdUpdateTime;
	}

	public String getRealName() {
		return this.realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public Integer getRetry() {
		return this.retry;
	}

	public void setRetry(Integer retry) {
		this.retry = retry;
	}

	public Timestamp getUpdateTime() {
		return this.updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	public Long getUpdateUser() {
		return this.updateUser;
	}

	public void setUpdateUser(Long updateUser) {
		this.updateUser = updateUser;
	}

	public String getOrgCode() {
		return orgCode;
	}

	public void setOrgCode(String orgCode) {
		this.orgCode = orgCode;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getCompanyCode() {
		return companyCode;
	}

	public void setCompanyCode(String companyCode) {
		this.companyCode = companyCode;
	}

	public String getCompanyName() {
		return companyName;
	}

	public void setCompanyName(String companyName) {
		this.companyName = companyName;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public Double getCurrencyRate() {
		return currencyRate;
	}

	public void setCurrencyRate(Double currencyRate) {
		this.currencyRate = currencyRate;
	}

	public Integer getSysType() {
		return sysType;
	}

	public void setSysType(Integer sysType) {
		this.sysType = sysType;
	}

	public Integer getUsers() {
		return users;
	}

	public void setUsers(Integer users) {
		this.users = users;
	}

	public Date getExpireDate() {
		return expireDate;
	}

	public void setExpireDate(Date expireDate) {
		this.expireDate = expireDate;
	}

	public boolean isAccount() {
		return account;
	}

	public void setAccount(boolean account) {
		this.account = account;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setAuthorities(List<GrantedAuthority> authorities) {
		this.authorities = authorities;
	}

	public void addAuthority(GrantedAuthority authority) {
		if (authorities == null)
			authorities = new ArrayList<GrantedAuthority>();
		authorities.add(authority);
	}
	
	public Integer getUserType() {
		return userType;
	}

	public void setUserType(Integer userType) {
		this.userType = userType;
	}

	public List<String> getBound() {
		return bound;
	}

	public void setBound(List<String> bound) {
		this.bound = bound;
	}

	/**
	 * @return the remark
	 */
	public String getRemark() {
		return remark;
	}

	/**
	 * @param remark the remark to set
	 */
	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	/**
	 * 初始化默认值
	 */
	public void initDefaultValue() {
		if (this.retry == null)
			this.retry = 0;
		if (this.editable == null)
			this.editable = true;
		if (this.enable == null)
			this.enable = true;
	}
	
	public void setNull(){
		if(this.email==null){
			this.email="";
		}
		if(this.mobile==null){
			this.mobile="";
		}
		if(this.portrait==null){
			this.portrait="";
		}
		if(this.realName==null){
			this.realName="";
		}
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
		User other = (User) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}