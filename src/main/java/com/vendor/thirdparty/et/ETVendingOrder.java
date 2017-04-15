package com.vendor.thirdparty.et;

import java.io.Serializable;

public class ETVendingOrder implements Serializable {

	private static final long serialVersionUID = 1L;
	private Integer id;
	private String company_id;
	private String company_name;
	private String area_id;
	private String area_name;
	private String line_id;
	private String line_name;
	private String f_id;
	private String asset_no;
	private String products_no;
	private String products_name;
	private Integer store_no;
	private Integer aisle_no;
	private Integer sold_count;
	private Double single_price;
	private Double cost_price;
	private Integer pay_type;
	private Integer cash_type;
	private Long sold_time;
	private Long oper_time;
	private String card_no;
	private String serial_no;
	private String sequence_no;

	public ETVendingOrder() {

	}

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the company_id
	 */
	public String getCompany_id() {
		return company_id;
	}

	/**
	 * @param company_id the company_id to set
	 */
	public void setCompany_id(String company_id) {
		this.company_id = company_id;
	}

	/**
	 * @return the company_name
	 */
	public String getCompany_name() {
		return company_name;
	}

	/**
	 * @param company_name the company_name to set
	 */
	public void setCompany_name(String company_name) {
		this.company_name = company_name;
	}

	/**
	 * @return the area_id
	 */
	public String getArea_id() {
		return area_id;
	}

	/**
	 * @param area_id the area_id to set
	 */
	public void setArea_id(String area_id) {
		this.area_id = area_id;
	}

	/**
	 * @return the area_name
	 */
	public String getArea_name() {
		return area_name;
	}

	/**
	 * @param area_name the area_name to set
	 */
	public void setArea_name(String area_name) {
		this.area_name = area_name;
	}

	/**
	 * @return the line_id
	 */
	public String getLine_id() {
		return line_id;
	}

	/**
	 * @param line_id the line_id to set
	 */
	public void setLine_id(String line_id) {
		this.line_id = line_id;
	}

	/**
	 * @return the line_name
	 */
	public String getLine_name() {
		return line_name;
	}

	/**
	 * @param line_name the line_name to set
	 */
	public void setLine_name(String line_name) {
		this.line_name = line_name;
	}

	/**
	 * @return the f_id
	 */
	public String getF_id() {
		return f_id;
	}

	/**
	 * @param f_id the f_id to set
	 */
	public void setF_id(String f_id) {
		this.f_id = f_id;
	}

	/**
	 * @return the asset_no
	 */
	public String getAsset_no() {
		return asset_no;
	}

	/**
	 * @param asset_no the asset_no to set
	 */
	public void setAsset_no(String asset_no) {
		this.asset_no = asset_no;
	}

	/**
	 * @return the products_no
	 */
	public String getProducts_no() {
		return products_no;
	}

	/**
	 * @param products_no the products_no to set
	 */
	public void setProducts_no(String products_no) {
		this.products_no = products_no;
	}

	/**
	 * @return the products_name
	 */
	public String getProducts_name() {
		return products_name;
	}

	/**
	 * @param products_name the products_name to set
	 */
	public void setProducts_name(String products_name) {
		this.products_name = products_name;
	}

	/**
	 * @return the store_no
	 */
	public Integer getStore_no() {
		return store_no;
	}

	/**
	 * @param store_no the store_no to set
	 */
	public void setStore_no(Integer store_no) {
		this.store_no = store_no;
	}

	/**
	 * @return the aisle_no
	 */
	public Integer getAisle_no() {
		return aisle_no;
	}

	/**
	 * @param aisle_no the aisle_no to set
	 */
	public void setAisle_no(Integer aisle_no) {
		this.aisle_no = aisle_no;
	}

	/**
	 * @return the sold_count
	 */
	public Integer getSold_count() {
		return sold_count;
	}

	/**
	 * @param sold_count the sold_count to set
	 */
	public void setSold_count(Integer sold_count) {
		this.sold_count = sold_count;
	}

	/**
	 * @return the single_price
	 */
	public Double getSingle_price() {
		return single_price;
	}

	/**
	 * @param single_price the single_price to set
	 */
	public void setSingle_price(Double single_price) {
		this.single_price = single_price;
	}

	/**
	 * @return the cost_price
	 */
	public Double getCost_price() {
		return cost_price;
	}

	/**
	 * @param cost_price the cost_price to set
	 */
	public void setCost_price(Double cost_price) {
		this.cost_price = cost_price;
	}

	/**
	 * @return the pay_type
	 */
	public Integer getPay_type() {
		return pay_type;
	}

	/**
	 * @param pay_type the pay_type to set
	 */
	public void setPay_type(Integer pay_type) {
		this.pay_type = pay_type;
	}

	/**
	 * @return the cash_type
	 */
	public Integer getCash_type() {
		return cash_type;
	}

	/**
	 * @param cash_type the cash_type to set
	 */
	public void setCash_type(Integer cash_type) {
		this.cash_type = cash_type;
	}

	/**
	 * @return the sold_time
	 */
	public Long getSold_time() {
		return sold_time;
	}

	/**
	 * @param sold_time the sold_time to set
	 */
	public void setSold_time(Long sold_time) {
		this.sold_time = sold_time;
	}

	/**
	 * @return the oper_time
	 */
	public Long getOper_time() {
		return oper_time;
	}

	/**
	 * @param oper_time the oper_time to set
	 */
	public void setOper_time(Long oper_time) {
		this.oper_time = oper_time;
	}

	/**
	 * @return the card_no
	 */
	public String getCard_no() {
		return card_no;
	}

	/**
	 * @param card_no the card_no to set
	 */
	public void setCard_no(String card_no) {
		this.card_no = card_no;
	}

	/**
	 * @return the serial_no
	 */
	public String getSerial_no() {
		return serial_no;
	}

	/**
	 * @param serial_no the serial_no to set
	 */
	public void setSerial_no(String serial_no) {
		this.serial_no = serial_no;
	}

	/**
	 * @return the sequence_no
	 */
	public String getSequence_no() {
		return sequence_no;
	}

	/**
	 * @param sequence_no the sequence_no to set
	 */
	public void setSequence_no(String sequence_no) {
		this.sequence_no = sequence_no;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ETVendingOrder [company_id=" + company_id + ", company_name=" + company_name + ", area_id=" + area_id + ", area_name=" + area_name + ", line_id=" + line_id
				+ ", line_name=" + line_name + ", f_id=" + f_id + ", asset_no=" + asset_no + ", products_no=" + products_no + ", products_name=" + products_name + ", store_no="
				+ store_no + ", aisle_no=" + aisle_no + ", sold_count=" + sold_count + ", single_price=" + single_price + ", cost_price=" + cost_price + ", pay_type=" + pay_type
				+ ", cash_type=" + cash_type + ", sold_time=" + sold_time + ", oper_time=" + oper_time + ", card_no=" + card_no + ", serial_no=" + serial_no + ", sequence_no="
				+ sequence_no + "]";
	}

}
