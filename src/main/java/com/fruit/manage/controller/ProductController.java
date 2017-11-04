package com.fruit.manage.controller;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.model.Product;
import com.fruit.manage.util.DataResult;
import com.jfinal.core.paragetter.Para;
import com.jfinal.kit.StrKit;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.*;

public class ProductController extends BaseController {
	private static Logger log = Logger.getLogger(ProductController.class);

	public void getData(Integer pageNum, Integer pageSize, Integer id, String name, Integer status,
						Date startTime, Date endTime, String prop, String order) {
		renderJson(Product.dao.page(pageNum, pageSize, id, name, status, startTime, endTime, prop, order));
	}

	public void setStatus(int status, Integer[] ids) {
		log.info("修改商品("+ StringUtils.join(ids, ",") +")状态为:" + status);// TODO 获取当前登录用户
		if(ids == null || ids.length == 0) {
			renderErrorText("商品ID不能为空");
			return;
		}
		int[] productIds = new int[ids.length];
		for(int i=0; i<ids.length; i++) {
			productIds[i] = ids[i].intValue();
		}
		renderResult(Product.dao.changeStatus(ids, status));
	}
}
