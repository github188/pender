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
* @ClassName: 限时打折
* @Description: TODO
* @author: duanyx
* @date: 2016年11月17日 下午2:06:53
*/
@Entity
@Table(name = "T_DISCOUNT")
@JsonFilter("com.vendor.po.Discount")
public class Discount implements Serializable {

	/** ID */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;
	
	/** 活动标题 */
	@Column(name = "TITLE")
	private String title;

	/** 活动开始时间 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "START_TIME")
	private Timestamp startTime;

	/** 活动结束时间 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "END_TIME")
	private Timestamp endTime;
	
	/** 活动下线时间 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "REFERRALS_TIME")
	private Timestamp referralsTime;
	
	
	
	/** 打折方式 1:全天打折,2:分时段打折 */
	@Column(name = "DISCOUNT_WAY")
	private String discountWay;
	
	/** 活动状态 0:未开始,1:开始,2:已结束,3:已下线 ,9:已删除*/
	@Column(name = "STATE")
	private String state;
	
	/** 周期(每周?打折 多个日期之间以","分隔开) */
	@Column(name = "CYCLE")
	private String cycle;

	/** 创建人Id */
	@Column(name = "USER_ID")
	private Long userId;
	
	/** 修改人 */
	@Column(name = "USER_EDIT_ID")
	private Long userEditId;
	
	/** 所属机构 */
	@Column(name = "ORG_ID")
	private Long orgId;
	
	/** 打折类型(1:按店铺,2:按商品) */
	@Column(name = "DISCOUNT_TYPE")
	private String discountType;
	
	/** 是否推送(0:未推送,1:已推送) */
	@Column(name = "PUSH")
	private String push;
	
	/** 商品集合 */
	@Transient
	private List<Product> productList = new ArrayList<Product>();
	
	/** 店铺集合 */
	@Transient
	private List<PointPlace> pointPlaceList = new ArrayList<PointPlace>();
	
	@Transient
	private List<DiscountProductPointPlace> discountPP = new ArrayList<DiscountProductPointPlace>();
	
	/** 时间段集合 */
	@Transient
	private List<DiscountPeriod> discountPeriodList = new ArrayList<DiscountPeriod>();//存放活动时段

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Timestamp getStartTime() {
		return startTime;
	}

	public void setStartTime(Timestamp startTime) {
		this.startTime = startTime;
	}

	public Timestamp getEndTime() {
		return endTime;
	}

	public void setEndTime(Timestamp endTime) {
		this.endTime = endTime;
	}

	public String getDiscountWay() {
		return discountWay;
	}

	public void setDiscountWay(String discountWay) {
		this.discountWay = discountWay;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getCycle() {
		return cycle;
	}

	public void setCycle(String cycle) {
		this.cycle = cycle;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getUserEditId() {
		return userEditId;
	}

	public void setUserEditId(Long userEditId) {
		this.userEditId = userEditId;
	}

	public String getDiscountType() {
		return discountType;
	}

	public void setDiscountType(String discountType) {
		this.discountType = discountType;
	}

	public List<DiscountProductPointPlace> getDiscountPP() {
		return discountPP;
	}

	public void setDiscountPP(List<DiscountProductPointPlace> discountPP) {
		this.discountPP = discountPP;
	}

	public List<DiscountPeriod> getDiscountPeriodList() {
		return discountPeriodList;
	}

	public void setDiscountPeriodList(List<DiscountPeriod> discountPeriodList) {
		this.discountPeriodList = discountPeriodList;
	}
	public List<Product> getProductList() {
		return productList;
	}

	public void setProductList(List<Product> productList) {
		this.productList = productList;
	}

	public List<PointPlace> getPointPlaceList() {
		return pointPlaceList;
	}

	public void setPointPlaceList(List<PointPlace> pointPlaceList) {
		this.pointPlaceList = pointPlaceList;
	}
	public Long getOrgId() {
		return orgId;
	}

	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}

	public String getPush() {
		return push;
	}

	public void setPush(String push) {
		this.push = push;
	}

	public Timestamp getReferralsTime() {
		return referralsTime;
	}

	public void setReferralsTime(Timestamp referralsTime) {
		this.referralsTime = referralsTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cycle == null) ? 0 : cycle.hashCode());
		result = prime * result + ((discountPP == null) ? 0 : discountPP.hashCode());
		result = prime * result + ((discountPeriodList == null) ? 0 : discountPeriodList.hashCode());
		result = prime * result + ((discountType == null) ? 0 : discountType.hashCode());
		result = prime * result + ((discountWay == null) ? 0 : discountWay.hashCode());
		result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((orgId == null) ? 0 : orgId.hashCode());
		result = prime * result + ((pointPlaceList == null) ? 0 : pointPlaceList.hashCode());
		result = prime * result + ((productList == null) ? 0 : productList.hashCode());
		result = prime * result + ((push == null) ? 0 : push.hashCode());
		result = prime * result + ((startTime == null) ? 0 : startTime.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((userEditId == null) ? 0 : userEditId.hashCode());
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
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
		Discount other = (Discount) obj;
		if (cycle == null) {
			if (other.cycle != null)
				return false;
		} else if (!cycle.equals(other.cycle))
			return false;
		if (discountPP == null) {
			if (other.discountPP != null)
				return false;
		} else if (!discountPP.equals(other.discountPP))
			return false;
		if (discountPeriodList == null) {
			if (other.discountPeriodList != null)
				return false;
		} else if (!discountPeriodList.equals(other.discountPeriodList))
			return false;
		if (discountType == null) {
			if (other.discountType != null)
				return false;
		} else if (!discountType.equals(other.discountType))
			return false;
		if (discountWay == null) {
			if (other.discountWay != null)
				return false;
		} else if (!discountWay.equals(other.discountWay))
			return false;
		if (endTime == null) {
			if (other.endTime != null)
				return false;
		} else if (!endTime.equals(other.endTime))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (orgId == null) {
			if (other.orgId != null)
				return false;
		} else if (!orgId.equals(other.orgId))
			return false;
		if (pointPlaceList == null) {
			if (other.pointPlaceList != null)
				return false;
		} else if (!pointPlaceList.equals(other.pointPlaceList))
			return false;
		if (productList == null) {
			if (other.productList != null)
				return false;
		} else if (!productList.equals(other.productList))
			return false;
		if (push == null) {
			if (other.push != null)
				return false;
		} else if (!push.equals(other.push))
			return false;
		if (startTime == null) {
			if (other.startTime != null)
				return false;
		} else if (!startTime.equals(other.startTime))
			return false;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (userEditId == null) {
			if (other.userEditId != null)
				return false;
		} else if (!userEditId.equals(other.userEditId))
			return false;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		return true;
	}

}
