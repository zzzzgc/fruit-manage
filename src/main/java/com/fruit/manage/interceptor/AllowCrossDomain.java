package com.fruit.manage.interceptor;

import com.fruit.manage.util.Common;
import com.fruit.manage.util.Constant;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;

/**
 * 允许跨域访问
 * @Description 
 * @author liuzhao
 * @date 2017年3月24日 下午3:00:51
 *
 */
public class AllowCrossDomain implements Interceptor{

	@Override
	public void intercept(Invocation arg0) {
		if(PropKit.getBoolean("devMode")){
			Common.removeCache(Constant.CACHE_KEY_10,Constant.CACHE_KEY_30, Constant.CACHE_KEY_60,
					Constant.CACHE_KEY_300, Constant.CACHE_KEY_600,Constant.CACHE_KEY_1800, Constant.CACHE_KEY_3600, Constant.CACHE_KEY_86400);
		}
		
        Controller c = arg0.getController();
        String origin = c.getRequest().getHeader("Origin");
        if(StrKit.notBlank(origin)){
        	c.getResponse().addHeader("Access-Control-Allow-Origin", origin);
        }
        c.getResponse().addHeader("Access-Control-Allow-Credentials", "true");
        String requestHeader = c.getRequest().getHeader("Access-Control-Request-Headers");
        if(StrKit.notBlank(requestHeader)){
        	c.getResponse().addHeader("Access-Control-Allow-Headers", requestHeader);
        }
        arg0.invoke();
	}

}
