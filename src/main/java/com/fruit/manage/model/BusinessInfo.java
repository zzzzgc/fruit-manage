package com.fruit.manage.model;

import com.fruit.manage.model.base.BaseBusinessInfo;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;

import java.util.ArrayList;
import java.util.List;

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

    public Page<BusinessInfo> getData(int pageNum, int pageSize, String orderBy, boolean isASC) {
        List<Object> params = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer();
        String select = "SELECT buser.id,buser.phone,binfo.business_name,binfo.address_province,binfo.address_city,binfo.address_shop,binfo.address_detail,auser.`name` AS sales_name,auser.phone AS asles_phone,buser.create_time ";
        sql.append("FROM a_user auser JOIN b_business_user buser ON buser.a_user_sales_id = auser.id JOIN b_business_info binfo ON binfo.u_id = buser.id ");
//        if(StrKit.notBlank(groupKey)){
//            sql.append("and group_key = ? ");
//            params.add(groupKey);
//        }
//        if(StrKit.notBlank(key)){
//            sql.append("and key = ? ");
//            params.add(key);
//        }
        orderBy = StrKit.isBlank(orderBy) ? "sort" : orderBy;
        sql.append("ORDER BY " + orderBy + " " + (isASC ? "" : "DESC "));
        System.out.println("sql:" + sql.toString());
        return paginate(pageNum, pageSize, select, sql.toString());
    }
}
