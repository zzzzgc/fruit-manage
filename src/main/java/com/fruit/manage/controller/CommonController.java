package com.fruit.manage.controller;

import java.util.*;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.constant.OrderConstant;
import com.fruit.manage.constant.ShipmentConstant;
import com.fruit.manage.model.Menu;
import com.fruit.manage.model.Permission;
import com.fruit.manage.util.Constant;
import com.fruit.manage.util.ImgUtil;
import com.jfinal.upload.UploadFile;

public class CommonController extends BaseController {

    /**
     * 上传图片公共方法
     */
    public void upload() {
        List<UploadFile> fileList = null;
        try {
            fileList = getFiles("file");
        } catch (Exception e) {
            renderNull();
            return;
        }
        if (fileList.size() == 1) {
            String picUrl = ImgUtil.upImg(fileList.get(0), getRequest());
            renderText(picUrl);
            return;
        }
        List<String> uploadText = new ArrayList<String>();
        for (UploadFile f : fileList) {
            String upImg = ImgUtil.upImg(f, getRequest());
            uploadText.add(upImg);
        }
        renderJson(uploadText);
    }

    /**
     * 获取所有权限列表
     */
    public void getPermissionList() {
        List<Permission> list = Permission.dao.find("select * from a_permission");
        renderJson(list);
    }

    /**
     * 获取所有菜单列表
     */
    public void getMenuList() {
        List<Menu> list = Menu.dao.find("select * from a_menu");
        renderJson(list);
    }

    /**
     * 根据uid，获取菜单列表
     */
    public void getMenuListByUid() {
        // 两级菜单

        // 根据父类分组
        List<Menu> menuListByUid = Menu.dao.getMenuListByUid(getSessionAttr(Constant.SESSION_UID));
        ArrayList<Map> menuList = new ArrayList<>();
        // ArrayList<ArrayList<Menu>> menuGroup = new ArrayList<>();
        // 一级菜单不能超过12个
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
            HashMap<String, Object> treeMap = new HashMap<>(2);
            treeMap.put("parentMenu", parentMenu);
            treeMap.put("sonMenus", sonMenu);
            menuList.add(treeMap);
        }
        renderJson(menuList);
    }

    /**
     * 获取所有订单状态
     */
    public void getAllOrderStatus() {
        renderJson(OrderConstant.ORDER_STATUS_MAP);
    }

    /**
     * 获取所有物流发送方式
     */
    public void getAllShipmentType(){
        renderJson(ShipmentConstant.SHIPMENT_TYPE);
    }
}



