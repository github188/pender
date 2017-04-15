package com.vendor.vo.app;

/**
 * 推荐商品
 * 
 * @author liujia on 2017年3月23日
 */
public class RecommendGoodsInfo {
	// 主商品编号
	private String mainProductNo;
	
	// 推荐商品编号
	private String recomProductNo;
	
	// 推荐商品图片（商品详情图）
	private String recomPicUrl;
	
	// 推荐商品价格
	private Double recomPrice;
	
	// 商品删除价格(零售价)
	private Double recomDeletePrice;
	
	// 商品可售状态 0：不可售 1：可售
	private Integer state;

	public String getMainProductNo() {
		return mainProductNo;
	}

	public void setMainProductNo(String mainProductNo) {
		this.mainProductNo = mainProductNo;
	}

	public String getRecomProductNo() {
		return recomProductNo;
	}

	public void setRecomProductNo(String recomProductNo) {
		this.recomProductNo = recomProductNo;
	}

	public String getRecomPicUrl() {
		return recomPicUrl;
	}

	public void setRecomPicUrl(String recomPicUrl) {
		this.recomPicUrl = recomPicUrl;
	}

	public Double getRecomPrice() {
		return recomPrice;
	}

	public void setRecomPrice(Double recomPrice) {
		this.recomPrice = recomPrice;
	}

	public Double getRecomDeletePrice() {
		return recomDeletePrice;
	}

	public void setRecomDeletePrice(Double recomDeletePrice) {
		this.recomDeletePrice = recomDeletePrice;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}
}
