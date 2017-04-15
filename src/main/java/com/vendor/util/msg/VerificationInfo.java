package com.vendor.util.msg;

/**
 * 验证码保存时间
 * **/
public class VerificationInfo {
	
	private String code;   //验证码
	private long time;  //保存时间，默认10分钟
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		this.time = time;
	}
	
	

}
