package com.vendor.po;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.springframework.beans.BeanUtils;

import com.ecarry.core.util.MathUtil;
import com.ecarry.core.web.core.ContextUtil;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.vendor.service.IDictionaryService;
import com.vendor.util.Commons;

/**
 * 优惠码明细
 * 
 * @author dranson on 2015年12月15日
 */
@Entity
@Table(name = "T_COUPONE_DETAIL")
@JsonFilter("com.vendor.po.CouponeDetail")
public class CouponeDetail implements Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "ID")
	private Long id;
	/**
	 * 面值
	 */
	@Column(name = "AMOUNT", length = 4, nullable = false)
	private Integer amount;
	/**
	 * 有效开始时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "BEGIN_DATE", nullable = false)
	private Timestamp beginDate;
	/**
	 * 优惠码
	 */
	@Column(name = "COUPONE_CODE", length = 32)
	private String couponeCode;
	/**
	 * 优惠码ID
	 */
	@Column(name = "COUPONE_ID", length = 8, nullable = false)
	private Long couponeId;
	/**
	 * 有效开始时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "END_DATE", nullable = false)
	private Timestamp endDate;
	/**
	 * 发放时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "ISSUE_TIME", nullable = false)
	private Timestamp issueTime;
	/**
	 * 有效天数
	 */
	@Column(name = "LIMIT_DAY", length = 4)
	private Integer limitDay;
	/**
	 * 订单编号
	 */
	@Column(name = "ORDER_NO", length = 32)
	private String orderNo;
	/**
	 * 商家ID
	 */
	@Column(name = "ORG_ID", length = 8, nullable = false)
	private Long orgId;
	/**
	 * 状态
	 */
	@Column(name = "STATE", length = 2, nullable = false)
	private Integer state;
	/**
	 * 优惠券ID
	 */
	@Column(name = "TICKET_ID", length = 8, nullable = false)
	private Long ticketId;
	/**
	 * 优惠券类型
	 */
	@Column(name = "TYPE", length = 2, nullable = false)
	private Integer type;
	/**
	 * 使用时间
	 */
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	@Column(name = "USE_TIME", nullable = false)
	private Timestamp useTime;
	/**
	 * 用户ID
	 */
	@Column(name = "USER_ID", length = 8, nullable = false)
	private Long userId;
	/**
	 * 乐观锁,乐观行锁标志，解决并发问题
	 */
	@Column(name = "VERSION", length = 8, nullable = false)
	private Long version;
	
	@Column(name = "NICKNAME", length = 64)
	private String nickname;
	
	/**
	 * 有效期时间标识  0：天数，1：时间范围
	 */
	@Column(name = "VALIDITY_TIME", length = 2, nullable = false)
	private Integer validityTime;
	
	/**
	 * 数量
	 */
	@Column(name = "QUANTITY", length = 4, nullable = false)
	private Integer quantity;
	

	@Transient
	private Double limitValue;
	
	@Transient
	private String remark;
	
	@Transient
	private String number;
	
	@Transient
	private String ticketTitle;
	
	@Transient
	private String typeStr;
	
	@Transient
	private String currency;
	
	@Transient
	private Integer amountLocal;
	

	
	public CouponeDetail() {}
	
	public CouponeDetail(String content) {
		BeanUtils.copyProperties(ContextUtil.getTByJson(CouponeDetail.class, content), this);
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getAmount() {
		return this.amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	public Timestamp getBeginDate() {
		return this.beginDate;
	}

	public void setBeginDate(Timestamp beginDate) {
		this.beginDate = beginDate;
	}

	public String getCouponeCode() {
		return this.couponeCode;
	}

	public void setCouponeCode(String couponeCode) {
		this.couponeCode = couponeCode;
	}

	public Long getCouponeId() {
		return this.couponeId;
	}

	public void setCouponeId(Long couponeId) {
		this.couponeId = couponeId;
	}

	public Timestamp getEndDate() {
		return this.endDate;
	}

	public void setEndDate(Timestamp endDate) {
		this.endDate = endDate;
	}

	public Timestamp getIssueTime() {
		return this.issueTime;
	}

	public void setIssueTime(Timestamp issueTime) {
		this.issueTime = issueTime;
	}

	public Integer getLimitDay() {
		return this.limitDay;
	}

	public void setLimitDay(Integer limitDay) {
		this.limitDay = limitDay;
	}

	public String getOrderNo() {
		return orderNo;
	}

	public void setOrderNo(String orderNo) {
		this.orderNo = orderNo;
	}

	public Long getOrgId() {
		return this.orgId;
	}

	public void setOrgId(Long orgId) {
		this.orgId = orgId;
	}

	public Integer getState() {
		return this.state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public Long getTicketId() {
		return this.ticketId;
	}

	public void setTicketId(Long ticketId) {
		this.ticketId = ticketId;
	}

	public Integer getType() {
		return this.type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Timestamp getUseTime() {
		return this.useTime;
	}

	public void setUseTime(Timestamp useTime) {
		this.useTime = useTime;
	}

	public Long getUserId() {
		return this.userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getVersion() {
		return this.version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	public Double getLimitValue() {
		return limitValue==null?0:limitValue;
	}

	public void setLimitValue(Double limitValue) {
		this.limitValue = limitValue;
	}
	
	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}
	
	public String getTicketTitle() {
		return ticketTitle;
	}

	public void setTicketTitle(String ticketTitle) {
		this.ticketTitle = ticketTitle;
	}
	

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	
	public Integer getValidityTime() {
		return validityTime;
	}

	public void setValidityTime(Integer validityTime) {
		this.validityTime = validityTime;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	
	public String getRemark() {
		return remark==null?"":remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public Integer getAmountLocal() {
		if (this.amountLocal == null && this.amount != null) {
			if (Commons.CURRENCY_LOCAL.equals(this.currency)||this.currency==null)
				return this.amount;
			IDictionaryService dictionaryService = ContextUtil.getBeanByName(IDictionaryService.class, "dictionaryService");
			if (dictionaryService != null) {
				Currency currency = dictionaryService.findCurrencyByCode(this.currency);
				if (currency != null)
					return (int)MathUtil.divMax(0, this.amount, currency.getRate());
			}
		}
		return amountLocal;
	}


	public String getTypeStr() {
		if(this.type==Commons.COUPONE_TYPE_EXPRESS)
			return "抵邮券";
		else if(this.type==Commons.COUPONE_TYPE_LIMIT)
			return "满减券";
		else if(this.type==Commons.COUPONE_TYPE_CASH)
			return "现金券";
		return  null;
	}

	/**
	 * 初始化默认值
	 */
	public void initDefaultValue() {
		if (this.state == null)
			this.state = 0;
		if (this.orgId == null)
			this.orgId = 0L;
		if (this.userId == null)
			this.userId = 0L;
		if (this.issueTime == null) {
			Calendar calendar = Calendar.getInstance();
			calendar.set(Calendar.YEAR, 1900);
			calendar.set(Calendar.MONTH, 1);
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			this.issueTime = new Timestamp(calendar.getTimeInMillis());
		}
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
		CouponeDetail other = (CouponeDetail) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}