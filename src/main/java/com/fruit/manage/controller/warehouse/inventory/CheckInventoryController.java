package com.fruit.manage.controller.warehouse.inventory;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.controller.common.CommonController;
import com.fruit.manage.model.CheckInventory;
import com.fruit.manage.model.CheckInventoryDetail;
import com.fruit.manage.model.User;
import com.fruit.manage.model.WarehouseLog;
import com.fruit.manage.util.Constant;
import com.fruit.manage.util.DateAndStringFormat;
import com.fruit.manage.util.ExcelCommon;
import com.fruit.manage.util.IdUtil;
import com.fruit.manage.util.excel.ExcelException;
import com.jfinal.aop.Before;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;
import org.apache.xmlbeans.impl.piccolo.util.DuplicateKeyException;

import java.sql.SQLException;
import java.util.*;

/**
 * @author partner
 * @date 2018/4/3 17:43
 */
public class CheckInventoryController extends BaseController {
    private Logger logger = Logger.getLogger(CheckInventoryController.class);

    /**
     * 根据条件查询获取带分页的仓库日志信息
     */
    public void getAllInfo() {
        int pageNum = getParaToInt("pageNum", 1);
        int pageSize = getParaToInt("pageSize", 10);
        Map map = new HashMap();
        String orderBy = getPara("prop");
        // ascending为升序，其他为降序
        boolean isASC = "ascending".equals(getPara("order"));
        String[] create_times = getParaValues("format_create_time");
        map.put("createTimes", create_times);
        renderJson(CheckInventory.dao.getAllInfo(pageNum, pageSize, orderBy, isASC, map));
    }

    /**
     * 添加盘点单
     */
    @Before(Tx.class)
    public void addCheckInventory() {
        Date orderClcyleDate = getParaToDate("pPlanDate");
        if (orderClcyleDate == null) {
            orderClcyleDate = new Date();
        }
        String CIDId = IdUtil.getCheckInventoryIdByDate(orderClcyleDate);
        CheckInventory checkInventories = CheckInventory.dao.findById(CIDId);
        if (checkInventories == null) {
            CheckInventory checkInventory = new CheckInventory();
            checkInventory.setId(CIDId);
            checkInventory.setCreateTime(orderClcyleDate);
            checkInventory.setOrderCycleDate(orderClcyleDate);
            checkInventory.save();
            renderNull();
            return;
        }
        renderErrorText("重复创建盘点单！");
    }

    /**
     * 根据盘点单号删除盘点单详细信息和盘点单信息
     */
    @Before(Tx.class)
    public void delCheckInventory() {
        String checkInventoryId = getPara("checkInventoryId");
        // 先删除盘点单详细信息
        CheckInventoryDetail.dao.delCheckInventoryDetailByCIId(checkInventoryId);
        // 删除盘点单信息
        CheckInventory.dao.deleteById(checkInventoryId);
        renderNull();
    }

    /**
     * 获取盘点人姓名
     */
    public void getInventory() {
        Integer uid = getSessionAttr(Constant.SESSION_UID);
        User user = User.dao.getUserById(uid);
        List<String> list = new ArrayList<>();
        if (user != null) {
            list.add(user.getNickName());
        }
        renderJson(list);
    }

    /**
     * 导出盘点单（Excel）
     */
    public void exportCheckInventory() {
        String checkInventoryId = getPara("checkInventoryId");
        List<CheckInventoryDetail> detailList = CheckInventoryDetail.dao.getCheckInventoryDetailsByCIId(checkInventoryId);
        if (detailList != null && detailList.size() > 0) {
            List<String[]> lists = new ArrayList<>();
            for (int i = 0; i < detailList.size(); i++) {
                String[] strs = new String[9];
                strs[0] = detailList.get(i).getProductName();
                strs[1] = detailList.get(i).getProductStandardName();
                strs[2] = detailList.get(i).getProductStandardId() + "";
                strs[3] = detailList.get(i).getProductWeight() + "";
                strs[4] = detailList.get(i).getInventoryPrice() + "";
                strs[5] = detailList.get(i).getInventoryTotalPrice() + "";
                strs[6] = detailList.get(i).getUserName();
                strs[7] = detailList.get(i).getCheckInventoryNum() + "";
                strs[8] = detailList.get(i).getInventoryRemark();
                lists.add(strs);
            }
            String fileName = UUID.randomUUID().toString().replaceAll("-", "") + ".xlsx";
            String path = CommonController.FILE_PATH;
            String title = "盘点单";
            Integer uid = getSessionAttr(Constant.SESSION_UID);
            User user = User.dao.findById(uid);
            String[] headers = {"商品名", "规格名", "规格编码", "重量(斤)", "库存单价", "库存总额", "盘点人", "盘点数量", "备注"};
            Map map = new HashMap();
            map.put("path", path);
            map.put("fileName", fileName);
            map.put("title", title);
            map.put("createBy", user.getName());
            map.put("header", headers);
            map.put("listData", lists);
            try {
                ExcelCommon.createExcelModul(map);
                List<String> list = new ArrayList<>();
                list.add(fileName);
                renderJson(list);
            } catch (ExcelException e) {
                renderErrorText("导出盘点Excel异常!");
            }
        } else {
            renderErrorText("该盘点单没有盘点详细记录!");
        }
    }

}
