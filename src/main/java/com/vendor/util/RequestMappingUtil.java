package com.vendor.util;

import com.ecarry.core.web.core.ContextUtil;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by Chris on 2017/3/21.
 * 路径工具类
 */
public class RequestMappingUtil
{

    /**
     * 获取某类某方法的路径集合
     *
     * @param aClass         类
     * @param methodName     方法名
     * @param parameterTypes 方法参数
     * @return the string [ ] 路径集合
     */
    public static String[] getMethodRequestMappings(Class<?> aClass, String methodName, Class<?>... parameterTypes)
    {
        try
        {
            return AnnotationUtils.getAnnotation(aClass.getDeclaredMethod(methodName, parameterTypes), RequestMapping.class).value();
        }
        catch (NoSuchMethodException e)
        {
            e.printStackTrace();
        }
        return null;
    }


    /**
     * 获取某类的路径集合
     *
     * @param aClass 类
     * @return 路径集合
     */
    public static String[] getClassRequestMappings(Class<?> aClass)
    {
        return AnnotationUtils.getAnnotation(aClass, RequestMapping.class).value();
    }


    /**
     * 获取某类的路径
     *
     * @param aClass 类
     * @param index  路径索引
     * @return 路径
     */
    public static String getClassRequestMapping(Class<?> aClass, int index)
    {
        return getClassRequestMappings(aClass)[index];
    }

    /**
     * 获取某类某方法的路径
     *
     * @param aClass         类
     * @param methodName     方法名
     * @param index          路径索引
     * @param parameterTypes 方法参数
     * @return 路径
     */
    public static String getMethodRequestMapping(Class<?> aClass, String methodName, int index, Class<?>... parameterTypes)
    {
        String[] mappings = getMethodRequestMappings(aClass, methodName, parameterTypes);
        if (mappings != null)
        {
            return mappings[index];
        }
        return null;
    }

    /**
     * 获取全路径(类的路径+"/"+方法路径)
     *
     * @param aClass         类
     * @param methodName     方法名
     * @param classIndex     类路径索引
     * @param methodIndex    方法路径索引
     * @param parameterTypes 方法参数
     * @return 全路径
     */
    public static String getFullRequestMapping(Class<?> aClass, String methodName, int classIndex, int methodIndex, Class<?>... parameterTypes)
    {
        return getClassRequestMapping(aClass, classIndex)
                + getMethodRequestMapping(aClass, methodName, methodIndex, parameterTypes);
    }

    /**
     * 获取默认全路径(索引都取0)
     *
     * @param aClass         类
     * @param methodName     方法名
     * @param parameterTypes 方法参数
     * @return 全路径
     */
    public static String getDefaultFullRequestMapping(Class<?> aClass, String methodName, Class<?>... parameterTypes)
    {
        return getFullRequestMapping(aClass, methodName, 0, 0, parameterTypes);
    }

    /**
     * 获取某个方法对应的URL(http://ip:port/xxx/xxx/xxx)
     *
     * @param methodName     方法名
     * @param parameterTypes 方法参数
     * @return url
     */
    public static String getUrl(Class aClass, String methodName, Class<?>... parameterTypes)
    {
        return URLPathUtil.getBaseUrl()
                + getDefaultFullRequestMapping(aClass, methodName, parameterTypes);
    }

}
