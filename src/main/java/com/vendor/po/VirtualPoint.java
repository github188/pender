package com.vendor.po;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * @ClassName: 虚拟商品-店铺(上架)关联实体
 * @author: duanyx
 * @date: 2017年2月20日 下午3:24:48
 */
@Entity
@Table(name = "T_VIRTUAL_POINT")
@JsonFilter("com.vendor.po.VirtualPoint")
public class VirtualPoint implements Serializable{

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    /** 上架设备的ID */
    @Column(name = "DEVICE_ID")
    private Long deviceId;

    /** 设备组号 */
    @Column(name = "FACTORY_DEV_NO")
    private  String factoryDevNo;

    /** 商品ID */
    @Column(name = "PRODUCT_ID")
    private Long productId;

    /** 所属机构ID */
    @Column(name = "ORG_ID")
    private Long orgId;

    /** 上架时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "CREATE_TIME", nullable = false)
    private Timestamp createTime;

    /** 是否已上架(1:已保存推送,2:已上架,3:上架失败,4:下架成功，5:下架失败) */
    @Column(name = "PUTAWAY_TYPE")
    private Integer putawayType;

    @Transient
    private String images;

    @Transient
    private String virtualUrl;

    @Transient
    private String productNo;

    @Transient
    private String productName;

    @Transient
    private Integer categoryType;

    @Transient
    private String picUrl;

    @Transient
    private String picDetailUrl;

    @Transient
    private String desc;

    @Transient
    private String qrcode_PicUrl;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFactoryDevNo() {
        return factoryDevNo;
    }

    public void setFactoryDevNo(String factoryDevNo) {
        this.factoryDevNo = factoryDevNo;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Integer getPutawayType() {
        return putawayType;
    }

    public void setPutawayType(Integer putawayType) {
        this.putawayType = putawayType;
    }

    public Long getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Long deviceId) {
        this.deviceId = deviceId;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }

    public String getProductNo() {
        return productNo;
    }

    public void setProductNo(String productNo) {
        this.productNo = productNo;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(Integer categoryType) {
        this.categoryType = categoryType;
    }

    public String getVirtualUrl() {
        return virtualUrl;
    }

    public void setVirtualUrl(String virtualUrl) {
        this.virtualUrl = virtualUrl;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public String getPicDetailUrl() {
        return picDetailUrl;
    }

    public void setPicDetailUrl(String picDetailUrl) {
        this.picDetailUrl = picDetailUrl;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getQrcode_PicUrl() {
        return qrcode_PicUrl;
    }

    public void setQrcode_PicUrl(String qrcode_PicUrl) {
        this.qrcode_PicUrl = qrcode_PicUrl;
    }
}
