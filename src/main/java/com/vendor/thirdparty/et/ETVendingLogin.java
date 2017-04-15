package com.vendor.thirdparty.et;

import java.io.Serializable;

public class ETVendingLogin implements Serializable {

	private static final long serialVersionUID = 1L;
	private String session_Id;
	private String fid;
	private String login_Name;
	private String login_PWD;
	private String view_Name;
	private String company_Name;
	private Integer oper_Type;

	public ETVendingLogin() {

	}

	/**
	 * @return the session_Id
	 */
	public String getSession_Id() {
		return session_Id;
	}

	/**
	 * @param session_Id the session_Id to set
	 */
	public void setSession_Id(String session_Id) {
		this.session_Id = session_Id;
	}

	/**
	 * @return the fid
	 */
	public String getFid() {
		return fid;
	}

	/**
	 * @param fid the fid to set
	 */
	public void setFid(String fid) {
		this.fid = fid;
	}

	/**
	 * @return the login_Name
	 */
	public String getLogin_Name() {
		return login_Name;
	}

	/**
	 * @param login_Name the login_Name to set
	 */
	public void setLogin_Name(String login_Name) {
		this.login_Name = login_Name;
	}

	/**
	 * @return the login_PWD
	 */
	public String getLogin_PWD() {
		return login_PWD;
	}

	/**
	 * @param login_PWD the login_PWD to set
	 */
	public void setLogin_PWD(String login_PWD) {
		this.login_PWD = login_PWD;
	}

	/**
	 * @return the view_Name
	 */
	public String getView_Name() {
		return view_Name;
	}

	/**
	 * @param view_Name the view_Name to set
	 */
	public void setView_Name(String view_Name) {
		this.view_Name = view_Name;
	}

	/**
	 * @return the company_Name
	 */
	public String getCompany_Name() {
		return company_Name;
	}

	/**
	 * @param company_Name the company_Name to set
	 */
	public void setCompany_Name(String company_Name) {
		this.company_Name = company_Name;
	}

	/**
	 * @return the oper_Type
	 */
	public Integer getOper_Type() {
		return oper_Type;
	}

	/**
	 * @param oper_Type the oper_Type to set
	 */
	public void setOper_Type(Integer oper_Type) {
		this.oper_Type = oper_Type;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "ETVendingOrder [session_Id=" + session_Id + ", fid=" + fid + ", login_Name=" + login_Name + ", login_PWD=" + login_PWD + ", view_Name=" + view_Name
				+ ", company_Name=" + company_Name + ", oper_Type=" + oper_Type + "]";
	}

}
