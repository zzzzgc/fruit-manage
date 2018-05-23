package com.fruit.manage.controller.user;

import com.fruit.manage.constant.RoleKeyCode;
import org.apache.log4j.Logger;
import org.apache.shiro.authz.annotation.RequiresPermissions;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.model.Role;
import com.fruit.manage.model.User;
import com.jfinal.kit.JsonKit;

import java.util.List;

public class UserController extends BaseController{

	private Logger log = Logger.getLogger(getClass());
	
	/**
	 * 获取列表数据
	 */
	@RequiresPermissions("user:query")
	public void getData(){
		String userName = getPara("userName");
		
		int pageNum = getParaToInt("pageNum", 1);
		int pageSize = getParaToInt("pageSize", 10);
		
		String orderBy = getPara("prop");
		boolean isASC = "ascending".equals(getPara("order"));// ascending为升序，其他为降序
		
		renderJson(User.dao.getData(userName, pageNum, pageSize, orderBy, isASC));
	}
	
	/**
	 * 编辑记录
	 */
	@RequiresPermissions("user:edit")
	public void info(){
		int id = getParaToInt("id");
		User model = User.dao.findById(id);
		if(model == null){
			renderErrorText("该记录不存在，无法编辑");
		}else{
			model.put("roleIds", Role.dao.getRoleIdsByUserId(id));
			renderJson(model);
		}
	}
	
	/**
	 * 保存修改或者添加的数据
	 */
	@RequiresPermissions("user:save")
	public void save(){
		log.info("保存配置数据："+JsonKit.toJson(getParaMap()));
		User model = getModel(User.class, "", true);
		String[] roleIds = getParaValues("roleIds");
		renderResult(User.dao.save(model, roleIds));
	}

	/**
	 * 获取所有采购用户
	 */
	public void getAllProcurement () {
		renderJson(User.dao.getUsersByRoleId(RoleKeyCode.PROCUREMENT));
	}
	
}
