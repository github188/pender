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
 * 屏幕广告信息
 * @author liujia on 2016年7月12日
 */
@Entity
@Table(name = "T_ADVERTISEMENT")
@JsonFilter("com.vendor.po.Advertisement")
public class Advertisement implements Serializable {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;
	
	/**
	 * 广告名称
	 */
	@Column(name = "ADV_NAME", length = 120)
	private String advName;

	/**
	 * 播放顺序(轮播排序)
	 */
	@Column(name = "INDEX", length = 4)
	private Integer index;

	/**
	 * 广告位置  1：锁屏上部  2：锁屏下部  3：锁屏背景
	 */
	@Column(name = "ADV_POSITION", length = 4)
	private Integer advPosition;

	/**
	 * 状态 0-未开始 1-进行中 2-结束
	 */
	@Column(name = "STATUS", length = 4)
	private Integer status;

	/**
	 * 广告类型  1：默认广告  2：普通广告
	 */
	@Column(name = "TYPE", length = 4)
	private Integer type;

	/**
	 * 机构ID
	 */
	@Column(name = "ORG_ID", length = 4)
	private Long orgId;
	
	/**
	 * 投放时间开始日期
	 */
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
	@Column(name = "START_DATE")
	private Timestamp startDate;
	
	/**
	 * 投放时间结束时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
	@Column(name = "END_DATE")
	private Timestamp endDate;

	/**
	 * 投放时间开始时间
	 */
	@Column(name = "BEGIN_TIME", length = 32)
	private String beginTime;
	
	/**
	 * 投放时间结束时间
	 */
	@Column(name = "END_TIME", length = 32)
	private String endTime;
	
	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "CREATIME")
	private Timestamp creatime;
	
	
	/**
	 * 投放屏幕类型  1:横屏  2:竖屏
	 */
	@Column(name = "SCREEN_TYPE", length = 4)
	private Integer screenType;

	/**
	 * 重复规则 1：周一  2：周二 ... 7：周日  0：不重复
	 */
	@Column(name = "REPEAT", length = 32)
	private String repeat;
	
	/**
	 * 广告图片
	 */
	@Transient
	private String images;
	
	/**
	 * 所投放的店铺
	 */
	@Transient
	private List<PointPlace> pointPlaces; 

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getAdvName() {
		return advName;
	}

	public void setAdvName(String advName) {
		this.advName = advName;
	}

	public Integer getIndex() {
		return index;
	}

	public void setIndex(Integer index) {
		this.index = index;
	}

	public Integer getAdvPosition() {
		return advPosition;
	}

	public void setAdvPosition(Integer advPosition) {
		this.advPosition = advPosition;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Long getOrgId() {
		return orgId;
	}

	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}

	public Timestamp getStartDate() {
		return startDate;
	}

	public void setStartDate(Timestamp startDate) {
		this.startDate = startDate;
	}

	public Timestamp getEndDate() {
		return endDate;
	}

	public void setEndDate(Timestamp endDate) {
		this.endDate = endDate;
	}

	public String getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(String beginTime) {
		this.beginTime = beginTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public Timestamp getCreatime() {
		return creatime;
	}

	public void setCreatime(Timestamp creatime) {
		this.creatime = creatime;
	}

	public Integer getScreenType() {
		return screenType;
	}

	public void setScreenType(Integer screenType) {
		this.screenType = screenType;
	}

	public String getRepeat() {
		return repeat;
	}

	public void setRepeat(String repeat) {
		this.repeat = repeat;
	}

	public String getImages() {
		return images;
	}

	public void setImages(String images) {
		this.images = images;
	}

	public List<PointPlace> getPointPlaces() {
		return pointPlaces;
	}

	public void setPointPlaces(List<PointPlace> pointPlaces) {
		this.pointPlaces = pointPlaces;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
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
		Advertisement other = (Advertisement) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
