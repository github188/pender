package com.vendor.util;

import com.vendor.control.app.FreeControl;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

/**
 * Created by Chris on 2017/3/21.
 * RequestMappingUtil测试类
 *
 * @see RequestMappingUtil
 */
public class RequestMappingUtilTest {
    @Test
    public void getMethodRequestMappings() throws Exception {
        String[] mappings = RequestMappingUtil.getMethodRequestMappings(FreeControl.class, "syncActiveCodeState",
                String.class, String.class, Integer.class);
        assertNotNull(mappings);
        System.out.printf("getMethodRequestMappings return " + Arrays.toString(mappings));
    }

    @Test
    public void getClassRequestMappings() throws Exception {
        String[] mappings = RequestMappingUtil.getClassRequestMappings(FreeControl.class);
        assertNotNull(mappings);
        System.out.printf("getClassRequestMappings return " + Arrays.toString(mappings));
    }

    @Test
    public void getClassRequestMapping() throws Exception {
        String mapping = RequestMappingUtil.getClassRequestMapping(FreeControl.class, 0);
        assertNotNull(mapping);
        System.out.printf("getClassRequestMapping return " + mapping);
    }

    @Test
    public void getMethodRequestMapping() throws Exception {
        String mapping = RequestMappingUtil.getMethodRequestMapping(FreeControl.class, "syncActiveCodeState", 0,
                String.class, String.class, Integer.class);
        assertNotNull(mapping);
        System.out.printf("getMethodRequestMapping return " + mapping);
    }

    @Test
    public void getFullRequestMapping() throws Exception {
        String mapping = RequestMappingUtil.getFullRequestMapping(FreeControl.class, "syncActiveCodeState", 0, 0,
                String.class, String.class, Integer.class);
        assertNotNull(mapping);
        System.out.printf("getFullRequestMapping return " + mapping);
    }

    @Test
    public void getDefaultFullRequestMapping() throws Exception {
        String mapping = RequestMappingUtil.getDefaultFullRequestMapping(FreeControl.class, "syncActiveCodeState",
                String.class, String.class, Integer.class);
        assertNotNull(mapping);
        System.out.printf("getDefaultFullRequestMapping return " + mapping);
    }

}