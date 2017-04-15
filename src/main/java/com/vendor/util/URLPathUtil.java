package com.vendor.util;

import java.util.ResourceBundle;

/**
 * Created by Chris Zhu on 2017/3/28.
 *
 * @author Chris Zhu
 */
public class URLPathUtil
{
    private static String mBaseUrl;

    static
    {
        ResourceBundle bundle = ResourceBundle.getBundle("url");
        mBaseUrl = bundle.getString("base-url");
    }

    public static String getBaseUrl()
    {
        return mBaseUrl;
    }
}
