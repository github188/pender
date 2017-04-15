/**
 * 
 */
package com.vendor.thirdparty.et;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFilter;

/**
 * @author dranson on 2016年6月3日
 */
@JsonFilter("com.vendor.po.ETReturn")
public class ETReturn implements Serializable {
	
	private Integer count;
	
	private List<ETWxOrder> result;

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public List<ETWxOrder> getResult() {
		return result;
	}

	public void setResult(List<ETWxOrder> result) {
		this.result = result;
	}
}
