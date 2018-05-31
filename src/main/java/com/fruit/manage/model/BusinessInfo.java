package com.fruit.manage.model;

import com.fruit.manage.model.base.BaseBusinessInfo;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;
import org.apache.commons.lang3.ArrayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import java.util.Map;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class BusinessInfo extends BaseBusinessInfo<BusinessInfo> {
	public static final BusinessInfo dao = new BusinessInfo().dao();

	/**
	 * 根据ID获取商户信息
	 * @param id
	 * @return
	 */
	public BusinessInfo getByID(Integer id) {
		String sql="select bi.id,bi.business_name,bi.business_contacts,bi.phone,bi.address_province," +
				"bi.address_city,bi.address_detail,bi.address_shop," +
				"bi.shipments_type from b_business_info bi where bi.id=?";
		return findFirst(sql,id);
	}

    public Page<BusinessInfo> getData(String searchProvince,String searchCity,String salesName,String sales_phone,String business_id,String uid,String business_phone,String[] createTime,int pageNum, int pageSize, String orderBy, boolean isASC,Integer saleId) {
        ArrayList<Object> params = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer();
        String select = "SELECT " +
                  "binfo.id AS business_id, " +
                  "binfo.u_id AS uid, " +
                  "binfo.phone AS business_phone, " +
                  "binfo.business_name, " +
                  "binfo.address_province, " +
                  "binfo.address_city, " +
                  "binfo.address_shop, " +
                  "binfo.address_detail, " +
                  "auser.nick_name AS sales_name, " +
                  "auser.phone AS sales_phone, " +
                  "bauth.audit, " +
                  "buser.create_time ";
        sql.append("FROM " +
                  "a_user auser " +
                "JOIN b_business_user buser ON buser.a_user_sales_id = auser.id " +
                "JOIN b_business_info binfo ON binfo.u_id = buser.id " +
                "LEFT JOIN b_business_auth bauth ON bauth.u_id = buser.id " +
                "WHERE " +
                  "1 = 1 ");
        // 添加了销售可以看到各自的客户
//        sql.append(" and (case when ? is null then 0 else 1 end = 0 or buser.a_user_sales_id = ? )\n" +
//                "\tand auser.id in (SELECT ur.user_id from a_user_role ur INNER JOIN a_role r on ur.role_id = r.id where r.role_key = 'sales') ");
        sql.append("and ( case when (select ur.user_id from a_user_role ur INNER JOIN a_role r on ur.role_id =r.id where r.role_key = 'supAdmin' and ur.user_id = ? ) is not null then 0 else 1 end =0 ");
            sql.append(" or ( (CASE WHEN ? IS NULL THEN 0 ELSE 1 END = 0 OR buser.a_user_sales_id = ? ) ");
            sql.append(" AND auser.id IN ( SELECT ur.user_id FROM a_user_role ur INNER JOIN a_role r ON ur.role_id = r.id WHERE r.role_key = 'sales' ))) ");
        params.add(saleId);
        params.add(saleId);
        params.add(saleId);
        String noStr = "全部";
        if (StrKit.notBlank(searchProvince) && !searchProvince.equals(noStr)) {
            sql.append("and binfo.address_province like ? ");
            params.add("%"+searchProvince+"%");
        }
        if (StrKit.notBlank(searchCity) && !searchCity.equals(noStr)) {
            sql.append("and binfo.address_city like ? ");
            params.add("%"+searchCity+"%");
        }
        if (StrKit.notBlank(salesName)) {
            sql.append("and auser.nick_name LIKE ? ");
            params.add("%"+salesName+"%");
        }
        if (StrKit.notBlank(sales_phone)) {
            sql.append("and auser.phone like ? ");
            params.add("%"+sales_phone+"%");
        }
        if (StrKit.notBlank(uid)) {
            sql.append("and binfo.u_id LIKE ? ");
            params.add("%"+uid+"%");
        }
        if (StrKit.notBlank(business_id)) {
            sql.append("and binfo.id = ? ");
            params.add(business_id);
        }
        if (StrKit.notBlank(business_phone)) {
            sql.append("and binfo.phone like ? ");
            params.add("%"+business_phone+"%");
        }
        if(ArrayUtils.isNotEmpty(createTime) && createTime.length == 2){
            sql.append("and buser.create_time BETWEEN ? and ? ");
            params.add(createTime[0] + " 00:00:00");
            params.add(createTime[1] + " 23:59:59");
        }
        orderBy = StrKit.isBlank(orderBy) ? "sort" : orderBy;
        sql.append("ORDER BY " + orderBy + " " + (isASC ? "" : "DESC "));
        System.out.println("sql:"+ select + sql.toString());
        return paginate(pageNum, pageSize, select, sql.toString(),params.toArray());
    }

    /**
     * 根据店铺编号获取店铺联系电话号码，发货的详细地址,物流方式
     * @param businessInfoID
     * @return
     */
    public BusinessInfo getBusinessInfoByID(Integer businessInfoID){
	    String sql="select bi.id,bi.phone,CONCAT(bi.address_province,bi.address_city,bi.address_shop) as detailAddress,bi.shipments_type,bi.u_id " +
                "\tfrom b_business_info bi\n" +
                "\twhere bi.id = ? ";
	    return findFirst(sql,businessInfoID);
    }

    /**
     * 根据用户编号获取商会信息
     * @param userId
     * @return
     */
    public BusinessInfo getBusinessInfoByUId(Integer userId) {
        String sql = "SELECT * from b_business_info where u_id = ?";
        return findFirst(sql, userId);
    }
}
