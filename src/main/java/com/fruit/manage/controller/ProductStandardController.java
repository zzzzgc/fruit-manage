package com.fruit.manage.controller;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.model.Product;
import com.fruit.manage.model.ProductStandard;
import com.jfinal.core.paragetter.Para;
import com.jfinal.kit.JsonKit;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Date;

public class ProductStandardController extends BaseController {
	private static Logger log = Logger.getLogger(ProductController.class);

	public void getData(Integer productId, String prop, String order) {
		if(productId == null) {
			renderJson(new ArrayList<>());
			return;
		}
		// TODO 对数据过滤
		renderJson(ProductStandard.dao.list(productId, prop, "descending".equals(order)));
	}

	public void changeStatus(int productId, int status) {
		Integer[] ids = getParaValuesToInt("ids");
		log.info("修改商品规格("+ StringUtils.join(ids, ",") +")状态为:" + status);// TODO 获取当前登录用户
		if(ids == null || ids.length == 0) {
			renderErrorText("商品规格ID不能为空");
			return;
		}

		renderResult(ProductStandard.dao.changeStatusOne(productId, ids, status));
	}

	public void info(Integer id) {
		renderJson(ProductStandard.dao.findById(id));
	}

	public void save() {
		ProductStandard productStandard = getModel(ProductStandard.class, "", true);
		log.info("保存规格数据：" + JsonKit.toJson(productStandard));
		productStandard.setPurchaserUid(0);// TODO 设置采购员ID，当前登录人员
		renderResult(ProductStandard.dao.save(productStandard));
	}
}
