package com.fruit.manage.util;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Administrator
 *
 */
public class RequestUtils {
	public static Logger LOG = LoggerFactory.getLogger(RequestUtils.class);	
	/**
	 * 获取当前应用部署的地址
	 * 如：http://localhost:8080
	 * 如果是80端口则不加端口号
	 * @param request
	 * @return
	 */
	public static String getContextAllPath(HttpServletRequest request) {
		StringBuilder sb = new StringBuilder();
		sb.append(request.getScheme()).append("://").append(request.getServerName());
		if(request.getServerPort() != 80){
			sb.append(":").append(request.getServerPort());
		}
		sb.append(request.getContextPath());
		String path = sb.toString();
		sb = null;
		return path;
	}
}
