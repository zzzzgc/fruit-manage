package com.fruit.manage.model;

import com.fruit.manage.model.base.BaseBusinessUser;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;

import java.sql.SQLException;
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
        String sql = "SELECT\n" +
                "\tu.`name` AS business_user_name,\n" +
                "\tu.id,\n" +
                "\tu.phone,\n" +
                "\tu.nick_name,\n" +
                "\tu.a_user_sales_id AS sales_id\n" +
                "FROM\n" +
                "\tb_business_user AS u ";
        if (!User.dao.isSales(uid)) {
            return dao.find(sql);
        }
        // 销售只能获取自己的客户
        return dao.find(sql + " WHERE u.a_user_sales_id = ? ", uid);
    }

    /**
     * 根据uid和查询字段获取用户信息
     * @param uid
     */
    public List<BusinessUser> getBusinessUsersByUidAndQuery(Integer uid, String queryString) {
        // 销售只能获取自己的客户,其他人能获取全部,但是其他人除了超级管理员都不能调用这个方法,因为权限控制.
        String sql = "SELECT\n" +
                "\tu.`name` AS business_user_name,\n" +
                "\tu.id,\n" +
                "\tu.phone,\n" +
                "\tu.nick_name,\n" +
                "\tu.a_user_sales_id AS sales_id\n" +
                "FROM\n" +
                "\tb_business_user AS u ";
        if (!User.dao.isSales(uid)) {
            return dao.find(sql + " WHERE u.`name` LIKE CONCAT('%',?,'%')", queryString);
        }
        // 销售只能获取自己的客户
        return dao.find(sql + " WHERE u.a_user_sales_id = ? AND u.`name` LIKE CONCAT('%','?,'%')", uid,queryString);
    }

    /**
     * 获取商户必要信息
     * @param customerId
     * @return
     */
    public BusinessUser getCustomerInfo(String customerId){
        StringBuffer sql = new StringBuffer();
        String selectStr = "SELECT\n" +
                "\tCONCAT(\n" +
                "\t\tinfo.address_province,\n" +
                "\t\tinfo.address_city,\n" +
                "\t\tinfo.address_detail\n" +
                "\t) AS buy_address,\n" +
                "\tau.`name` AS a_user_sales,\n" +
                "\tinfo.phone AS buy_phone,\n" +
                "\tu.`name` AS buy_user_name,\n" +
                "\tinfo.shipments_type AS delivery_type,\n" +
                "\tDATE_FORMAT(NOW(), '%Y %T') AS create_time ";
        sql.append("FROM\n" +
                "\tb_business_user AS u\n" +
                "INNER JOIN b_business_info AS info ON u.id = info.u_id\n" +
                "INNER JOIN a_user AS au ON u.a_user_sales_id = au.id WHERE 1 = 1 ");
        sql.append("AND u.id = ? ");
        return dao.findFirst(selectStr + sql,customerId);
    }
}
