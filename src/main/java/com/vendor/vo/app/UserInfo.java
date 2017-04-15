/**
 * 
 */
package com.vendor.vo.app;

import java.io.Serializable;

/**
 * 【补货APP】用户信息
 * 
 * @author liujia on 2016年6月27日
 */
public class UserInfo implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * 获取access_token需要的code
	 */
	private String code;

	/**
	 * 用户名
	 */
	private String username;
	/**
	 * 真实姓名
	 */
	private String realName;
	/**
	 * 用户ID
	 */
	private Integer userId;
	/**
	 * 邮箱
	 */
	private String email;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

}
