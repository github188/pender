package com.vendor.vo.app;

import java.io.Serializable;
/**
 * 订单返回信息
 * ***/
public class VendingOrder implements Serializable{
	private String name;
	private String code;
	private Integer total_fee;
	private String device_no;
	private Integer track;
	private String out_trade_no;
	private Integer pay_type;
	private String pay_time;
	private String transaction_id;
	private String user_id;
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}
	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}
	/**
	 * @return the total_fee
	 */
	public Integer getTotal_fee() {
		return total_fee;
	}
	/**
	 * @param total_fee the total_fee to set
	 */
	public void setTotal_fee(Integer total_fee) {
		this.total_fee = total_fee;
	}
	/**
	 * @return the device_no
	 */
	public String getDevice_no() {
		return device_no;
	}
	/**
	 * @param device_no the device_no to set
	 */
	public void setDevice_no(String device_no) {
		this.device_no = device_no;
	}
	/**
	 * @return the track
	 */
	public Integer getTrack() {
		return track;
	}
	/**
	 * @param track the track to set
	 */
	public void setTrack(Integer track) {
		this.track = track;
	}
	/**
	 * @return the out_trade_no
	 */
	public String getOut_trade_no() {
		return out_trade_no;
	}
	/**
	 * @param out_trade_no the out_trade_no to set
	 */
	public void setOut_trade_no(String out_trade_no) {
		this.out_trade_no = out_trade_no;
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
	 * @return the pay_time
	 */
	public String getPay_time() {
		return pay_time;
	}
	/**
	 * @param pay_time the pay_time to set
	 */
	public void setPay_time(String pay_time) {
		this.pay_time = pay_time;
	}
	/**
	 * @return the transaction_id
	 */
	public String getTransaction_id() {
		return transaction_id;
	}
	/**
	 * @param transaction_id the transaction_id to set
	 */
	public void setTransaction_id(String transaction_id) {
		this.transaction_id = transaction_id;
	}
	/**
	 * @return the user_id
	 */
	public String getUser_id() {
		return user_id;
	}
	/**
	 * @param user_id the user_id to set
	 */
	public void setUser_id(String user_id) {
		this.user_id = user_id;
	}
	
	

}
