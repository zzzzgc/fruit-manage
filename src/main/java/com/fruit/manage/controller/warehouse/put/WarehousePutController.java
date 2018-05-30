package com.fruit.manage.controller.warehouse.put;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.controller.common.CommonController;
import com.fruit.manage.model.PutWarehouse;
import com.fruit.manage.model.PutWarehouseDetail;
import com.fruit.manage.model.User;
import com.fruit.manage.util.Constant;
import com.fruit.manage.util.DateAndStringFormat;
import com.fruit.manage.util.ExcelCommon;
import com.fruit.manage.util.excel.Excel;
import com.fruit.manage.util.excel.ExcelException;
import com.jfinal.aop.Before;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.tx.Tx;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;


/**
 * @author partner
 * @date 2018/4/1 14:36
 */
public class WarehousePutController extends BaseController {
    private Logger logger = Logger.getLogger(WarehousePutController.class);

    /**
     * 获取根据条件查询出来的数据集合
     */
    public void getAllPutWarehouseInfo() {
        int pageNum = getParaToInt("pageNum", 1);
        int pageSize = getParaToInt("pageSize", 10);
        Map map = new HashMap();
        String orderBy = getPara("prop");
        // ascending为升序，其他为降序
        boolean isASC = "ascending".equals(getPara("order"));
        String[] create_time = getParaValues("format_create_time");
        map.put("createTime", create_time);
        renderJson(PutWarehouse.dao.getAllInfo(pageNum, pageSize, orderBy, isASC, map));
    }

    /**
     * 创建入库单
     */
    public void createWarehouse() {
        try {
            System.out.println("---------------putInTime----------------");
            String putIntTimeStr = getPara("putInTime");
            System.out.println("putInTime:"+putIntTimeStr);
            PutWarehouse putWarehouse = new PutWarehouse();
            putWarehouse.setPutNum(0);
            putWarehouse.setPutTypeNum(0);
            putWarehouse.setPutTotalPrice(new BigDecimal(0));
            putWarehouse.setPutType(0);
            putWarehouse.setWarehouseAddress("默认地址");
            putWarehouse.setCreateTime(DateAndStringFormat.strToDate(putIntTimeStr,"yyyy-MM-dd HH:mm:ss"));
            putWarehouse.setOrderCycleDate(DateAndStringFormat.strToDate(putIntTimeStr,"yyyy-MM-dd"));
            putWarehouse.save();
            renderNull();
        } catch (Exception e) {
            renderErrorText("创建失败!");
        }
    }

    /**
     * 根据入库编号删除入库单
     */
    @Before(Tx.class)
    public void delWarehouse() {
        try {
            Integer putId = getParaToInt("putId");
            // 根据入库单编号先删除关联的入库详细信息
            PutWarehouseDetail.dao.delWarehousePutDetailByWPId(putId);
            // 根据入库单编号删除入库信息
            PutWarehouse.dao.deleteById(putId);
            renderNull();
        } catch (Exception e) {
            renderErrorText("删除失败!");
        }
    }

    /**
     * 导出入库
     */
    public void exportWarehousePut() {
        Integer putId = getParaToInt("putId");
        List<PutWarehouseDetail> detailList = PutWarehouseDetail.dao.getAllInfoByPutId(putId);
        if (detailList != null && detailList.size() > 0) {
            List<String[]> lists = new ArrayList<>();
            for (int i = 0; i < detailList.size(); i++) {
                // 修改了长度
                String[] strs = new String[9];
                strs[0] = detailList.get(i).getProductName();
                strs[1] = detailList.get(i).getProductStandardName();
                strs[2] = detailList.get(i).getProductStandardId() + "";
                strs[3] = detailList.get(i).getProductWeight() + "";
                strs[4] = detailList.get(i).getProcurementPrice() + "";
                strs[5] = detailList.get(i).getProcurementTotalPrice() + "";
                strs[6] = detailList.get(i).getBoothCost() + "";
                strs[7] = detailList.get(i).getPutNum() + "";
                strs[8] = detailList.get(i).getProcurementName();
                lists.add(strs);
            }
            java.lang.String fileName = UUID.randomUUID().toString().replaceAll("-", "") + ".xlsx";
            String path = CommonController.FILE_PATH;
            String title = "入库单";
            Integer uid = getSessionAttr(Constant.SESSION_UID);
            // 获取当前操作用户
            User user = User.dao.findById(uid);
            String[] headers = {"商品名", "规格名", "规格编码", "重量(斤)", "采购单价", "采购总价", "摊位费", "入库数量", "采购人"};
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
                renderErrorText("导出Excel异常!");
            }
        } else {
            renderErrorText("该入库单没有记录!");
        }
    }
}
