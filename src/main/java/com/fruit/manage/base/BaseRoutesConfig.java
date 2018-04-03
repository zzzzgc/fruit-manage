package com.fruit.manage.base;

import com.fruit.manage.controller.BannerController;
import com.fruit.manage.controller.common.CommonController;
import com.fruit.manage.controller.IndexController;
import com.fruit.manage.controller.common.ExcelController;
import com.fruit.manage.controller.procurement.PlanController;
import com.fruit.manage.controller.ProductController;
import com.fruit.manage.controller.ProductMarketController;
import com.fruit.manage.controller.ProductStandardController;
import com.fruit.manage.controller.TypeController;
import com.fruit.manage.controller.TypeGroupController;
import com.fruit.manage.controller.customer.CustomerController;
import com.fruit.manage.controller.login.LoginController;
import com.fruit.manage.controller.order.OrderController;
import com.fruit.manage.controller.procurement.PlanDetailController;
import com.fruit.manage.controller.procurement.QuotaContrller;
import com.fruit.manage.controller.user.RolesController;
import com.fruit.manage.controller.user.UserController;
import com.fruit.manage.controller.warehouse.out.WarehouseOutContrller;
import com.fruit.manage.controller.warehouse.out.WarehouseOutDetailContrller;
import com.fruit.manage.controller.warehouse.put.WarehousePutController;
import com.fruit.manage.controller.warehouse.put.WarehousePutDetailController;
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
		// 通用
		add("/manage/excel", ExcelController.class);
		add("/manage/common", CommonController.class);

		add("/manage/order", OrderController.class);
		add("/manage/banner", BannerController.class);
		add("/manage/type", TypeController.class);
		add("/manage/typeGroup", TypeGroupController.class);
		add("/manage/role", RolesController.class);
		add("/manage/user", UserController.class);
		add("/manage/customer", CustomerController.class);
		add("/manage/procurement/plan", PlanController.class);
		add("/manage/procurement/quota", QuotaContrller.class);
		add("/manage/procurement/plan/detail", PlanDetailController.class);
		add("/manage/warehouse/put",WarehousePutController.class);
		add("/manage/warehouse/put/detail",WarehousePutDetailController.class);
		add("/manage/warehouse/out",WarehouseOutContrller.class);
		add("/manage/warehouse/out/detail",WarehouseOutDetailContrller.class);
	}

}
