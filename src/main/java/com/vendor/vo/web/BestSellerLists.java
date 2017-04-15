package com.vendor.vo.web;

import java.io.Serializable;
import java.util.List;

import com.vendor.po.Order;

public class BestSellerLists implements Serializable {

	private static final long serialVersionUID = 1L;
	
	List<Order> storeSales;
	
	List<Order> productSales;

	/**
	 * 商品销售量排行榜
	 */
	List<Order> productSalesQtyOrders;

	public List<Order> getStoreSales() {
		return storeSales;
	}

	public void setStoreSales(List<Order> storeSales) {
		this.storeSales = storeSales;
	}

	public List<Order> getProductSales() {
		return productSales;
	}

	public void setProductSales(List<Order> productSales) {
		this.productSales = productSales;
	}

	public List<Order> getProductSalesQtyOrders() {
		return productSalesQtyOrders;
	}

	public void setProductSalesQtyOrders(List<Order> productSalesQtyOrders) {
		this.productSalesQtyOrders = productSalesQtyOrders;
	}
	
}
