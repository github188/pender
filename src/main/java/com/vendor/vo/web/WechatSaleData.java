package com.vendor.vo.web;

import java.util.List;

import com.ecarry.core.util.MathUtil;
import com.vendor.po.Order;

/**
 * 销售数据
 * @author liujia 2016年11月7日
 */
public class WechatSaleData {
	/**
	 * 总销售额
	 */
	private Double allSalesAmount = 0.0;
	
	/**
	 * 总销售量
	 */
	private Integer allSalesVolume = 0;
	
	/**
	 * 平均单价
	 */
	private Double allAveragePrice = 0.0;

	/**
	 * 销售额，销售量，平均单价明细
	 */
	private List<Order> orderList;

	public Double getAllSalesAmount() {
		return MathUtil.round(null == this.allSalesAmount ? 0 : this.allSalesAmount, 2);
	}

	public void setAllSalesAmount(Double allSalesAmount) {
		this.allSalesAmount = allSalesAmount;
	}

	public Integer getAllSalesVolume() {
		return allSalesVolume;
	}

	public void setAllSalesVolume(Integer allSalesVolume) {
		this.allSalesVolume = allSalesVolume;
	}

	public Double getAllAveragePrice() {
		return MathUtil.round(null == this.allAveragePrice ? 0 : this.allAveragePrice, 2);
	}

	public void setAllAveragePrice(Double allAveragePrice) {
		this.allAveragePrice = allAveragePrice;
	}

	public List<Order> getOrderList() {
		return orderList;
	}

	public void setOrderList(List<Order> orderList) {
		this.orderList = orderList;
	}

}
