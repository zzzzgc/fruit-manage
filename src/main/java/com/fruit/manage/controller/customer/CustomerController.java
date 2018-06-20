package com.fruit.manage.controller.customer;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.model.*;
import com.fruit.manage.util.Constant;
import com.fruit.manage.util.MessageSend;
import com.fruit.manage.util.excelRd.ExcelRd;
import com.fruit.manage.util.excelRd.ExcelRdException;
import com.fruit.manage.util.excelRd.ExcelRdRow;
import com.fruit.manage.util.excelRd.ExcelRdTypeEnum;
import com.jfinal.aop.Before;
import com.jfinal.kit.HashKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

public class CustomerController extends BaseController {
    private Logger logger = Logger.getLogger(getClass());

    @Before(Tx.class)
    public void importProduct() {
        String filePath = "C:\\Users\\Administrator\\Desktop\\fruit-document\\importData\\product02.xlsx";
        Iterator<ExcelRdRow> iterator = readExcel2(filePath).iterator();
        while (iterator.hasNext()) {
            ExcelRdRow excelRdRow = iterator.next();
            List<Object> list = excelRdRow.getRow();

        }
    }


    public static Integer getShimpmentType(String values) {
        if ("物流车".equals(values)) {
            return 1;
        } else if ("自提".equals(values)) {
            return 2;
        } else if ("市场车".equals(values)) {
            return  0;
        }else {
            return -1;
        }
    }

    /**
     * 批量导入
     */
    @Before(Tx.class)
    public void importBusiness(){
        String filePath = "C:\\Users\\Administrator\\Desktop\\fruit-document\\importData\\business.xlsx";
        Iterator<ExcelRdRow> iterator = readExcel(filePath).iterator();
        int count = 0;
        Date date = new Date();
        while (iterator.hasNext()) {
            ExcelRdRow excelRdRow = iterator.next();
            List<Object> list = excelRdRow.getRow();
            count++;
            if (count > 1) {
                String businessName = list.get(0) + "";
                String businessContacts = list.get(1) + "";
                String phone = list.get(2) + "";
                String province = list.get(3) + "";
                String city = list.get(4) + "";
                String addressDetail = list.get(5) + "";
                String salesName = list.get(6) + "";
                String shipmentsTypeValue = list.get(7) + "";
//                Integer userId = User.dao.getUserIdByName(salesName);
                String sql = "SELECT id from a_user where nick_name like  ? ";
                Integer userId= Db.queryInt(sql, "%"+salesName+"%");

                if(phone.length()>11){
                    System.out.println("phone:"+phone);
                }

                BusinessUser businessUser =new BusinessUser();
                businessUser.setAUserSalesId(userId);
                businessUser.setName(businessContacts);
                businessUser.setNickName(businessContacts);
                String pwd = "xiguo" + phone.substring(7, phone.length());
                pwd = HashKit.md5(pwd);
                businessUser.setPass(pwd);
                businessUser.setPhone(phone);
                businessUser.setCreateTime(date);
                businessUser.setUpdateTime(date);
                businessUser.save();

                BusinessInfo businessInfo =new BusinessInfo();
                businessInfo.setUId(businessUser.getId());
                businessInfo.setBusinessName(businessName);
                businessInfo.setBusinessContacts(businessContacts);
                businessInfo.setPhone(phone);
                businessInfo.setAddressProvince(province);
                businessInfo.setAddressCity(city);
                businessInfo.setAddressDetail(addressDetail);
                businessInfo.setAddressShop(addressDetail);
                businessInfo.setShipmentsType(getShimpmentType(shipmentsTypeValue));
                businessInfo.setCreateTime(date);
                businessInfo.setUpdateTime(date);
                businessInfo.save();


                BusinessAuth businessAuth =new BusinessAuth();
                businessAuth.setUId(businessUser.getId());
                businessAuth.setLegalPersonName(businessContacts);
                businessAuth.setIdentity("441521198010211230");
                businessAuth.setBankAccount("6212262201023557228");
                businessAuth.setBusinessLicense("532501100006302");
                businessAuth.setAuthType("1");
                businessAuth.setAudit(2);
                businessAuth.setImgIdentityFront("http://www.atool.org/placeholder.png?size=300x200&text=%E9%BB%98%E8%AE%A4%E5%9B%BE%E7%89%87&&bg=868686&fg=fff");
                businessAuth.setImgIdentityReverse("http://www.atool.org/placeholder.png?size=300x200&text=%E9%BB%98%E8%AE%A4%E5%9B%BE%E7%89%87&&bg=868686&fg=fff");
                businessAuth.setImgLicense("http://www.atool.org/placeholder.png?size=300x200&text=%E9%BB%98%E8%AE%A4%E5%9B%BE%E7%89%87&&bg=868686&fg=fff");
                businessAuth.setCreateTime(date);
                businessAuth.setUpdateTime(date);
                businessAuth.save();
            }
        }
        renderNull();
    }

