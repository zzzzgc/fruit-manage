package com.fruit.manage.controller;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.model.Product;
import com.fruit.manage.util.DataResult;
import com.jfinal.core.paragetter.Para;

import java.util.*;

public class ProductController extends BaseController {

	public void getData(Integer pageNum, Integer pageSize, Integer productId, Integer status, String fruitType
			, String recommendType, String country, Date start, Date end, String prop, String order) {
		renderJson(Product.dao.page(pageNum, pageSize, productId, status, fruitType, recommendType, country, start, end, prop, order));
	}

}
