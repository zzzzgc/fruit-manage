package com.fruit.manage.controller.Procurement;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.model.ProcurementPlan;
import com.jfinal.plugin.activerecord.Page;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class PlanController extends BaseController {
    public static Logger logger= Logger.getLogger(PlanController.class);

    public void getPlan(){
        int pageNum = getParaToInt("pageNum", 1);
        int pageSize = getParaToInt("pageSize", 10);
        Map map =new HashMap();
        String orderBy = getPara("prop");
        map.put("createTime", getParaValues("format_create_time"));
        // ascending为升序，其他为降序
        boolean isASC = "ascending".equals(getPara("order"));
        Page<ProcurementPlan> pPlanPage = ProcurementPlan.dao.getAllProcurementPlan(pageNum, pageSize, orderBy, isASC, map);
        renderJson(pPlanPage);
    }

    public void delPPlan(){
        int pPlanId = getParaToInt("pPlanId");
        renderResult(ProcurementPlan.dao.deleteById(pPlanId));
    }
}