    @Before(Tx.class)
    public void save() {
            BusinessAuth businessAuth = getModel(BusinessAuth.class, "", true);
            BusinessInfo businessInfo = getModel(BusinessInfo.class, "", true);
            Integer businessAuthExtID = getParaToInt("businessAuthExtID");
            businessAuth.setId(businessAuthExtID);
            Integer saleUserId = getParaToInt("saleUserId");
            // 给用户信息进行赋值
            BusinessUser businessUser = new BusinessUser();
            if (businessInfo.getPhone() != null) {
                String pwd = "xiguo" + businessInfo.getPhone().substring(7, businessInfo.getPhone().length());
                pwd = HashKit.md5(pwd); // 进行MD5加密
                businessUser.setPass(pwd);
            }
            if (businessInfo.getUId() != null && businessInfo.getUId() > 0) {
                businessUser.setId(businessInfo.getUId());
            }
            businessUser.setPhone(businessInfo.getPhone());
            businessUser.setAUserSalesId(saleUserId);
            businessUser.setUpdateTime(new Date());
            businessUser.setCreateTime(new Date());
            // 法人姓名既是用户名
            businessUser.setName(businessAuth.getLegalPersonName());
            // 判断是添加还是修改
            if (businessInfo.getId() != null && businessInfo.getId() > 0) {
                businessAuth.setUpdateTime(new Date());
                businessInfo.setUpdateTime(new Date());
                businessAuth.update();
                businessInfo.update();
                //根据商户编号获取商户信息
                BusinessInfo businessInfo2 = BusinessInfo.dao.getBusinessInfoByID(businessInfo.getId());
                businessUser.setId(businessInfo2.getUId());
                businessUser.update();
            } else {
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
    }

    public static List<ExcelRdRow> readExcel2(String filePath) {
        ExcelRd excelRd = new ExcelRd(filePath);
        excelRd.setStartRow(2);
        excelRd.setStartCol(0);
        ExcelRdTypeEnum[] types = {
                ExcelRdTypeEnum.STRING,
                ExcelRdTypeEnum.STRING,
                ExcelRdTypeEnum.STRING,
                ExcelRdTypeEnum.STRING,
                ExcelRdTypeEnum.STRING, // 5
                ExcelRdTypeEnum.DOUBLE,
                ExcelRdTypeEnum.DOUBLE,
                ExcelRdTypeEnum.DOUBLE,
                ExcelRdTypeEnum.STRING,
                ExcelRdTypeEnum.STRING, // 10
                ExcelRdTypeEnum.STRING,
                ExcelRdTypeEnum.STRING,
                ExcelRdTypeEnum.STRING,
                ExcelRdTypeEnum.STRING,
                ExcelRdTypeEnum.INTEGER, // 15
                ExcelRdTypeEnum.STRING,
                ExcelRdTypeEnum.STRING,
                ExcelRdTypeEnum.STRING,
                ExcelRdTypeEnum.STRING,
                ExcelRdTypeEnum.STRING, // 20
                ExcelRdTypeEnum.STRING,
                ExcelRdTypeEnum.STRING,
                ExcelRdTypeEnum.STRING


        };
        // 指定每列的类型
        excelRd.setTypes(types);

        List<ExcelRdRow> rows = null;
        try {
            rows = excelRd.analysisXlsx();
        } catch (ExcelRdException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rows;
    }


    public static List<ExcelRdRow> readExcel(String filePath) {
        ExcelRd excelRd = new ExcelRd(filePath);
        excelRd.setStartRow(1);
        excelRd.setStartCol(0);
        ExcelRdTypeEnum[] types = {
                ExcelRdTypeEnum.STRING,
                ExcelRdTypeEnum.STRING,
                ExcelRdTypeEnum.STRING,
                ExcelRdTypeEnum.STRING,
                ExcelRdTypeEnum.STRING,
                ExcelRdTypeEnum.STRING,
                ExcelRdTypeEnum.STRING,
                ExcelRdTypeEnum.STRING
        };
        // 指定每列的类型
        excelRd.setTypes(types);

        List<ExcelRdRow> rows = null;
        try {
            rows = excelRd.analysisXlsx();
        } catch (ExcelRdException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rows;
    }

    /**
     * 获取商户数据
     */
    public void getData() {

        int pageNum = getParaToInt("pageNum", 1);
        int pageSize = getParaToInt("pageSize", 10);

        BusinessInfo model = getModel(BusinessInfo.class);

        String searchProvince = getPara("search_province");
        String searchCity = getPara("search_city");
        String salesName = getPara("sales_name");
        String business_id = getPara("business_id");
        String business_name = getPara("business_name");
        String uid = getPara("uid");
        String business_phone = getPara("business_phone");
        String sales_phone = getPara("sales_phone");
        String[] create_time = getParaValues("format_create_time");

        String orderBy = getPara("prop");
        // ascending为升序，其他为降序
        boolean isASC = "ascending".equals(getPara("order"));
        Integer userId = getSessionAttr(Constant.SESSION_UID);
        renderJson(BusinessInfo.dao.getData(searchProvince, searchCity, salesName, sales_phone, business_id, uid, business_phone, create_time, pageNum, pageSize, orderBy, isASC,userId,business_name));
    }

    /**
     * 根据编号ID获取商户信息和商户认证
     */
    public void info() {
        Integer id = getParaToInt("id");
        if (id == null || id <= 0) {
            renderErrorText("商户ID不能为空");
        }
        Map map = new HashMap();
        map.put("businessInfo", BusinessInfo.dao.getByID(id));
        map.put("businessAuth", BusinessAuth.dao.getBusinessAuthByBusinessInfoID(id));
        map.put("saleUser", User.dao.getSaleUserIDByBusinessInfoID(id));
        renderJson(map);
    }

    /**
     * 销售角色和订单管理权限的用户获取商户的部分信息
     */
    public void getCustomerInfo() {
        Integer uid = getSessionAttr(Constant.SESSION_UID);
        renderJson(BusinessUser.dao.getBusinessUsersByAUid(uid));
    }

    /**
     * 销售角色和订单管理权限的用户获取商户的部分信息
     * 只能去服务器查(更保密)
     */
    public void getCustomerInfoByQuery() {
        Integer uid = getSessionAttr(Constant.SESSION_UID);
        String queryString = getPara("QueryString", " ");
        renderJson(BusinessUser.dao.getBusinessUsersByUidAndQuery(uid, queryString));
    }

    /**
     * 获取所有的销售人员信息
     */
    public void getAllSaleUser() {
        renderJson(User.dao.getAllUserByRoleKey());
    }

    /**
     * 根据手机号码判断是否存在该商户
     */
    public void getBusinessUserByPhone() {
        String phone = getPara("phone");
        renderJson(BusinessUser.dao.getBusinessUserByPhone(phone));
    }

    /**
     * 修改认证状态(用户是否可用开关)
     */
    public void setStatus() {
        String status = getPara("status");
        String uid = getPara("uid");
        if (BusinessAuth.dao.setStatus(uid, status)) {
            if ("2".equals(status)) {
                // 修改成功
                BusinessUser user = BusinessUser.dao.findById(uid);
                // 关闭订单锁
                user.setLock(0);
                user.update();
                MessageSend.sendMessage(user.getPhone(),Param.dao.getParam("message.content.approve"));
            }
            renderNull();
            return;
        }
        renderErrorText("没有修改成功");
    }

    /**
     * 修改禁单状态(用户是否可下单开关)
     */
    public void setLockStatus() {
        String status = getPara("status");
        String uid = getPara("uid");
        if (BusinessAuth.dao.setLockStatus(uid, status)) {
            renderNull();
            return;
        }
        renderErrorText("修改失败");
    }
}
