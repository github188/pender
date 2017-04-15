package com.vendor.vo.app;

/**
 * 返回客户端状态
 * 
 ***/
public class RestStatus {
	private Boolean state;
	private String msg;
	private Integer code;

	public RestStatus() {
		this.code=0;
	}

	public RestStatus(Boolean state) {
		this.state = state;
		this.code=0;
	}

	public RestStatus(boolean state,Integer code){
		this.state=state;
		this.code=code;
	}
	
	public Boolean getState() {
		return state;
	}

	public void setState(Boolean state) {
		this.state = state;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public Integer getCode() {
		return code;
	}

	public void setCode(Integer code) {
		this.code = code;
	}

	

}
