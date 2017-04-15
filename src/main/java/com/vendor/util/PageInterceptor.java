package com.vendor.util;

import org.springframework.web.method.HandlerMethod;

import com.ecarry.core.web.core.PageEasyUIInterceptor;
import com.vendor.vo.app.RestStatus;
import com.vendor.vo.common.ResultAppBase;

public class PageInterceptor extends PageEasyUIInterceptor{

    @Override
    public boolean isSupport(HandlerMethod handler) {
        return super.isSupport(handler)|| ResultAppBase.class.isAssignableFrom(handler.getReturnType().getParameterType()) || RestStatus.class.isAssignableFrom(handler.getReturnType().getParameterType());
    }
}
