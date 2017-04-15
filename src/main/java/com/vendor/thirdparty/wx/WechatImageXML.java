/**
 * 
 */
package com.vendor.thirdparty.wx;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlElement;

/**
 * @author dranson on 2015年6月3日
 */
public class WechatImageXML implements Serializable {
	
	private String mediaId;

	@XmlElement(name="MediaId")
	public String getMediaId() {
		return mediaId;
	}

	public void setMediaId(String mediaId) {
		this.mediaId = mediaId;
	}
}
