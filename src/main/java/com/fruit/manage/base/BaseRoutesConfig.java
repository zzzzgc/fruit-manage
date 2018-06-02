package com.fruit.manage.base;

import com.fruit.manage.controller.product.BannerController;
import com.fruit.manage.controller.common.CommonController;
import com.fruit.manage.controller.IndexController;
import com.fruit.manage.controller.common.ExcelController;
import com.fruit.manage.controller.order.PayOrderController;
import com.fruit.manage.controller.procurement.PlanController;
import com.fruit.manage.controller.product.ProductController;
import com.fruit.manage.controller.product.ProductMarketController;
import com.fruit.manage.controller.product.ProductStandardController;
import com.fruit.manage.controller.product.TypeController;
import com.fruit.manage.controller.product.TypeGroupController;
import com.fruit.manage.controller.customer.CustomerController;
import com.fruit.manage.controller.login.LoginController;
import com.fruit.manage.controller.order.OrderController;
import com.fruit.manage.controller.procurement.PlanDetailController;
import com.fruit.manage.controller.procurement.QuotaContrller;
import com.fruit.manage.controller.statement.ArrearsStatisticsController;
import com.fruit.manage.controller.statement.ProcurementStoreoutController;
import com.fruit.manage.controller.statement.ProductSaleRankListController;
import com.fruit.manage.controller.statement.SalesMarginController;
import com.fruit.manage.controller.system.MenuController;
import com.fruit.manage.controller.system.PermissionController;
import com.fruit.manage.controller.user.RolesController;
import com.fruit.manage.controller.user.UserController;
import com.fruit.manage.controller.warehouse.inventory.CheckInventoryController;
import com.fruit.manage.controller.warehouse.inventory.CheckInventoryDetailController;
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
		add("/manage/warehouse/inventory", CheckInventoryController.class);
		add("/manage/warehouse/inventory/detail", CheckInventoryDetailController.class);
		add("/manage/payOrder", PayOrderController.class);
		add("/manage/menu", MenuController.class);
		add("/manage/permission", PermissionController.class);
		add("/manage/statement/salesMargin", SalesMarginController.class);
		add("/manage/statement/storeout", ProcurementStoreoutController.class);
		add("/manage/statement/pSRankList", ProductSaleRankListController.class);
		add("/manage/statement/arrearsStatistics", ArrearsStatisticsController.class);
	}

}
