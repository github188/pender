package com.vendor.thirdparty.et;

import java.io.Serializable;

public class ETVendingTrack implements Serializable {

	private static final long serialVersionUID = 9151023386674582274L;
	private String assetNo;
	private Integer storeNo;
	private String aisleNo;
	private Integer supplementNo;
	private Integer remainingNo;
	private Integer aisleContainer;
	private String productsNo;
	private String placement;
	private String product_Name;
	private String supplementNoLast;
	private Integer singlePrice;
	private String operTimeString;

	/**
	 * @return the assetNo
	 */
	public String getAssetNo() {
		return assetNo;
	}

	/**
	 * @param assetNo the assetNo to set
	 */
	public void setAssetNo(String assetNo) {
		this.assetNo = assetNo;
	}

	/**
	 * @return the storeNo
	 */
	public Integer getStoreNo() {
		return storeNo;
	}

	/**
	 * @param storeNo the storeNo to set
	 */
	public void setStoreNo(Integer storeNo) {
		this.storeNo = storeNo;
	}

	/**
	 * @return the aisleNo
	 */
	public String getAisleNo() {
		return aisleNo;
	}

	/**
	 * @param aisleNo the aisleNo to set
	 */
	public void setAisleNo(String aisleNo) {
		this.aisleNo = aisleNo;
	}

	/**
	 * @return the supplementNo
	 */
	public Integer getSupplementNo() {
		return supplementNo;
	}

	/**
	 * @param supplementNo the supplementNo to set
	 */
	public void setSupplementNo(Integer supplementNo) {
		this.supplementNo = supplementNo;
	}

	/**
	 * @return the remainingNo
	 */
	public Integer getRemainingNo() {
		return remainingNo;
	}

	/**
	 * @param remainingNo the remainingNo to set
	 */
	public void setRemainingNo(Integer remainingNo) {
		this.remainingNo = remainingNo;
	}

	/**
	 * @return the aisleContainer
	 */
	public Integer getAisleContainer() {
		return aisleContainer;
	}

	/**
	 * @param aisleContainer the aisleContainer to set
	 */
	public void setAisleContainer(Integer aisleContainer) {
		this.aisleContainer = aisleContainer;
	}

	/**
	 * @return the productsNo
	 */
	public String getProductsNo() {
		return productsNo;
	}

	/**
	 * @param productsNo the productsNo to set
	 */
	public void setProductsNo(String productsNo) {
		this.productsNo = productsNo;
	}

	/**
	 * @return the placement
	 */
	public String getPlacement() {
		return placement;
	}

	/**
	 * @param placement the placement to set
	 */
	public void setPlacement(String placement) {
		this.placement = placement;
	}

	/**
	 * @return the product_Name
	 */
	public String getProduct_Name() {
		return product_Name;
	}

	/**
	 * @param product_Name the product_Name to set
	 */
	public void setProduct_Name(String product_Name) {
		this.product_Name = product_Name;
	}

	/**
	 * @return the supplementNoLast
	 */
	public String getSupplementNoLast() {
		return supplementNoLast;
	}

	/**
	 * @param supplementNoLast the supplementNoLast to set
	 */
	public void setSupplementNoLast(String supplementNoLast) {
		this.supplementNoLast = supplementNoLast;
	}

	/**
	 * @return the singlePrice
	 */
	public Integer getSinglePrice() {
		return singlePrice;
	}

	/**
	 * @param singlePrice the singlePrice to set
	 */
	public void setSinglePrice(Integer singlePrice) {
		this.singlePrice = singlePrice;
	}

	/**
	 * @return the operTimeString
	 */
	public String getOperTimeString() {
		return operTimeString;
	}

	/**
	 * @param operTimeString the operTimeString to set
	 */
	public void setOperTimeString(String operTimeString) {
		this.operTimeString = operTimeString;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ETDeviceProdReponse [assetNo=" + assetNo + ", storeNo=" + storeNo + ", aisleNo=" + aisleNo + ", supplementNo=" + supplementNo + ", remainingNo="
				+ remainingNo + ", aisleContainer=" + aisleContainer + ", productsNo=" + productsNo + ", placement=" + placement + ", product_Name=" + product_Name
				+ ", supplementNoLast=" + supplementNoLast + ", singlePrice=" + singlePrice + ", operTimeString=" + operTimeString + "]";
	}

}
