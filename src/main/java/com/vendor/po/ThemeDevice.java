package com.vendor.po;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 *  主题设备关联实体
 */
@Entity
@Table(name = "T_THEME_DEVICE")
@JsonFilter("com.vendor.po.ThemeDevice")
public class ThemeDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    /** 主题类型(0:横屏,1:竖屏) */
    @Column(name = "THEME_TYPE")
    private Integer themeType;

    /** 机构ID */
    @Column(name = "ORG_ID")
    private Long orgId;

    /** 设备组号 */
    @Column(name = "FACTORY_DEV_NO")
    private String factoryDevNo;

    /** 当前主题ID */
    @Column(name = "THEME_ID")
    private Long themeId;

    /** 待应用ID */
    @Column(name = "TOENABLE_ID")
    private Long toenableId;

    /** 执行状态(0:未执行,1:执行中,2:已取消,3:执行成功,4:执行失败) */
    @Column(name = "EXECUTING_STATE")
    private Integer executingState;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "START_TIME")
    private Timestamp startTime;

    /** 横屏主题 */
    @Transient
    private Long hTheme;

    /** 竖屏主题 */
    @Transient
    private Long vTheme;

    /** 店铺名称 */
    @Transient
    private String pointName;

    /** 店铺地址 */
    @Transient
    private String pointAddress;

    /**
     * 点位类型  1小区，2学校，3写字楼，4酒店，5医院，6火车站/高铁站，7汽车站，8机场，9地铁站，10运动场，11会所
     */
    @Transient
    private Integer pointType;

    /** 当前主题名称 */
    @Transient
    private String themeName;

    /** 待应用主题名称 */
    @Transient
    private String toenableName;

    /** 设备状态(2离线,1在线) */
    @Transient
    private String deviceStatus;

     /** 主题路径 */
    @Transient
    private String themeUrl;

    @Transient
    private String images;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getThemeName() {
        return themeName;
    }

    public void setThemeName(String themeName) {
        this.themeName = themeName;
    }

    public Integer getThemeType() {
        return themeType;
    }

    public void setThemeType(Integer themeType) {
        this.themeType = themeType;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public String getFactoryDevNo() {
        return factoryDevNo;
    }

    public void setFactoryDevNo(String factoryDevNo) {
        this.factoryDevNo = factoryDevNo;
    }

    public Long getThemeId() {
        return themeId;
    }

    public void setThemeId(Long themeId) {
        this.themeId = themeId;
    }

    public Long getToenableId() {
        return toenableId;
    }

    public void setToenableId(Long toenableId) {
        this.toenableId = toenableId;
    }

    public Integer getExecutingState() {
        return executingState;
    }

    public void setExecutingState(Integer executingState) {
        this.executingState = executingState;
    }

    public Long gethTheme() {
        return hTheme;
    }

    public void sethTheme(Long hTheme) {
        this.hTheme = hTheme;
    }

    public Long getvTheme() {
        return vTheme;
    }

    public void setvTheme(Long vTheme) {
        this.vTheme = vTheme;
    }

    public String getPointName() {
        return pointName;
    }

    public void setPointName(String pointName) {
        this.pointName = pointName;
    }

    public String getPointAddress() {
        return pointAddress;
    }

    public void setPointAddress(String pointAddress) {
        this.pointAddress = pointAddress;
    }

    public String getToenableName() {
        return toenableName;
    }

    public void setToenableName(String toenableName) {
        this.toenableName = toenableName;
    }

    public String getDeviceStatus() {
        return deviceStatus;
    }

    public void setDeviceStatus(String deviceStatus) {
        this.deviceStatus = deviceStatus;
    }

    public Integer getPointType() {
        return pointType;
    }

    public void setPointType(Integer pointType) {
        this.pointType = pointType;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public String getThemeUrl() {
        return themeUrl;
    }

    public void setThemeUrl(String themeUrl) {
        this.themeUrl = themeUrl;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }
}
