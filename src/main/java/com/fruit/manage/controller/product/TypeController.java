package com.fruit.manage.controller.product;

import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.shiro.authz.annotation.RequiresPermissions;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.model.Type;
import com.fruit.manage.util.DataResult;
import com.jfinal.kit.JsonKit;
import com.jfinal.plugin.activerecord.Page;

/**
 * 分类的管理
 */
public class TypeController extends BaseController{
	
	private Logger log = Logger.getLogger(getClass());
	
	/**
	 * 查看所有信息
	 * */
	@RequiresPermissions("type:query")
	public void getData(){
		Page<Type> datas = Type.dao.getData(getParaToInt("pageNum"), getParaToInt("pageSize"), getPara("group_id"), getPara("name"), getPara("prop"), getPara("order"));
		renderJson(datas);
	}
	/**
	 * 保存类型信息
	 * */
	@RequiresPermissions("type:save")
	public void save(){
		log.info("保存数据："+JsonKit.toJson(getParaMap()));
		Type model = getModel(Type.class, "", true);
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
	 * 修改类型状态
	 */
	@RequiresPermissions("type:edit")
	public void changeStatus(){
		String[] ids  = getParaValues("ids");
		if(ids == null || ids.length == 0){
			renderErrorText("参数错误");
			return;
		}
		Integer status = getParaToInt("status");
		if(status == null){
			renderErrorText("参数错误");
			return;
		}
		DataResult<Object> result = Type.dao.changeStatus(ids, status);
		if(result.isSuccessCode()){
			renderNull();
			return;
		}
		renderErrorText(result.getMsg());
	}

	/**
	 * 获取所有可用的类型
	 */
	public void getTypes(){
		renderJson(Type.dao.getTypes());
	}
}
