package com.vendor.po;

import java.io.Serializable;
import java.sql.Timestamp;
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
 * 点位信息
 * 
 */
@Entity
@Table(name = "T_POINT_PLACE")
@JsonFilter("com.vendor.po.PointPlace")
public class PointPlace implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;
	/**
	 * 点位编号
	 */
	@Column(name = "POINT_NO", length = 16)
	private String pointNo;
	/**
	 * 点位名称
	 */
	@Column(name = "POINT_NAME", length = 64)
	private String pointName;
	/**
	 * 点位地址
	 */
	@Column(name = "POINT_ADDRESS", length = 256)
	private String pointAddress;
	/**
	 * 点位类型  1小区，2学校，3写字楼，4酒店，5医院，6火车站/高铁站，7汽车站，8机场，9地铁站，10运动场，11会所
	 */
	@Column(name = "POINT_TYPE", length = 4)
	private Integer pointType;
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
	 * 修改时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "UPDATE_TIME")
	private Timestamp updateTime;
	
	/**
	 * 折扣
	 */
	@Column(name = "DISCOUNT", length = 8)
	private Double discount;
	
	/**
	 * 省
	 */
	@Column(name = "PROV", length = 16)
	private String prov;
	
	/**
	 * 市
	 */
	@Column(name = "CITY", length = 16)
	private String city;
	
	/**
	 * 区
	 */
	@Column(name = "DIST", length = 16)
	private String dist;

	/**
	 * 经纬度
	 */
	@Column(name = "LATITUDE_LONGITUDE", length = 32)
	private String latitudeLongitude;
	
	/**
	 * 人流量
	 */
	@Column(name = "HUMAN_TRAFFIC", length = 16)
	private Integer humanTraffic;

	/**
	 * 性质  1：开放式  2：封闭式  3：半封闭式
	 */
	@Column(name = "NATURE", length = 4)
	private Integer nature;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	/**
	 * 店铺所属人
	 */
	@Column(name = "USER_ID", length = 8,nullable = true)
	private Long userId;
		
	/**
	 * 所属人名称
	 */
	@Transient
	private String orgName;
	
	/**
	 * 店铺商品补货清单
	 */
	@Transient
	private List<DeviceAisle> deviceAisles;
	
	@Transient
	private String text;

	/**
	 * 店铺零售价
	 */
	@Transient
	private Double priceOnLine;
	
	/**
	 * 是否可售  0：不可售  1：可售
	 */
	@Transient
	private Integer sellable;

	/**
	 * 打折活动1:已选,0未选中
	 */
	@Transient
	private Integer checked;
	
	/**
	 * 店铺补货时间
	 */
	@Transient
	private List<PointReplenishTime> pointReplenishTimes;
	
	@Transient
	private List<Device> devices;
	
	/**
	 * 店铺状态    1:默认; 9:删除
	 */
	@Column(name = "STATE", length = 2)
	private Integer state;
	
	/**
	 * 是否包含管理组织（下级所有有管理关系的组织）   1：是    0：否
	 */
	@Transient
	private Integer containSubOrg;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPointNo() {
		return pointNo;
	}

	public void setPointNo(String pointNo) {
		this.pointNo = pointNo;
	}

	public String getPointName() {
		return pointName;
	}

	public void setPointName(String pointName) {
		this.pointName = pointName;
	}

	public String getPointAddress() {
		return pointAddress;
	}

	public void setPointAddress(String pointAddress) {
		this.pointAddress = pointAddress;
	}

	public Integer getPointType() {
		return pointType;
	}

	public void setPointType(Integer pointType) {
		this.pointType = pointType;
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

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public List<DeviceAisle> getDeviceAisles() {
		return deviceAisles;
	}

	public void setDeviceAisles(List<DeviceAisle> deviceAisles) {
		this.deviceAisles = deviceAisles;
	}

	public String getText() {
		return this.pointName;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Double getDiscount() {
		return discount;
	}

	public void setDiscount(Double discount) {
		this.discount = discount;
	}

	public Double getPriceOnLine() {
		return priceOnLine;
	}

	public void setPriceOnLine(Double priceOnLine) {
		this.priceOnLine = priceOnLine;
	}

	public Integer getSellable() {
		return sellable;
	}

	public void setSellable(Integer sellable) {
		this.sellable = sellable;
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

	public String getLatitudeLongitude() {
		return latitudeLongitude;
	}

	public void setLatitudeLongitude(String latitudeLongitude) {
		this.latitudeLongitude = latitudeLongitude;
	}

	public Integer getHumanTraffic() {
		return humanTraffic;
	}

	public void setHumanTraffic(Integer humanTraffic) {
		this.humanTraffic = humanTraffic;
	}

	public Integer getNature() {
		return nature;
	}

	public void setNature(Integer nature) {
		this.nature = nature;
	}

	public List<PointReplenishTime> getPointReplenishTimes() {
		return pointReplenishTimes;
	}

	public void setPointReplenishTimes(List<PointReplenishTime> pointReplenishTimes) {
		this.pointReplenishTimes = pointReplenishTimes;
	}
	
	public Integer getChecked() {
		return checked;
	}

	public void setChecked(Integer checked) {
		this.checked = checked;
	}

	public List<Device> getDevices() {
		return devices;
	}

	public void setDevices(List<Device> devices) {
		this.devices = devices;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public Integer getContainSubOrg() {
		return containSubOrg;
	}

	public void setContainSubOrg(Integer containSubOrg) {
		this.containSubOrg = containSubOrg;
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
		PointPlace other = (PointPlace) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}