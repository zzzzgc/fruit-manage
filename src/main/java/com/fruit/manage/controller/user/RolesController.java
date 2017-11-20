package com.fruit.manage.controller.user;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.shiro.authz.annotation.RequiresPermissions;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.model.Menu;
import com.fruit.manage.model.Permission;
import com.fruit.manage.model.Role;
import com.jfinal.kit.JsonKit;

public class RolesController extends BaseController{

	private Logger log = Logger.getLogger(getClass());
	
	/**
	 * 获取列表数据
	 */
	@RequiresPermissions("role:query")
	public void getData(){
		String roleName = getPara("roleName");
		
		int pageNum = getParaToInt("pageNum", 1);
		int pageSize = getParaToInt("pageSize", 10);
		
		String orderBy = getPara("prop");
		boolean isASC = "ascending".equals(getPara("order"));// ascending为升序，其他为降序
		
		renderJson(Role.dao.getData(roleName, pageNum, pageSize, orderBy, isASC));
	}
	
	/**
	 * 编辑记录
	 */
	@RequiresPermissions("role:edit")
	public void info(){
		int id = getParaToInt("id");
		Role model = Role.dao.findById(id);
		if(model == null){
			renderErrorText("该记录不存在，无法编辑");
		}else{
			model.put("menuIds", Menu.dao.getMenuIdsByRoleId(id));
			model.put("permissionIds", Permission.dao.getPermissionIdsByRoleId(id));
			renderJson(model);
		}
	}
	
	/**
	 * 保存修改或者添加的数据
	 */
	@RequiresPermissions("role:save")
	public void save(){
		log.info("保存配置数据："+JsonKit.toJson(getParaMap()));
		Role model = getModel(Role.class, "", true);
		String[] permissionIds = getParaValues("permissionIds");
		String[] menuIds = getParaValues("menuIds");
		renderResult(Role.dao.save(model, menuIds, permissionIds));
	}
	
	/**
	 * 删除记录
	 */
	@RequiresPermissions("role:delete")
	public void delete(){
		String id = getPara("ids");
		if(StringUtils.isBlank(id)){
			renderErrorText("参数错误");
			return;
		}
		renderResult(Role.dao.delete(id));
	}
	
	/**
	 * 获取所有角色列表
	 */
	public void getRoleList(){
		List<Role> list = Role.dao.find("select * from a_role");
		renderJson(list);
	}
}
