package com.fruit.manage.controller.common;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.constant.OrderConstant;
import com.fruit.manage.constant.ShipmentConstant;
import com.fruit.manage.model.Menu;
import com.fruit.manage.model.Permission;
import com.fruit.manage.util.Constant;
import com.jfinal.kit.PathKit;
import com.jfinal.kit.PropKit;
import com.jfinal.upload.UploadFile;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.*;

public class CommonController extends BaseController {
    private static Logger log = Logger.getLogger(ExcelController.class);

    /**
     * 文件上传的路径,可以根据这个路径和文件名获取文件
     * 不明白为什么设置的上传下载地址必须加上file
     */
    public static final String FILE_PATH = PathKit.getWebRootPath() + File.separator + PropKit.get("file.baseUploadPath");

    /**
     * 上传文件公共方法
     */
    public void upload() {
        List<UploadFile> updataFiles = null;
        try {
            updataFiles = getFiles("file");
        } catch (Exception e) {
            renderNull();
            return;
        }
        if (updataFiles.size() == 1) {
            String saveFileName = renameFile(updataFiles.get(0), getRequest());
            renderText(saveFileName);
            return;
        }
        List<String> saveFileNames = new ArrayList<String>();
        for (UploadFile f : updataFiles) {
            String saveFileName = renameFile(f, getRequest());
            saveFileNames.add(saveFileName);
        }
        renderJson(saveFileNames);
    }

    /**
     * 下载文件公共方法
     */
    public void download() {
        String fileName = getPara("fileName");
        renderFile(new File(FILE_PATH + File.separator + fileName));
    }

    /**
     * 重命名文件
     * 避免出现重名的文件
     *
     * @param uploadFile
     * @param request
     * @return
     */
    public static String renameFile(UploadFile uploadFile, HttpServletRequest request) {
        File file = uploadFile.getFile();
        String name = file.getName();
        String saveFileName = "file_" + UUID.randomUUID().toString().replace("-", "") + name.substring(name.lastIndexOf("."));
        File saveFile = new File(file.getParent() + File.separator + saveFileName);
        if (file.renameTo(saveFile)) {
            return saveFileName;
        } else {
            throw new RuntimeException("保存或重命名文件名失败");
        }
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
     * 获取所有订单状态
     */
    public void getAllOrderStatus() {
        renderJson(OrderConstant.ORDER_STATUS_MAP);
    }

    /**
     * 获取所有物流发送方式
     */
    public void getAllShipmentType() {
        renderJson(ShipmentConstant.SHIPMENT_TYPE);
    }
}



