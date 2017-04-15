package com.vendor.po;

/**
 * 虚拟商品推送对象list详情
 * @author
 * @create 2017-03-02 17:48
 **/
public class ChangeVirtualPointData {

    private String productNo;//商品编号
    private String productName;//商品名称
    private String picUrl;//商品图片
    private String picDetailUrl;//商品图片
    private String state;// 商品可售状态  0：不可售  1：可售
    private String desc;//商品详细介绍
    private Integer cagetory_type;// 1饮料 2小吃 3 其它
    private String QRcode_PicUrl;//虚拟商品二维码图片
    private String QRcode_StrUrl;//虚拟商品二维码字符串

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

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Integer getCagetory_type() {
        return cagetory_type;
    }

    public void setCagetory_type(Integer cagetory_type) {
        this.cagetory_type = cagetory_type;
    }

    public String getQRcode_PicUrl() {
        return QRcode_PicUrl;
    }

    public void setQRcode_PicUrl(String QRcode_PicUrl) {
        this.QRcode_PicUrl = QRcode_PicUrl;
    }

    public String getQRcode_StrUrl() {
        return QRcode_StrUrl;
    }

    public void setQRcode_StrUrl(String QRcode_StrUrl) {
        this.QRcode_StrUrl = QRcode_StrUrl;
    }
}
