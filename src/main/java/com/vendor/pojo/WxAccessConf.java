package com.vendor.pojo;

import java.util.Date;

public class WxAccessConf {


    /**
     * 微信名称
     */
    private String wxName;

    /**
     * openId
     */
    private String openId;

    /**
     * 获取到的凭证
     */
    private String accessToken = "";

    /**
     * 凭证有效时间
     */
    private Date expiresTime;

    /**
     * 微信appid
     */
    private String appId;

    /**
     * 微信appsecret
     */
    private String appSecret;

    /**
     * 微信token
     */
    private String token = "";

    /**
     * 微信jsapiTicket
     */
    private String jsapiTicket = "";

    /**
     * JS凭证有效时间
     */
    private Date jsapExpiresTime;

    /**
     * 微信aeskey
     */
    private String aesKey = "";

    /**
     * 微信支付商户号
     */
    private String wxMerchantNo;

    /**
     * 支付回调URL
     */
    private String wxPayCallbackUrl;

    /**
     * 商户签名加密key
     * key设置路径：微信商户平台(pay.weixin.qq.com)-->账户设置-->API安全-->密钥设置
     */
    private String apiKey = "";

    private String authOauth2Url = "https://open.weixin.qq.com/connect/oauth2/authorize?";
    
    private String authAccessTokenUrl = "https://api.weixin.qq.com/sns/oauth2/access_token?";
    
    private String authApiTokenUrl = "https://api.weixin.qq.com/cgi-bin/token?";
    
    
    private String authSignJsUrl = "";
    /**
     * 双向验证签权文件 add20151224
     */
    private String certFilePath = "";
    
    private String certFileId = "";
    
    /**
     * 微信名称
     *
     * @return the value of wx_access_conf.wx_name
     */
    public String getWxName() {
        return wxName;
    }

    public void setWxName(String wxName) {
        this.wxName = wxName == null ? null : wxName.trim();
    }

    /**
     * 获取到的凭证
     *
     * @return the value of wx_access_conf.access_token
     */
    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken == null ? null : accessToken.trim();
    }

    /**
     * 凭证有效时间
     *
     * @return the value of wx_access_conf.expires_time
     */
    public Date getExpiresTime() {
        return expiresTime;
    }

    public void setExpiresTime(Date expiresTime) {
        this.expiresTime = expiresTime;
    }

    /**
     * 微信appid
     *
     * @return the value of wx_access_conf.app_id
     */
    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId == null ? null : appId.trim();
    }

    /**
     * 微信appsecret
     *
     * @return the value of wx_access_conf.app_secret
     */
    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret == null ? null : appSecret.trim();
    }

    /**
     * 微信token
     *
     * @return the value of wx_access_conf.token
     */
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token == null ? null : token.trim();
    }

    /**
     * 微信jsapiTicket
     *
     * @return the value of wx_access_conf.jsapi_ticket
     */
    public String getJsapiTicket() {
        return jsapiTicket;
    }

    public void setJsapiTicket(String jsapiTicket) {
        this.jsapiTicket = jsapiTicket == null ? null : jsapiTicket.trim();
    }

    /**
     * JS凭证有效时间
     *
     * @return the value of wx_access_conf.jsap_expires_time
     */
    public Date getJsapExpiresTime() {
        return jsapExpiresTime;
    }

    public void setJsapExpiresTime(Date jsapExpiresTime) {
        this.jsapExpiresTime = jsapExpiresTime;
    }

    /**
     * 微信aeskey
     *
     * @return the value of wx_access_conf.aes_key
     */
    public String getAesKey() {
        return aesKey;
    }

    public void setAesKey(String aesKey) {
        this.aesKey = aesKey == null ? null : aesKey.trim();
    }

    /**
     * 微信支付商户号
     *
     * @return the value of wx_access_conf.wx_merchant_no
     */
    public String getWxMerchantNo() {
        return wxMerchantNo;
    }

    public void setWxMerchantNo(String wxMerchantNo) {
        this.wxMerchantNo = wxMerchantNo == null ? null : wxMerchantNo.trim();
    }

    /**
     * 支付回调URL
     *
     * @return the value of wx_access_conf.wx_pay_callback_url
     */
    public String getWxPayCallbackUrl() {
        return wxPayCallbackUrl;
    }

    public void setWxPayCallbackUrl(String wxPayCallbackUrl) {
        this.wxPayCallbackUrl = wxPayCallbackUrl == null ? null : wxPayCallbackUrl.trim();
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

	public String getAuthOauth2Url() {
		return authOauth2Url;
	}

	public void setAuthOauth2Url(String authOauth2Url) {
		this.authOauth2Url = authOauth2Url;
	}

	public String getAuthAccessTokenUrl() {
		return authAccessTokenUrl;
	}

	public void setAuthAccessTokenUrl(String authAccessTokenUrl) {
		this.authAccessTokenUrl = authAccessTokenUrl;
	}

	public String getAuthApiTokenUrl() {
		return authApiTokenUrl;
	}

	public void setAuthApiTokenUrl(String authApiTokenUrl) {
		this.authApiTokenUrl = authApiTokenUrl;
	}

	public String getAuthSignJsUrl() {
		return authSignJsUrl;
	}

	public void setAuthSignJsUrl(String authSignJsUrl) {
		this.authSignJsUrl = authSignJsUrl;
	}

	public String getCertFilePath() {
		return certFilePath;
	}

	public void setCertFilePath(String certFilePath) {
		this.certFilePath = certFilePath;
	}

	public String getCertFileId() {
		return certFileId;
	}

	public void setCertFileId(String certFileId) {
		this.certFileId = certFileId;
	}

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}
	
}
