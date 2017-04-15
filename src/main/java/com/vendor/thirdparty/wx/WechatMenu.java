/**
 * 
 */
package com.vendor.thirdparty.wx;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author dranson on 2015年5月28日
 */
public class WechatMenu implements Serializable {

	private String type;
	
	private String name;
	
	private String key;
	
	private String url;
	
	@JsonProperty(value = "media_id")
	private String mediaId;
	
	@JsonProperty(value = "sub_button")
	private List<WechatMenu> wechatMenus;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getMediaId() {
		return mediaId;
	}

	public void setMediaId(String mediaId) {
		this.mediaId = mediaId;
	}

	public List<WechatMenu> getWechatMenus() {
		return wechatMenus;
	}

	public void setWechatMenus(List<WechatMenu> wechatMenus) {
		this.wechatMenus = wechatMenus;
	}
	
	public void addWechatMenu(WechatMenu wechatMenu) {
		if (this.wechatMenus == null)
			this.wechatMenus = new ArrayList<WechatMenu>();
		this.wechatMenus.add(wechatMenu);
	}
}
