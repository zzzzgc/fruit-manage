package com.fruit.manage.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.model.Product;
import com.fruit.manage.model.ProductImg;
import com.fruit.manage.model.ProductKeyword;
import com.fruit.manage.model.ProductRecommend;
import com.fruit.manage.model.ProductType;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.StrKit;

public class ProductController extends BaseController {
	private static Logger log = Logger.getLogger(ProductController.class);

	public void getData(Integer pageNum, Integer pageSize, Integer id, String name, Integer status,
						Date startTime, Date endTime, String prop, String order) {
		renderJson(Product.dao.page(pageNum, pageSize, id, name, status, startTime, endTime, prop, order));
	}

	public void setStatus(int status) {
		Integer[] ids = getParaValuesToInt("idss");
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

	public void info(Integer id) {
		if(id == null) {
			renderErrorText("商品ID不能为null");
			return;
		}
	    Product product = Product.dao.getById(id);
		if(product == null) {
			renderErrorText("商品不存在");
			return;
		}
		List<String> produceArea = new ArrayList<>();
		produceArea.add(product.getCountry());
		if(StrKit.notBlank(product.getProvince())) {
			produceArea.add(product.getProvince());
		}
		product.put("produceArea", produceArea.toArray(new String[]{}));
		product.put("types", ProductType.dao.getProductTypes(id));
		product.put("keywords", ProductKeyword.dao.getProductKeyword(id));
		product.put("recommendTypes", ProductRecommend.dao.getProductRecommend(id));
		product.put("imgs", ProductImg.dao.getProductImg(id, 1));
		renderJson(product);
	}

	public void save() {
		Product product = getModel(Product.class, "", true);// 针对model的参数，放到参数中不能完全注入属性，bug
		if(product == null) {
			renderErrorText("保存的商品不能为空");
			return;
		}
		String[] imgs = getParaValues("imgs");
		if(imgs == null || imgs.length == 0) {
			renderErrorText("必须上传至少一张图片");
			return;
		}
		product.setImg(imgs[0]);

		String[] keywords = getParaValues("keywords");
		Integer[] typesId = getParaValuesToInt("types");
		Integer[] recommends = getParaValuesToInt("recommendTypes");

		log.info(String.format("保存商品：product=%s，imgs=%s，keywords=?，typesId=?，recommends=?",
				JsonKit.toJson(product), StringUtils.join(imgs, ","), StringUtils.join(keywords, ","),
				StringUtils.join(typesId, ","), StringUtils.join(recommends, ",")));
		renderResult(Product.dao.save(product, imgs, keywords, typesId, recommends));
	}
}
