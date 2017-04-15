package com.vendor.po;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.util.StringUtils;

import com.ecarry.core.util.MathUtil;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.vendor.util.Commons;

/**
 * 订单表
 * @author dranson on 2015年12月1日
 */
@Entity
@Table(name = "T_ORDER")
@JsonFilter("com.vendor.po.Order")
public class Order implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;
	/**
	 * 订单金额
	 */
	@Column(name = "AMOUNT", length = 8, nullable = false)
	private Double amount;
	/**
	 * 订单编号
	 */
	@Column(name = "CODE", length = 32, nullable = false)
	private String code;
	/**
	 * 抵扣券
	 */
	@Column(name = "COUPONE", length = 8, nullable = false)
	private Double coupone;
	/**
	 * 优惠金额，折扣
	 */
	@Column(name = "DISCOUNT", length = 8, nullable = false)
	private Double discount;
	/**
	 * 积分抵扣金额
	 */
	@Column(name = "FEE_SCORE", length = 8)
	private Double feeScore;
	/**
	 * 订单时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "CREATE_TIME", nullable = false)
	private Timestamp createTime;
	/**
	 * 买家ID
	 */
	@Column(name = "USER_ID", length = 8, nullable = false)
	private Long userId;
	/**
	 * 买家用户名
	 */
	@Column(name = "USERNAME", length = 128, nullable = false)
	private String username;
	/**
	 * 所属商家
	 */
	@Column(name = "ORG_ID", length = 8, nullable = false)
	private Long orgId;
	/**
	 * 支付流水号
	 */
	@Column(name = "PAY_CODE", length = 64)
	private String payCode;
	/**
	 * 付款时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "PAY_TIME")
	private Timestamp payTime;
	/**
	 * 订单状态
	 */
	@Column(name = "STATE", length = 2, nullable = false)
	private Integer state;
	/**
	 * 备注
	 */
	@Column(name = "REMARK", length = 256)
	private String remark;

	@Column(name = "VERSION", length = 8)
	private Long version;
	
	/**
	 * 设备编号
	 * ****/
	@Column(name = "DEVICE_NO", length = 20)
	private String deviceNo;

	/**
	 * 店铺编号
	 * ****/
	@Column(name = "POINT_NO", length = 20)
	private String pointNo;
	
	/**
	 * 支付类型  6 微信 1支付宝
	 * ***/
	@Column(name = "PAY_TYPE", length = 4)
	private Integer payType;
	
	@Transient
	private String sku;

	@Transient
	private String skuName;
	
	@Transient
	private String orgName;
	
	@Transient
	private String orgLogo;

	@Transient
	private String category;
	
	@Transient
	private String details;
	
	@Transient
	private List<Order> orders;
	
	@Transient
	private List<OrderDetail> orderDetails;
	
	@Transient
	private List<Device> deviceList;
	
	@Transient
	private Long lotteryProductId;
	
	/** 抽奖活动零售价 */ 
	@Transient
	private Double lotteryPrice;
	
	/** 平均零售价 */ 
	@Transient
	private Double lotteryMeanPrice;
	
	/**
	 * 下单总数
	 */
	@Transient
	private Integer totalOrdersNumber = 0;
	
	/**
	 * 成交件数
	 */
	@Transient
	private Integer dealQuantity = 0;
	
	/**
	 * 下单总金额
	 */
	@Transient
	private Double totalOrderAmount = 0.0;
	
	/**
	 * 成交总金额
	 */
	@Transient
	private Double clinchDealOrder = 0.0;
	
	/**
	 * 新用户成交订单数
	 */
	@Transient
	private Integer newClinchDealOrders = 0;
	
	/**
	 * 新用户成交总金额
	 */
	@Transient
	private Double newClinchDealAmount = 0.0;
	
	/**
	 * 新用户数
	 */
	@Transient
	private Integer newUserNumber = 0;
	
	/**
	 * 人均成交订单数
	 */
	@Transient
	private Integer capitaOrders = 0;
	
	/**
	 * 人均成交商品数
	 */
	@Transient
	private Integer capitaProducts = 0;
	
	/**
	 * 人均成交金额
	 */
	@Transient
	private Double capitaAmount = 0.0;
	
	/**
	 * 成交用户数
	 */
	@Transient
	private Integer totalCustom = 0;
	
	/**
	 * 成交商品数
	 */
	@Transient
	private Integer totalProductNum = 0;
	
	/**
	 * 商品销售额
	 */
	@Transient
	private Double totalPrice = 0.0;
	
	/**
	 * 成交笔数
	 */
	@Transient
	private Integer dealNumber = 0;
	
	/**
	 * 日期，格式：yyyy-MM-dd
	 */
	@Transient
	private String date;
	
	/**
	 * 销售额
	 */
	@Transient
	private Double salesAmount = 0.0;
	
	/**
	 * 销售量
	 */
	@Transient
	private Integer salesVolume = 0;

	/**
	 * 购物车销售额
	 */
	@Transient
	private Double cartSalesAmount = 0.0;
	
	/**
	 * 购物车销售量
	 */
	@Transient
	private Integer cartSalesVolume = 0;
	
	/**
	 * 商品名称
	 */
	@Transient
	private String productName;
	
	/**
	 * 店铺名称
	 */
	@Transient
	private String pointName;
	
	/**
	 * 省
	 */
	@Transient
	private String prov = "";
	/**
	 * 市
	 */
	@Transient
	private String city = "";
	/**
	 * 区
	 */
	@Transient
	private String dist = "";
	/**
	 * 区域：省+市+区
	 */
	@Transient
	private String area = "";
	
	/**
	 * 该订单已退款金额
	 */
	@Transient
	private Double refundAmount = 0.0;

	/**
	 * 该订单已退款数量
	 */
	@Transient
	private Integer refundQty = 0;
	
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

	@Transient
	private String name;
	
	/**
	 * 平均单价
	 */
	@Transient
	private Double averagePrice = 0.0;

	/**
	 * 购物车平均单价
	 */
	@Transient
	private Double cartAveragePrice = 0.0;
	
	/**
	 * 销售额占比
	 */
	@Transient
	private Double salesRate = 0.0;
	@Transient
	private String salesRateStr = "";
	
	/**
	 * 购物车销售额占比
	 */
	@Transient
	private Double cartSalesRate = 0.0;
	@Transient
	private String cartSalesRateStr = "";

	/**
	 * 销售量占比
	 */
	@Transient
	private Double salesVolumeRate = 0.0;
	@Transient
	private String salesVolumeRateStr = "";

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Double getAmount() {
		return MathUtil.round(null == this.amount ? 0 : this.amount, 2);
	}

	public void setAmount(Double amount) {
		this.amount = amount;
	}

	public String getCode() {
		return this.code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Double getCoupone() {
		return this.coupone;
	}

	public void setCoupone(Double coupone) {
		this.coupone = coupone;
	}

	public Double getDiscount() {
		return this.discount;
	}

	public void setDiscount(Double discount) {
		this.discount = discount;
	}

	public Double getFeeScore() {
		return this.feeScore;
	}

	public void setFeeScore(Double feeScore) {
		this.feeScore = feeScore;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Long getOrgId() {
		return this.orgId;
	}

	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}

	public String getPayCode() {
		return this.payCode;
	}

	public void setPayCode(String payCode) {
		this.payCode = payCode;
	}

	public Timestamp getPayTime() {
		return this.payTime;
	}

	public void setPayTime(Timestamp payTime) {
		this.payTime = payTime;
	}

	public Integer getState() {
		return this.state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Long getVersion() {
		return this.version;
	}

	public void setVersion(Long version) {
		this.version = version;
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

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public String getOrgLogo() {
		return orgLogo;
	}

	public void setOrgLogo(String orgLogo) {
		this.orgLogo = orgLogo;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getDetails() {
		return details;
	}

	public void setDetails(String details) {
		this.details = details;
	}

	public List<Order> getOrders() {
		return orders;
	}

	public void setOrders(List<Order> orders) {
		this.orders = orders;
	}
	
	public Integer getTotalCustom() {
		return totalCustom;
	}

	public void setTotalCustom(Integer totalCustom) {
		this.totalCustom = totalCustom;
	}

	public Double getTotalPrice() {
		return totalPrice;
	}

	public void setTotalPrice(Double totalPrice) {
		this.totalPrice = totalPrice;
	}

	public Integer getDealQuantity() {
		return dealQuantity;
	}

	public void setDealQuantity(Integer dealQuantity) {
		this.dealQuantity = dealQuantity;
	}

	public Integer getDealNumber() {
		return dealNumber;
	}

	public void setDealNumber(Integer dealNumber) {
		this.dealNumber = dealNumber;
	}

	public void addOrder(Order order) {
		if (this.orders == null)
			this.orders = new ArrayList<Order>();
		this.orders.add(order);
	}

	public List<OrderDetail> getOrderDetails() {
		return orderDetails;
	}

	public void setOrderDetails(List<OrderDetail> orderDetails) {
		this.orderDetails = orderDetails;
	}

	public void addOrderDetail(OrderDetail orderDetail) {
		if (this.orderDetails == null)
			this.orderDetails = new ArrayList<OrderDetail>();
		this.orderDetails.add(orderDetail);
	}

	public List<Device> getDeviceList() {
		return deviceList;
	}

	public void setDeviceList(List<Device> deviceList) {
		this.deviceList = deviceList;
	}

	/**
	 * @return the deviceNo
	 */
	public String getDeviceNo() {
		return deviceNo;
	}

	/**
	 * @param deviceNo the deviceNo to set
	 */
	public void setDeviceNo(String deviceNo) {
		this.deviceNo = deviceNo;
	}

	/**
	 * @return the payType
	 */
	public Integer getPayType() {
		return payType;
	}

	/**
	 * @param payType the payType to set
	 */
	public void setPayType(Integer payType) {
		this.payType = payType;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public Double getSalesAmount() {
		return MathUtil.round(null == salesAmount ? 0.0 : salesAmount, 2);
	}

	public void setSalesAmount(Double salesAmount) {
		this.salesAmount = salesAmount;
	}

	public Integer getSalesVolume() {
		return salesVolume;
	}

	public void setSalesVolume(Integer salesVolume) {
		this.salesVolume = salesVolume;
	}

	public Double getCartSalesAmount() {
		return MathUtil.round(null == cartSalesAmount ? 0.0 : cartSalesAmount, 2);
	}

	public void setCartSalesAmount(Double cartSalesAmount) {
		this.cartSalesAmount = cartSalesAmount;
	}

	public Integer getCartSalesVolume() {
		return cartSalesVolume;
	}

	public void setCartSalesVolume(Integer cartSalesVolume) {
		this.cartSalesVolume = cartSalesVolume;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getPointName() {
		return pointName;
	}

	public void setPointName(String pointName) {
		this.pointName = pointName;
	}

	public Double getRefundAmount() {
		return MathUtil.round(null == this.refundAmount ? 0 : this.refundAmount, 2);
	}

	public void setRefundAmount(Double refundAmount) {
		this.refundAmount = refundAmount;
	}

	public String getFactoryDevNo() {
		return factoryDevNo;
	}

	public void setFactoryDevNo(String factoryDevNo) {
		this.factoryDevNo = factoryDevNo;
	}

	public String getPointNo() {
		return pointNo;
	}

	public void setPointNo(String pointNo) {
		this.pointNo = pointNo;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPointAddress() {
		return pointAddress;
	}

	public void setPointAddress(String pointAddress) {
		this.pointAddress = pointAddress;
	}

	public Integer getTotalOrdersNumber() {
		return totalOrdersNumber;
	}

	public Double getLotteryPrice() {
		return lotteryPrice;
	}

	public void setLotteryPrice(Double lotteryPrice) {
		this.lotteryPrice = lotteryPrice;
	}

	public Double getLotteryMeanPrice() {
		return lotteryMeanPrice;
	}

	public void setLotteryMeanPrice(Double lotteryMeanPrice) {
		this.lotteryMeanPrice = lotteryMeanPrice;
	}

	public void setTotalOrdersNumber(Integer totalOrdersNumber) {
		this.totalOrdersNumber = totalOrdersNumber;
	}

	public Double getTotalOrderAmount() {
		return totalOrderAmount;
	}

	public void setTotalOrderAmount(Double totalOrderAmount) {
		this.totalOrderAmount = totalOrderAmount;
	}

	public Double getClinchDealOrder() {
		return clinchDealOrder;
	}

	public void setClinchDealOrder(Double clinchDealOrder) {
		this.clinchDealOrder = clinchDealOrder;
	}

	public Integer getNewClinchDealOrders() {
		return newClinchDealOrders;
	}

	public void setNewClinchDealOrders(Integer newClinchDealOrders) {
		this.newClinchDealOrders = newClinchDealOrders;
	}

	public Double getNewClinchDealAmount() {
		return newClinchDealAmount;
	}

	public void setNewClinchDealAmount(Double newClinchDealAmount) {
		this.newClinchDealAmount = newClinchDealAmount;
	}

	public Integer getNewUserNumber() {
		return newUserNumber;
	}

	public void setNewUserNumber(Integer newUserNumber) {
		this.newUserNumber = newUserNumber;
	}

	public Integer getCapitaOrders() {
		return capitaOrders;
	}

	public Long getLotteryProductId() {
		return lotteryProductId;
	}

	public void setLotteryProductId(Long lotteryProductId) {
		this.lotteryProductId = lotteryProductId;
	}

	public void setCapitaOrders(Integer capitaOrders) {
		this.capitaOrders = capitaOrders;
	}

	public Integer getCapitaProducts() {
		return capitaProducts;
	}

	public void setCapitaProducts(Integer capitaProducts) {
		this.capitaProducts = capitaProducts;
	}

	public Double getCapitaAmount() {
		return capitaAmount;
	}

	public void setCapitaAmount(Double capitaAmount) {
		this.capitaAmount = capitaAmount;
	}

	public Integer getTotalProductNum() {
		return totalProductNum;
	}

	public void setTotalProductNum(Integer totalProductNum) {
		this.totalProductNum = totalProductNum;
	}
	
	public Double getSalesRate() {
		return MathUtil.round(null == this.salesRate ? 0 : this.salesRate, 2);
	}

	public void setSalesRate(Double salesRate) {
		this.salesRate = salesRate;
	}
	
	public Double getCartSalesRate() {
		return MathUtil.round(null == this.cartSalesRate ? 0 : this.cartSalesRate, 2);
	}

	public void setCartSalesRate(Double cartSalesRate) {
		this.cartSalesRate = cartSalesRate;
	}

	public Double getSalesVolumeRate() {
		return MathUtil.round(null == this.salesVolumeRate ? 0 : this.salesVolumeRate, 2);
	}

	public void setSalesVolumeRate(Double salesVolumeRate) {
		this.salesVolumeRate = salesVolumeRate;
	}

	public Double getAveragePrice() {
		return MathUtil.round(null == this.averagePrice ? 0 : this.averagePrice, 2);
	}

	public void setAveragePrice(Double averagePrice) {
		this.averagePrice = averagePrice;
	}

	public Double getCartAveragePrice() {
		return MathUtil.round(null == this.cartAveragePrice ? 0 : this.cartAveragePrice, 2);
	}

	public void setCartAveragePrice(Double cartAveragePrice) {
		this.cartAveragePrice = cartAveragePrice;
	}

	public Integer getRefundQty() {
		return refundQty;
	}

	public void setRefundQty(Integer refundQty) {
		this.refundQty = refundQty;
	}

	public String getProv() {
		return StringUtils.isEmpty(this.prov) ? "" : this.prov;
	}

	public void setProv(String prov) {
		this.prov = prov;
	}

	public String getCity() {
		return StringUtils.isEmpty(this.city) ? "" : this.city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getDist() {
		return StringUtils.isEmpty(this.dist) ? "" : this.dist;
	}

	public void setDist(String dist) {
		this.dist = dist;
	}

	public String getArea() {
		return getProv() + getCity() + getDist();
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getSalesRateStr() {
		return getSalesRate() + "%";
	}

	public void setSalesRateStr(String salesRateStr) {
		this.salesRateStr = salesRateStr;
	}

	public String getCartSalesRateStr() {
		return getCartSalesRate() + "%";
	}

	public void setCartSalesRateStr(String cartSalesRateStr) {
		this.cartSalesRateStr = cartSalesRateStr;
	}

	public String getSalesVolumeRateStr() {
		return getSalesVolumeRate() + "%";
	}

	public void setSalesVolumeRateStr(String salesVolumeRateStr) {
		this.salesVolumeRateStr = salesVolumeRateStr;
	}

	/**
	 * 初始化默认值
	 */
	public void initDefaultValue() {
		if (this.state == null)
			this.state = Commons.ORDER_STATE_NEW;
		if (this.coupone == null)
			this.coupone = 0d;
		if (this.discount == null)
			this.discount = 1d;
		if (this.feeScore == null)
			this.feeScore = 0d;
		if(this.refundAmount==null)
			this.refundAmount=0d;
		
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
		Order other = (Order) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}