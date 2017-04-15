package com.vendor.vo.web;

public class JoinUsData {
	
	/**
	 * 错误标识  0成功   1失败
	 */
	private Integer Err = 1;

	/**
	 * 提示信息
	 */
	private String Msg;

	public Integer getErr() {
		return Err;
	}

	public void setErr(Integer err) {
		Err = err;
	}

	public String getMsg() {
		return Msg;
	}

	public void setMsg(String msg) {
		Msg = msg;
	}
	
}
