package com.vendor.thirdparty.et;

import java.io.Serializable;
import java.util.List;

/**
 * 易触同步订单对象
 * 
 * @author liujia on 2016年5月30日
 */
public class ETOrder implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<ETSyncOrder> orders;

	/**
	 * @return the orders
	 */
	public List<ETSyncOrder> getOrders() {
		return orders;
	}

	/**
	 * @param orders the orders to set
	 */
	public void setOrders(List<ETSyncOrder> orders) {
		this.orders = orders;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ETOrder [orders=" + orders + "]";
	}

}
