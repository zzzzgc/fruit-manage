package com.fruit.manage.model;

import com.fruit.manage.model.base.BaseProcurementQuota;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;
import com.sun.org.apache.bcel.internal.generic.Select;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class ProcurementQuota extends BaseProcurementQuota<ProcurementQuota> {
	public static final ProcurementQuota dao = new ProcurementQuota().dao();

    public Page<ProcurementQuota> getData(int pageNum, int pageSize, String orderBy, boolean isASC, ProcurementQuota quota, String[] createTime) {
        String select = "SELECT\n" +
                "\tpq.id,\n" +
                "\tpq.product_id,\n" +
                "\tpq.product_name,\n" +
                "\tpq.product_standard_id,\n" +
                "\tpq.product_standard_name,\n" +
                "\tpq.procurement_id,\n" +
                "\tpq.procurement_name,\n" +
                "\tpq.procurement_phone,\n" +
                "\tpq.create_time,\n" +
                "\tpq.update_time \n ";

        List<Object> params = new ArrayList<Object>();

        StringBuffer fromAndWhere =  fromAndWhere = new StringBuffer();
        fromAndWhere.append(" FROM b_procurement_quota AS pq WHERE 1 = 1 ");

        if (quota.getProcurementId() !=null ) {
            fromAndWhere.append(" AND pq.procurement_id = ? ");
            params.add(quota.getProcurementId());
        }

        if (quota.getProductId() !=null ) {
            fromAndWhere.append(" AND pq.product_id = ? ");
            params.add(quota.getProductId());
        }

        if (quota.getProductStandardId() !=null ) {
            fromAndWhere.append(" AND pq.product_standard_id = ? ");
            params.add(quota.getProductStandardId());
        }

        if (StringUtils.isNotBlank(quota.getProcurementName())) {
            fromAndWhere.append(" AND pq.procurement_name = ? ");
            params.add(quota.getProcurementName());
        }

        if (StringUtils.isNotBlank(quota.getProductName())) {
            fromAndWhere.append(" AND pq.product_name = ? ");
            params.add(quota.getProductName());
        }

        if (StringUtils.isNotBlank(quota.getProductStandardName())) {
            fromAndWhere.append(" AND pq.product_standard_name = ? ");
            params.add(quota.getProductStandardName());
        }

        if (StringUtils.isNotBlank(quota.getProcurementPhone())) {
            fromAndWhere.append(" AND pq.procurement_phone = ? ");
            params.add(quota.getProcurementPhone());
        }

        if (ArrayUtils.isNotEmpty(createTime) && createTime.length == 2) {
            fromAndWhere.append(" AND pq.create_time BETWEEN ? and ? ");
            params.add(createTime[0] + " 00:00:00");
            params.add(createTime[1] + " 23:59:59");
        }

        orderBy = StrKit.isBlank(orderBy) ? "o.create_time" : orderBy;
        fromAndWhere.append(" order by " + orderBy + " " + (isASC ? "" : "desc "));
        System.out.println(select+fromAndWhere);
        System.out.println(params);
        return paginate(pageNum,pageSize,select,fromAndWhere.toString(),params.toArray());
    }
}
