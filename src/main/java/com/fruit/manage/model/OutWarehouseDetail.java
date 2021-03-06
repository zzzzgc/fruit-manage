package com.fruit.manage.model;

import com.fruit.manage.model.base.BaseOutWarehouseDetail;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Page;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class OutWarehouseDetail extends BaseOutWarehouseDetail<OutWarehouseDetail> {
	public static final OutWarehouseDetail dao = new OutWarehouseDetail().dao();


    public Page<OutWarehouseDetail> getData(int pageNum, int pageSize, String orderBy, boolean isASC, String[] createTime, OutWarehouseDetail outWarehouseDetail) {
        List<Object> params = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer();
        sql.append("FROM b_out_warehouse_detail AS owd WHERE 1 = 1 ");

        Integer outId = outWarehouseDetail.getOutId();
        sql.append("AND owd.out_id = ? ");
        params.add(outId);

        if (StringUtils.isNotBlank(outWarehouseDetail.getProductStandardName())){
            sql.append("AND owd.product_standard_name LIKE CONCAT('%',?,'%') ");
            params.add(outWarehouseDetail.getProductStandardName());
        }

        if (StringUtils.isNotBlank(outWarehouseDetail.getProductName())){
            sql.append("AND owd.product_name LIKE CONCAT('%',?,'%') ");
            params.add(outWarehouseDetail.getProductName());
        }

        if (outWarehouseDetail.getProductId() != null){
            sql.append("AND owd.product_id = ? ");
            params.add(outWarehouseDetail.getProductId());
        }

        if (outWarehouseDetail.getProductStandardId() != null){
            sql.append("AND owd.product_standard_id = ? ");
            params.add(outWarehouseDetail.getProductStandardId());
        }

        orderBy = StrKit.isBlank(orderBy) ? "owd.create_time" : orderBy;
        sql.append("order by " + orderBy + " " + (isASC ? "" : "desc "));

        String selectStr = "SELECT\n" +
                "\towd.id,\n" +
                "\towd.product_name,\n" +
                "\towd.product_id,\n" +
                "\towd.product_standard_id,\n" +
                "\towd.product_standard_name,\n" +
                "\towd.product_weight,\n" +
                "\towd.out_num,\n" +
                "\towd.out_price,\n" +
                "\towd.out_time,\n" +
                "\towd.out_total_price,\n" +
                "\towd.out_average_price,\n" +
                "\towd.user_name,\n" +
                "\towd.user_id,\n" +
                "\towd.order_num,\n" +
                "\towd.order_time,\n" +
                "\towd.out_type,\n" +
                "\towd.approver_name,\n" +
                "\towd.out_remark,\n" +
                "\towd.create_time,\n" +
                "\towd.update_time,\n" +
                "\towd.out_Id ";

        System.out.println(selectStr + sql.toString());
        return paginate(pageNum, pageSize, selectStr, sql.toString(), params.toArray());
    }

    /**
     * 根据出库id导出所有出库详细
     * @param outId
     * @return
     */
    public List<OutWarehouseDetail> getOutWarehouseDetail(Integer outId) {
        if (outId == null) {
            return null;
        }
        return dao.find("SELECT " +
                " ow.id, " +
                " ow.out_Id, " +
                " ow.product_id, " +
                " ow.product_standard_id, " +
                " ow.user_id, " +
                " ow.product_name, " +
                " ow.product_standard_name, " +
                " ow.product_weight, " +
                " ow.out_num, " +
                " ow.out_price, " +
                " ow.out_total_price, " +
                " ow.out_average_price, " +
                " ow.user_name, " +
                " ow.order_detail_id, " +
                " ow.order_num, " +
                " ow.order_time, " +
                " ow.out_type, " +
                " ow.approver_name, " +
                " ow.out_remark, " +
                " ow.out_time, " +
                " ow.create_time, " +
                " ow.update_time " +
                "FROM " +
                " b_out_warehouse_detail AS ow " +
                "WHERE " +
                " ow.out_Id = ? ",outId);
    }

    /**
     * 添加出库
     * @return
     */
    public OutWarehouseDetail addOutWarehouseDetail(Integer productStandardId, String productName, String approverName, String productStandardName, Date orderTime, Integer productId, Integer outType, Date
            outTime, BigDecimal outTotalPrice, Integer outNum, Integer orderNum, Integer outId, BigDecimal outAveragePrice, String productWeight, String userName, Integer userId, Integer orderDetailId, BigDecimal outPrice, String outRemark) {
        OutWarehouseDetail outWarehouseDetail = new OutWarehouseDetail();
        outWarehouseDetail.setProductId(productId);
        outWarehouseDetail.setOrderDetailId(orderDetailId);
        outWarehouseDetail.setUserId(userId);
        outWarehouseDetail.setOutId(outId);
        outWarehouseDetail.setProductName(productName);
        outWarehouseDetail.setProductWeight(productWeight);
        outWarehouseDetail.setOutNum(outNum);
        outWarehouseDetail.setOutPrice(outPrice);
        outWarehouseDetail.setProductStandardId(productStandardId);
        outWarehouseDetail.setProductStandardName(productStandardName);
        outWarehouseDetail.setOutAveragePrice(outAveragePrice);
        outWarehouseDetail.setApproverName(approverName);
        outWarehouseDetail.setOutTime(outTime);
        outWarehouseDetail.setOrderNum(orderNum);
        outWarehouseDetail.setOutRemark(outRemark);
        outWarehouseDetail.setUserName(userName);
        outWarehouseDetail.setOutType(outType);
        outWarehouseDetail.setCreateTime(new Date());
        outWarehouseDetail.setUpdateTime(new Date());
        outWarehouseDetail.setOrderTime(orderTime);
        outWarehouseDetail.setOutTotalPrice(outTotalPrice);
        outWarehouseDetail.save();
        return outWarehouseDetail;
    }


}
