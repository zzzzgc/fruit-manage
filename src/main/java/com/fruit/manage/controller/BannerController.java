package com.fruit.manage.controller;

import java.util.Date;

import org.apache.log4j.Logger;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.model.Banner;
import com.fruit.manage.util.DataResult;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.kit.JsonKit;

@ControllerBind(controllerKey="/manage/banner",viewPath="/")
public class BannerController extends BaseController {

	private Logger log = Logger.getLogger(getClass());
	
	/**
	 * 获取列表数据
	 */
	public void getData(){
		String groupKey = getPara("groupKey");
		String key = getPara("key");
		
		int pageNum = getParaToInt("pageNum", 1);
		int pageSize = getParaToInt("pageSize", 10);
		
		String orderBy = getPara("prop");
		boolean isASC = "ascending".equals(getPara("order"));// ascending为升序，其他为降序
		
		renderJson(Banner.dao.getData(groupKey, key, pageNum, pageSize, orderBy, isASC));
	}
	
	/**
	 * 编辑记录
	 */
	public void info(){
		int id = getParaToInt("id");
		Banner model = Banner.dao.findById(id);
		if(model == null){
			renderErrorText("该记录不存在，无法编辑");
		}else{
			renderJson(model);
		}
	}
	
	/**
	 * 保存修改或者添加的数据
	 */
	public void save(){
		log.info("保存配置数据："+JsonKit.toJson(getParaMap()));
		Banner model = getModel(Banner.class, "", true);
		if(model.getId() == null){
			model.setCreateTime(new Date());
		}
		model.setUpdateTime(new Date());
		if(model.getId() == null){
			renderResult(model.save());
		}else{
			renderResult(model.update());
		}
	}
	
	/**
	 * 删除记录
	 */
	public void delete(){
		String[] ids  = getParaValues("ids");
		if(ids == null || ids.length == 0){
			renderErrorText("参数错误");
			return;
		}
		DataResult<Object> result = Banner.dao.delete(ids);
		if(result.isSuccessCode()){
			renderNull();
		}else{
			renderErrorText(result.getMsg());
		}
	}

}
