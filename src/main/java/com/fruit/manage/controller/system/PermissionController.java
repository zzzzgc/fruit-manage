package com.fruit.manage.controller.system;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.model.Permission;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author: ZGC
 * @Date Created in 15:18 2018/5/18
 */
public class PermissionController extends BaseController {

    /**
     * 权限管理
     */
    public void getAllPermission() {
        List<Permission> permissions = Permission.dao.find("SELECT * FROM a_permission ");
        Map<String, Permission> permission = permissions.stream().collect(Collectors.toMap(Permission -> {
            return Permission.getPermissionName().split(":")[0];
        }, Function.identity()));
    }
}
