package com.fruit.manage.model;

import com.fruit.manage.model.base.BaseBusinessUser;
import com.jfinal.kit.HashKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class BusinessUser extends BaseBusinessUser<BusinessUser> {
    public static final BusinessUser dao = new BusinessUser().dao();

    public void updateBusinessUserSaleIDByUid(Integer uid, Integer saleId) {
        Db.tx(new IAtom() {
            @Override
            public boolean run() throws SQLException {
                String sql = "update b_business_user bu set bu.a_user_sales_id=? where bu.id=?";
                Db.update(sql, saleId, uid);
                return true;
            }
        });
    }

    /**
     * 根据uid获取用户信息
     *
     * @param uid
     */
    public List<BusinessUser> getBusinessUsersByUid(Integer uid) {
        // 销售只能获取自己的客户,其他人能获取全部,但是其他人除了超级管理员都不能调用这个方法,因为权限控制.
        String sql = "SELECT  " +
                "u.`name` AS business_user_name,  " +
                "u.id,  " +
                "u.phone,  " +
                "u.nick_name,  " +
                "u.a_user_sales_id AS sales_id  " +
                "FROM  " +
                //只能获取自己的客户
                "b_business_user AS u WHERE u.a_user_sales_id = ? ";
        return dao.find(sql, uid);
    }

    /**
     * 根据uid和查询字段获取用户信息
     *
     * @param uid
     */
    public List<BusinessUser> getBusinessUsersByUidAndQuery(Integer uid, String queryString) {
        // 销售只能获取自己的客户,其他人能获取全部,但是其他人除了超级管理员都不能调用这个方法,因为权限控制.
        String sql = "SELECT  " +
                "u.`name` AS business_user_name,  " +
                "u.id,  " +
                "u.phone,  " +
                "u.nick_name,  " +
                "u.a_user_sales_id AS sales_id  " +
                "FROM  " +
                "b_business_user AS u  " +
                // 销售只能获取自己的客户
                "WHERE u.a_user_sales_id = ? AND u.`name` LIKE CONCAT('%','?,'%')";
        return dao.find(sql, uid, queryString);
    }

    /**
     * 根据手机号码获取商户信息
     *
     * @param phone 手机号码
     * @return 返回一个商户信息
     */
    public BusinessUser getBusinessUserByPhone(String phone) {
        String sql = "select * from b_business_user bu where bu.phone = ?";
        return dao.findFirst(sql, phone);
    }

    /**
     * 获取商户必要信息
     *
     * @param customerId
     * @return
     */
    public BusinessUser getCustomerInfo(String customerId) {
        StringBuffer sql = new StringBuffer();
        String selectStr = "SELECT\n" +
                "\tCONCAT(\n" +
                "\t\tinfo.address_province,\n" +
                "\t\tinfo.address_city,\n" +
                "\t\tinfo.address_detail\n" +
                "\t) AS buy_address,\n" +
                "\tau.`name` AS a_user_sales,\n" +
                "\tu.id AS business_user_id,\n" +
                "\tinfo.phone AS buy_phone,\n" +
                "\tu.`name` AS buy_user_name,\n" +
                "\tinfo.shipments_type AS delivery_type,\n" +
                "\tDATE_FORMAT(NOW(), '%Y %T') AS create_time ";
        sql.append("FROM\n" +
                "\tb_business_user AS u\n" +
                "INNER JOIN b_business_info AS info ON u.id = info.u_id\n" +
                "INNER JOIN a_user AS au ON u.a_user_sales_id = au.id WHERE 1 = 1 ");
        sql.append("AND u.id = ? ");
        return dao.findFirst(selectStr + sql, customerId);
    }

    /**
     * 根据用户编号获取用户姓名，电话号码
     *
     * @param businessUserId
     * @return
     */
    public BusinessUser getBusinessUserByID(Integer businessUserId) {
        String sql = "select bu.id,bu.name,bu.phone,bu.a_user_sales_id from b_business_user bu  where bu.id = ?";
        return findFirst(sql, businessUserId);
    }

    /**
     *  根据用户名来获取用户
     * @param name
     * @return
     */
    public List<BusinessUser> getBusinessUserByName(String name){
        String sql = "select bu.id,bu.name,bu.phone,bu.a_user_sales_id from b_business_user bu  where bu.`name` = ?";
        return find(sql, name);
    }

    /**
     * 封装的添加用户的方法
     * @param userName
     * @param userPhone
     * @param salesUserId
     * @return
     */
    public BusinessUser addBusinessUser(String userName, String userPhone, Integer salesUserId) {
        BusinessUser businessUser = new BusinessUser();
        businessUser.setAUserSalesId(salesUserId);
        businessUser.setName(userName);
        businessUser.setPass(HashKit.md5("xiguo"+userPhone.substring(userPhone.length()-4,userPhone.length())));
        businessUser.setPhone(userPhone);
        businessUser.setPhone(userPhone);
        businessUser.setUpdateTime(new Date());
        businessUser.setCreateTime(new Date());
        businessUser.save();
        return businessUser;
    }
}
