package com.vendor.po;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * @ClassName 排序推荐中间数据
 * @author duanyx
 * @create 2017-03-01 11:31
 **/
@Entity
@Table(name = "T_SORTING_RECOMMENDED")
@JsonFilter("com.vendor.po.SortingRecommended")
public class SortingRecommended {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Integer id;

    /** 商品ID */
    @Column(name = "PRODUCT_ID")
    private Long productId;

    /** 店铺ID */
    @Column(name = "POINT_ID")
    private String pointId;

    /** 所属机构ID */
    @Column(name = "ORG_ID")
    private Long orgId;

    /** 设备组号 */
    @Column(name = "FACTORY_DEV_NO")
    private String factoryDevNo;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "CREATE_TIME")
    private Timestamp createTime;

    /** 商品排序序号(用于商品前端置顶排序功能) */
    @Column(name = "SERIAL_NUMBER")
    private Integer serialNumber;

    /** 置顶时间(用于商品前端置顶排序功能) */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "STICK_TIME")
    private Timestamp stickTime;

    @Transient
    private Integer type;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
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

    public Timestamp getCreateTime() {
        return createTime;
    }

    public String getPointId() {
        return pointId;
    }

    public void setPointId(String pointId) {
        this.pointId = pointId;
    }

    public String getFactoryDevNo() {
        return factoryDevNo;
    }

    public void setFactoryDevNo(String factoryDevNo) {
        this.factoryDevNo = factoryDevNo;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }
}
