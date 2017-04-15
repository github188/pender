package com.vendor.po;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.ecarry.core.util.MathUtil;
import com.ecarry.core.web.core.ContextUtil;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.vendor.service.IDictionaryService;
import com.vendor.util.Commons;

/**
 * 商品信息
 * @author dranson on 2015年12月11日
 */
@Entity
@Table(name = "T_PRODUCT")
@JsonFilter("com.vendor.po.Product")
public class Product implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;
	/**
	 * 配送范围
	 */
	@Column(name = "AREA", length = 256)
	private String area;
	/**
	 * 品牌
	 */
	@Column(name = "BRAND", length = 32)
	private String brand;
	/**
	 * 限购数量,0表示无限购
	 */
	@Column(name = "BUY_LIMIT", length = 4, nullable = false)
	private Integer buyLimit;
	/**
	 * 商家内部编码
	 */
	@Column(name = "CODE", length = 16)
	private String code;
	/**
	 * 是否组合产品,0:否;1:是
	 */
	@Column(name = "COMBO", nullable = false)
	private Boolean combo;
	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "CREATE_TIME", nullable = false)
	private Timestamp createTime;
	/**
	 * 创建人
	 */
	@Column(name = "CREATE_USER", length = 8, nullable = false)
	private Long createUser;
	/**
	 * 保质期
	 */
	@Column(name = "EXPIRATION", length = 2)
	private Integer expiration;
	/**
	 * 高
	 */
	@Column(name = "HEIGHT", length = 4)
	private Integer height;
	/**
	 * 长
	 */
	@Column(name = "LENGTH", length = 4)
	private Integer length;
	/**
	 * 下架时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "OFF_TIME")
	private Timestamp offTime;
	/**
	 * 上架时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "ON_TIME")
	private Timestamp onTime;
	/**
	 * 所属商家
	 */
	@Column(name = "ORG_ID", length = 8, nullable = false)
	private Long orgId;
	/**
	 * 原产地
	 */
	@Column(name = "ORIGIN", length = 32)
	private String origin;
	/**
	 * 零售价
	 */
	@Column(name = "PRICE", length = 8, nullable = false)
	private Double price;
	/**
	 * 最高价
	 */
	@Column(name = "PRICE_MAX", length = 8)
	private Double priceMax;
	
	/**
	 * 一级渠道价
	 * ***/
	@Column(name = "PRICE_ONE", length = 8)
	private Double priceOne;
	
	/**
	 * 二级渠道价
	 * ***/
	@Column(name = "PRICE_TWO", length = 8)
	private Double priceTwo;
	/**
	 * 币别
	 */
	@Column(name = "CURRENCY", length = 4, nullable = false)
	private String currency;
	/**
	 * 税率
	 */
	@Column(name = "TAX_RATE", length = 8, nullable = false)
	private Double taxRate;
	/**
	 * 成分
	 */
	@Column(name = "INGREDIENT", length = 128, nullable = false)
	private String ingredient;
	/**
	 * 备案名称
	 */
	@Column(name = "REGIST_NAME", length = 128)
	private String registName;
	/**
	 * 图文描述
	 */
	@Column(name = "DESCRIPTION", length = 1024)
	private String description;
	/**
	 * 驳回原因
	 */
	@Column(name = "REASON", length = 256)
	private String reason;
	/**
	 * 备注
	 */
	@Column(name = "REMARK", length = 256)
	private String remark;
	/**
	 * SKU
	 */
	@Column(name = "SKU", length = 32, nullable = false)
	private String sku;
	/**
	 * 商品名称
	 */
	@Column(name = "SKU_NAME", length = 128, nullable = false)
	private String skuName;
	/**
	 * 在线状态,1:上架;0:下架
	 */
	@Column(name = "STATE", length = 2, nullable = false)
	private Integer state;
	/**
	 * 库存数量
	 */
	@Column(name = "STOCK", length = 4, nullable = false)
	private Integer stock;
	/**
	 * 锁定库存
	 */
	@Column(name = "STOCK_HOLD", length = 4, nullable = false)
	private Integer stockHold;
	/**
	 * 附标题
	 */
	@Column(name = "TITLE", length = 128)
	private String title;
	/**
	 * 修改时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "UPDATE_TIME")
	private Timestamp updateTime;
	/**
	 * 修改人
	 */
	@Column(name = "UPDATE_USER", length = 8)
	private Long updateUser;
	/**
	 * 重量
	 */
	@Column(name = "WEIGHT", length = 8, nullable = false)
	private Double weight;
	/**
	 * 宽
	 */
	@Column(name = "WIDTH", length = 4)
	private Integer width;
	/**
	 * 厂商
	 * ***/
	@Column(name = "FIRE", length = 35)
	private String firm;

	/**
	 * 是否已经上传 0 未上传 1 已上传
	 * **/
	@Column(name = "IS_UPLOAD", length = 4)
	private Integer isUpload;
	/**
	 * 商品在置换机中的状态 1 上架 2 下架
	 * ****/
	@Column(name = "V_STATUS", length = 4)
	private Integer vStatus;
	/**
	 * 月销量
	 * ****/
	@Column(name="SALES",length=8)
	private Integer sales;
	/**
     * 商品类型  1：自有  2：平台供货
     */
    @Column(name = "TYPE", length = 4, nullable = false)
    private Integer type;
    
    /**
     * 规格名称(同一类商品用户可以选择不同的规格)-商品聚合时使用
     * ***/
    @Column(name = "SPEC", length = 16, nullable = false)
    private String spec;
    
    /** (商品规格字段)商品组号(用于关联统一规格的商品) */
    @Column(name = "PRODUCT_COMBINATION")
    private String productCombination;

	/**
	 * 聚合数量
	 */
	@Column(name = "COMBINATION_NUMBER")
    private Integer combinationNumber;
    
    /** (虚拟商品字段)区分0:实物,1:虚拟商品 */
    @Column(name = "TRUE_OR_FALSE")
    private Integer trueOrFalse;
    
    /** (虚拟商品字段)虚拟商品所需路径 */
    @Column(name = "VIRTUAL_URL")
    private String virtualUrl;
    
    @Transient
    private String virtualImage;
	
	/** 是否选中1:选中,0:未选中 */
	@Transient
	private Integer sort;
	
	@Transient
	private Double priceLocal;
	
	@Transient
	private Double priceMaxLocal;
	/**
	 * 汇率
	 */
	@Transient
	private Double currencyRate;

	@Transient
	private String orgName;

	@Transient
	private Integer qty;

	@Transient
	private Long promoteId;
	
	@Transient
	private String images;
	
	@Transient
	private String imagesDetail = "";
	
	@Transient
	private String models;
	
	@Transient
	private String pointPlaceList;
