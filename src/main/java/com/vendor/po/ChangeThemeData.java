package com.vendor.po;

/**
 * 主题推送实体
 *
 */
public class ChangeThemeData {

    /** 主题ID */
    private Long themeId;

    private String themaName;

    /** MD5值,留以后设备端用来区分此文件已存在设备端 */
    private String MD5;

    /** 主题路径 */
    private String themeUrl;

    public Long getThemeId() {
        return themeId;
    }

    public void setThemeId(Long themeId) {
        this.themeId = themeId;
    }

    public String getMD5() {
        return MD5;
    }

    public void setMD5(String MD5) {
        this.MD5 = MD5;
    }

    public String getThemeUrl() {
        return themeUrl;
    }

    public void setThemeUrl(String themeUrl) {
        this.themeUrl = themeUrl;
    }

    public String getThemaName() {
        return themaName;
    }

    public void setThemaName(String themaName) {
        this.themaName = themaName;
    }
}
