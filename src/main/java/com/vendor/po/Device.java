package com.vendor.po;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.vendor.po.observer.DeviceLogObserver;
import com.vendor.util.Commons;

/**
 * 设备
 * 
 * @author zhaoss on 2016年12月1日
 */
@Entity
@Table(name = "T_DEVICE")
@JsonFilter("com.vendor.po.Device")
public class Device extends Observable implements Serializable, Comparable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;
	/**
	 * 设备编号
	 */
	@Column(name = "DEV_NO", length = 32)
	private String devNo;
	/**
	 * 设备地址
	 */
	@Column(name = "ADDRESS", length = 128)
	private String address;
	/**
	 * 设备性质
	 */
	@Column(name = "NATRUE", length = 16)
	private Integer natrue;
	/**
	 * 设备状态
	 */
	@Column(name = "STATE", length = 4)
	private Integer state;
	/**
	 * 所属机构
	 */
	@Column(name = "ORG_ID", length = 8, nullable = false)
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
	 * 修改人
	 */
	@Column(name = "UPDATE_USER", length = 8)
	private Long updateUser;

	/**
	 * 货道数
	 */
	@Column(name = "AISLE_COUNT")
	private Integer aisleCount;

	@Transient
	private Integer aisleCounts;

	/**
	 * 生产厂家
	 */
	@Column(name = "MANUFACTURER")
	private String manufacturer;

	/**
	 * 设备组合模式
	 */
	@Column(name = "COMBINATION_NO")
	private Integer combinationNo;

	/**
	 * 修改时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "UPDATE_TIME")
	private Timestamp updateTime;

	/**
	 * 所属人名称
	 */
	@Transient
	private String orgName;
	/**
	 * 销售额
	 */
	@Transient
	private Double salePrice;

	/**
	 * 销售数量
	 */
	@Transient
	private Long saleNumber;

	/**
	 * 设备类型 1：饮料机 2：弹簧机
	 */
	@Column(name = "TYPE", length = 4)
	private Integer type;

	/**
	 * 点位ID
	 */
	@Column(name = "POINT_ID", length = 8)
	private Long pointId;

	/**
	 * 点位绑定状态（设备首次安装APP时须绑定点位设备） 0:初始化 1：绑定成功
	 */
	@Column(name = "bind_state", length = 4)
	private Integer bindState;
	
	@Transient
	private Integer deviceStatus;

	@Transient
	private List<DeviceAisle> deviceAisles;

	@Transient
	private List<Product> productList = new ArrayList<Product>();
	
	@Transient
	List<LotteryProduct> lotteryProductList = new ArrayList<LotteryProduct>();

	/**
	 * 点位地址
	 */
	@Transient
	private String pointAddress;
	
	
	@Transient
	private String pointName;

	/**
	 * 点位编号
	 */
	@Transient
	private String pointNo;

	@Transient
	private String pointType;

	@Transient
	private String images;

	/**
	 * 型号
	 */
	@Transient
	private String model;

	/**
	 * 型号备用字段，当型号是【CVM-SPG64】或【CVM-SPG40】时，统一返回【CVM-SPG】
	 */
	@Transient
	private String gridModel;

	/**
	 * 出厂编号
	 */
	@Column(name = "FACTORY_NO", length = 64)
	private String factoryNo;

	/**
	 * 版本号
	 */
	@Column(name = "VERSION", length = 32)
	private String version;

	/**
	 * 设备类型，excel导入用
	 */
	@Transient
	private String typeStr;

	/**
	 * 出厂日期，excel导入用
	 */
	@Transient
	private String factoryTimeStr;

	/**
	 * 货柜编号，excel导入用
	 */
	@Transient
	private String cabinetNo;

	@Transient
	private List<Cabinet> cabinets;

	/**
	 * 广告ID,查询用
	 */
	@Transient
	private Long advertiseId;

	@Transient
	private String factoryDevNo;

	/** 抽奖活动选中的设备:1是已选中,0:未选中 */
	@Transient
	private String sort;

	/**
	 * 是否离线 true:离线 false:在线
	 */
	@Transient
	private Boolean isOffLine;
	
	/**
	 * 货道是否可见 0：不可见 1：可见，导入excel用
	 */
	@Transient
	private Integer visiable;


	/**
	 * 当前网络类型(0:wifi,1:4g,2:3g,3:2g,4:网线)
	 */
	@Transient
	private Integer netWorkType;

	/**
	 * 软件版本号
	 */
	@Transient
	private String softwareVersion;

	/**
	 * 固件版本号
	 */
	@Transient
	private String firmwareVersion;

	/**
	 * 占用内存
	 */
	@Transient
	private Long memory;


	/**
	 * 售空货道数量
	 */
	@Transient
	private Integer saleEmptyQty;


	/**
	 * 省
	 */
	@Transient
	private String prov;

	/**
	 * 市
	 */
	@Transient
	private String city;

	/**
	 * 区
	 */
	@Transient
	private String dist;


	public void addCainets(Cabinet cabinet) {
		if (this.cabinets == null)
			this.cabinets = new ArrayList<Cabinet>();
		this.cabinets.add(cabinet);
	}

	public List<Cabinet> getCabinets() {
		return cabinets;
	}

	public void setCabinets(List<Cabinet> cabinets) {
		this.cabinets = cabinets;
	}

	public List<DeviceAisle> getDeviceAisles() {
		return deviceAisles;
	}

	public void setDeviceAisles(List<DeviceAisle> deviceAisles) {
		this.deviceAisles = deviceAisles;
	}

	public Integer getDeviceStatus() {
		return deviceStatus;
	}

	public void setDeviceStatus(Integer deviceStatus) {
		this.deviceStatus = deviceStatus;
	}

	public List<Product> getProductList() {
		return productList;
	}

	public void setProductList(List<Product> productList) {
		this.productList = productList;
	}

	public List<LotteryProduct> getLotteryProductList() {
		return lotteryProductList;
	}

	public void setLotteryProductList(List<LotteryProduct> lotteryProductList) {
		this.lotteryProductList = lotteryProductList;
	}

	public String getImages() {
		return images;
	}

	public void setImages(String images) {
		this.images = images;
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPointName() {
		return pointName;
	}

	public void setPointName(String pointName) {
		this.pointName = pointName;
	}

	public Double getSalePrice() {
		return salePrice;
	}

	public void setSalePrice(Double salePrice) {
		this.salePrice = salePrice;
	}

	public String getDevNo() {
		return devNo;
	}

	public void setDevNo(String devNo) {
		this.devNo = devNo;
	}

	public Long getOrgId() {
		return orgId;
	}

	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public Integer getNatrue() {
		return natrue;
	}

	public void setNatrue(Integer natrue) {
		this.natrue = natrue;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public void changeState(Integer state) {
		state = null == state ? Commons.NORMAL : state;
		this.state = state;

		setChanged();

		// 登记观察者
		new DeviceLogObserver(this);// 设备日志观察者

		notifyObservers(this);
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


	/**
	 * @return the orgName
	 */
	public String getOrgName() {
		return orgName;
	}

	/**
	 * @param orgName the orgName to set
	 */
	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	/**
	 * @return the saleNumber
	 */
	public Long getSaleNumber() {
		return saleNumber;
	}

	/**
	 * @param saleNumber the saleNumber to set
	 */
	public void setSaleNumber(Long saleNumber) {
		this.saleNumber = saleNumber;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Long getPointId() {
		return pointId;
	}

	public void setPointId(Long pointId) {
		this.pointId = pointId;
	}

	public void initDefaultValue() {
		if (null == this.pointId)
			this.pointId = 0L;
		if (null == this.bindState)
			this.bindState = 0;
	}

	public String getPointAddress() {
		return pointAddress;
	}

	public void setPointAddress(String pointAddress) {
		this.pointAddress = pointAddress;
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

	public String getTypeStr() {
		return typeStr;
	}

	public void setTypeStr(String typeStr) {
		this.typeStr = typeStr;
	}

	public String getFactoryTimeStr() {
		return factoryTimeStr;
	}

	public void setFactoryTimeStr(String factoryTimeStr) {
		this.factoryTimeStr = factoryTimeStr;
	}

	public String getCabinetNo() {
		return cabinetNo;
	}

	public void setCabinetNo(String cabinetNo) {
		this.cabinetNo = cabinetNo;
	}

	public String getGridModel() {
		if (null != model && (Commons.DEVICE_MODEL_GRID64.equals(model) || Commons.DEVICE_MODEL_GRID40.equals(model) || Commons.DEVICE_MODEL_GRID60.equals(model)))
			return Commons.DEVICE_MODEL_GRID;
		if (null != model && (Commons.DEVICE_MODEL_SPRING.equals(model) || Commons.DEVICE_MODEL_CATERPILLAR.equals(model)))
			return Commons.DEVICE_MODEL_SPRING_CATERPILLAR;
		return model;
	}

	public void setGridModel(String gridModel) {
		this.gridModel = gridModel;
	}

	public Integer getBindState() {
		return bindState;
	}

	public void setBindState(Integer bindState) {
		this.bindState = bindState;
	}

	public Long getAdvertiseId() {
		return advertiseId;
	}

	public void setAdvertiseId(Long advertiseId) {
		this.advertiseId = advertiseId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cabinetNo == null) ? 0 : cabinetNo.hashCode());
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
		Device other = (Device) obj;
		if (cabinetNo == null) {
			if (other.cabinetNo != null)
				return false;
		} else if (!cabinetNo.equals(other.cabinetNo))
			return false;
		// 不比id了
		// if (id == null) {
		// if (other.id != null)
		// return false;
		// } else if (!id.equals(other.id))
		// return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type)) {
			return false;
		}

		return true;
	}

	public Integer getAisleCount() {
		return aisleCount;
	}

	public void setAisleCount(Integer asileCount) {
		this.aisleCount = asileCount;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	@Override
	public int compareTo(Object obj) {
		if (Integer.valueOf(this.cabinetNo) > Integer.valueOf(((Device) obj).cabinetNo)) {
			return -1;
		} else if (Integer.valueOf(this.cabinetNo) == Integer.valueOf(((Device) obj).cabinetNo)) {
			return 0;
		} else {
			return 1;
		}
	}

	public Integer getCombinationNo() {
		return combinationNo;
	}

	public void setCombinationNo(Integer combinationNo) {
		this.combinationNo = combinationNo;
	}

	public String getFactoryDevNo() {
		return factoryDevNo;
	}

	public void setFactoryDevNo(String factoryDevNo) {
		this.factoryDevNo = factoryDevNo;
	}

	public Integer getAisleCounts() {
		return aisleCounts;
	}

	public void setAisleCounts(Integer aisleCounts) {
		this.aisleCounts = aisleCounts;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public String getPointType() {
		return pointType;
	}

	public void setPointType(String pointType) {
		this.pointType = pointType;
	}

	public String getPointNo() {
		return pointNo;
	}

	public void setPointNo(String pointNo) {
		this.pointNo = pointNo;
	}

	public Boolean getIsOffLine() {
		return isOffLine;
	}

	public void setIsOffLine(Boolean isOffLine) {
		this.isOffLine = isOffLine;
	}

	public Integer getVisiable() {
		return visiable;
	}

	public void setVisiable(Integer visiable) {
		this.visiable = visiable;
	}

	public Integer getNetWorkType() {
		return netWorkType;
	}

	public void setNetWorkType(Integer netWorkType) {
		this.netWorkType = netWorkType;
	}

	public String getSoftwareVersion() {
		return softwareVersion;
	}

	public void setSoftwareVersion(String softwareVersion) {
		this.softwareVersion = softwareVersion;
	}

	public String getFirmwareVersion() {
		return firmwareVersion;
	}

	public void setFirmwareVersion(String firmwareVersion) {
		this.firmwareVersion = firmwareVersion;
	}

	public Long getMemory() {
		return memory;
	}

	public void setMemory(Long memory) {
		this.memory = memory;
	}

	public Integer getSaleEmptyQty() {
		return saleEmptyQty;
	}

	public void setSaleEmptyQty(Integer saleEmptyQty) {
		this.saleEmptyQty = saleEmptyQty;
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
}