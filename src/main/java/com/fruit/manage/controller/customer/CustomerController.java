package com.fruit.manage.controller.customer;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.model.BusinessAuth;
import com.fruit.manage.model.BusinessInfo;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.apache.log4j.Logger;

import java.util.*;

public class CustomerController extends BaseController{
    private Logger logger = Logger.getLogger(getClass());
    @Before(Tx.class)
    public void save() {
        BusinessAuth businessAuth=getModel(BusinessAuth.class,"",true);
        BusinessInfo businessInfo=getModel(BusinessInfo.class,"",true);
        if(businessInfo.getId()!=null && businessInfo.getId()>0){
            businessAuth.setUpdateTime(new Date());
            businessInfo.setUpdateTime(new Date());
            businessAuth.update();
            businessInfo.update();
        }else{
            // 给店铺认证赋值
            businessAuth.setCreateTime(new Date());
            businessAuth.setUpdateTime(new Date());
            businessAuth.setAudit(2);
            // TODO 获取用户ID并赋值
            businessAuth.setUId(1);
            // 给店铺信息
            businessInfo.setUId(1);
            businessInfo.setCreateTime(new Date());
            businessInfo.setUpdateTime(new Date());
            businessInfo.save(); //添加商户信息
            businessAuth.save(); //添加商户认证
        }
    }


    /**
     * 获取商户数据
     */
    public void getData(){

        int pageNum = getParaToInt("pageNum", 1);
        int pageSize = getParaToInt("pageSize", 10);

        String searchProvince = getPara("search_province");
        String searchCity = getPara("search_city");
        String salesName = getPara("sales_name");
        String business_name = getPara("business_name");
        String[] create_time = getParaValues("format_create_time");

        String orderBy = getPara("prop");
        // ascending为升序，其他为降序
        boolean isASC = "ascending".equals(getPara("order"));
        renderJson(BusinessInfo.dao.getData(searchProvince,searchCity,salesName,business_name,create_time,pageNum, pageSize, orderBy, isASC));
    }

    /**
     * 根据编号ID获取商户信息和商户认证
     */
    public void info(){
        Integer id =getParaToInt("id");
        if (id==null || id<=0){
            renderErrorText("商品ID不能为空");
        }
        Map map =new HashMap();
        map.put("businessInfo",BusinessInfo.dao.getByID(id));
        map.put("businessAuth",BusinessAuth.dao.getBusinessAuthByBusinessInfoID(id));
        renderJson(map);
    }
}
