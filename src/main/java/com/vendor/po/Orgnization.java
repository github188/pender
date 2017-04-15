package com.vendor.po;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.vendor.util.Commons;
/**
 * 组织机构
 * @author dranson on 2015年12月1日
 */
@Entity
@Table(name = "SYS_ORG")
@JsonFilter("com.vendor.po.Orgnization")
public class Orgnization implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;	
	/**
	 * 编码
	 */
	@Column(name = "CODE", length = 16, nullable = false)
	private String code;
	/**
	 * 所属公司
	 */
	@Column(name = "COMPANY_ID", length = 8, nullable = false)
	private Long companyId;
	
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "CREATE_TIME", nullable = false)
	private Timestamp createTime;

	@Column(name = "CREATE_USER", length = 8, nullable = false)
	private Long createUser;
	/**
	 * 负责人
	 */
	@Column(name = "MANAGER", length = 32)
	private String manager;
	/**
	 * 机构名称
	 */
	@Column(name = "NAME", length = 64)
	private String name;
	/**
	 * 办公地点
	 */
	@Column(name = "OFFICE", length = 256)
	private String office;
	/**
	 * 所属机构
	 */
	@Column(name = "PARENT_ID", length = 8)
	private Long parentId;

	@Transient
	private String parentName = "";
	/**
	 * 办公电话
	 */
	@Column(name = "PHONE", length = 16)
	private String phone;
	/**
	 * 序号
	 */
	@Column(name = "SORT", length = 16, nullable = false)
	private String sort;
	/**
	 * 状态
	 */
	@Column(name = "STATE", length = 2, nullable = false)
	private Integer state;
	/**
	 * 所属区域
	 */
	@Column(name = "GENUS_AREA", length = 128)
	private String genusArea;
	/**
	 * 合作起始时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
	@Column(name = "SETTLED_TIME")
	private Timestamp settledTime;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "UPDATE_TIME")
	private Timestamp updateTime;

	@Column(name = "UPDATE_USER", length = 8)
	private Long updateUser;
	
	@Transient
	private String currency;

	@Transient
	private Double currencyRate;
	
	/**
	 * 省
	 */
	@Column(name = "PROV", length = 64)
	private String prov;
	
	/**
	 * 市
	 */
	@Column(name = "CITY", length = 64)
	private String city;
	
	/**
	 * 区
	 */
	@Column(name = "DIST", length = 64)
	private String dist;
	
	@Transient
	private List<Orgnization> orgnizations;
	
	/**
	 * 父节点是否已关联数据(1:已关联、2：没有关联)
	 */
	@Column(name = "IS_RELATE", length = 32)
	private Integer isRelate;
	
	/**
	 * 父节点关联申请(1:提出了申请、2：申请被拒绝、3：申请已同意、0：默认值)
	 */
	@Column(name = "APPLY_RELATE", length = 32)
	private Integer applyRelate = Commons.ORG_APPLY_RELATE_DEFAULT;
	
	/**
	 * 创建组织时会默认创建一个账户，该组织的别名就是该账户的username
	 */
	@Column(name = "USER_ID", length = 64)
	private Long userId;
	
	/**
	 * 组织类型 1：管理者 2：经营者
	 */
	@Column(name = "ORG_TYPE", length = 32)
	private Integer orgType;

	/**
	 * 合作方式 1：合作 2：联营 3：加盟 4：自营
	 */
	@Column(name = "MODE", length = 32)
	private Integer mode;
	
	/**
	 * 组织别名
	 */
	@Transient
	private String alias = "";
	
	/**
	 * 关联申请时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "APPLY_TIME")
	private Timestamp applyTime;

	/**
	 * 区号
	 */
	@Transient
	private String areaCode;

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	public String getGenusArea() {
		return genusArea;
	}

	public void setGenusArea(String genusArea) {
		this.genusArea = genusArea;
	}

	public Timestamp getSettledTime() {
		return settledTime;
	}

	public void setSettledTime(Timestamp settledTime) {
		this.settledTime = settledTime;
	}

	public String getCode() {
		return this.code;
	}

	public void setCode(String code) {
		this.code = code;
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

	public String getManager() {
		return this.manager;
	}

	public void setManager(String manager) {
		this.manager = manager;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOffice() {
		return this.office;
	}

	public void setOffice(String office) {
		this.office = office;
	}

	public Long getParentId() {
		return this.parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public String getPhone() {
		return this.phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getSort() {
		return this.sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public Integer getState() {
		return this.state;
	}

	public void setState(Integer state) {
		this.state = state;
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

	public String getProv() {
		return prov;
	}

	public void setProv(String prov) {
		this.prov = prov;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getDist() {
		return dist;
	}

	public void setDist(String dist) {
		this.dist = dist;
	}

	public List<Orgnization> getOrgnizations() {
		return orgnizations;
	}

	public void setOrgnizations(List<Orgnization> orgnizations) {
		this.orgnizations = orgnizations;
	}
	
	public void addOrgnization(Orgnization orgnization) {
		if (this.orgnizations == null)
			this.orgnizations = new ArrayList<Orgnization>();
		this.orgnizations.add(orgnization);
	}

	public Integer getIsRelate() {
		return isRelate;
	}

	public void setIsRelate(Integer isRelate) {
		this.isRelate = isRelate;
	}

	public Integer getApplyRelate() {
		return applyRelate;
	}

	public void setApplyRelate(Integer applyRelate) {
		this.applyRelate = applyRelate;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Integer getOrgType() {
		return orgType;
	}

	public void setOrgType(Integer orgType) {
		this.orgType = orgType;
	}

	public Integer getMode() {
		return mode;
	}

	public void setMode(Integer mode) {
		this.mode = mode;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public Timestamp getApplyTime() {
		return applyTime;
	}

	public void setApplyTime(Timestamp applyTime) {
		this.applyTime = applyTime;
	}

	public String getAreaCode() {
		return areaCode;
	}

	public void setAreaCode(String areaCode) {
		this.areaCode = areaCode;
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
		Orgnization other = (Orgnization) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}