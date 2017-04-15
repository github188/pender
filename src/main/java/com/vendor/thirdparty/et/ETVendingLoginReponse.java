package com.vendor.thirdparty.et;

import java.io.Serializable;
import java.util.Map;

public class ETVendingLoginReponse implements Serializable {

	private static final long serialVersionUID = 1L;
	private Boolean success;
	private String msg;
	private String code;
	private String exportUrl;
	private Map<String, Object> rows;

	public ETVendingLoginReponse() {

	}

	/**
	 * @return the success
	 */
	public Boolean getSuccess() {
		return success;
	}

	/**
	 * @param success the success to set
	 */
	public void setSuccess(Boolean success) {
		this.success = success;
	}

	/**
	 * @return the msg
	 */
	public String getMsg() {
		return msg;
	}

	/**
	 * @param msg the msg to set
	 */
	public void setMsg(String msg) {
		this.msg = msg;
	}

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * @return the exportUrl
	 */
	public String getExportUrl() {
		return exportUrl;
	}

	/**
	 * @param exportUrl the exportUrl to set
	 */
	public void setExportUrl(String exportUrl) {
		this.exportUrl = exportUrl;
	}

	/**
	 * @return the rows
	 */
	public Map<String, Object> getRows() {
		return rows;
	}

	/**
	 * @param rows the rows to set
	 */
	public void setRows(Map<String, Object> rows) {
		this.rows = rows;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ETVendingOrderReponse [success=" + success + ", msg=" + msg + ", code=" + code + ", exportUrl=" + exportUrl + ", rows=" + rows + "]";
	}

}
