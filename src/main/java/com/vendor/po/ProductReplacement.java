package com.vendor.po;

import java.io.Serializable;
import java.sql.Timestamp;

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
 * 商品换货表
 * 
 */
@Entity
@Table(name = "T_PRODUCT_REPLACEMENT")
@JsonFilter("com.vendor.po.ProductReplacement")
public class ProductReplacement implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;
	/**
	 * 产品ID
	 */
	@Column(name = "PRODUCT_ID", length = 8)
	private Long productId;

	/**
	 * 商品编码
	 */
	@Column(name = "PRODUCT_CODE", length = 30)
	private String productCode;

	/**
	 * 商品名称
	 */
	@Column(name = "PRODUCT_NAME", length = 100)
	private String productName;

	/**
	 * 设备号
	 */
	@Column(name = "DEVICE_NO", length = 30)
	private String deviceNo;

	/**
	 * 货柜号
	 */
	@Column(name = "CABINET_NO", length = 30)
	private String cabinetNo;

	/**
	 * 货道号
	 */
	@Column(name = "ROAD_NO", length = 30)
	private String roadNo;

	/**
	 * 库存
	 */
	@Column(name = "STOCK", length = 8, nullable = false)
	private Integer stock;

	/**
	 * 货道容量
	 */
	@Column(name = "CAPACITY", length = 8, nullable = false)
	private Integer capacity;

	/**
	 * 应补货数量
	 */
	@Column(name = "SUPPLEMENT_NO", length = 8)
	private Integer supplementNo;

	/**
	 * 换货数量
	 */
	@Column(name = "REPLACE_CAPACITY", length = 8, nullable = false)
	private Integer replaceCapacity;

	/**
	 * 换货后产品ID
	 */
	@Column(name = "REPLACE_PRODUCT_ID", length = 8)
	private Long replaceProductid;

	/**
	 * 换货后商品编码
	 */
	@Column(name = "REPLACE_PRODUCT_CODE", length = 30)
	private String replaceProductCode;

	/**
	 * 换货后商品名称
	 */
	@Column(name = "REPLACE_PRODUCT_NAME", length = 100)
	private String replaceProductName;

	/**
	 * 机构ID
	 */
	@Column(name = "ORG_ID", length = 8)
	private Long orgId;

	/**
	 * 创建人
	 */
	@Column(name = "CREATE_USER", length = 8, nullable = false)
	private Long createUser;
	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "CREATE_TIME", nullable = false)
	private Timestamp createTime;
	
	/**
	 * 设备组号
	 */
	@Transient
	private String machineNum;
	
	/**
	 * 商品类型  1：自有  2：平台供货
	 */
	@Transient
	private Integer type;
	
	/**
	 * 操作员ID
	 */
	@Transient
	private Long userId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public Integer getStock() {
		return stock;
	}

	public void setStock(Integer stock) {
		this.stock = stock;
	}

	public Integer getCapacity() {
		return capacity;
	}

	public void setCapacity(Integer capacity) {
		this.capacity = capacity;
	}

	public Integer getSupplementNo() {
		return supplementNo;
	}

	public void setSupplementNo(Integer supplementNo) {
		this.supplementNo = supplementNo;
	}

	public Integer getReplaceCapacity() {
		return replaceCapacity;
	}

	public void setReplaceCapacity(Integer replaceCapacity) {
		this.replaceCapacity = replaceCapacity;
	}

	public Long getReplaceProductid() {
		return replaceProductid;
	}

	public void setReplaceProductid(Long replaceProductid) {
		this.replaceProductid = replaceProductid;
	}

	public String getReplaceProductCode() {
		return replaceProductCode;
	}

	public void setReplaceProductCode(String replaceProductCode) {
		this.replaceProductCode = replaceProductCode;
	}

	public String getReplaceProductName() {
		return replaceProductName;
	}

	public void setReplaceProductName(String replaceProductName) {
		this.replaceProductName = replaceProductName;
	}

	public Long getOrgId() {
		return orgId;
	}

	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}

	public Long getCreateUser() {
		return createUser;
	}

	public void setCreateUser(Long createUser) {
		this.createUser = createUser;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public String getCabinetNo() {
		return cabinetNo;
	}

	public void setCabinetNo(String cabinetNo) {
		this.cabinetNo = cabinetNo;
	}

	public String getRoadNo() {
		return roadNo;
	}

	public void setRoadNo(String roadNo) {
		this.roadNo = roadNo;
	}

	public String getMachineNum() {
		return machineNum;
	}

	public void setMachineNum(String machineNum) {
		this.machineNum = machineNum;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getDeviceNo() {
		return deviceNo;
	}

	public void setDeviceNo(String deviceNo) {
		this.deviceNo = deviceNo;
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
		ProductReplacement other = (ProductReplacement) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}