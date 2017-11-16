package com.fruit.manage.controller;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.model.Product;
import com.fruit.manage.model.ProductImg;
import com.fruit.manage.model.ProductMarket;
import com.jfinal.kit.JsonKit;

public class ProductMarketController extends BaseController {
	private static Logger log = Logger.getLogger(ProductMarketController.class);

	public void info(Integer productId) {
		if(productId == null) {
			renderErrorText("商品ID不能为null");
			return;
		}
		ProductMarket productMarket = ProductMarket.dao.getMarket(productId);
		if(productMarket == null) {
			productMarket = new ProductMarket();
		}
		productMarket.put("imgs", ProductImg.dao.getProductImg(productId, 2));
		productMarket.put("productName", Product.dao.findById(productId).getName());
		renderJson(productMarket);
	}

	public void save() {
		ProductMarket productMarket = getModel(ProductMarket.class, "", true);// 针对model的参数，放到参数中不能完全注入属性，bug
		if(productMarket == null) {
			renderErrorText("保存的商品不能为空");
			return;
		}
		productMarket.setLastEditUserId(0);// TODO
		String[] imgs = getParaValues("imgs");

		log.info(String.format("保存商品行情：productMarket=%s，imgs=%s",
				JsonKit.toJson(productMarket), StringUtils.join(imgs, ",")));
		renderResult(ProductMarket.dao.save(productMarket, imgs));
	}
}
