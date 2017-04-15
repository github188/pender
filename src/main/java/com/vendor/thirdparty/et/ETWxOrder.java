/**
 * 
 */
package com.vendor.thirdparty.et;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFilter;

/**
 * @author dranson on 2016年6月3日
 */
@JsonFilter("com.vendor.po.ETOrder")
public class ETWxOrder implements Serializable {

	private Integer amount;
	
	private String orderid;
	
	private Integer goodsid;
	
	private String openid;
	
	private String transid;
	
	private String borderid;
	
	private String liaodaohao;
	
	private Integer machineid;
	
	private String partner;
	
	private Integer dazhe;
	
	private Integer changetype;
	
	private String appid;
	
	private Long id;
	
	private String subpartner;
	
	private Integer danjia;
	
	private String guihao;
	
	private String status;
	
	private ETTime ctime;

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	public String getOrderid() {
		return orderid;
	}

	public void setOrderid(String orderid) {
		this.orderid = orderid;
	}

	public Integer getGoodsid() {
		return goodsid;
	}

	public void setGoodsid(Integer goodsid) {
		this.goodsid = goodsid;
	}

	public String getOpenid() {
		return openid;
	}

	public void setOpenid(String openid) {
		this.openid = openid;
	}

	public String getTransid() {
		return transid;
	}

	public void setTransid(String transid) {
		this.transid = transid;
	}

	public String getBorderid() {
		return borderid;
	}

	public void setBorderid(String borderid) {
		this.borderid = borderid;
	}

	public String getLiaodaohao() {
		return liaodaohao;
	}

	public void setLiaodaohao(String liaodaohao) {
		this.liaodaohao = liaodaohao;
	}

	public Integer getMachineid() {
		return machineid;
	}

	public void setMachineid(Integer machineid) {
		this.machineid = machineid;
	}

	public String getPartner() {
		return partner;
	}

	public void setPartner(String partner) {
		this.partner = partner;
	}

	public Integer getDazhe() {
		return dazhe;
	}

	public void setDazhe(Integer dazhe) {
		this.dazhe = dazhe;
	}

	public Integer getChangetype() {
		return changetype;
	}

	public void setChangetype(Integer changetype) {
		this.changetype = changetype;
	}

	public String getAppid() {
		return appid;
	}

	public void setAppid(String appid) {
		this.appid = appid;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSubpartner() {
		return subpartner;
	}

	public void setSubpartner(String subpartner) {
		this.subpartner = subpartner;
	}

	public Integer getDanjia() {
		return danjia;
	}

	public void setDanjia(Integer danjia) {
		this.danjia = danjia;
	}

	public String getGuihao() {
		return guihao;
	}

	public void setGuihao(String guihao) {
		this.guihao = guihao;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public ETTime getCtime() {
		return ctime;
	}

	public void setCtime(ETTime ctime) {
		this.ctime = ctime;
	}
}
