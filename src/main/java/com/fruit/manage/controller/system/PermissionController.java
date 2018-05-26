package com.fruit.manage.controller.system;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.model.Menu;
import com.fruit.manage.model.Permission;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.tx.Tx;

import java.util.ArrayList;
import java.util.Date;
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
     * 规范:
     * 1.权限组必须包含一个All权限(*)
     *  比如 order:*`order:query`order:delete   不能是只有 order:query`order:delete
     */
    public void getAllPermission() {
        List<Permission> permissions = Permission.dao.find("SELECT * FROM a_permission ");

        // 分组
        Map<String, List<Permission>> permissionGrop = permissions.stream().collect(Collectors.groupingBy((p) -> {
            return p.getPermissionKey().split(":")[0];
        }));


        // 组合
        ArrayList<Object> data = new ArrayList<>();
        permissionGrop.forEach(
                (mainKeyName, permissionList) -> {
                    for (Permission p : permissionList) {
                        if (p.getPermissionKey().equals(mainKeyName.concat(":*"))) {
                            boolean remove = permissionList.remove(p);
                            System.out.println(remove);
                            data.add(p);
                            if (permissionList.size() > 0) {
                                p.put("sonMenu", permissionList);
                            }
                            break;
                        }
                    }

                }
        );
        renderJson(data);
    }

    /**
     * 保存或更新
     */
    @Before(Tx.class)
    public void saveAndUpdate() {
        Permission permission = getModel(Permission.class, "", true);
        if (permission.getId() != null) {
            // 更新

            permission.setUpdateTime(new Date());
            permission.update();
        } else {
            // 新增

            permission.setCreateTime(new Date());
            permission.setUpdateTime(new Date());
            permission.save();
        }
        renderJson(permission);
    }

    /**
     * 删除菜单项
     */
    @Before(Tx.class)
    public void remove() {
        Permission permission = getModel(Permission.class, "", true);
        permission.delete();
        renderNull();
    }
}
