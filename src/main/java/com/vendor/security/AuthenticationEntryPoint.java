package com.vendor.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.RedirectUrlBuilder;
import org.springframework.util.AntPathMatcher;

public class AuthenticationEntryPoint extends LoginUrlAuthenticationEntryPoint {

	private final Logger logger = Logger.getLogger(getClass());

	private String loginMobileUrl;
	private String[] urls;
	private AntPathMatcher matcher;

	public AuthenticationEntryPoint(String loginFormUrl) {
		super(loginFormUrl);
		this.matcher = new AntPathMatcher();
	}

	@Override
	protected String buildHttpsRedirectUrlForRequest(HttpServletRequest request) throws IOException, ServletException {
		int serverPort = getPortResolver().getServerPort(request);
		Integer httpsPort = getPortMapper().lookupHttpsPort(Integer.valueOf(serverPort));

		if (httpsPort != null) {
			RedirectUrlBuilder urlBuilder = new RedirectUrlBuilder();
			urlBuilder.setScheme("https");
			urlBuilder.setServerName(request.getServerName());
			urlBuilder.setPort(httpsPort.intValue());
			urlBuilder.setContextPath(request.getContextPath());
			urlBuilder.setServletPath(request.getServletPath());
			urlBuilder.setPathInfo(request.getPathInfo());
			urlBuilder.setQuery(request.getQueryString());

			return urlBuilder.getUrl();
		}

		// Fall through to server-side forward with warning message
		logger.warn("Unable to redirect to HTTPS as no port mapping found for HTTP port " + serverPort);
		return null;
	}

	@Override
	protected String determineUrlToUseForThisRequest(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) {
		String path = request.getServletPath();
		for (String url : urls)
			if (matcher.match(url, path))
				return loginMobileUrl;
		return getLoginFormUrl();
	}

	public void setLoginMobileUrl(String loginMobileUrl) {
		this.loginMobileUrl = loginMobileUrl;
	}

	public void setUrls(String[] urls) {
		this.urls = urls;
	}

	public void setMatcher(AntPathMatcher matcher) {
		this.matcher = matcher;
	}
}
