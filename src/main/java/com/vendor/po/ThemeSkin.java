package com.vendor.po;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * 主题皮肤实体类
 *
 * @author
 * @create 2017-03-07 13:39
 **/
@Entity
@Table(name = "T_THEME_SKIN")
@JsonFilter("com.vendor.po.ThemeSkin")
public class ThemeSkin {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Long id;

    /** 主题名称 */
    @Column(name = "THEME_NAME")
    private String themeName;

    /** 主题类型(0:横屏,1:竖屏) */
    @Column(name = "THEME_TYPE")
    private Integer themeType;

    /** 所属机构ID */
    @Column(name = "ORG_ID")
    private Long orgId;

    /** 主题是否可用状态(0:可用,9:已删除) */
    @Column(name = "STATE")
    private Integer state;

    /** 是否是默认主题(0:默认,1:不是默认) */
    @Column(name = "DEFAULT_THEME")
    private Integer defaultTheme;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "CREATE_TIME")
    private Timestamp createTime;

    /** 修改时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "UPDATE_TIME")
    private Timestamp updateTime;

    /** 删除时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "DELETE_TIME")
    private Timestamp deleteTime;

    /** 预览图 */
    @Transient
    private String images;

    @Transient
    private Long key;//图片的key

    @Transient
    private Long[] fileIds;


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

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public Integer getDefaultTheme() {
        return defaultTheme;
    }

    public void setDefaultTheme(Integer defaultTheme) {
        this.defaultTheme = defaultTheme;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    public Timestamp getDeleteTime() {
        return deleteTime;
    }

    public void setDeleteTime(Timestamp deleteTime) {
        this.deleteTime = deleteTime;
    }

    public String getImages() {
        return images;
    }

    public void setImages(String images) {
        this.images = images;
    }


    public Long getKey() {
        return key;
    }

    public void setKey(Long key) {
        this.key = key;
    }

    public Long[] getFileIds() {
        return fileIds;
    }

    public void setFileIds(Long[] fileIds) {
        this.fileIds = fileIds;
    }

}
