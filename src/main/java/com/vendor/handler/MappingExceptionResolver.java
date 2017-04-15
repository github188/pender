package com.vendor.handler;

import com.ecarry.core.exception.AccessException;
import com.ecarry.core.exception.BusinessException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.SimpleMappingExceptionResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Chris
 */
public class MappingExceptionResolver extends SimpleMappingExceptionResolver {

    private static final Logger logger = Logger.getLogger(com.ecarry.core.web.core.MappingExceptionResolver.class);

    @Value("${system.exception}")
    private String debug;

    /* (non-Javadoc)
     * @see org.springframework.web.servlet.handler.AbstractHandlerExceptionResolver#logException(java.lang.Exception, javax.servlet.http.HttpServletRequest)
     */
    @Override
    protected void logException(Exception ex, HttpServletRequest request) {
    }

    @Override
    protected ModelAndView doResolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        if (!(ex instanceof BusinessException || ex instanceof AccessException)) {
            logger.error("system exception throw by framework", ex);
            ex = new BusinessException("系统出现异常，请联系管理员！");
        }
        StackTraceElement[] stackTraceElements = new StackTraceElement[0];
        ex.setStackTrace(stackTraceElements);
        return super.doResolveException(request, response, handler, ex);
    }
}