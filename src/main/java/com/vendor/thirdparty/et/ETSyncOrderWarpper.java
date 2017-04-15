package com.vendor.thirdparty.et;

import java.io.Serializable;

public class ETSyncOrderWarpper implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * 订单金额
	 **/
	private Integer amount;
	
	/**
	 * 微信商户订单号
	 * */
	private String orderid;
	
	/**
	 * 商品ID
	 **/
	private String goodsid;
	
	/**
	 * openId
	 * ***/
	private String openid;
	
	/**
	 * 微信支付订单号
	 * */
	private String transid;
	
	/**
	 * 料道号 ==货道号?
	 * **/
	private String liaodaohao;
	/**
	 * 机器号 ==设备编号?
	 * **/
	private Integer machineid;
	
	/**
	 * 柜号 ==货道?或==啥?
	 * **/
	private String guihao;

	/**
	 * @return the amount
	 */
	public Integer getAmount() {
		return amount;
	}

	/**
	 * @param amount the amount to set
	 */
	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	/**
	 * @return the orderid
	 */
	public String getOrderid() {
		return orderid;
	}

	/**
	 * @param orderid the orderid to set
	 */
	public void setOrderid(String orderid) {
		this.orderid = orderid;
	}

	/**
	 * @return the goodsid
	 */
	public String getGoodsid() {
		return goodsid;
	}

	/**
	 * @param goodsid the goodsid to set
	 */
	public void setGoodsid(String goodsid) {
		this.goodsid = goodsid;
	}

	/**
	 * @return the openid
	 */
	public String getOpenid() {
		return openid;
	}

	/**
	 * @param openid the openid to set
	 */
	public void setOpenid(String openid) {
		this.openid = openid;
	}

	/**
	 * @return the transid
	 */
	public String getTransid() {
		return transid;
	}

	/**
	 * @param transid the transid to set
	 */
	public void setTransid(String transid) {
		this.transid = transid;
	}

	/**
	 * @return the liaodaohao
	 */
	public String getLiaodaohao() {
		return liaodaohao;
	}

	/**
	 * @param liaodaohao the liaodaohao to set
	 */
	public void setLiaodaohao(String liaodaohao) {
		this.liaodaohao = liaodaohao;
	}

	/**
	 * @return the machineid
	 */
	public Integer getMachineid() {
		return machineid;
	}

	/**
	 * @param machineid the machineid to set
	 */
	public void setMachineid(Integer machineid) {
		this.machineid = machineid;
	}

	/**
	 * @return the guihao
	 */
	public String getGuihao() {
		return guihao;
	}

	/**
	 * @param guihao the guihao to set
	 */
	public void setGuihao(String guihao) {
		this.guihao = guihao;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ETSyncOrderWarpper [amount=" + amount + ", orderid=" + orderid + ", goodsid=" + goodsid + ", openid="
				+ openid + ", transid=" + transid + ", liaodaohao=" + liaodaohao + ", machineid=" + machineid
				+ ", guihao=" + guihao + "]";
	}
	
	

}
