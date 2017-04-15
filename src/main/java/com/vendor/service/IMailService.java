package com.vendor.service;

import java.util.Map;

import org.springframework.mail.SimpleMailMessage;
/**
 * 邮件通用工具类
 * @author dranson on 2015年12月29日
 */
public interface IMailService {
	/**
	 * 发送邮件
	 * @param mail	需要发送的邮件
	 * @param bool	是否为简单格式文本邮件
	 * @return	是否发送成功
	 */
	public <T> boolean sendMail(SimpleMailMessage mail, Boolean text);
	/**
	 * 发送简单格式文本邮件
	 * @param mail	需要发送的邮件
	 * @return	是否发送成功
	 */
	public <T> boolean sendMailText(SimpleMailMessage mail);
	/**
	 * 发送富文本邮件
	 * @param mail	需要发送的邮件
	 * @return	是否发送成功
	 */
	public <T> boolean sendMailRichText(SimpleMailMessage mail);
	/**
	 * 发送模板邮件
	 * @param subject	邮件标题
	 * @param to	收件邮箱
	 * @param model	模板数据
	 * @param templet	模板文件
	 * @return	是否发送成功
	 */
	public boolean sendMail(String subject, String to, Map<String, Object> model, String templet);
	/**
	 * 发送模板邮件
	 * @param subject	邮件标题
	 * @param to	收件邮箱
	 * @param model	模板数据
	 * @param templet	模板文件
	 * @param images	图片
	 * @return	是否发送成功
	 */
	public boolean sendMail(String subject, String to, Map<String, Object> model, String templet, String[] images);
	/**
	 * 群发模板邮件
	 * @param subject	邮件标题
	 * @param toes	收件邮箱集
	 * @param model	模板数据
	 * @param templet	模板文件
	 * @return	是否发送成功
	 */
	public boolean sendMail(String subject, String[] toes, Map<String, Object> model, String templet);
	/**
	 * 群发模板邮件
	 * @param subject	邮件标题
	 * @param tos	收件邮箱集
	 * @param model	模板数据
	 * @param templet	模板文件
	 * @param images	图片
	 * @return	是否发送成功
	 */
	public boolean sendMail(String subject, String[] toes, Map<String, Object> model, String templet, String[] images);
}
