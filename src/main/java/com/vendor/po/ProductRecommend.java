package com.vendor.po;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonFilter;

/**
* 关联推荐
*/
@Entity
@Table(name = "T_PRODUCT_RECOMMEND")
@JsonFilter("com.vendor.po.ProductRecommend")
public class ProductRecommend implements Serializable {

	/** ID */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;
	
	/**
	 * 机构ID
	 */
	@Column(name = "ORG_ID", length = 8, nullable = false)
	private Long orgId;

	/**
	 * 商品ID
	 */
	@Column(name = "PRODUCT_ID", length = 8, nullable = false)
	private Long productId;

	/**
	 * 关联商品ID
	 */
	@Column(name = "RECOMMEND_PRODUCT_ID", length = 8, nullable = false)
	private Long recommendProductId;
	
	/**
	 * 关联度
	 */
	@Column(name = "RELEVANCY", length = 4, nullable = false)
	private Integer relevancy;
	
	/**
	 * 商品名称
	 */
	@Transient
	private String skuName;

	/**
	 * 关联商品名称
	 */
	@Transient
	private String recommendSkuName;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public Long getRecommendProductId() {
		return recommendProductId;
	}

	public void setRecommendProductId(Long recommendProductId) {
		this.recommendProductId = recommendProductId;
	}

	public Integer getRelevancy() {
		return relevancy;
	}

	public void setRelevancy(Integer relevancy) {
		this.relevancy = relevancy;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProductRecommend other = (ProductRecommend) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public String getSkuName() {
		return skuName;
	}

	public void setSkuName(String skuName) {
		this.skuName = skuName;
	}

	public String getRecommendSkuName() {
		return recommendSkuName;
	}

	public void setRecommendSkuName(String recommendSkuName) {
		this.recommendSkuName = recommendSkuName;
	}

	public Long getOrgId() {
		return orgId;
	}

	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}
	
}
