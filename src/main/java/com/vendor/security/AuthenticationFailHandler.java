/**
 * 
 */
package com.vendor.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;

/**
 * @author dranson on 2016年4月20日
 */
public class AuthenticationFailHandler implements AuthenticationFailureHandler {

	private final Logger logger = Logger.getLogger(getClass());

	private String defaultFailureUrl;
	private String defaultMobileUrl;
	private boolean forwardToDestination = false;
	private boolean allowSessionCreation = true;
	private RedirectStrategy redirectStrategy;
	private String[] urls;
	private AntPathMatcher matcher;

	public AuthenticationFailHandler() {
		this.redirectStrategy = new DefaultRedirectStrategy();
		this.matcher = new AntPathMatcher();
	}

	/**
	 * Performs the redirect or forward to the {@code defaultFailureUrl} if set, otherwise returns a 401 error code.
	 * <p>
	 * If redirecting or forwarding, {@code saveException} will be called to cache the exception for use in the target view.
	 */
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
		String path = request.getServletPath();
		String defaultUrl = null;
		if (urls == null) {
			defaultUrl = defaultFailureUrl;
		} else {
			if (matcher.match("/login", path)) {
				String root = request.getRequestURL().substring(0, request.getRequestURL().length() - 6);
				if (root.indexOf("//localhost") != -1 || root.indexOf("//127.0.0.1") != -1) {
					path = request.getHeader("Referer");
					String tmp = path.replaceAll("//", "");
					int idx = tmp.indexOf("/");
					path = path.substring(idx + 2);
				} else {
					path = request.getHeader("Referer").replace(root, "");
				}
			}
			boolean found = false;
			for (String url : urls) {
				if (matcher.match(url, path)) {
					defaultUrl = defaultMobileUrl;
					found = true;
					break;
				}
			}
			if (!found)
				defaultUrl = defaultFailureUrl;
		}
		if (defaultUrl == null) {
			logger.debug("No failure URL set, sending 401 Unauthorized error");
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication Failed: " + exception.getMessage());
		} else {
			saveException(request, exception);
			if (forwardToDestination) {
				logger.debug("Forwarding to " + defaultUrl);
				request.getRequestDispatcher(defaultUrl).forward(request, response);
			} else {
				logger.debug("Redirecting to " + defaultUrl);
				redirectStrategy.sendRedirect(request, response, defaultUrl);
			}
		}
	}

	/**
	 * Caches the {@code AuthenticationException} for use in view rendering.
	 * <p>
	 * If {@code forwardToDestination} is set to true, request scope will be used, otherwise it will attempt to store the exception in the session. If there is no session and
	 * {@code allowSessionCreation} is {@code true} a session will be created. Otherwise the exception will not be stored.
	 */
	protected final void saveException(HttpServletRequest request, AuthenticationException exception) {
		if (forwardToDestination) {
			request.setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, exception);
		} else {
			HttpSession session = request.getSession(false);

			if (session != null || allowSessionCreation) {
				request.getSession().setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, exception);
			}
		}
	}

	/**
	 * The URL which will be used as the failure destination.
	 * @param defaultFailureUrl the failure URL, for example "/loginFailed.jsp".
	 */
	public void setDefaultFailureUrl(String defaultFailureUrl) {
		Assert.isTrue(UrlUtils.isValidRedirectUrl(defaultFailureUrl), "'" + defaultFailureUrl + "' is not a valid redirect URL");
		this.defaultFailureUrl = defaultFailureUrl;
	}

	public void setDefaultMobileUrl(String defaultMobileUrl) {
		this.defaultMobileUrl = defaultMobileUrl;
	}

	public void setUrls(String[] urls) {
		this.urls = urls;
	}

	protected boolean isUseForward() {
		return forwardToDestination;
	}

	/**
	 * If set to <tt>true</tt>, performs a forward to the failure destination URL instead of a redirect. Defaults to <tt>false</tt>.
	 */
	public void setUseForward(boolean forwardToDestination) {
		this.forwardToDestination = forwardToDestination;
	}

	public void setRedirectStrategy(RedirectStrategy redirectStrategy) {
		this.redirectStrategy = redirectStrategy;
	}

	protected RedirectStrategy getRedirectStrategy() {
		return redirectStrategy;
	}

	protected boolean isAllowSessionCreation() {
		return allowSessionCreation;
	}

	public void setAllowSessionCreation(boolean allowSessionCreation) {
		this.allowSessionCreation = allowSessionCreation;
	}
}
