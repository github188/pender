package com.vendor.util;

/**
 * Created by Chris on 2017/2/24.
 * 字符串工具类
 */
public class StringUtil
{
	public static boolean isEmpty(String string)
	{
		return string == null || string.trim().equals("");
	}

	public static String trim(String string)
	{
		return string == null ? "" : string.trim();
	}

	public static String truncateString(String string, int length)
	{
		StringBuilder stringBuilder = new StringBuilder(string);
		if (string.length() <= length)
		{
			return string;
		}
		stringBuilder.setLength(length);
		return stringBuilder.toString();
	}
}
