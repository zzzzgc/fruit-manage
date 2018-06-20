package com.fruit.manage.controller.statement;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.controller.common.CommonController;
import com.fruit.manage.model.OrderDetail;
import com.fruit.manage.model.User;
import com.fruit.manage.util.Constant;
import com.fruit.manage.util.ExcelCommon;
import com.fruit.manage.util.excel.ExcelException;
import com.jfinal.kit.StrKit;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

/**
 * @Description 4.产品毛利排行报表
 * @author partner
 * @date 2018/6/2 13:56
 */
public class ProcurementMarginController extends BaseController {
    private List list = new ArrayList();

    public void getData() {
        int pageNum = getParaToInt("pageNum", 1);
        int pageSize = getParaToInt("pageSize", 10);
        String orderBy = getPara("prop");
        boolean isASC = "ascending".equals(getPara("order"));
        Integer productId = getParaToInt("product_id");
        String productName = getPara("product_name");
        Integer productStandardId = getParaToInt("product_standard_id");
        String productStandardName = getPara("product_standard_name");
        String[] createTimes = getParaValues("format_create_time");
        String select = _getSqlSelect();
        // TODO 修改参数
        String selectExcept = _getSqlSelectExcept(list, productId, productName, productStandardId, productStandardName,createTimes);
        System.out.println("-------------产品毛利排行报表SQL START--------------");
        System.out.println(select + selectExcept);
        for (int i = 0; i < list.size(); i++) {
            System.out.println("list index:"+i+",list value:"+list.get(i));
        }
        System.out.println("-------------产品毛利排行报表SQL END--------------");
        renderJson(OrderDetail.dao.paginate(pageNum,pageSize,select,selectExcept,list.toArray()));
    }

    public void exportInventoryStatistics() {
        Integer productId = getParaToInt("product_id");
        String productName = getPara("product_name");
        Integer productStandardId = getParaToInt("product_standard_id");
        String productStandardName = getPara("product_standard_name");
        String[] createTimes = getParaValues("format_create_time");
        String select = _getSqlSelect();
        String selectExcept = _getSqlSelectExcept(list, productId, productName, productStandardId, productStandardName,createTimes);
        System.out.println("-------------产品销售排行版报表SQL START--------------");
        System.out.println(select + selectExcept);
        for (int i = 0; i < list.size(); i++) {
            System.out.println("list index:"+i+",list value:"+list.get(i));
        }
        System.out.println("-------------产品销售排行版报表SQL END--------------");


        // TODO 设置headers
        String[] headers = {};
        List<Object> objects = new ArrayList<>();
        if (objects != null && objects.size() >0) {
            List<String[]> lists = new ArrayList<>();
            for (int i = 0; i < objects.size(); i++) {
                String[] strings = new String[headers.length];
                // TODO 进行数据封装
                lists.add(strings);
            }

            Integer uid = getSessionAttr(Constant.SESSION_UID);
            User user = User.dao.findById(uid);
            String fileName = UUID.randomUUID().toString().replaceAll("-", "") + ".xlsx";
            String path = CommonController.FILE_PATH;
            String title = "库存统计报表";
            Map map = new HashMap();
            map.put("path", path);
            map.put("fileName", fileName);
            map.put("title", title);
            map.put("createBy", user.getNickName());
            map.put("header", headers);
            map.put("listData", lists);
            try {
                ExcelCommon.createExcelModul(map);
                List<String> list = new ArrayList<>();
                list.add(fileName);
                renderJson(list);
            } catch (ExcelException e) {
                e.printStackTrace();
                renderErrorText("导出库存统计错误!");
            }
        } else {
            renderErrorText("该库存统计没有记录!");
        }

    }


    private String _getSqlSelect() {
        String select = "";
        return select;
    }

    private String _getSqlSelectExcept(List list, Integer productId, String productName, Integer productStandardId, String productStandardName,String [] createTiems) {
        String selectExcept = "";


        if (productId != null && productId > 0) {
            selectExcept += "\tand od.product_id like  ?\n";
            list.add("%" + productId + "%");
        }
        if (StrKit.notBlank(productName)) {

            selectExcept += "\tand od.product_name like ?\n";
            list.add("%" + productName + "%");
        }
        if (productStandardId != null && productStandardId > 0) {

            selectExcept += "\tand od.product_standard_id LIKE ?\n";
            list.add("%" + productStandardId + "%");
        }
        if (StrKit.notBlank(productStandardName)) {
            selectExcept += "\tand od.product_standard_name LIKE ?\n";
            list.add("%" + productStandardName + "%");
        }
        if (ArrayUtils.isNotEmpty(createTiems) && createTiems.length == 2) {
            selectExcept += " AND od.create_time BETWEEN ? AND ? ";
            list.add(createTiems[0]);
            list.add(createTiems[1]);
        }
        return selectExcept;
    }

}
