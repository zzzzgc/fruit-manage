package com.fruit.manage.controller.customer;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.model.BusinessAuth;
import com.fruit.manage.model.BusinessInfo;
import com.fruit.manage.model.BusinessUser;
import com.fruit.manage.model.User;
import com.fruit.manage.util.Constant;
import com.jfinal.aop.Before;
import com.jfinal.kit.HashKit;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.apache.log4j.Logger;

import java.util.*;

public class CustomerController extends BaseController{
    private Logger logger = Logger.getLogger(getClass());
    @Before(Tx.class)
    public void save() {
        try {
            BusinessAuth businessAuth=getModel(BusinessAuth.class,"",true);
            BusinessInfo businessInfo=getModel(BusinessInfo.class,"",true);
            Integer businessAuthExtID = getParaToInt("businessAuthExtID");
            businessAuth.setId(businessAuthExtID);
            Integer saleUserId =getParaToInt("saleUserId");
           // 给用户信息进行赋值
            BusinessUser businessUser=new BusinessUser();
            if(businessInfo.getPhone()!=null){
                String pwd ="xiguo"+businessInfo.getPhone().substring(7,businessInfo.getPhone().length());
                pwd=HashKit.md5(pwd); // 进行MD5加密
                businessUser.setPass(pwd);
            }
            if(businessInfo.getUId()!=null && businessInfo.getUId()>0){
                businessUser.setId(businessInfo.getUId());
            }
            businessUser.setPhone(businessInfo.getPhone());
            businessUser.setAUserSalesId(saleUserId);
            businessUser.setUpdateTime(new Date());
            businessUser.setCreateTime(new Date());
            // 法人姓名既是用户名
            businessUser.setName(businessAuth.getLegalPersonName());
            // 判断是添加还是修改
            if(businessInfo.getId()!=null && businessInfo.getId()>0){
                businessAuth.setUpdateTime(new Date());
                businessInfo.setUpdateTime(new Date());
                businessAuth.update();
                businessInfo.update();
                //根据商户编号获取商户信息
                BusinessInfo businessInfo2=BusinessInfo.dao.getBusinessInfoByID(businessInfo.getId());
                businessUser.setId(businessInfo2.getUId());
                businessUser.update();
            }else{
                // 给店铺认证赋值
                businessAuth.setCreateTime(new Date());
                businessAuth.setUpdateTime(new Date());
                businessAuth.setAudit(2);
                businessUser.save();
                //设置商户ID
                businessAuth.setUId(businessUser.get("id"));
                // 给店铺信息
                businessInfo.setUId(businessUser.get("id"));
                businessInfo.setCreateTime(new Date());
                businessInfo.setUpdateTime(new Date());
                businessInfo.save(); //添加商户信息
                businessAuth.save(); //添加商户认证
            }
            BusinessUser.dao.updateBusinessUserSaleIDByUid(1, saleUserId);
            renderNull();
        } catch (Exception e) {
            renderErrorText("请检查是否填写正确!");
        }
    }

    /**
     * 获取商户数据
     */
    public void getData(){

        int pageNum = getParaToInt("pageNum", 1);
        int pageSize = getParaToInt("pageSize", 10);

        BusinessInfo model = getModel(BusinessInfo.class);

        String searchProvince = getPara("search_province");
        String searchCity = getPara("search_city");
        String salesName = getPara("sales_name");
        String business_id = getPara("business_id");
        String business_name = getPara("business_name");
        String business_phone = getPara("business_phone");
        String sales_phone = getPara("sales_phone");
        String[] create_time = getParaValues("format_create_time");

        String orderBy = getPara("prop");
        // ascending为升序，其他为降序
        boolean isASC = "ascending".equals(getPara("order"));
        renderJson(BusinessInfo.dao.getData(searchProvince,searchCity,salesName,sales_phone,business_id,business_name,business_phone,create_time,pageNum, pageSize, orderBy, isASC));
    }

    /**
     * 根据编号ID获取商户信息和商户认证
     */
    public void info(){
        Integer id =getParaToInt("id");
        if (id==null || id<=0){
            renderErrorText("商户ID不能为空");
        }
        Map map =new HashMap();
        map.put("businessInfo",BusinessInfo.dao.getByID(id));
        map.put("businessAuth",BusinessAuth.dao.getBusinessAuthByBusinessInfoID(id));
        map.put("saleUser",User.dao.getSaleUserIDByBusinessInfoID(id));
        renderJson(map);
    }

    /**
     * 销售角色和订单管理权限的用户获取商户的部分信息
     */
    public void  getCustomerInfo() {
        Integer uid = getSessionAttr(Constant.SESSION_UID);
        renderJson(BusinessUser.dao.getBusinessUsersByUid(uid));
    }

    /**
     * 销售角色和订单管理权限的用户获取商户的部分信息
     * 只能去服务器查(更保密)
     */
    public void  getCustomerInfoByQuery() {
        Integer uid = getSessionAttr(Constant.SESSION_UID);
        String queryString = getPara("QueryString", " ");
        renderJson(BusinessUser.dao.getBusinessUsersByUidAndQuery(uid,queryString));
    }

    /**
     * 获取所有的销售人员信息
     */
    public void getAllSaleUser(){
        renderJson(User.dao.getAllUserByRoleKey());
    }

    /**
     * 根据手机号码判断是否存在该商户
     */
    public void getBusinessUserByPhone(){
        String phone =getPara("phone");
        renderJson(BusinessUser.dao.getBusinessUserByPhone(phone));
    }
}
