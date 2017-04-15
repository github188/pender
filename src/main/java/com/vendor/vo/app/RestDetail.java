package com.vendor.vo.app;

/**
 * 返回客户端单个对象
 * **/
public class RestDetail<T> extends RestStatus{
	
	private T data;

	public RestDetail(boolean state){
		super(state);
	}
	
	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}
	

}
