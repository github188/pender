package com.vendor.vo.app;

/**
 * 屏幕广告信息
 */
public class VAdvertisement {
	
	/**
	 * 广告ID
	 */
	private Long advId;
	
	/**
	 * 广告名称
	 */
	private String advName;

	/**
	 * 播放顺序(轮播排序)
	 */
	private Integer advIndex;

	/**
	 * 广告位置  1：锁屏上部  2：锁屏下部  3：锁屏背景
	 */
	private Integer advPosition;

	/**
	 * 广告类型  1：默认广告  2：普通广告
	 */
	private Integer type;

	/**
	 * 投放时间开始日期
	 */
	private String startDate = "";
	
	/**
	 * 投放时间结束时间
	 */
	private String endDate = "";

	/**
	 * 投放时间开始时间
	 */
	private String beginTime = "";
	
	/**
	 * 投放时间结束时间
	 */
	private String endTime = "";
	
	/**
	 * 投放屏幕类型  1:横屏  2:竖屏
	 */
	private Integer screenType;

	/**
	 * 广告图片/视频  URL
	 */
	private String fileUrls;

	public String getAdvName() {
		return advName;
	}

	public void setAdvName(String advName) {
		this.advName = advName;
	}

	public Integer getAdvPosition() {
		return advPosition;
	}

	public void setAdvPosition(Integer advPosition) {
		this.advPosition = advPosition;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
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

	public Integer getScreenType() {
		return screenType;
	}

	public void setScreenType(Integer screenType) {
		this.screenType = screenType;
	}

	public String getFileUrls() {
		return fileUrls;
	}

	public void setFileUrls(String fileUrls) {
		this.fileUrls = fileUrls;
	}

	public Long getAdvId() {
		return advId;
	}

	public void setAdvId(Long advId) {
		this.advId = advId;
	}

	public Integer getAdvIndex() {
		return advIndex;
	}

	public void setAdvIndex(Integer advIndex) {
		this.advIndex = advIndex;
	}

}
