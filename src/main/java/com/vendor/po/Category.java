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

/**
 * 商品类目
 * @author dranson on 2015年12月8日
 */
@Entity
@Table(name = "T_CATEGORY")
@JsonFilter("com.vendor.po.Category")
public class Category implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;
	/**
	 * 编码
	 */
	@Column(name = "CODE", length = 4, nullable = false)
	private String code;
	/**
	 * 商家数量
	 */
	@Column(name = "COUNT_ORG", length = 4, nullable = false)
	private Integer countOrg;
	/**
	 * 在线商品数量
	 */
	@Column(name = "COUNT_SALE", length = 4, nullable = false)
	private Integer countSale;
	/**
	 * 商品数量
	 */
	@Column(name = "COUNT_SKU", length = 2, nullable = false)
	private Integer countSKU;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "CREATE_TIME", nullable = false)
	private Timestamp createTime;

	@Column(name = "CREATE_USER", length = 8, nullable = false)
	private Long createUser;
	/**
	 * 类目层级
	 */
	@Column(name = "LEVEL", length = 2, nullable = false)
	private Integer level;
	/**
	 * 图标
	 */
	@Column(name = "LOGO", length = 128)
	private String logo;
	/**
	 * 类目名称
	 */
	@Column(name = "NAME", length = 32, nullable = false)
	private String name;
	/**
	 * 所属类目
	 */
	@Column(name = "PARENT_ID", length = 8)
	private Long parentId;
	/**
	 * 所属类目代码
	 */
	@Column(name = "PARENT_CODE", length = 16)
	private String parentCode;
	/**
	 * 税率
	 */
	@Column(name = "TAX_RATE", length = 8)
	private Double taxRate;

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "UPDATE_TIME")
	private Timestamp updateTime;

	@Column(name = "UPDATE_USER", length = 8, nullable = false)
	private Long updateUser;
	
	@Transient
	private List<Category> categories;
	
	@Transient
	private String image;

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCode() {
		return this.code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Integer getCountOrg() {
		return this.countOrg;
	}

	public void setCountOrg(Integer countOrg) {
		this.countOrg = countOrg;
	}

	public Integer getCountSale() {
		return this.countSale;
	}

	public void setCountSale(Integer countSale) {
		this.countSale = countSale;
	}

	public Integer getCountSKU() {
		return this.countSKU;
	}

	public void setCountSKU(Integer countSKU) {
		this.countSKU = countSKU;
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

	public Integer getLevel() {
		return this.level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public String getLogo() {
		return this.logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getName() {
		return null == this.name ? this.name : this.name.trim();
	}

	public void setName(String name) {
		this.name = name;
	}

	public Long getParentId() {
		return this.parentId;
	}

	public void setParentId(Long parentId) {
		this.parentId = parentId;
	}

	public String getParentCode() {
		return parentCode;
	}

	public void setParentCode(String parentCode) {
		this.parentCode = parentCode;
	}

	public Double getTaxRate() {
		return taxRate;
	}

	public void setTaxRate(Double taxRate) {
		this.taxRate = taxRate;
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

	public List<Category> getCategories() {
		return categories;
	}

	public void setCategories(List<Category> categories) {
		this.categories = categories;
	}
	
	public void addCategory(Category category) {
		if (this.categories == null)
			this.categories = new ArrayList<Category>();
		this.categories.add(category);
	}
	
	public String getImage() {
		return image==null?"":image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	/**
	 * 初始化默认值
	 */
	public void initDefaultValue() {
		if (this.countOrg == null)
			this.countOrg = 0;
		if (this.countSale == null)
			this.countSale = 0;
		if (this.countSKU == null)
			this.countSKU = 0;
//		if (this.taxRate == null)
//			this.taxRate = 0d;
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
		Category other = (Category) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}