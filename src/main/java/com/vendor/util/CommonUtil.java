package com.vendor.util;

import java.util.*;

import com.vendor.po.Orgnization;

/**
 * 数据信息工具类
 * @author Administrator
 *
 */
public class CommonUtil {
	
	/**
	 * 获取树状组织集的组织ID集合
	 * @param orgnizations 树状组织集
	 */
	public static List<Long> getOrgIdsByCyclicOrgnization(List<Orgnization> orgnizations) {
		List<Long> orgIds = new ArrayList<Long>();
		searchCyclicOrgnization(orgnizations, orgIds);
		
		return orgIds;
	}
	
	/**
	 * 获取树状组织集的组织ID集合
	 * @param orgnizations 树状组织集
	 * @param orgIds 组织ID集合
	 */
	public static void searchCyclicOrgnization(List<Orgnization> orgnizations, List<Long> orgIds) {
		if (null == orgIds)
			orgIds = new ArrayList<Long>();
		
		for (Orgnization orgnization : orgnizations) {
			orgIds.add(orgnization.getId());
			
			if (null != orgnization.getOrgnizations() && !orgnization.getOrgnizations().isEmpty())
				searchCyclicOrgnization(orgnization.getOrgnizations(), orgIds);
		}
	}

	/**
	 * 获取树状组织集的平级结构组织集
	 * @param orgnizations 树状组织集
	 */
	public static List<Orgnization> getOrgnizationsByCyclicOrgnization(List<Orgnization> orgnizations) {
		List<Orgnization> finalOrgnizations = new ArrayList<Orgnization>();
		searchCyclicPeersOrgnization(orgnizations, finalOrgnizations);
		
		return finalOrgnizations;
	}
	
	/**
	 * 获取树状组织集的平级结构组织集
	 * @param orgnizations 树状组织集
	 */
	public static void searchCyclicPeersOrgnization(List<Orgnization> orgnizations, List<Orgnization> finalOrgnizations) {
		if (null == finalOrgnizations)
			finalOrgnizations = new ArrayList<Orgnization>();
		
		for (Orgnization orgnization : orgnizations) {
			finalOrgnizations.add(orgnization);
			
			if (null != orgnization.getOrgnizations() && !orgnization.getOrgnizations().isEmpty())
				searchCyclicPeersOrgnization(orgnization.getOrgnizations(), finalOrgnizations);
		}
	}
	
	/** 
	 * 把所有的推送消息，截取成15条一次
	 * @param lists 所有需要推送的消息
	 * @param leng  截取的长度
	 * @return
	 * List<List<T>> 返回类型 
	 */
	public static <T> List<List<T>> fatherList(List<T> lists, int leng) {
		List<List<T>> fatherList = new ArrayList<>();
		int sd = lists.size() / leng;
		int yu = lists.size() % leng;
		for (int i = 0; i < sd; i++)
			fatherList.add(lists.subList(i * leng, i * leng + leng));

		if (yu != 0)
			fatherList.add(lists.subList(lists.size() - yu, lists.size()));
		return fatherList;
	}
	
	
	public static String converToString(String[] strArr, String separator) {
		StringBuilder builder = new StringBuilder("");
		if (strArr != null && strArr.length > 0)
			for (int i = 0; i < strArr.length; i++)
				builder.append(strArr[i] + separator);

		builder.setLength(builder.length() - 1);
		return builder.toString();
	}
	
	public static String[] convertToStringArr(List<Object> objList) {
		String[] strArr = new String[objList.size()];
		for (int i = 0; i < strArr.length; i++)
			strArr[i] = objList.get(i).toString();
		return strArr;
	}

	/**
	 * 保留两位小数
	 * @param num
	 * @return
	 */
	public static double getFormatTwoNum(double num) {
		java.text.DecimalFormat df = new java.text.DecimalFormat("#.##");
		return Double.valueOf(df.format(num));

	}

    /**
     * @Title:取到两个数组的交集
     * @param arr1
     * @param arr2
     * @return
     * @return: Long[]
     */
    public static Long[] intersect(Long[] arr1, Long[] arr2){
        List<Long> l = new LinkedList<Long>();
        Set<Long> common = new HashSet<Long>();
        for(Long str:arr1){
            if(!l.contains(str)){
                l.add(str);
            }
        }
        for(Long str:arr2){
            if(l.contains(str)){
                common.add(str);
            }
        }
        Long[] result={};
        return common.toArray(result);
    }

    /**
     * @Title:求两个数组的差集
     * @param arr1
     * @param arr2
     * @return
     * @return: String[]
     */
    public static Long[] substract(Long[] arr1, Long[] arr2) {
        LinkedList<Long> list = new LinkedList<Long>();
        for (Long str : arr1) {
            if(!list.contains(str)) {
                list.add(str);
            }
        }
        for (Long str : arr2) {
            if (list.contains(str)) {
                list.remove(str);
            }
        }
        Long[] result = {};
        return list.toArray(result);
    }

