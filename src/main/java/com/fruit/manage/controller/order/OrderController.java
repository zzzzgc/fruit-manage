package com.fruit.manage.controller.order;

import org.apache.log4j.Logger;
import org.apache.shiro.authz.annotation.RequiresPermissions;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.model.Order;

public class OrderController extends BaseController {

	private Logger log = Logger.getLogger(getClass());

	/**
	 * 获取列表数据
	 */
	@RequiresPermissions("order:query")
	public void getData(){
		Order order = getModel(Order.class, "", true);
		log.info("订单搜索参数order=" + order);
		int pageNum = getParaToInt("pageNum", 1);
		int pageSize = getParaToInt("pageSize", 10);

		String orderBy = getPara("prop");

		// ascending为升序，其他为降序
		boolean isASC = "ascending".equals(getPara("order"));

		// 下单时间
		String[] orderTime = getParaValues("order_time");

		renderJson(Order.dao.getData(order, orderTime, pageNum, pageSize, orderBy, isASC));
	}

}
