package com.vendor.vo.common;

/**
 * 接口返回基础对象
 * 
 ***/
public class ResultAppBase<T> {

	private Integer resultCode;

	private String resultMessage;
	
	private T data;
	
	public ResultAppBase() {
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

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

}
