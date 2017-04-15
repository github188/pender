package com.vendor.util;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ecarry.core.exception.BusinessException;

public abstract class VendingUtil {
	private static Logger logger = Logger.getLogger(VendingUtil.class);
	
	public static DecimalFormat df = new DecimalFormat("#.##");
	
	public static SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
	
	public static String toSign(Map<String, Object> param) {
		if (param == null)
			return "";
		StringBuffer sb = new StringBuffer();
		for (String key : param.keySet()) {
			sb.append(key).append("=").append(param.get(key)).append("&");
		}
		return sb.toString();
	}

	/**
	 * @param o传入的对象
	 * @param url接口url
	 * @param bl 是否
	 ***/
	public static Map<String, Object> toMap(Object o, String url, String auth,String orgId) {
		if (o == null)
			return null;
		Map<String, Object> map = new HashMap<>();
		Field fields[] = o.getClass().getDeclaredFields();
		for (Field f : fields) {
			f.setAccessible(true);
			try {
				FieldMeta meta = f.getAnnotation(FieldMeta.class);
				if (meta != null && meta.nullable())
					continue;
				Object value = f.get(o);
				if(value!=null){
					value = URLEncoder.encode(value.toString(),"UTF-8");
				}
                map.put(f.getName(), value);
			} catch (IllegalArgumentException e) {
				return null;
			} catch (IllegalAccessException e) {
				return null;
			} catch (UnsupportedEncodingException e) {
				return null;
			}
		}
		return addMap(map, url, auth,orgId);
	}

	public static Map<String, Object> addMap(Map<String, Object> map, String url, String auth,String orgId) {
        if(map==null)
        	return null;
		map.put("hash", "");
		map.put("signType", "sha-1");
		map.put("v", "1.2.0");
		map.put("dataType", "json");
		map.put("t", System.currentTimeMillis());
		map.put("auth", auth);
		map.put("mti", url);
		map.put("org_id", orgId);
		return map;
	}

	/**
	 * @param bean 
	 * @param paramType
	 * @param data
	 * @param bl
	 ****/
	@SuppressWarnings("unchecked")
	public static Object toBean(Class<?> beanClass, Class<?> paramClass, Map<String, Object> data, boolean bl) {
		if (beanClass == null && data == null) {
			return null;
		}
		if(beanClass.isInterface())
			throw new BusinessException("bean Class was interface!");
		Object bean = null;
		String field = null;
		try {
			bean = beanClass.newInstance();
			Method[] methods = beanClass.getDeclaredMethods();
			for (Method method : methods) {
				if (method.getName().startsWith("set")) {
					field = method.getName();
					field = field.substring(field.indexOf("set") + 3);
					if (bl) {
						field = field.toLowerCase().charAt(0) + field.substring(1);
					}
					Class<?>[] param = method.getParameterTypes();
					if (param[0].isAssignableFrom(List.class)) {
						final String finalField = field;
						List<Object> body = (List) data.get(field);
						if(body==null)continue;
						List<Object> newBody = new ArrayList<Object>();
						for (Object o : body) {
							if (Map.class.isAssignableFrom(o.getClass())) {
								Map<String, Object> map = (Map<String, Object>) o;
								Object obj= toBean(paramClass, null, map, true);
								if(obj!=null){
									newBody.add(obj);
								}
							} else if(List.class.isAssignableFrom(o.getClass())){
								//TODO
							}
						}
						data.put(finalField, newBody);
					} else if (param[0].isAssignableFrom(Map.class)) {
						final String finalField = field;
						Map<String, Object> body = (Map) data.get(field);
						if (body == null) continue;
						Map<String, Object> newBody = body;
						data.put(finalField, newBody);
					}
					Object object = data.get(field);
					method.invoke(bean, new Object[] { object });
				}
			}
		} catch (Exception e) {
			logger.error("Fileld------->"+ field,e);
			throw new BusinessException("JSON 转 object 出现异常!");
		}
		return bean;
	}

	public static String getSHA1Value(String value, boolean bl) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			digest.update(value.getBytes());
			if (bl) {
				return toHex(digest.digest()).toUpperCase();
			}
			return toHex(digest.digest());
		} catch (Exception e) {
			return null;
		}
	}

	public static String toHex(byte[] bytes) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			String tmp = (Integer.toHexString(bytes[i] & 0xFF));
			if (tmp.length() == 1)
				buf.append("0");
			buf.append(tmp);
		}
		return buf.toString();
	}

}
