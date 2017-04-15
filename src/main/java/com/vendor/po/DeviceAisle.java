package com.vendor.po;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.vendor.util.Commons;

/**
 * 设备货道
 */
@Entity
@Table(name = "T_DEVICE_AISLE")
@JsonFilter("com.vendor.po.DeviceAisle")
public class DeviceAisle implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;
	/**
	 * 货道号
	 */
	@Column(name = "AISLE_NUM", length = 8, nullable = false)
	private Integer aisleNum;
	/**
	 * 产品ID
	 */
	@Column(name = "PRODUCT_ID", length = 8)
	private Long productId;
	/**
	 * 零售价
	 */
	@Column(name = "PRICE", length = 8)
	private Double price;
	
	/**
	 * 折扣值
	 */
	@Column(name = "DISCOUNT_VALUE", length = 8)
	private Double discountValue;
	
	/**
	 * 库存
	 */
	@Column(name = "STOCK", length = 8, nullable = false)
	private Integer stock;
	/**
	 * 库存提醒
	 */
	@Column(name = "STOCK_REMIND", length = 8)
	private Integer stockRemind;
	/**
	 * 设备ID
	 */
	@Column(name = "DEVICE_ID", length = 8)
	private Long deviceId;

	/**
	 * 货柜ID
	 */
	@Column(name = "CABINET_ID", length = 8)
	private Long cabinetId;
	/**
	 * 货道容量
	 */
	@Column(name = "CAPACITY", length = 8, nullable = false)
	private Integer capacity;
	/**
	 * 销量
	 */
	@Column(name = "SALES", length = 8, nullable = false)
	private Integer sales;
	/**
	 * 创建人
	 */
	@Column(name = "CREATE_USER", length = 8, nullable = false)
	private Long createUser;
	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "CREATE_TIME", nullable = false)
	private Timestamp createTime;
	/**
	 * 修改人
	 */
	@Column(name = "UPDATE_USER", length = 8)
	private Long updateUser;
	/**
	 * 修改时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "UPDATE_TIME")
	private Timestamp updateTime;
	/**
	 * 商品名称
	 */
	@Column(name = "PRODUCT_NAME", length = 100)
	private String productName;
	/**
	 * 商品编码
	 */
	@Column(name = "PRODUCT_CODE", length = 30)
	private String productCode;
	
	/**
	 * 待同步价格
	 */
	@Column(name = "PRICE_ON_LINE")
	private Double priceOnLine;
	
	/**
	 * 是否可售  0：不可售  1：可售
	 */
	@Column(name = "SELLABLE", length = 8)
	private Integer sellable;

	public Double getPriceOnLine() {
		return priceOnLine;
	}

	public void setPriceOnLine(Double priceOnLine) {
		this.priceOnLine = priceOnLine;
	}

	/**
	 * 商品图片
	 */
	@Transient
	private String images;
	/**
	 * 销售额
	 */
	@Transient
	private Double salesVolume;
	/** (虚拟商品字段)区分0:实物,1:虚拟商品 */
	@Transient
    private Integer trueOrFalse;
	/**
	 * 销售额
	 */
	@Transient
	private Double priceOne;
	/**
	 * 销售额
	 */
	@Transient
	private Double priceTwo;
	
	/**
	 * 应补货数量
	 */
	@Column(name = "SUPPLEMENT_NO", length = 8)
	private Integer supplementNo;


	/** 商品排序序号(用于商品前端置顶排序功能) */
	@Transient
	private Integer serialNumber;

	/** 置顶时间(用于商品前端置顶排序功能) */
	@Transient
	private Timestamp stickTime;
	
	/**
	 * 设备地址
	 */
	@Transient
	private String address;

	/**
	 * 设备编号
	 */
	@Transient
	private String code;

	/** (商品规格字段)商品组号(用于关联统一规格的商品) */
	@Transient
	private String productCombination;

	/**
	 * 是否是显示的主商品 0不是 1 是
	 */
	@Transient
	private Integer wetherSpuMainGoods;

	@Transient
	private Integer beverageMachine;

	@Transient
	private String cabinetNo;

	@Transient
	private String model;
	
	/**
	 * 设备出货指令用货柜号
	 */
	@Transient
	private String shipmentCabinetNo;

	/**
	 * 设备出货指令用货道号
	 */
	@Transient
	private String shipmentAisleNum;
	
	/**
	 * 商品周长   单位：mm
	 */
	@Transient
	private Integer perimeter;

	/**
	 * 商品类型 1：自有  2：平台供货
	 */
	@Transient
	private Integer type;
	
	@Transient
	private String typeDesc;
	
	@Transient
	private static final Map<Integer, String> TYPE_MAP = new HashMap<Integer, String>();
	static {
		TYPE_MAP.put(Commons.PROD_TYPE_SELF, "自有");
		TYPE_MAP.put(Commons.PROD_TYPE_PLATFORM, "平台供货");
	}
	
	public String getTypeDesc() {
        this.typeDesc = TYPE_MAP.get(this.type);
        return this.typeDesc;
    }
	
	
	/**
	 * 补货清单：总剩余库存
	 */
	@Transient
	private Integer totalStock;

	/**
	 * 补货清单：总应补货数量
	 */
	@Transient
	private Integer totalSupplementNo;

	/**
	 * 补货清单：总货道容量
	 */
	@Transient
	private Integer totalCapacity;
	
	/**
	 * 产地
	 */
	@Transient
	private String original;

	/**
	 * 规格
	 */
	@Transient
	private String spec;
	
	/**
	 * 商品详细介绍
	 */
	@Transient
	private String desc;
	
	/**
	 * 商品类别  1饮料 2小吃
	 */
	@Transient
	private Integer category;

	/**
	 * 货道长度, 单位mm
	 */
	@Transient
	private Integer roadLength;
	
	/**
	 * 店铺信息
	 */
	@Transient
	private List<PointPlace> pointPlaces;
	
	/**
	 * 设备出厂编号
	 */
	@Transient
	private String factoryDevNo;

	/**
	 * 推荐商品编号
	 */
	@Transient
	private String recommendProductCode;

	/**
	 * 推荐商品价格（组合价）
	 */
	@Transient
	private Double recomPrice;

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Double getPriceOne() {
		return priceOne;
	}

	public void setPriceOne(Double priceOne) {
		this.priceOne = priceOne;
	}

	public Double getPriceTwo() {
		return priceTwo;
	}

	public void setPriceTwo(Double priceTwo) {
		this.priceTwo = priceTwo;
	}

	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

	public String getImages() {
		return images;
	}

	public void setImages(String images) {
		this.images = images;
	}

	public Double getSalesVolume() {
		return salesVolume;
	}

	public void setSalesVolume(Double salesVolume) {
		this.salesVolume = salesVolume;
	}

	public Integer getSales() {
		return sales;
	}

	public void setSales(Integer sales) {
		this.sales = sales;
	}

	public Integer getAisleNum() {
		return aisleNum;
	}

	public void setAisleNum(Integer aisleNum) {
		this.aisleNum = aisleNum;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Integer getStock() {
		return stock;
	}

	public void setStock(Integer stock) {
		this.stock = stock;
	}

	public Integer getStockRemind() {
		return stockRemind;
	}

	public void setStockRemind(Integer stockRemind) {
		this.stockRemind = stockRemind;
	}

	public Long getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(Long deviceId) {
		this.deviceId = deviceId;
	}

	public Integer getCapacity() {
		return capacity;
	}

	public void setCapacity(Integer capacity) {
		this.capacity = capacity;
	}

	public Long getCreateUser() {
		return createUser;
	}

	public void setCreateUser(Long createUser) {
		this.createUser = createUser;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public Timestamp getUpdateTime() {
		return this.updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	public Long getUpdateUser() {
		return this.updateUser;
	}

	public void setUpdateUser(Long updateUser) {
		this.updateUser = updateUser;
	}

	public Integer getSerialNumber() {
		return serialNumber;
	}

	public void setSerialNumber(Integer serialNumber) {
		this.serialNumber = serialNumber;
	}

	public Timestamp getStickTime() {
		return stickTime;
	}

	public void setStickTime(Timestamp stickTime) {
		this.stickTime = stickTime;
	}

	/**
	 * @return the productName
	 */
	public String getProductName() {
		return productName;
	}

	/**
	 * @param productName
	 *            the productName to set
	 */
	public void setProductName(String productName) {
		this.productName = productName;
	}

	/**
	 * @return the supplementNo
	 */
	public Integer getSupplementNo() {
		return supplementNo;
	}

	/**
	 * @param supplementNo the supplementNo to set
	 */
	public void setSupplementNo(Integer supplementNo) {
		this.supplementNo = supplementNo;
	}

    public Integer getTrueOrFalse() {
        return trueOrFalse;
    }

    public void setTrueOrFalse(Integer trueOrFalse) {
        this.trueOrFalse = trueOrFalse;
    }

    /**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * @param address the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @return the beverageMachine
	 */
	public Integer getBeverageMachine() {
		return beverageMachine;
	}

	/**
	 * @param beverageMachine the beverageMachine to set
	 */
	public void setBeverageMachine(Integer beverageMachine) {
		this.beverageMachine = beverageMachine;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	public Long getCabinetId() {
		return cabinetId;
	}

	public void setCabinetId(Long cabinetId) {
		this.cabinetId = cabinetId;
	}

	public String getCabinetNo() {
		return cabinetNo;
	}

	public void setCabinetNo(String cabinetNo) {
		this.cabinetNo = cabinetNo;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getShipmentCabinetNo() {
		return shipmentCabinetNo;
	}

	public void setShipmentCabinetNo(String shipmentCabinetNo) {
		this.shipmentCabinetNo = shipmentCabinetNo;
	}

	public String getShipmentAisleNum() {
		return shipmentAisleNum;
	}

	public void setShipmentAisleNum(String shipmentAisleNum) {
		this.shipmentAisleNum = shipmentAisleNum;
	}

	public Integer getPerimeter() {
		return perimeter;
	}

	public void setPerimeter(Integer perimeter) {
		this.perimeter = perimeter;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Integer getTotalStock() {
		return totalStock;
	}

	public void setTotalStock(Integer totalStock) {
		this.totalStock = totalStock;
	}

	public Integer getTotalSupplementNo() {
		return totalSupplementNo;
	}

	public void setTotalSupplementNo(Integer totalSupplementNo) {
		this.totalSupplementNo = totalSupplementNo;
	}

	public String getOriginal() {
		return original;
	}

	public void setOriginal(String original) {
		this.original = original;
	}

	public String getSpec() {
		return spec;
	}

	public void setSpec(String spec) {
		this.spec = spec;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public Integer getCategory() {
		return category;
	}

	public void setCategory(Integer category) {
		this.category = category;
	}

	public Integer getRoadLength() {
		return roadLength;
	}

	public void setRoadLength(Integer roadLength) {
		this.roadLength = roadLength;
	}

	public Integer getSellable() {
		return sellable;
	}

	public void setSellable(Integer sellable) {
		this.sellable = sellable;
	}

	public List<PointPlace> getPointPlaces() {
		return pointPlaces;
	}

	public void setPointPlaces(List<PointPlace> pointPlaces) {
		this.pointPlaces = pointPlaces;
	}

	public Double getDiscountValue() {
		return discountValue;
	}

	public void setDiscountValue(Double discountValue) {
		this.discountValue = discountValue;
	}

	public String getProductCombination() {
		return productCombination;
	}

	public void setProductCombination(String productCombination) {
		this.productCombination = productCombination;
	}

	public String getRecommendProductCode() {
		return recommendProductCode;
	}

	public void setRecommendProductCode(String recommendProductCode) {
		this.recommendProductCode = recommendProductCode;
	}

	public Double getRecomPrice() {
		return recomPrice;
	}

	public void setRecomPrice(Double recomPrice) {
		this.recomPrice = recomPrice;
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
		DeviceAisle other = (DeviceAisle) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}