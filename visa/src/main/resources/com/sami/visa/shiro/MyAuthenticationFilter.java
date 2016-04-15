/*
 * 

 * 
 */
package net.sahv.blzy.shiro;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Resource;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.filter.authc.PassThruAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;

/**
 * Filter - 权限认证
 * 
 * 
 */
public class MyAuthenticationFilter extends PassThruAuthenticationFilter {
	
	private final Logger log = Logger.getLogger(MyAuthenticationFilter.class);

	@Override
	protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
		log.debug("onAccessDenied");
//		HttpServletRequest request = (HttpServletRequest) servletRequest;
//		HttpServletResponse response = (HttpServletResponse) servletResponse;
//		String requestType = request.getHeader("X-Requested-With");
//		if (requestType != null && requestType.equalsIgnoreCase("XMLHttpRequest")) {
//			response.addHeader("loginStatus", "accessDenied");
//			response.sendError(HttpServletResponse.SC_FORBIDDEN);
//			return false;
//		}
		return super.onAccessDenied(servletRequest, servletResponse);
	}
}