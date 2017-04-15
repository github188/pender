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
 * 优惠码
 * 
 * @author dranson on 2015年12月15日
 */
@Entity
@Table(name = "T_COUPONE")
@JsonFilter("com.vendor.po.Coupone")
public class Coupone implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;
	/**
	 * 有效开始时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "BEGIN_DATE")
	private Timestamp beginDate;
	/**
	 * 标题
	 */
	@Column(name = "TITLE", length = 64, nullable = false)
	private String title;
	/**
	 * 优惠码
	 */
	@Column(name = "CODE", length = 32, nullable = false)
	private String code;
	/**
	 * 起始编码
	 */
	@Column(name = "CODE_START", length = 8)
	private String codeStart;
	/**
	 * 生成数量
	 */
	@Column(name = "CODE_COUNT", length = 4)
	private Integer codeCount;
	/**
	 * 创建时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "CREATE_TIME", nullable = false)
	private Timestamp createTime;
	/**
	 * 创建人
	 */
	@Column(name = "CREATE_USER", length = 8, nullable = false)
	private Long createUser;
	/**
	 * 有效天数
	 */
	@Column(name = "DAYS", length = 4, nullable = false)
	private Integer days;
	/**
	 * 有效结束时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "END_DATE")
	private Timestamp endDate;
	/**
	 * 数量
	 */
	@Column(name = "QUANTITY", length = 4, nullable = false)
	private Integer quantity;
	/**
	 * 是否随机
	 */
	@Column(name = "RANDOM", nullable = false)
	private Boolean random;
	/**
	 * 备注
	 */
	@Column(name = "REMARK", length = 256)
	private String remark;
	/**
	 * 优惠码类型
	 */
	@Column(name = "TYPE", length = 2, nullable = false)
	private Integer type;
	/**
	 * 更新时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "UPDATE_TIME")
	private Timestamp updateTime;
	/**
	 * 修改人
	 */
	@Column(name = "UPDATE_USER", length = 8)
	private Long updateUser;
	/**
	 * 是否虚拟码
	 */
	@Column(name = "VIRTUAL", nullable = false)
	private Boolean virtual;
	

	/**
	 * 别名
	 */
	@Column(name = "NICKNAME", length = 64, nullable = false)
	private String nickname;

	@Transient
	private Integer useNumber;

	@Transient
	private Integer orderCount;

	@Transient
	private Double orderAmount;
	
	@Transient
	private Integer amounts;

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Timestamp getBeginDate() {
		return this.beginDate;
	}

	public void setBeginDate(Timestamp beginDate) {
		this.beginDate = beginDate;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getCode() {
		return this.code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getCodeStart() {
		return this.codeStart;
	}

	public void setCodeStart(String codeStart) {
		this.codeStart = codeStart;
	}

	public Integer getCodeCount() {
		return codeCount;
	}

	public void setCodeCount(Integer codeCount) {
		this.codeCount = codeCount;
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

	public Integer getDays() {
		return this.days;
	}

	public void setDays(Integer days) {
		this.days = days;
	}

	public Timestamp getEndDate() {
		return this.endDate;
	}

	public void setEndDate(Timestamp endDate) {
		this.endDate = endDate;
	}

	public Integer getQuantity() {
		return this.quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Boolean getRandom() {
		return random;
	}

	public void setRandom(Boolean random) {
		this.random = random;
	}

	public String getRemark() {
		return this.remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Integer getType() {
		return this.type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Timestamp getUpdateTime() {
		return this.updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	public Long getUpdateUser() {
		return this.updateUser;
	}

	public void setUpdateUser(Long updateUser) {
		this.updateUser = updateUser;
	}

	public Boolean getVirtual() {
		return this.virtual;
	}

	public void setVirtual(Boolean virtual) {
		this.virtual = virtual;
	}

	public Integer getUseNumber() {
		return useNumber;
	}

	public void setUseNumber(Integer useNumber) {
		this.useNumber = useNumber;
	}

	public Integer getOrderCount() {
		return orderCount;
	}

	public void setOrderCount(Integer orderCount) {
		this.orderCount = orderCount;
	}

	public Double getOrderAmount() {
		return orderAmount;
	}

	public void setOrderAmount(Double orderAmount) {
		this.orderAmount = orderAmount;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	
	public Integer getAmounts() {
		return amounts;
	}

	public void setAmounts(Integer amounts) {
		this.amounts = amounts;
	}

	/**
	 * 初始化默认值
	 */
	public void initDefaultValue() {
		if (this.virtual == null)
			this.virtual = true;
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
		Coupone other = (Coupone) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}