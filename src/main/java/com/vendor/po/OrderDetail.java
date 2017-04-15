package com.vendor.po;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.beans.BeanUtils;

import com.ecarry.core.util.MathUtil;
import com.ecarry.core.web.core.ContextUtil;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.vendor.service.IDictionaryService;
import com.vendor.util.Commons;
import com.vendor.util.VendingUtil;

/**
 * 订单明细
 * @author dranson on 2015年12月1日
 */
@Entity
@Table(name = "T_ORDER_DETAIL")
@JsonFilter("com.vendor.po.OrderDetail")
public class OrderDetail implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;
	/**
	 * 所属订单
	 */
	@Column(name = "ORDER_NO", length = 32, nullable = false)
	private String orderNo;
	/**
	 * 所属商家
	 */
	@Column(name = "ORG_ID", length = 8, nullable = false)
	private Long orgId;
	/**
	 * 单价
	 */
	@Column(name = "PRICE", length = 8, nullable = false)
	private Double price;
	/**
	 * 购买数量
	 */
	@Column(name = "QTY", length = 4, nullable = false)
	private Integer qty;
	/**
	 * 币别
	 */
	@Column(name = "CURRENCY", length = 4, nullable = false)
	private String currency;
	/**
	 * 商品ID
	 */
	@Column(name = "SKU_ID", length = 8, nullable = false)
	private Long skuId;
	/**
	 * 商品规格
	 */
	@Column(name = "PRODUCT_MODEL", length = 32)
	private String productModel;
	/**
	 * 订单时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "CREATE_TIME", nullable = false)
	private Timestamp createTime;
	
	/** 抽奖商品编号 */
	@Column(name = "LOTTERY_PRODUCT_NO", length = 50)
	private String lotteryProductNo;
	
	@Transient
	private String sku;
	
	@Transient
	private String skuName;

	@Transient
	private String spec;
	
	@Transient
	private Double priceLocal;
	
	@Transient
	private Integer stock;
	
	@Transient
	private Double weight;

	@Transient
	private String orgName;
	
	@Transient
	private String orgEmail;
	
	@Transient
	private String idCard;
	
	@Transient
	private String consignee;
	
	@Transient
	private String phone;
	
	@Transient
	private String address;
	
	@Transient
	private String zip;
	
	@Transient
	private String country;
	
	@Transient
	private Long cartId;
	
	@Transient
	private String image;
	
	@Transient
	private String error;
	
	@Transient
	private String salePrice;
	
	@Transient
	private Integer saleNumber;
	
	@Transient
	private String code;

	/**
	 * 商品编码
	 */
	@Transient
	private String productCode;

	/**
	 * 金额
	 */
	@Transient
	private Double amount;

	/**
	 * 交易日期
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Transient
	private Timestamp payTime;
	
	/**
	 * 支付流水号
	 */
	@Transient
	private String payCode;
	
	/**
	 * 厂商设备编号
	 * ****/
	@Transient
	private String factoryDevNo;
	
	/**
	 * 店铺地址
	 */
	@Transient
	private String pointAddress;

	/**
	 * openId
	 */
	@Transient
	private String openId;
	
	/**
	 * 订单类型  0：普通订单  1：限时打折  2：抽奖活动
	 */
	@Column(name = "ORDER_TYPE", length = 4)
	private Integer orderType;
	
	@Transient
	private String orderTypeDesc;
	
	@Transient
	private static final Map<Integer, String> ORDER_TYPE_MAP = new HashMap<Integer, String>();
	static {
		ORDER_TYPE_MAP.put(Commons.ORDER_TYPE_COMMON, "普通订单");
		ORDER_TYPE_MAP.put(Commons.ORDER_TYPE_DISCOUNT, "限时打折");
		ORDER_TYPE_MAP.put(Commons.ORDER_TYPE_LOTTERY, "抽奖活动");
	}
	
	public String getOrderTypeDesc() {
        this.orderTypeDesc = ORDER_TYPE_MAP.get(this.orderType);
        return this.orderTypeDesc;
    }
	
	/**
	 * 折扣
	 */
	@Column(name = "DISCOUNT", length = 8)
	private Double discount;
	
	@Transient
	private String discountDesc;
	
	/**
	 * 退货数量
	 */
	@Transient
	private Integer refundQty;

	/**
	 * 退款金额
	 */
	@Transient
	private Double refundAmount;
	
	/**
	 * 退款原因
	 */
	@Transient
	private String refundReason;
	
	public String getDiscountDesc() {
		return (null == this.discount || 1 == this.discount) ? "无" : this.discount + "";
	}

	public Integer getOrderType() {
		return orderType;
	}

	public void setOrderType(Integer orderType) {
		this.orderType = orderType;
	}
	
	public Double getDiscount() {
		return MathUtil.round(null == this.discount ? 0 : this.discount, 2);
	}

	public void setDiscount(Double discount) {
		this.discount = discount;
	}

	public OrderDetail() {}
	
	public OrderDetail(String content) {
		BeanUtils.copyProperties(ContextUtil.getTByJson(OrderDetail.class, content), this);
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public Long getOrgId() {
		return this.orgId;
	}

	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}

	public Double getPrice() {
		return this.price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public Integer getQty() {
		return this.qty;
	}

	public void setQty(Integer qty) {
		this.qty = qty;
	}

	public Long getSkuId() {
		return this.skuId;
	}

	public void setSkuId(Long skuId) {
		this.skuId = skuId;
	}

	public String getProductModel() {
		return productModel;
	}

	public void setProductModel(String productModel) {
		this.productModel = productModel;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public Double getWeight() {
		return weight;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
	}

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public String getSkuName() {
		return skuName;
	}

	public void setSkuName(String skuName) {
		this.skuName = skuName;
	}

	public Double getPriceLocal() {
		if (this.priceLocal == null && this.price != null && this.currency != null) {
			if (Commons.CURRENCY_LOCAL.equals(this.currency))
				return this.price;
			IDictionaryService dictionaryService = ContextUtil.getBeanByName(IDictionaryService.class, "dictionaryService");
			if (dictionaryService != null) {
				Currency currency = dictionaryService.findCurrencyByCode(this.currency);
				if (currency != null)
					return MathUtil.divMax(0, this.price, currency.getRate());
			}
		}
		return priceLocal;
	}

	public void setPriceLocal(Double priceLocal) {
		this.priceLocal = priceLocal;
	}

	public Integer getStock() {
		return stock;
	}

	public void setStock(Integer stock) {
		this.stock = stock;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getOrgEmail() {
		return orgEmail;
	}

	public void setOrgEmail(String orgEmail) {
		this.orgEmail = orgEmail;
	}

	public String getIdCard() {
		return idCard;
	}

	public void setIdCard(String idCard) {
		this.idCard = idCard;
	}

	public String getConsignee() {
		return consignee;
	}

	public void setConsignee(String consignee) {
		this.consignee = consignee;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getZip() {
		return zip;
	}

	public void setZip(String zip) {
		this.zip = zip;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public Long getCartId() {
		return cartId;
	}

	public void setCartId(Long cartId) {
		this.cartId = cartId;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}
    
	

	/**
	 * @return the salePrice
	 */
	public String getSalePrice() {
		if(this.salePrice!=null&&this.salePrice.length()>4)
			return VendingUtil.df.format(Double.parseDouble(this.salePrice));
		return salePrice;
	}
	
	/**
	 * @param salePrice the salePrice to set
	 */
	public void setSalePrice(String salePrice) {
		this.salePrice = salePrice;
	}

	/**
	 * @return the saleNumber
	 */
	public Integer getSaleNumber() {
		return saleNumber;
	}

	/**
	 * @param saleNumber the saleNumber to set
	 */
	public void setSaleNumber(Integer saleNumber) {
		this.saleNumber = saleNumber;
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

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public Double getAmount() {
		return amount;
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public Timestamp getPayTime() {
		return payTime;
	}

	public void setPayTime(Timestamp payTime) {
		this.payTime = payTime;
	}

	public String getPayCode() {
		return payCode;
	}

	public void setPayCode(String payCode) {
		this.payCode = payCode;
	}

	public String getFactoryDevNo() {
		return factoryDevNo;
	}

	public void setFactoryDevNo(String factoryDevNo) {
		this.factoryDevNo = factoryDevNo;
	}

	public String getPointAddress() {
		return pointAddress;
	}

	public void setPointAddress(String pointAddress) {
		this.pointAddress = pointAddress;
	}

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}
	
	public Integer getRefundQty() {
		return refundQty;
	}

	public void setRefundQty(Integer refundQty) {
		this.refundQty = refundQty;
	}

	public Double getRefundAmount() {
		return refundAmount;
	}

	public String getSpec() {
		return spec;
	}

	public void setSpec(String spec) {
		this.spec = spec;
	}

	public void setRefundAmount(Double refundAmount) {
		this.refundAmount = refundAmount;
	}

	public String getRefundReason() {
		return refundReason;
	}

	public void setRefundReason(String refundReason) {
		this.refundReason = refundReason;
	}


	public String getLotteryProductNo() {
		return lotteryProductNo;
	}

	public void setLotteryProductNo(String lotteryProductNo) {
		this.lotteryProductNo = lotteryProductNo;
	}

	/**
	 * 初始化默认值
	 */
	public void initDefaultValue() {
		if (this.orderType == null)
			this.orderType = Commons.ORDER_TYPE_COMMON;
		if (this.discount == null)
			this.discount = 0d;
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
		OrderDetail other = (OrderDetail) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}