package com.vendor.vo.common;

/**
 * 接口返回基础对象
 * 
 ***/
public class ResultBase {

	private Integer resultCode;

	private String resultMessage;
	
	public ResultBase() {
		this.resultCode = -1;//默认失败；0表示成功
	}
	
	public Integer getResultCode() {
		return resultCode;
	}

	public void setResultCode(Integer resultCode) {
		this.resultCode = resultCode;
	}

	public String getResultMessage() {
		return resultMessage;
	}

	public void setResultMessage(String resultMessage) {
		this.resultMessage = resultMessage;
	}

}
