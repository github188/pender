/**
 * 
 */
package com.vendor.thirdparty.et;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonFilter;

/**
 * @author dranson on 2016年6月3日
 */
@JsonFilter("com.vendor.po.ETTime")
public class ETTime implements Serializable {
	
	private Integer date;
	
	private Integer hours;
	
	private Integer seconds;
	
	private Integer month;
	
	private Integer nanos;
	
	private Integer timezoneOffset;
	
	private Integer year;
	
	private Integer minutes;
	
	private Long time;
	
	private Integer day;

	public Integer getDate() {
		return date;
	}

	public void setDate(Integer date) {
		this.date = date;
	}

	public Integer getHours() {
		return hours;
	}

	public void setHours(Integer hours) {
		this.hours = hours;
	}

	public Integer getSeconds() {
		return seconds;
	}

	public void setSeconds(Integer seconds) {
		this.seconds = seconds;
	}

	public Integer getMonth() {
		return month;
	}

	public void setMonth(Integer month) {
		this.month = month;
	}

	public Integer getNanos() {
		return nanos;
	}

	public void setNanos(Integer nanos) {
		this.nanos = nanos;
	}

	public Integer getTimezoneOffset() {
		return timezoneOffset;
	}

	public void setTimezoneOffset(Integer timezoneOffset) {
		this.timezoneOffset = timezoneOffset;
	}

	public Integer getYear() {
		return year;
	}

	public void setYear(Integer year) {
		this.year = year;
	}

	public Integer getMinutes() {
		return minutes;
	}

	public void setMinutes(Integer minutes) {
		this.minutes = minutes;
	}

	public Long getTime() {
		return time;
	}

	public void setTime(Long time) {
		this.time = time;
	}

	public Integer getDay() {
		return day;
	}

	public void setDay(Integer day) {
		this.day = day;
	}
}
