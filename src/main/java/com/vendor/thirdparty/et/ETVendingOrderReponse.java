package com.vendor.thirdparty.et;

import java.io.Serializable;
import java.util.List;

public class ETVendingOrderReponse implements Serializable {

	private static final long serialVersionUID = 1L;
	private Boolean success;
	private String msg;
	private String code;
	private String exportUrl;
	private Integer pageNumber;
	private Integer pageSize;
	private Integer pageCount;
	private Integer total;
	private List<Object> rows;
	
	private Integer count;
	private List<Object> result;

	public ETVendingOrderReponse() {

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
	public List<Object> getRows() {
		return rows;
	}

	/**
	 * @param rows the rows to set
	 */
	public void setRows(List<Object> rows) {
		this.rows = rows;
	}

	/**
	 * @return the pageNumber
	 */
	public Integer getPageNumber() {
		return pageNumber;
	}

	/**
	 * @param pageNumber the pageNumber to set
	 */
	public void setPageNumber(Integer pageNumber) {
		this.pageNumber = pageNumber;
	}

	/**
	 * @return the pageSize
	 */
	public Integer getPageSize() {
		return pageSize;
	}

	/**
	 * @param pageSize the pageSize to set
	 */
	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	/**
	 * @return the pageCount
	 */
	public Integer getPageCount() {
		return pageCount;
	}

	/**
	 * @param pageCount the pageCount to set
	 */
	public void setPageCount(Integer pageCount) {
		this.pageCount = pageCount;
	}

	/**
	 * @return the total
	 */
	public Integer getTotal() {
		return total;
	}

	/**
	 * @param total the total to set
	 */
	public void setTotal(Integer total) {
		this.total = total;
	}
	
	

	/**
	 * @return the count
	 */
	public Integer getCount() {
		return count;
	}

	/**
	 * @param count the count to set
	 */
	public void setCount(Integer count) {
		this.count = count;
	}

	/**
	 * @return the result
	 */
	public List<Object> getResult() {
		return result;
	}

	/**
	 * @param result the result to set
	 */
	public void setResult(List<Object> result) {
		this.result = result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ETVendingOrderReponse [success=" + success + ", msg=" + msg + ", code=" + code + ", exportUrl=" + exportUrl + ", pageNumber=" + pageNumber + ", pageSize="
				+ pageSize + ", pageCount=" + pageCount + ", total=" + total + ", rows=" + rows + "]";
	}
}
