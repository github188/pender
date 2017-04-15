package com.vendor.po;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
 * 货柜
 * 
 */
@Entity
@Table(name = "T_CABINET")
@JsonFilter("com.vendor.po.Cabinet")
public class Cabinet implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;
	/**
	 * 设备ID
	 */
	@Column(name = "DEVICE_ID", length = 8)
	private Long deviceId;

	/**
	 * 货柜编号
	 */
	@Column(name = "CABINET_NO", length = 32)
	private String cabinetNo;

	/**
	 * 货柜类型
	 */
	@Column(name = "TYPE", length = 8)
	private Integer type;
	
	@Transient
	private String typeDesc;
	
	@Transient
	private static final Map<Integer, String> TYPE_MAP = new HashMap<Integer, String>();
	static {
		TYPE_MAP.put(Commons.DEVICE_TYPE_DRINK, Commons.DEVICE_TYPE_STR_DRINK);
		TYPE_MAP.put(Commons.DEVICE_TYPE_DRINK_SMALL, Commons.DEVICE_TYPE_STR_DRINK_SMALL);
		TYPE_MAP.put(Commons.DEVICE_TYPE_CENTER_CONTROL, Commons.DEVICE_TYPE_STR_CENTER_CONTROL);
		TYPE_MAP.put(Commons.DEVICE_TYPE_SPRING, Commons.DEVICE_TYPE_STR_SPRING);
		TYPE_MAP.put(Commons.DEVICE_TYPE_GRID64, Commons.DEVICE_TYPE_STR_GRID64);
		TYPE_MAP.put(Commons.DEVICE_TYPE_GRID40, Commons.DEVICE_TYPE_STR_GRID40);
		TYPE_MAP.put(Commons.DEVICE_TYPE_GRID60, Commons.DEVICE_TYPE_STR_GRID60);
		TYPE_MAP.put(Commons.DEVICE_TYPE_CATERPILLAR, Commons.DEVICE_TYPE_STR_CATERPILLAR);
		TYPE_MAP.put(Commons.DEVICE_TYPE_INTELLIGENT_PRODUCT, Commons.DEVICE_TYPE_STR_INTELLIGENT_PRODUCT);
	}
	
	public String getTypeDesc() {
        this.typeDesc = TYPE_MAP.get(this.type);
        return this.typeDesc;
    }
	
	/**
	 * 货道数量
	 */
	@Column(name = "AISLE_COUNT", length = 8)
	private Integer aisleCount;
	
	/**
	 * 生产厂家
	 */
	@Column(name = "MANUFACTURER")
	private String manufacturer;
	
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
	 * 修改人
	 */
	@Column(name = "UPDATE_USER", length = 8)
	private Long updateUser;
	/**
	 * 修改时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "UPDATE_TIME")
	private Timestamp updateTime;
	
	/**
	 * 型号
	 */
	@Column(name = "MODEL", length = 32)
	private String model;
	
	/**
	 * 出厂编号
	 */
	@Column(name = "FACTORY_NO", length = 64)
	private String factoryNo;
	
	/**
	 * 出厂日期
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "FACTORY_TIME")
	private Timestamp factoryTime;
	
	@Transient
	private List<DeviceAisle> deviceAisles;
	
	@Transient
	private String factoryDevNo;
	
	/**
	 * 点位绑定状态（设备首次安装APP时须绑定点位设备） 0:初始化 1：绑定成功
	 */
	@Transient
	private Integer bindState;
	
	/**
	 * 货道是否可见 0：不可见 1：可见
	 */
	@Column(name = "VISIABLE", length = 8)
	private Integer visiable;
	
	public void addDeviceAisles(DeviceAisle deviceAisle) {
	    if (null == this.deviceAisles)
	        this.deviceAisles = new ArrayList<DeviceAisle>();
	    this.deviceAisles.add(deviceAisle);
	}

	public List<DeviceAisle> getDeviceAisles() {
		return deviceAisles;
	}


	public void setDeviceAisles(List<DeviceAisle> deviceAisles) {
		this.deviceAisles = deviceAisles;
	}


	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(Long deviceId) {
		this.deviceId = deviceId;
	}

	public String getCabinetNo() {
		return cabinetNo;
	}

	public void setCabinetNo(String cabinetNo) {
		this.cabinetNo = cabinetNo;
	}

	public Integer getAisleCount() {
		return aisleCount;
	}

	public void setAisleCount(Integer aisleCount) {
		this.aisleCount = aisleCount;
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

	public Long getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(Long updateUser) {
		this.updateUser = updateUser;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getFactoryNo() {
		return factoryNo;
	}

	public void setFactoryNo(String factoryNo) {
		this.factoryNo = factoryNo;
	}

	public Timestamp getFactoryTime() {
		return factoryTime;
	}

	public void setFactoryTime(Timestamp factoryTime) {
		this.factoryTime = factoryTime;
	}

	public Integer getVisiable() {
		return visiable;
	}

	public void setVisiable(Integer visiable) {
		this.visiable = visiable;
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
		Cabinet other = (Cabinet) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public String getFactoryDevNo() {
		return factoryDevNo;
	}

	public void setFactoryDevNo(String factoryDevNo) {
		this.factoryDevNo = factoryDevNo;
	}

	public Integer getBindState() {
		return bindState;
	}

	public void setBindState(Integer bindState) {
		this.bindState = bindState;
	}
}