	/**
	 * 获取银行代码
	 * @param type
	 * @return
	 */
	public static String getBankCodeByType(Integer type) {
		switch (type) {
			case 1:
				return "0102"; // 中国工商银行
			case 2:
				return "0105"; // 中国建设银行
			case 3:
				return "0308"; // 招商银行
			case 4:
				return "0104"; // 中国银行
			case 5:
				return "0103"; // 中国农业银行
			case 6:
				return "0301"; // 交通银行
			case 7:
				return "0302"; // 中信银行
			case 8:
				return "0303"; // 光大银行
			case 9:
				return "0304"; // 华夏银行
			case 10:
				return "0305"; // 中国民生银行
			case 11:
				return ""; // 平安银行
			case 12:
				return "0309"; // 兴业银行
			case 13:
				return "0310"; // 浦发银行
			case 14:
				return "0306"; // 广发银行
			case 15:
				return "0403"; // 中国邮政储蓄银行
			case 16:
				return ""; // 贵阳银行
			case 17:
				return "04031000"; // 北京银行
			case 18:
				return "04083320"; // 宁波银行
			case 19:
				return "04243010"; // 南京银行
			case 20:
				return "04256020"; // 东莞银行
			default:
				return "";
		}
	}
	
	/**
	 * 根据货柜型号取到设备类型描述
	 * @param model
	 * @return
	 */
	public static String getDeviceTypeStrByModel(String model) {
		switch(model) {
			case Commons.DEVICE_MODEL_DRINK:
				return Commons.DEVICE_TYPE_STR_DRINK;// 饮料机
			case Commons.DEVICE_MODEL_DRINK_SMALL:
				return Commons.DEVICE_TYPE_STR_DRINK_SMALL;// 小型饮料机
			case Commons.DEVICE_MODEL_CENTER_CONTROL:
				return Commons.DEVICE_TYPE_STR_CENTER_CONTROL;// 中控机
			case Commons.DEVICE_MODEL_SPRING:
				return Commons.DEVICE_TYPE_STR_SPRING;// 弹簧机
			case Commons.DEVICE_MODEL_CATERPILLAR:
				return Commons.DEVICE_TYPE_STR_CATERPILLAR;// 履带机
			case Commons.DEVICE_MODEL_GRID64:
				return Commons.DEVICE_TYPE_STR_GRID64;// 64门格子柜
			case Commons.DEVICE_MODEL_GRID40:
				return Commons.DEVICE_TYPE_STR_GRID40;// 40门格子柜
			case Commons.DEVICE_MODEL_GRID60:
				return Commons.DEVICE_TYPE_STR_GRID60;// 60门格子柜
			case Commons.DEVICE_MODEL_INTELLIGENT_PRODUCT:
				return Commons.DEVICE_TYPE_STR_INTELLIGENT_PRODUCT;// 智能商品机
			default:
				return "";
		}
	}
	
	/**
	 * 根据货柜型号取到货道数量
	 * @param model
	 * @return
	 */
	public static Integer getAisleCountByModel(String model) {
		switch(model) {
		case Commons.DEVICE_MODEL_DRINK:
			return 21;// 饮料机
		case Commons.DEVICE_MODEL_DRINK_SMALL:
			return 10;// 小型饮料机
		case Commons.DEVICE_MODEL_CENTER_CONTROL:
			return 0;// 中控机
		case Commons.DEVICE_MODEL_SPRING:
		case Commons.DEVICE_MODEL_CATERPILLAR:
			return 48;// 弹簧机||履带机
		case Commons.DEVICE_MODEL_GRID64:
			return 64;// 64门格子柜
		case Commons.DEVICE_MODEL_GRID40:
			return 40;// 40门格子柜
		case Commons.DEVICE_MODEL_GRID60:
			return 60;// 60门格子柜
		case Commons.DEVICE_MODEL_INTELLIGENT_PRODUCT:
			return 50;// 智能商品机
		default:
			return -1;
		}
	}
	
	/**
	 * 根据货柜型号取到设备类型
	 * @param model
	 * @return
	 */
	public static Integer getDeviceTypeByModel(String model) {
		switch(model) {
			case Commons.DEVICE_MODEL_DRINK:
				return Commons.DEVICE_TYPE_DRINK;// 饮料机
			case Commons.DEVICE_MODEL_DRINK_SMALL:
				return Commons.DEVICE_TYPE_DRINK_SMALL;// 小型饮料机
			case Commons.DEVICE_MODEL_CENTER_CONTROL:
				return Commons.DEVICE_TYPE_CENTER_CONTROL;// 中控机
			case Commons.DEVICE_MODEL_SPRING:
				return Commons.DEVICE_TYPE_SPRING;// 弹簧机
			case Commons.DEVICE_MODEL_CATERPILLAR:
				return Commons.DEVICE_TYPE_CATERPILLAR;// 履带机
			case Commons.DEVICE_MODEL_GRID64:
				return Commons.DEVICE_TYPE_GRID64;// 64门格子柜
			case Commons.DEVICE_MODEL_GRID40:
				return Commons.DEVICE_TYPE_GRID40;// 40门格子柜
			case Commons.DEVICE_MODEL_GRID60:
				return Commons.DEVICE_TYPE_GRID60;// 60门格子柜
			case Commons.DEVICE_MODEL_INTELLIGENT_PRODUCT:
				return Commons.DEVICE_TYPE_INTELLIGENT_PRODUCT;// 智能商品机
			default:
				return -1;
		}
	}
}
