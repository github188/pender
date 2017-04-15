/**
 * 
 */
package com.vendor.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;

/**
 * @author dranson on 2016年4月20日
 */
public class AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private RequestCache requestCache;

	private String targetMobileUrl;
	private String[] urls;
	private AntPathMatcher matcher;

	public AuthenticationSuccessHandler() {
		this.requestCache = new HttpSessionRequestCache();
		this.matcher = new AntPathMatcher();
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler#onAuthenticationSuccess(javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse, org.springframework.security.core.Authentication)
	 */
	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws ServletException, IOException {
		SavedRequest savedRequest = requestCache.getRequest(request, response);

		if (savedRequest == null) {
			super.onAuthenticationSuccess(request, response, authentication);
			return;
		}
		String targetUrlParameter = getTargetUrlParameter();
		if (isAlwaysUseDefaultTargetUrl() || (targetUrlParameter != null && StringUtils.hasText(request.getParameter(targetUrlParameter)))) {
			requestCache.removeRequest(request, response);
			super.onAuthenticationSuccess(request, response, authentication);

			return;
		}

		clearAuthenticationAttributes(request);

		// Use the DefaultSavedRequest URL
		String targetUrl = savedRequest.getRedirectUrl();
		logger.debug("Redirecting to DefaultSavedRequest Url: " + targetUrl);
		getRedirectStrategy().sendRedirect(request, response, targetUrl);
	}

	@Override
	protected String determineTargetUrl(HttpServletRequest request, HttpServletResponse response) {
		String path = request.getServletPath();
		String targetUrl = null;
		if (urls == null) {
			targetUrl = getDefaultTargetUrl();
		} else {
			if (matcher.match("/login", path)) {
				String root = request.getRequestURL().substring(0, request.getRequestURL().length() - 6);
				path = request.getHeader("Referer").replace(root, "");
			}
			boolean found = false;
			for (String url : urls) {
				if (matcher.match(url, path)) {
					targetUrl = targetMobileUrl;
					found = true;
					break;
				}
			}
			if (!found)
				targetUrl = getDefaultTargetUrl();;
		}
		if (isAlwaysUseDefaultTargetUrl()) {
			return targetUrl;
		}

		if (getTargetUrlParameter() != null) {
			targetUrl = request.getParameter(getTargetUrlParameter());

			if (StringUtils.hasText(targetUrl)) {
				logger.debug("Found targetUrlParameter in request: " + targetUrl);

				return targetUrl;
			}
		}
		if (!StringUtils.hasLength(targetUrl)) {
			targetUrl = request.getHeader("Referer");
			logger.debug("Using Referer header: " + targetUrl);
		}

		if (!StringUtils.hasText(targetUrl)) {
			targetUrl = getDefaultTargetUrl();
			logger.debug("Using default Url: " + targetUrl);
		}

		return targetUrl;
	}

	public void setRequestCache(RequestCache requestCache) {
		this.requestCache = requestCache;
	}

	public void setTargetMobileUrl(String targetMobileUrl) {
		this.targetMobileUrl = targetMobileUrl;
	}

	public void setUrls(String[] urls) {
		this.urls = urls;
	}
}
