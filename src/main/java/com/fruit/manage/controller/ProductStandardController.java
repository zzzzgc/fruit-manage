package com.fruit.manage.controller;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.model.Product;
import com.fruit.manage.model.ProductStandard;
import com.jfinal.core.paragetter.Para;

import java.util.ArrayList;
import java.util.Date;

public class ProductStandardController extends BaseController {

	public void getData(Integer productId) {
		if(productId == null) {
			renderJson(new ArrayList<>());
			return;
		}
		// TODO 对数据过滤
		renderJson(ProductStandard.dao.list(productId));
	}

}
