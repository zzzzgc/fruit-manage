package com.fruit.manage.controller.customer;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.model.BusinessAuth;
import com.fruit.manage.model.BusinessInfo;
import com.fruit.manage.util.Constant;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.apache.log4j.Logger;

import java.util.Date;

public class CustomerController extends BaseController{
    private Logger logger = Logger.getLogger(getClass());
    @Before(Tx.class)
    public void save() {
        BusinessAuth businessAuth=getModel(BusinessAuth.class,"",true);
        BusinessInfo businessInfo=getModel(BusinessInfo.class,"",true);
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

    /**
     * 获取商户数据
     */
    public void getData(){
        int pageNum = getParaToInt("pageNum", 1);
        int pageSize = getParaToInt("pageSize", 10);

        String orderBy = getPara("prop");
        boolean isASC = "ascending".equals(getPara("order"));// ascending为升序，其他为降序
        renderJson(BusinessInfo.dao.getData(pageNum, pageSize, orderBy, isASC));
    }
}
