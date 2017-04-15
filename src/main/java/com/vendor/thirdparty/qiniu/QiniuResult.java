/**
 * 
 */
package com.vendor.thirdparty.qiniu;

import java.io.Serializable;

/**
 * 七牛服务器返回结果
 * @author dranson on 2015年12月17日
 */
public class QiniuResult implements Serializable {

	private long fsize;
	private String key;
	private String hash;
	private int width;
	private int height;
	private int status;

	public long getFsize() {
		return fsize;
	}

	public void setFsize(long fsize) {
		this.fsize = fsize;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}
}
