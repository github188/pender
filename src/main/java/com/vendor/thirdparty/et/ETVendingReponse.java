package com.vendor.thirdparty.et;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ETVendingReponse implements Serializable {

	private static final long serialVersionUID = 1L;
	private Integer status;
	private List<Object> data;

	public ETVendingReponse() {

	}

	/**
	 * @return the status
	 */
	public Integer getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}

	/**
	 * @return the data
	 */
	public List<Object> getData() {
		return data;
	}

	/**
	 * @param data the data to set
	 */
	public void setData(List<Object> data) {
		if (data == null)
			this.data = new ArrayList<>();
		else
			this.data = data;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ETVendingReponse [status=" + status + ", data=" + data + "]";
	}

}
