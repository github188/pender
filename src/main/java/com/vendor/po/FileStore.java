package com.vendor.po;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 存储文件
 * @author dranson on 2015年12月11日
 */
@Entity
@Table(name = "T_FILE")
@JsonFilter("com.vendor.po.FileStore")
public class FileStore implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;
	/**
	 * 上传时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "CREATE_TIME", nullable = false)
	private Timestamp createTime;
	/**
	 * 上传人员
	 */
	@Column(name = "CREATE_USER", length = 8, nullable = false)
	private Long createUser;
	/**
	 * 所属业务ID,主表关联外键
	 */
	@Column(name = "INFO_ID", length = 8, nullable = false)
	private Long infoId;
	/**
	 * 名称
	 */
	@Column(name = "NAME", length = 128, nullable = false)
	private String name;
	/**
	 * 上传文件路径
	 */
	@Column(name = "REAL_PATH", length = 128, nullable = false)
	private String realPath;
	/**
	 * 是否为第三方存储服务器
	 */
	@Column(name = "REMOTE", nullable = false)
	private Boolean remote;
	/**
	 * 文件大小
	 */
	@Column(name = "FILE_SIZE", length = 8, nullable = false)
	private Long fileSize;
	/**
	 * 第三方HASH
	 */
	@Column(name = "THIRD_HASH", length = 128)
	private String thirdHash;
	/**
	 * 备注
	 */
	@Column(name = "REMARK", length = 256)
	private String remark;
	/**
	 * 预览文件
	 */
	@Column(name = "SMALL_PATH", length = 128)
	private String smallPath;
	/**
	 * 类型,字段预留，满足各种业务需要
	 */
	@Column(name = "TYPE", length = 2, nullable = false)
	private Integer type;
	
	@Transient
	private String images;

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Timestamp getCreateTime() {
		return this.createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	public Long getCreateUser() {
		return this.createUser;
	}

	public void setCreateUser(Long createUser) {
		this.createUser = createUser;
	}

	public Long getInfoId() {
		return this.infoId;
	}

	public void setInfoId(Long infoId) {
		this.infoId = infoId;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRealPath() {
		return this.realPath;
	}

	public void setRealPath(String realPath) {
		this.realPath = realPath;
	}

	public Boolean getRemote() {
		return remote;
	}

	public void setRemote(Boolean remote) {
		this.remote = remote;
	}

	public Long getFileSize() {
		return fileSize;
	}

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	public String getThirdHash() {
		return thirdHash;
	}

	public void setThirdHash(String thirdHash) {
		this.thirdHash = thirdHash;
	}

	public String getRemark() {
		return this.remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getSmallPath() {
		return this.smallPath;
	}

	public void setSmallPath(String smallPath) {
		this.smallPath = smallPath;
	}

	public Integer getType() {
		return this.type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getImages() {
		return images;
	}

	public void setImages(String images) {
		this.images = images;
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
		FileStore other = (FileStore) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}