//	private List<PointPlace> pointPlaceList = new ArrayList<PointPlace>();
	
	@Transient
	private String shareUrl;
	@Transient
    private String shareImg;
	
	@Transient
	private Long proxyId;
	
	@Transient
	private Double channelPrice;
	
	@Transient
	private Long diviceId;
	
	@Transient
	private String restockType;
	
	@Transient
	private String searchType;
	
	@Transient
	private Long searchId;
	
	/**
	 * 组合价
	 */
	@Column(name = "PRICE_COMBO", length = 8, nullable = false)
	private Double priceCombo;
	
	/**
	 * 供货数量
	 */
	@Transient
	private Integer supplyCount;

	/**
	 * 商品周长   单位：mm
	 */
	@Column(name = "PERIMETER", length = 16, nullable = false)
	private Integer perimeter;
	
	/**
	 * 待同步价格
	 */
	@Transient
	private Double priceOnLine;
	
	/**
	 * 商品类别
	 */
	@Column(name = "CATEGORY", length = 16, nullable = false)
	private Integer category;
	
	/**
	 * 店铺ID
	 */
	@Transient
	private Long pointId;
	
	/** 概率 */
	@Transient
	private Double probabil;
	
	/** 数量 */
	@Transient
	private Integer num;
	
	/**
	 * 是否可售  0：不可售  1：可售
	 */
	@Transient
	private Integer sellable;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getRestockType() {
		return restockType;
	}

	public void setRestockType(String restockType) {
		this.restockType = restockType;
	}

	public String getSearchType() {
		return searchType;
	}

	public void setSearchType(String searchType) {
		this.searchType = searchType;
	}

	public Long getSearchId() {
		return searchId;
	}

	public void setSearchId(Long searchId) {
		this.searchId = searchId;
	}

	public Long getDiviceId() {
		return diviceId;
	}

	public void setDiviceId(Long diviceId) {
		this.diviceId = diviceId;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public Integer getBuyLimit() {
		return buyLimit;
	}

	public void setBuyLimit(Integer buyLimit) {
		this.buyLimit = buyLimit;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Boolean getCombo() {
		return combo;
	}

	public void setCombo(Boolean combo) {
		this.combo = combo;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public Long getCreateUser() {
		return createUser;
	}

	public void setCreateUser(Long createUser) {
		this.createUser = createUser;
	}

	public Integer getExpiration() {
		return expiration;
	}

	public void setExpiration(Integer expiration) {
		this.expiration = expiration;
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	public Timestamp getOffTime() {
		return offTime;
	}

	public void setOffTime(Timestamp offTime) {
		this.offTime = offTime;
	}

	public Timestamp getOnTime() {
		return onTime;
	}

	public void setOnTime(Timestamp onTime) {
		this.onTime = onTime;
	}

	public Long getOrgId() {
		return orgId;
	}

	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}

	public String getOrigin() {
		return origin;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
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

	public Double getPriceMax() {
		return priceMax;
	}

	public void setPriceMax(Double priceMax) {
		this.priceMax = priceMax;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public Double getTaxRate() {
		return taxRate;
	}

	public void setTaxRate(Double taxRate) {
		this.taxRate = taxRate;
	}

	public String getIngredient() {
		return ingredient;
	}

	public void setIngredient(String ingredient) {
		this.ingredient = ingredient;
	}

    public Double getPriceMaxLocal() {
		if (this.priceMaxLocal == null && this.priceMax != null && this.currency != null) {
			if (Commons.CURRENCY_LOCAL.equals(this.currency))
				return this.priceMax;
			IDictionaryService dictionaryService = ContextUtil.getBeanByName(IDictionaryService.class, "dictionaryService");
			if (dictionaryService != null) {
				Currency currency = dictionaryService.findCurrencyByCode(this.currency);
				if (currency != null)
					return MathUtil.divMax(0, this.priceMax, currency.getRate());
			}
		}
		return priceMaxLocal;
	}

	public void setPriceMaxLocal(Double priceMaxLocal) {
		this.priceMaxLocal = priceMaxLocal;
	}

	public Double getCurrencyRate() {
		return currencyRate;
	}

	public void setCurrencyRate(Double currencyRate) {
		this.currencyRate = currencyRate;
	}

	public String getRegistName() {
		return registName;
	}

	public void setRegistName(String registName) {
		this.registName = registName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getSku() {
		return sku;
	}

	public void setSku(String sku) {
		this.sku = sku;
	}

	public String getSkuName() {
		return null == skuName ? skuName : skuName.trim();
	}

	public void setSkuName(String skuName) {
		this.skuName = skuName;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public Integer getStock() {
		return stock;
	}

	public void setStock(Integer stock) {
		this.stock = stock;
	}

	public Integer getStockHold() {
		return stockHold;
	}

	public void setStockHold(Integer stockHold) {
		this.stockHold = stockHold;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	public Long getUpdateUser() {
		return updateUser;
	}

	public void setUpdateUser(Long updateUser) {
		this.updateUser = updateUser;
	}

	public Double getWeight() {
		return weight;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public String getOrgName() {
		return orgName;
	}

	public void setOrgName(String orgName) {
		this.orgName = orgName;
	}

	public Integer getQty() {
		return qty;
	}

	public void setQty(Integer qty) {
		this.qty = qty;
	}

	public Long getPromoteId() {
		return promoteId;
	}

	public void setPromoteId(Long promoteId) {
		this.promoteId = promoteId;
	}

	public String getImages() {
		return images==null?"":images;
	}

	public void setImages(String images) {
		this.images = images;
	}

	public String getModels() {
		return models;
	}

	public void setModels(String models) {
		this.models = models;
	}

	public String getShareUrl() {
		return shareUrl;
	}

	public void setShareUrl(String shareUrl) {
		this.shareUrl = shareUrl;
	}

	public String getShareImg() {
		if(this.images!=null){
			String [] is = images.split(",");
			for(String image:is){
				if(image!=null||image!=""){
					return image;
				}
			}
		}
		return null;
	}

	public void setShareImg(String shareImg) {
		this.shareImg = shareImg;
	}

	/**
	 * @return the priceTwo
	 */
	public Double getPriceTwo() {
		return priceTwo;
	}

	/**
	 * @param priceTwo the priceTwo to set
	 */
	public void setPriceTwo(Double priceTwo) {
		this.priceTwo = priceTwo;
	}

	/**
	 * @return the sales
	 */
	public Integer getSales() {
		return sales;
	}

	/**
	 * @param sales the sales to set
	 */
	public void setSales(Integer sales) {
		this.sales = sales;
	}

	/**
	 * @return the proxyId
	 */
	public Long getProxyId() {
		return proxyId;
	}

	/**
	 * @param proxyId the proxyId to set
	 */
	public void setProxyId(Long proxyId) {
		this.proxyId = proxyId;
	}

	
	/**
	 * @return the priceOne
	 */
	public Double getPriceOne() {
		return priceOne;
	}

	/**
	 * @param priceOne the priceOne to set
	 */
	public void setPriceOne(Double priceOne) {
		this.priceOne = priceOne;
	}

	
	/**
	 * @return the channelPrice
	 */
	public Double getChannelPrice() {
		return channelPrice;
	}

	/**
	 * @param channelPrice the channelPrice to set
	 */
	public void setChannelPrice(Double channelPrice) {
		this.channelPrice = channelPrice;
	}

	/**
	 * @return the firm
	 */
	public String getFirm() {
		return firm;
	}

	/**
	 * @param firm the firm to set
	 */
	public void setFirm(String firm) {
		this.firm = firm;
	}
	
	

	/**
	 * @return the isUpload
	 */
	public Integer getIsUpload() {
		return isUpload;
	}

	/**
	 * @param isUpload the isUpload to set
	 */
	public void setIsUpload(Integer isUpload) {
		this.isUpload = isUpload;
	}
	
	

	/**
	 * @return the vStatus
	 */
	public Integer getvStatus() {
		return vStatus;
	}

	/**
	 * @param vStatus the vStatus to set
	 */
	public void setvStatus(Integer vStatus) {
		this.vStatus = vStatus;
	}

	/**
	 * 初始化默认值
	 */
	public void initDefaultValue() {
		if (this.state == null)
			this.state = Commons.PRODUCT_STATE_STOCK;
		if (this.stockHold == null)
			this.stockHold = 0;
		if (this.taxRate == null)
			this.taxRate = 0d;
		if (this.stockHold == null)
			this.stockHold = 0;
		if (this.buyLimit == null)
			this.buyLimit = 0;
		if (this.combo == null)
			this.combo = false;
		if (this.sales == null)
			this.sales=0;
		if(this.isUpload==null)
			this.isUpload=0;
		if(this.vStatus==null)
			this.vStatus=2;
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
		Product other = (Product) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public Double getPriceCombo() {
		return priceCombo;
	}

	public void setPriceCombo(Double priceCombo) {
		this.priceCombo = priceCombo;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getSpec() {
		return spec;
	}

	public void setSpec(String spec) {
		this.spec = spec;
	}

	public Integer getSupplyCount() {
		return supplyCount;
	}

	public void setSupplyCount(Integer supplyCount) {
		this.supplyCount = supplyCount;
	}

	public Integer getPerimeter() {
		return perimeter;
	}

	public void setPerimeter(Integer perimeter) {
		this.perimeter = perimeter;
	}

	public String getVirtualImage() {
        return virtualImage;
    }

    public void setVirtualImage(String virtualImage) {
        this.virtualImage = virtualImage;
    }

    public Double getPriceOnLine() {
		return priceOnLine;
	}

	public void setPriceOnLine(Double priceOnLine) {
		this.priceOnLine = priceOnLine;
	}

	public Integer getCategory() {
		return category;
	}

	public void setCategory(Integer category) {
		this.category = category;
	}

	public String getImagesDetail() {
		return imagesDetail;
	}

	public void setImagesDetail(String imagesDetail) {
		this.imagesDetail = imagesDetail;
	}

	public Long getPointId() {
		return pointId;
	}

	public void setPointId(Long pointId) {
		this.pointId = pointId;
	}

	public String getPointPlaceList() {
		return pointPlaceList;
	}

	public void setPointPlaceList(String pointPlaceList) {
		this.pointPlaceList = pointPlaceList;
	}

	public Integer getSellable() {
		return sellable;
	}

	public void setSellable(Integer sellable) {
		this.sellable = sellable;
	}

	public Double getProbabil() {
		return probabil;
	}

	public void setProbabil(Double probabil) {
		this.probabil = probabil;
	}

	public Integer getNum() {
		return num;
	}

	public void setNum(Integer num) {
		this.num = num;
	}

	public Integer getSort() {
		return sort;
	}

	public void setSort(Integer sort) {
		this.sort = sort;
	}

	public String getProductCombination() {
		return productCombination;
	}

	public void setProductCombination(String productCombination) {
		this.productCombination = productCombination;
	}

	public Integer getTrueOrFalse() {
		return trueOrFalse;
	}

	public void setTrueOrFalse(Integer trueOrFalse) {
		this.trueOrFalse = trueOrFalse;
	}

	public String getVirtualUrl() {
		return virtualUrl;
	}

	public void setVirtualUrl(String virtualUrl) {
		this.virtualUrl = virtualUrl;
	}
	
}