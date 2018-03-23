package com.fruit.manage.controller.procurement;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.model.Order;
import com.fruit.manage.model.ProcurementPlan;
import com.fruit.manage.model.ProcurementQuota;
import com.fruit.manage.util.ExcelCommon;
import com.jfinal.plugin.activerecord.Page;
import org.apache.log4j.Logger;

import java.io.File;

/**
 * 采购配额
 *
 * @author ZGC
 * @date 2018-03-21 16:29
 **/
public class QuotaContrller extends BaseController {
    private org.apache.log4j.Logger log = Logger.getLogger(getClass());


//    @RequiresPermissions("order:query")

    /**
     * 获取所有订单的列表数据
     */
    public void getData() {
        log.info("获取数据:" + getParaMap());
        ProcurementQuota quota = getModel(ProcurementQuota.class, "", true);

        int pageNum = getParaToInt("pageNum", 1);
        int pageSize = getParaToInt("pageSize", 10);
        String orderBy = getPara("prop");
        // ascending为升序，其他为降序
        boolean isASC = "ascending".equals(getPara("order"));

        // 下单时间
        String[] orderTime = getParaValues("format_create_time");
        Page<ProcurementQuota> pageData = ProcurementQuota.dao.getData(pageNum, pageSize, orderBy, isASC, quota, orderTime);
        renderJson(pageData);
    }

    /**
     * 导出所有的商品规格信息表
     */
    public void getProductStandardLibrary() {
        System.out.println("进来了");
        // xls表头顺序：商品名，规格名，规格编码，采购姓名，采购人id
//        getRequest().getSession().getServletContext().getRealPath("static/excel");
//        File file = new File("C:" + File.separator + "Users" + File.separator + "Administrator" + File.separator + "Desktop" + File.separator + "项目");
        File file = new File("项目.png");
        renderFile("test.txt","aaa.png");
    }
}
