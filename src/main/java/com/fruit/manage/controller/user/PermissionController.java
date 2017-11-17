package com.fruit.manage.controller.user;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.shiro.authz.annotation.RequiresPermissions;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.model.Permission;
import com.fruit.manage.model.Role;
import com.fruit.manage.util.DataResult;
import com.jfinal.kit.JsonKit;

public class PermissionController extends BaseController{

	private Logger log = Logger.getLogger(getClass());
	
	/**
	 * 获取列表数据
	 */
	@RequiresPermissions("permission:query")
	public void getData(){
		String permissionName = getPara("permissionName");
		
		int pageNum = getParaToInt("pageNum", 1);
		int pageSize = getParaToInt("pageSize", 10);
		
		String orderBy = getPara("prop");
		boolean isASC = "ascending".equals(getPara("order"));// ascending为升序，其他为降序
		
		renderJson(Role.dao.getData(permissionName, pageNum, pageSize, orderBy, isASC));
	}
	
	/**
	 * 编辑记录
	 */
	@RequiresPermissions("permission:edit")
	public void info(){
		int id = getParaToInt("id");
		Role model = Role.dao.findById(id);
		if(model == null){
			renderErrorText("该记录不存在，无法编辑");
		}else{
			renderJson(model);
		}
	}
	
	/**
	 * 保存修改或者添加的数据
	 */
	@RequiresPermissions("permission:save")
	public void save(){
		log.info("保存配置数据："+JsonKit.toJson(getParaMap()));
		Role model = getModel(Role.class, "", true);
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
	@RequiresPermissions("permission:delete")
//	public void delete(){
//		String[] ids  = getParaValues("ids");
//		if(ids == null || ids.length == 0){
//			renderErrorText("参数错误");
//			return;
//		}
//		DataResult<Object> result = Role.dao.delete(ids);
//		if(result.isSuccessCode()){
//			renderNull();
//		}else{
//			renderErrorText(result.getMsg());
//		}
//	}
	
	/**
	 * 获取所有权限列表
	 */
	public void getPermissionList(){
		List<Permission> list = Permission.dao.find("select * from a_permission");
		renderJson(list);
	}
}
