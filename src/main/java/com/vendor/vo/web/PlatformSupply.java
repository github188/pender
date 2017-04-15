package com.vendor.vo.web;

import java.io.Serializable;
import java.util.List;

import com.vendor.po.Product;

/**
 * 平台供货对象
 */
public class PlatformSupply implements Serializable {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 供货对象的机构Id
	 */
	private Long supplyOrgId;
	
	/**
	 * 供货商品信息
	 */
	private List<Product> products;

	public Long getSupplyOrgId() {
		return supplyOrgId;
	}

	public void setSupplyOrgId(Long supplyOrgId) {
		this.supplyOrgId = supplyOrgId;
	}

	public List<Product> getProducts() {
		return products;
	}

	public void setProducts(List<Product> products) {
		this.products = products;
	}
	
}
