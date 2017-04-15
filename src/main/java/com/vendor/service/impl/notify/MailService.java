/**
 * 
 */
package com.vendor.service.impl.notify;

import java.util.Date;
import java.util.Map;

import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import com.vendor.service.IMailService;

import freemarker.template.Template;

/**
 * @author dranson on 2015年12月29日
 */
@Service("mailService")
public class MailService implements IMailService {
	
	private final static Logger logger = Logger.getLogger(MailService.class);

	private JavaMailSender mailSender;

	private FreeMarkerConfig freeMarker;

	/*(non-Javadoc)
	 * @see com.ecarry.service.IMailService#sendMail(org.springframework.mail.SimpleMailMessage, java.lang.Boolean)
	 */
	@Override
	public <T> boolean sendMail(SimpleMailMessage mail, Boolean text) {
		if (text) {
			return sendMailText(mail);
		} else {
			return sendMailRichText(mail);
		}
	}

	/*(non-Javadoc)
	 * @see com.ecarry.service.IMailService#sendMailText(org.springframework.mail.SimpleMailMessage)
	 */
	@Override
	public <T> boolean sendMailText(SimpleMailMessage mail) {
		mail.setSentDate(new Date());
		mail.setFrom(((JavaMailSenderImpl) mailSender).getUsername());
		try {
			this.mailSender.send(mail);
			return true;
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
	}

	/*(non-Javadoc)
	 * @see com.ecarry.service.IMailService#sendMailRichText(org.springframework.mail.SimpleMailMessage)
	 */
	@Override
	public <T> boolean sendMailRichText(SimpleMailMessage mail) {
		try {
			MimeMessage msg = this.mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
			helper.setFrom(((JavaMailSenderImpl) mailSender).getUsername());
			helper.setTo(mail.getTo());
			helper.setBcc(mail.getBcc());
			helper.setCc(mail.getCc());
			helper.setSubject(mail.getSubject());
			helper.setText(mail.getText(), true);
			this.mailSender.send(msg);
			return true;
		} catch (Exception e) {
			logger.error(e);
			return false;
		}
	}

	/*(non-Javadoc)
	 * @see com.ecarry.service.IMailService#sendMail(java.lang.String, java.lang.String, java.util.Map, java.lang.String)
	 */
	@Override
	public boolean sendMail(String subject, String to, Map<String, Object> model, String templet) {
		return sendMail(subject, new String[]{to}, model, templet, null);
	}

	/*(non-Javadoc)
	 * @see com.ecarry.service.IMailService#sendMail(java.lang.String, java.lang.String, java.util.Map, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean sendMail(String subject, String to, Map<String, Object> model, String templet, String[] images) {
		return sendMail(subject, new String[]{to}, model, templet, images);
	}

	/*(non-Javadoc)
	 * @see com.ecarry.service.IMailService#sendMail(java.lang.String, java.lang.String[], java.util.Map, java.lang.String)
	 */
	@Override
	public boolean sendMail(String subject, String[] toes, Map<String, Object> model, String templet) {
		return sendMail(subject, toes, model, templet, null);
	}

	/*(non-Javadoc)
	 * @see com.ecarry.service.IMailService#sendMail(java.lang.String, java.lang.String[], java.util.Map, java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean sendMail(String subject, String[] toes, Map<String, Object> model, String templet, String[] images) {
		try {
			// 建立邮件消息,发送简单邮件和html邮件的区别
			MimeMessage mailMessage = mailSender.createMimeMessage();
			// 注意这里的boolean,等于真的时候才能嵌套图片，在构建MimeMessageHelper时候，所给定的值是true表示启用，
			// multipart模式
			MimeMessageHelper messageHelper = new MimeMessageHelper(mailMessage, true, "UTF-8");			
			// 设置收件人，寄件人
			messageHelper.setTo(toes);
			messageHelper.setSentDate(new Date());
			messageHelper.setFrom(((JavaMailSenderImpl) mailSender).getUsername());
			messageHelper.setSubject(subject);			
			Template template = freeMarker.getConfiguration().getTemplate(templet);
			messageHelper.setText(FreeMarkerTemplateUtils.processTemplateIntoString(template, model), true);
			mailSender.send(mailMessage);
			return false;
		} catch (Exception e) {
			logger.error(e);
			return true;
		}
	}

	public void setMailSender(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	public void setFreeMarker(FreeMarkerConfig freeMarker) {
		this.freeMarker = freeMarker;
	}
}
