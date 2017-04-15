package com.vendor.util;

import java.util.Date;
import java.util.Random;

/**
 * Created by chenying on 2015/6/10. 生成随机数
 */
public abstract class RandomUtil {

	/**
	 * @return 随机字符串
	 *         <p/>
	 *         **
	 */
	public static String random(int length) {// 传入的字符串的长度
		StringBuilder builder = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			int r = (int) (Math.random() * 3);
			int rn1 = (int) (48 + Math.random() * 10);
			int rn2 = (int) (65 + Math.random() * 26);
			int rn3 = (int) (97 + Math.random() * 26);
			switch (r) {
			case 0:
				builder.append((char) rn1);
				break;
			case 1:
				builder.append((char) rn2);
				break;
			case 2:
				builder.append((char) rn3);
				break;
			}
		}
		return builder.toString();
	}

	/**
	 * @return 随机数字 随机优惠码规则：共n位，数字+英文字
	 *         <p/>
	 *         **
	 */
	public static String getCharAndNumr(int length) {
		String val = "";
		Random random = new Random();
		for (int i = 0; i < length; i++) {
			// 输出字母还是数字
			String charOrNum = random.nextInt(2) % 2 == 0 ? "char" : "num";
			// 字符串
			if ("char".equalsIgnoreCase(charOrNum)) {
				// 取得大写字母还是小写字母
				int choice = random.nextInt(2) % 2 == 0 ? 65 : 97;
				val += (char) (choice + random.nextInt(26));
			} else if ("num".equalsIgnoreCase(charOrNum)) { // 数字
				val += String.valueOf(random.nextInt(10));
			}
		}
		return val;
	}

	/**
	 * @return 随机数字
	 *         <p/>
	 *         **
	 */
	public static int buildRandom(int length) {
		int num = 1;
		double random = Math.random();
		if (random < 0.1) {
			random = random + 0.1;
		}
		for (int i = 0; i < length; i++) {
			num = num * 10;
		}
		return (int) ((random * num));
	}
	
	/**
	 * @return 生成唯一随机数
	 *         <p/>
	 *         **
	 */
	  private static Date date = new Date();  
	  private static StringBuilder buf = new StringBuilder();  
	  private static int seq = 0;  
	  private static final int ROTATION = 99999;  
	  
	  public static synchronized long next(){  
	    if (seq > ROTATION) seq = 0;  
	    buf.delete(0, buf.length());  
	    date.setTime(System.currentTimeMillis());  
	    String str = String.format("%1$tY%1$tm%1$td%1$tk%1$tM%1$tS%2$05d", date, seq++);  
	    return Long.parseLong(str);  
	  } 
}
