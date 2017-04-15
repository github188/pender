package com.vendor.util.msg;

import java.util.Properties;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

public class PropertUtil {

	private static PropertUtil install = null;
	public static Properties prop = null;

	private PropertUtil() {
	}

	static {
		try {
			prop = PropertiesLoaderUtils.loadProperties(new ClassPathResource("init.properties"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static PropertUtil getInstall() {
		if (null == install) {
			install = new PropertUtil();
		}
		return install;
	}

}
