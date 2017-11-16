package com.fruit.manage.interceptor;

import java.util.List;

import com.fruit.manage.model.Permission;
import com.fruit.manage.plugin.shiro.ShiroMethod;
import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;

/**
 * 让 shiro基于url拦截
 *
 * 主要 数据库中也用url 保存权限
 */
public class UrlShiroInterceptor implements Interceptor{
	
    /**
     * 获取全部 需要控制的权限
     */
    private static List<String> urls;
 
    public static void updateUrls() {
        urls = Permission.dao.getPermissionList();
    }
 
    public void intercept(Invocation ai) {
        if (urls == null) {
            urls = Permission.dao.getPermissionList();
        }
 
        String url = ai.getActionKey();
        try {
            if (url.contains("add")){
//                LogService.me.add(ai.getController(), LogService..EVENT_ADD);
            } else if (url.contains("delete")) {
//                LogService.me.add(ai.getController(), Log.EVENT_DELETE);
            } else if (url.contains("update")) {
//                LogService.me.add(ai.getController(), Log.EVENT_UPDATE);
            } else if (url.contains("index")) {
//                LogService.me.add(ai.getController(), Log.EVENT_QUERY);
            } else if (url.contains("goto")) {
 
            }
 
            System.out.println(ShiroMethod.hasPermission(url));
            
            if (urls.contains(url) && !ShiroMethod.hasPermission(url)) {
                ai.getController().renderError(403);
            }
        } catch (Exception e) {
            ai.getController().renderError(403);
        }
 
        ai.invoke();
    }
}