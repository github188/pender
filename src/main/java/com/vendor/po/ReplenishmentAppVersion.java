package com.vendor.po;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 补货APP版本信息对象
 * 
 */
@Entity
@Table(name = "T_REPLENISHMENT_APP_VERSION")
@JsonFilter("com.vendor.po.ReplenishmentAppVersion")
public class ReplenishmentAppVersion implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;

	/**
	 * 是否静默下载：有新版本时不提示直接下载
	 */
	@Column(name = "IS_SILENT", nullable = false)
	private Boolean isSilent;

	/**
	 * 是否强制安装：不安装无法使用app
	 */
	@Column(name = "IS_FORCE", nullable = false)
	private Boolean isForce;

	/**
	 * 是否下载完成后自动安装
	 */
	@Column(name = "IS_AUTO_INSTALL", nullable = false)
	private Boolean isAutoInstall;

	/**
	 * 是否可忽略该版本
	 */
	@Column(name = "IS_IGNORABLE", nullable = false)
	private Boolean isIgnorable;

	/**
	 * 是否是增量补丁包
	 */
	@Column(name = "IS_PATCH", nullable = false)
	private Boolean isPatch;

	/**
	 * 版本号
	 */
	@Column(name = "VERSION_CODE", length = 32, nullable = false)
	private Integer versionCode;

	/**
	 * 版本名称
	 */
	@Column(name = "VERSION_NAME", length = 30, nullable = false)
	private String versionName;

	/**
	 * 更新内容
	 */
	@Column(name = "UPDATE_CONTENT", length = 256, nullable = false)
	private String updateContent;

	/**
	 * apk下载地址
	 */
	@Column(name = "URL", length = 256, nullable = false)
	private String url;

	/**
	 * md5加密串
	 */
	@Column(name = "MD5", length = 256, nullable = false)
	private String md5;

	/**
	 * apk安装包大小
	 */
	@Column(name = "SIZE", length = 8, nullable = false)
	private Long size;

	/**
	 * 补丁apk下载地址
	 */
	@Column(name = "PATCH_URL", length = 256, nullable = false)
	private String patchUrl;

	/**
	 * 补丁md5加密串
	 */
	@Column(name = "PATCH_MD5", length = 256, nullable = false)
	private String patchMd5;

	/**
	 * 补丁apk安装包大小
	 */
	@Column(name = "PATCH_SIZE", length = 8, nullable = false)
	private Long patchSize;

	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "CREATE_TIME")
	private Timestamp createTime;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getIsSilent() {
		return isSilent;
	}

	public void setIsSilent(Boolean isSilent) {
		this.isSilent = isSilent;
	}

	public Boolean getIsForce() {
		return isForce;
	}

	public void setIsForce(Boolean isForce) {
		this.isForce = isForce;
	}

	public Boolean getIsAutoInstall() {
		return isAutoInstall;
	}

	public void setIsAutoInstall(Boolean isAutoInstall) {
		this.isAutoInstall = isAutoInstall;
	}

	public Boolean getIsIgnorable() {
		return isIgnorable;
	}

	public void setIsIgnorable(Boolean isIgnorable) {
		this.isIgnorable = isIgnorable;
	}

	public Boolean getIsPatch() {
		return isPatch;
	}

	public void setIsPatch(Boolean isPatch) {
		this.isPatch = isPatch;
	}

	public Integer getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(Integer versionCode) {
		this.versionCode = versionCode;
	}

	public String getVersionName() {
		return versionName;
	}

	public void setVersionName(String versionName) {
		this.versionName = versionName;
	}

	public String getUpdateContent() {
		return updateContent;
	}

	public void setUpdateContent(String updateContent) {
		this.updateContent = updateContent;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public Long getSize() {
		return size;
	}

	public void setSize(Long size) {
		this.size = size;
	}

	public String getPatchUrl() {
		return patchUrl;
	}

	public void setPatchUrl(String patchUrl) {
		this.patchUrl = patchUrl;
	}

	public String getPatchMd5() {
		return patchMd5;
	}

	public void setPatchMd5(String patchMd5) {
		this.patchMd5 = patchMd5;
	}

	public Long getPatchSize() {
		return patchSize;
	}

	public void setPatchSize(Long patchSize) {
		this.patchSize = patchSize;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReplenishmentAppVersion other = (ReplenishmentAppVersion) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}