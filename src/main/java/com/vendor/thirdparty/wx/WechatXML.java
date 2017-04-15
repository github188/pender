/**
 * 
 */
package com.vendor.thirdparty.wx;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 接收、发送文本信息
 * @author dranson on 2015年5月27日
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="xml") 
public class WechatXML implements Serializable {
	
	public static final String SUBSCRIBE = "subscribe";
	public static final String UNSUBSCRIBE = "unsubscribe";
	public static final String TEXT = "text";
	public static final String IMAGE = "image";
	public static final String VOICE = "voice";
	public static final String VIDEO = "video";
	public static final String SHORT_VIDEO = "shortvideo";
	public static final String LOCATION = "location";
	public static final String LINK = "link";
	public static final String EVENT = "event";
	public static final String NEWS = "news";

	@XmlElement(name="ToUserName", required = true)
	private String toUserName;
	
	@XmlElement(name="FromUserName", required = true)
	private String fromUserName;

	@XmlElement(name="CreateTime")
	private Long createTime;
	
	@XmlElement(name="MsgType")
	private String msgType;
	
	@XmlElement(name="Content")
	private String content;
	
	@XmlElement(name="MsgId")
	private Long msgId;
	
	@XmlElement(name="MediaId")
	private String mediaId;
	/**
	 * 语音格式：amr
	 */
	@XmlElement(name="Format")
	private String format;
	/**
	 * 语言识别结果，UTF-8编码
	 */
	@XmlElement(name="Recognition")
	private String recognition;
	
	@XmlElement(name="ArticleCount")
	private Integer articleCount;
	
	@XmlElement(name="Event")
	private String event;
	
	@XmlElement(name="EventKey")
	private String eventKey;
	
	@XmlElement(name="FuncFlag")
	private Integer funcFlag;
	
	@XmlElement(name="Image")
	private WechatImageXML imageXML;
	
	@XmlElementWrapper(name = "Articles")
	@XmlElement(name="item")
	private List<WechatNewsXML> newsXMLs;
	
	public String getToUserName() {
		return toUserName;
	}

	public void setToUserName(String toUserName) {
		this.toUserName = toUserName;
	}
	
	public String getFromUserName() {
		return fromUserName;
	}

	public void setFromUserName(String fromUserName) {
		this.fromUserName = fromUserName;
	}
	
	public Long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Long createTime) {
		this.createTime = createTime;
	}
	
	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}
	
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Long getMsgId() {
		return msgId;
	}

	public void setMsgId(Long msgId) {
		this.msgId = msgId;
	}

	public String getMediaId() {
		return mediaId;
	}

	public void setMediaId(String mediaId) {
		this.mediaId = mediaId;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}
	
	public String getRecognition() {
		return recognition;
	}

	public void setRecognition(String recognition) {
		this.recognition = recognition;
	}

	public Integer getArticleCount() {
		return articleCount;
	}

	public void setArticleCount(Integer articleCount) {
		this.articleCount = articleCount;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public String getEventKey() {
		return eventKey;
	}

	public void setEventKey(String eventKey) {
		this.eventKey = eventKey;
	}

	public Integer getFuncFlag() {
		return funcFlag;
	}

	public void setFuncFlag(Integer funcFlag) {
		this.funcFlag = funcFlag;
	}

	public WechatImageXML getImageXML() {
		return imageXML;
	}

	public void setImageXML(WechatImageXML imageXML) {
		this.imageXML = imageXML;
	}

	public List<WechatNewsXML> getNewsXMLs() {
		return newsXMLs;
	}

	public void setNewsXMLs(List<WechatNewsXML> newsXMLs) {
		this.newsXMLs = newsXMLs;
	}

	public void addNewsXML(WechatNewsXML newsXML) {
		if (this.newsXMLs == null)
			this.newsXMLs = new ArrayList<WechatNewsXML>();
		this.newsXMLs.add(newsXML);
	}
}
