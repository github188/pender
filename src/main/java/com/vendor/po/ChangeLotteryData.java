package com.vendor.po;

/**
 * 打折对象推送对象
 * 
 * @author duanyx
 *
 */
public class ChangeLotteryData {

	private Long Id;
	private String productNo;// : 商品编码
	private Integer num;//数量
//	private Double probabil;//概率
	
	
	
	public Long getId() {
		return Id;
	}
	public void setId(Long id) {
		Id = id;
	}
	public String getProductNo() {
		return productNo;
	}
	public void setProductNo(String productNo) {
		this.productNo = productNo;
	}
	public Integer getNum() {
		return num;
	}
	public void setNum(Integer num) {
		this.num = num;
	}
//	public Double getProbabil() {
//		return probabil;
//	}
//	public void setProbabil(Double probabil) {
//		this.probabil = probabil;
//	}
}
