package com.vendor.util;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ecarry.core.exception.BusinessException;
import com.vendor.pojo.WxAccessConf;

/**
 * 微信工具类
 * @author liujia on 2016年8月11日
 */
public class WeChatUtil {
	
	private static final Logger LOGGER = Logger.getLogger(WeChatUtil.class);
	
	public  final static String session_wx_access_conf = "wx_access_conf_";
	
	public static HttpServletRequest getRequest() {
		if (null != RequestContextHolder.getRequestAttributes()) {
			return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		} else {
			return null;
		}
	}

	public static HttpSession getSession() {
		if (getRequest() == null) {
			return null;
		}
		return getRequest().getSession();
	}
	
	public static String getAccessToken(String accessTokenUrl, String appId, String appSecret, String code) {
		StringBuilder url = new StringBuilder();
		url.append(accessTokenUrl);
		url.append("appid=").append(appId);
		url.append("&secret=").append(appSecret);
		url.append("&code=").append(code);
		url.append("&grant_type=authorization_code");
		
		return new HttpAdapter().getData(url.toString());
	}
	
	public static String getPaySignature(Map<String, Object> params, String wxKey) {
		String[] ary = new String[params.size()];
		params.keySet().toArray(ary);
		Arrays.sort(ary);
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < ary.length; i++)
			buf.append(ary[i]).append("=").append(params.get(ary[i])).append("&");
		buf.append("key=").append(wxKey);
		return DigestUtils.md5DigestAsHex(buf.toString().getBytes()).toUpperCase();
	}
	
	/**
	 * 获取有效JsapiTicket
	 * 
	 * @param openId
	 * @return
	 */
	public static WxAccessConf getJsapiTicket(String openId, String appId, String appSecret) {
		return getJsapiTicket(openId, appId, appSecret, getSession());
	}
	
	private static synchronized WxAccessConf getJsapiTicket(String openId, String appId, String appSecret, HttpSession session) {
		WxAccessConf wc = null;
		if (session != null) {
			wc = (WxAccessConf) session.getAttribute(session_wx_access_conf + openId);
		}
		if (wc == null) {
			wc = new WxAccessConf();
			wc.setOpenId(openId);
		}
		if (wc.getJsapExpiresTime() == null || StringUtils.isEmpty(wc.getJsapiTicket())
				|| new Date().getTime() > wc.getJsapExpiresTime().getTime()) {
			try {
				WxAccessConf accessToken = getAccessToken(openId, appId, appSecret);
				if (accessToken == null || StringUtils.isEmpty(accessToken.getAccessToken())) {
					throw new Exception("accessToken为空");
				}
				getJsapiTicket(accessToken.getAccessToken(), wc);
			} catch (Exception e) {
				LOGGER.error("获取JsapiTicket异常：" + e.getMessage());
				return null;
			}
		}
		session.setAttribute(session_wx_access_conf + openId, wc);
		return wc;
	}
	
	@SuppressWarnings("unchecked")
	public static synchronized void getJsapiTicket(String accessToken, WxAccessConf wc) throws Exception {
		try {
			String url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token=" + accessToken + "&type=jsapi";
			String response = new HttpAdapter().getData(url);
			LOGGER.info("获取JsapiTicket返回json：" + response);
			
			ObjectMapper objectMapper = new ObjectMapper();
			Map<String, Object> map = objectMapper.readValue(response, Map.class);
			
			String errmsg=map.get("errmsg") == null ? "" : map.get("errmsg").toString();
			String errcode=map.get("errcode") == null ? "" : map.get("errcode").toString();
			
			if (errmsg.equals("ok") && errcode.equals("0")) {
				wc.setJsapiTicket(map.get("ticket") == null ? "" : map.get("ticket").toString());
				String expiresIn = map.get("expires_in") == null ? "0" : map.get("expires_in").toString();
				Date expiresTime = new Date(new Date().getTime() + Integer.valueOf(expiresIn)*1000);
				wc.setJsapExpiresTime(expiresTime);
			}
		} catch (Exception e) {
			LOGGER.error("获取JsapiTicket异常");
			throw new BusinessException("获取JsapiTicket异常");
		}
	}
	
	/**
	 * 获取有效Accesstoken
	 * 
	 * @param openId
	 * @return
	 */
	public static WxAccessConf getAccessToken(String openId, String appId, String appSecret) {
		return getAccessToken(openId, appId, appSecret, getSession());
	}
	
	private static synchronized WxAccessConf getAccessToken(String openId, String appId, String appSecret, HttpSession session) {
		WxAccessConf wc = null;
		if (session != null) {
			wc = (WxAccessConf) session.getAttribute(session_wx_access_conf + openId);
		}
		if (wc == null) {
			wc = new WxAccessConf();
			wc.setOpenId(openId);
		}
		if (StringUtils.isEmpty(appId) || StringUtils.isEmpty(appSecret)) {
			LOGGER.info("appId获取appSecret不存在");
			return null;
		}
		if (wc.getExpiresTime() == null || StringUtils.isEmpty(wc.getAccessToken())
				|| new Date().getTime() > wc.getExpiresTime().getTime()) {
			try {
				setWcAccessToken(appId, appSecret, wc);
			} catch (Exception e) {
				LOGGER.error("获取AccessToken异常：" + e.getMessage());
				return null;
			}
		}
		session.setAttribute(session_wx_access_conf + openId, wc);
		return wc;
	}
	
	@SuppressWarnings("unchecked")
	private static synchronized void setWcAccessToken(String appId, String secret, WxAccessConf wc) throws Exception {
		try {
			String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + appId + "&secret=" + secret;
			LOGGER.info("进来url：=" + url);
			String response = new HttpAdapter().getData(url);
			if (StringUtils.isEmpty(response)) {
				LOGGER.info("response为空:=" + response);
			}

			ObjectMapper objectMapper = new ObjectMapper();
			Map<String, Object> map = objectMapper.readValue(response, Map.class);

			if (map != null) {
				if (StringUtils.isEmpty(map.get("errcode"))) {
					LOGGER.info("获取到新的token:" + response);
					wc.setAccessToken(map.get("access_token") == null ? "" : map.get("access_token").toString());
					String expiresIn = map.get("expires_in") == null ? "0" : map.get("expires_in").toString();
					Date expiresTime = new Date(new Date().getTime() + Integer.valueOf(expiresIn) * 1000);
					wc.setExpiresTime(expiresTime);
				}
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new BusinessException("获取accessToken异常");
		}
	}

}
