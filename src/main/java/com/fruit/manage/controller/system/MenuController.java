package com.fruit.manage.controller.system;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.model.Menu;
import com.fruit.manage.util.Constant;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.tx.Tx;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author: ZGC
 * @Date Created in 11:53 2018/5/18
 */
public class MenuController extends BaseController {

    /**
     * 根据uid，获取菜单列表
     */
    public void getAllMenu() {
        // 两级菜单

        // 根据父类分组
        List<Menu> menuListByUid = Menu.dao.getAllMenu();
        ArrayList<Object> menuList = new ArrayList<>();
        // ArrayList<ArrayList<Menu>> menuGroup = new ArrayList<>();
        // 一级菜单id ,不能超过12这个下标,比如 menu_id为13 这是会报错的
        ArrayList<Menu>[] menuGroup = new ArrayList[12];
        for (Menu menu : menuListByUid) {
            Integer parentId = menu.getParentId();
            ArrayList<Menu> arrayList = menuGroup[parentId];
            if (arrayList != null) {
                arrayList.add(menu);
                menuGroup[parentId] = arrayList;
            } else {
                ArrayList<Menu> menus = new ArrayList<>();
                menus.add(menu);
                menuGroup[parentId] = menus;
            }
        }

        // 根据 'parent_id = 0为根节点' 开始组装
        ArrayList<Menu> parentGroup = menuGroup[0];
        for (Menu parentMenu : parentGroup) {
            Integer parentId = parentMenu.getId();
            ArrayList<Menu> sonMenu = menuGroup[parentId];
            parentMenu.put("sonMenu",sonMenu);
            menuList.add(parentMenu);
        }
        renderJson(menuList);
    }

    /**
     * 保存或更新
     */
    @Before(Tx.class)
    public void saveAndUpdate() {
        Menu menu = getModel(Menu.class, "", true);
        if (menu.getId() != null) {
            // 更新

            menu.setUpdateTime(new Date());
            menu.update();
        } else {
            // 新增

            menu.setCreateTime(new Date());
            menu.setUpdateTime(new Date());
            menu.save();
        }
        renderJson(menu);
    }

    /**
     * 删除菜单项
     */
    @Before(Tx.class)
    public void remove() {
        Menu menu = getModel(Menu.class, "", true);
        menu.delete();
        renderNull();
    }
}
