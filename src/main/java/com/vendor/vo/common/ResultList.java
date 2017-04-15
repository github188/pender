package com.vendor.vo.common;

import java.util.List;

/**
 * 接口返回列表形式json
 ***/
public class ResultList<T> extends ResultBase {
	public List<T> resultList;
	
	public ResultList() {
		super();
	}

	public List<T> getResultList() {
		return resultList;
	}

	public void setResultList(List<T> resultList) {
		this.resultList = resultList;
	}

}
