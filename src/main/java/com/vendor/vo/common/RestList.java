package com.vendor.vo.common;

import java.util.List;

import com.vendor.vo.app.RestStatus;

/**
 * 返回列表形式json
 * ***/
public class RestList<T> extends RestStatus{
	public Long total;
	public List<T> list;
	
	public RestList(){
		super();
	}
	
	public RestList(boolean state){
		super(state);
	}
	
	public Long getTotal() {
		return total;
	}
	public void setTotal(Long total) {
		this.total = total;
	}
	public List<T> getList() {
		return list;
	}
	public void setList(List<T> list) {
		this.list = list;
	}
	
	

}
