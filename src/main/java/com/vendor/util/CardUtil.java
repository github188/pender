package com.vendor.util;

/**
 * 银行卡工具类
 * 
 * @author liujia on 2016年4月6日
 */
public class CardUtil {
	/*
	 *  该校验的过程： 
	 *  1、从卡号最后一位数字开始，逆向将奇数位(1、3、5等等)相加。 
	 *  2、从卡号最后一位数字开始，逆向将偶数位数字，先乘以2（如果乘积为两位数，则将其减去9），再求和。
	 *  3、将奇数位总和加上偶数位总和，结果应该可以被10整除。 例如，卡号是：5432123456788881 则奇数、偶数位（用红色标出）分布：5432123456788881 奇数位和=35 偶数位乘以2（有些要减去9）的结果：1 6 2 6 1 5 7 7，求和=35。 
	 *    最后35+35=70 可以被10整除，认定校验通过。
	 */

	/**
	 * 校验银行卡卡号
	 * 
	 * @param cardId
	 * @return
	 */
	public static boolean checkBankCard(String cardId) {
		char bit = getBankCardCheckCode(cardId.substring(0, cardId.length() - 1));
		if (bit == 'N') {
			return false;
		}
		return cardId.charAt(cardId.length() - 1) == bit;
	}

	/**
	 * 从不含校验位的银行卡卡号采用 Luhm 校验算法获得校验位
	 * 
	 * @param nonCheckCodeCardId
	 * @return
	 */
	public static char getBankCardCheckCode(String nonCheckCodeCardId) {
		if (nonCheckCodeCardId == null || nonCheckCodeCardId.trim().length() == 0 || !nonCheckCodeCardId.matches("\\d+")) {
			// 如果传的不是数据返回N
			return 'N';
		}
		char[] chs = nonCheckCodeCardId.trim().toCharArray();
		int luhmSum = 0;
		for (int i = chs.length - 1, j = 0; i >= 0; i--, j++) {
			int k = chs[i] - '0';
			if (j % 2 == 0) {
				k *= 2;
				k = k / 10 + k % 10;
			}
			luhmSum += k;
		}
		return (luhmSum % 10 == 0) ? '0' : (char) ((10 - luhmSum % 10) + '0');
	}
	
	public static void main(String[] args) {
		String cardNo = "6225882403999029";
		System.out.println(checkBankCard(cardNo));
		
	}

}
