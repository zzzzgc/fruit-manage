package com.fruit.manage.controller.statement;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.controller.common.CommonController;
import com.fruit.manage.model.OrderDetail;
import com.fruit.manage.model.ProcurementPlanDetail;
import com.fruit.manage.model.ProductStandard;
import com.fruit.manage.model.User;
import com.fruit.manage.util.Constant;
import com.fruit.manage.util.ExcelCommon;
import com.fruit.manage.util.excel.ExcelException;
import com.jfinal.kit.StrKit;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

/**
 * @author partner
 * @date 2018/6/1 17:41
 */
public class InventoryStatisticsController extends BaseController {
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
        String select = _getSqlSelect();
        // TODO 修改参数
        String selectExcept = _getSqlSelectExcept(list, productId, productName, productStandardId, productStandardName);
        System.out.println("-------------库存统计报表SQL START--------------");
        System.out.println(select + selectExcept);
        for (int i = 0; i < list.size(); i++) {
            System.out.println("list index:"+i+",list value:"+list.get(i));
        }
        System.out.println("-------------库存统计报表SQL END--------------");
        renderJson(ProductStandard.dao.paginate(pageNum,pageSize,select,selectExcept,list.toArray()));
    }

    public void exportInventoryStatistics() {
        Integer productId = getParaToInt("product_id");
        String productName = getPara("product_name");
        Integer productStandardId = getParaToInt("product_standard_id");
        String productStandardName = getPara("product_standard_name");
        String select = _getSqlSelect();
        String selectExcept = _getSqlSelectExcept(list, productId, productName, productStandardId, productStandardName);
        System.out.println("-------------产品销售排行版报表SQL START--------------");
        System.out.println(select + selectExcept);
        for (int i = 0; i < list.size(); i++) {
            System.out.println("list index:"+i+",list value:"+list.get(i));
        }
        System.out.println("-------------产品销售排行版报表SQL END--------------");

        String[] headers = {"商品名称","规格名称","规格编号","库存数量","今日备注"};
        List<ProductStandard> objects = ProductStandard.dao.find(select + selectExcept, list.toArray());
        if (objects != null && objects.size() >0) {
            List<String[]> lists = new ArrayList<>();
            for (int i = 0; i < objects.size(); i++) {
                ProductStandard productStandard=objects.get(i);
                String[] strings = new String[headers.length];
                strings[0] = productStandard.get("productName")+"";
                strings[1] = productStandard.get("productStandardName")+"";
                strings[2] = productStandard.get("productStandardId")+"";
                strings[3] = productStandard.get("stock")+"";
                strings[4] = productStandard.get("todayRemark")+"";
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
        String select = "SELECT p.`name` as productName,p.id  as productId,ps.`name` as productStandardName,ps.id as productStandardId,ps.stock as stock,(SELECT \"\") as todayRemark ";
        return select;
    }

    private String _getSqlSelectExcept(List list, Integer productId, String productName, Integer productStandardId, String productStandardName) {
        String selectExcept = "\tfrom b_product p \n" +
                "\tINNER JOIN b_product_standard ps ON p.id = ps.product_id\n" +
                "\twhere 1=1\n";
        if (productId != null && productId > 0) {
            selectExcept += "\tAND p.id LIKE ? \n";
            list.add("%" + productId + "%");
        }
        if (StrKit.notBlank(productName)) {
            selectExcept+="\tand p.name like ?\n";
            list.add("%" + productName + "%");
        }
        if (productStandardId != null && productStandardId > 0) {
            selectExcept+="\tand ps.id LIKE ?\n";
            list.add("%" + productStandardId + "%");
        }
        if (StrKit.notBlank(productStandardName)) {
            selectExcept+="\tand ps.`name` LIKE ?\n";
            list.add("%" + productStandardName + "%");
        }

        selectExcept+="\tORDER BY ps.stock desc,p.id,ps.id";
        return selectExcept;
    }
}
