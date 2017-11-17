package com.fruit.manage.base;

import com.fruit.manage.controller.BannerController;
import com.fruit.manage.controller.IndexController;
import com.fruit.manage.controller.ProductController;
import com.fruit.manage.controller.ProductMarketController;
import com.fruit.manage.controller.ProductStandardController;
import com.fruit.manage.controller.TypeController;
import com.fruit.manage.controller.TypeGroupController;
import com.fruit.manage.controller.login.LoginController;
import com.fruit.manage.controller.user.PermissionController;
import com.fruit.manage.controller.user.RolesController;
import com.jfinal.config.Routes;

/**
 * 路由规则注册
 * 控制用户请求转到具体action
 */
public class BaseRoutesConfig extends Routes {

	@Override
	public void config() {
		add("/", IndexController.class);
		add("/product", ProductController.class);
		add("/productStandard", ProductStandardController.class);
		add("/productMarket", ProductMarketController.class);
		add("/login", LoginController.class);
		
		add("/manage/banner", BannerController.class);
		add("/manage/type", TypeController.class);
		add("/manage/typeGroup", TypeGroupController.class);
		add("/manage/role", RolesController.class);
		add("/manage/permission", PermissionController.class);
	}

}
