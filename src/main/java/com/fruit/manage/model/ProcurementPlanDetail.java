package com.fruit.manage.model;

import com.fruit.manage.model.base.BaseProcurementPlanDetail;
import com.jfinal.core.paragetter.IntegerGetter;
import com.jfinal.kit.StrKit;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class ProcurementPlanDetail extends BaseProcurementPlanDetail<ProcurementPlanDetail> {
	public static final ProcurementPlanDetail dao = new ProcurementPlanDetail().dao();
	private static Logger logger = Logger.getLogger(ProcurementPlanDetail.class);

	/**
	 * 获取所有的采购计划数据
	 * @param pageNum
	 * @param pageSize
	 * @param orderBy
	 * @param isASC
	 * @param map
	 * @return
	 */
	public Page<ProcurementPlanDetail> getPPlanDetail(int pageNum, int pageSize, String orderBy, boolean isASC,Map map){
		ArrayList<Object> params = new ArrayList<Object>();
		String selectStr="select ppd.id,ppd.product_id,ppd.product_standard_id,ppd.procurement_id, " +
				"ppd.product_name,ppd.product_standard_name,ppd.sell_price,ppd.inventory_num, " +
				"(select ps.stock from b_product_standard ps where ps.id=ppd.product_standard_id) as stock, "+
				"ppd.procurement_num,ppd.product_standard_num,ppd.procurement_need_price, " +
				"ppd.procurement_total_price,ppd.order_remark,ppd.procurement_remark,ppd.create_time,ppd.update_time,u.`nick_name` as userName,ppd.procurement_plan_id ";
		StringBuilder sql=new StringBuilder();
		sql.append(" from b_procurement_plan_detail ppd, a_user u ");
		sql.append("where 1=1 and  ppd.procurement_id=u.id ");
		// ccz 2018-5-25 由时间筛选的条件换成采购计划编号
//		sql.append(" and ppd.create_time BETWEEN ? and ? ");
		sql.append(" AND ppd.procurement_plan_id = ? ");
		String procurementPlanId = (String) map.get("procurementPlanId");
		params.add(procurementPlanId);
		String [] createTimes = (String [])map.get("createTimes");
//		params.add(createTimes[0]);
//		params.add(createTimes[1]);
		if(StrKit.notBlank((String)map.get("userName"))){
			sql.append("and u.`name` like ? ");
			params.add("%"+map.get("userName")+"%");
		}
		if(StrKit.notBlank((String)map.get("userPhone"))){
			sql.append("and u.phone like ? ");
			params.add("%"+map.get("userPhone")+"%");
		}
		if(StrKit.notBlank((String)map.get("userID"))){
			sql.append("and u.`id` like ? ");
			params.add("%"+map.get("userID")+"%");
		}
		if(StrKit.notBlank((String)map.get("productName"))){
			sql.append("and ppd.product_name like ? ");
			params.add("%"+map.get("productName")+"%");
		}
		if(StrKit.notBlank((String)map.get("productID"))){
			sql.append("and ppd.product_id like ? ");
			params.add("%"+map.get("productID")+"%");
		}
		if(StrKit.notBlank((String)map.get("productStandardName"))){
			sql.append("and ppd.product_standard_name like ? ");
			params.add("%"+map.get("productStandardName")+"%");
		}
		if(StrKit.notBlank((String)map.get("productStandardID"))){
			sql.append("and ppd.product_standard_id like ? ");
			params.add("%"+map.get("productStandardID")+"%");
		}
		orderBy = StrKit.isBlank(orderBy) ? "ppd.create_time" : orderBy;
		sql.append("order by " + orderBy + " " + (isASC ? "" : "desc "));
		System.out.println(("--------------采购导出数据 START--------------"));
		System.out.println(selectStr+"\n"+sql.toString());
		System.out.println("pageNum:" + pageNum);
		System.out.println("pageSize:" + pageSize);
		System.out.println("params:" + params.toArray());
		System.out.println("--------------采购导出数据 END--------------");
		return paginate(pageNum,pageSize,selectStr,sql.toString(),params.toArray());
	}

	/**
	 * 删除采购计划
//	 * @param creatTimes
	 */
//	public boolean delPPlanDetail(String [] creatTimes) {
	public boolean delPPlanDetail(String procurementPlanId) {
			// ccz 2018-5-28 修改了采购计划详细的获取的条件转成橙采购计划编号
//		String sql="delete FROM b_procurement_plan_detail where create_time BETWEEN ? and ? ";
		String sql="delete FROM b_procurement_plan_detail where procurement_plan_id = ? ";
//		int count =Db.update(sql,creatTimes[0],creatTimes[1]);
		int count =Db.update(sql,procurementPlanId);
		if(count>0){
			return true;
		}
		return  false;
	}

	/**
	 * 根据采购计划详情ID获取采购计划详情
	 * @param id 采购计划详情ID
	 * @return 采购计划详情
	 */
	public ProcurementPlanDetail getPPlanDetailByID(Integer id){
		String sql ="SELECT ppd.id,ppd.product_id,ppd.product_standard_id,ppd.procurement_id," +
				"ppd.product_name,ppd.product_standard_name,ppd.sell_price,ppd.inventory_num," +
				"ppd.procurement_num,ppd.product_standard_num,ppd.procurement_need_price," +
				"ppd.procurement_total_price,ppd.order_remark,ppd.procurement_remark,ppd.create_time," +
				"ppd.update_time FROM b_procurement_plan_detail ppd where ppd.id = ? ";
		return findFirst(sql,id);
	}

	/**
	 * 根据商品规格编号获取采购计划详情
	 * @param productStandardID
	 * @return
	 */
//	public ProcurementPlanDetail getPPlanDetailByPSID(Integer productStandardID,String [] createTimes,Integer procurementID){
	public ProcurementPlanDetail getPPlanDetailByPSID(Integer productStandardID,String procurementPlanId,Integer procurementID){
		StringBuilder sql =new StringBuilder();
		sql.append("select ppd.id,ppd.product_id,ppd.product_standard_id,ppd.procurement_id, ");
		sql.append("ppd.product_name,ppd.product_standard_name,ppd.sell_price,ppd.inventory_num,ppd.procurement_num,ppd.product_standard_num, ");
		sql.append("ppd.procurement_need_price,ppd.procurement_total_price,ppd.procurement_need_price,ppd.order_remark,ppd.create_time,ppd.update_time ");
		sql.append("from b_procurement_plan_detail ppd ");
		sql.append("where ppd.product_standard_id = ? ");
		// ccz 2018-5-28 修改了采购计划详细的获取的条件转成橙采购计划编号
		sql.append("and ppd.procurement_plan_id = ? ");
//		sql.append("and ppd.create_time BETWEEN ? and ? ");
		if(procurementID!=null && procurementID>0){
			sql.append("AND ppd.procurement_id =? ");
//			return findFirst(sql.toString(),productStandardID,createTimes[0],createTimes[1],procurementID);
			return findFirst(sql.toString(),productStandardID,procurementPlanId,procurementID);

		}else {
//			return findFirst(sql.toString(),productStandardID,createTimes[0],createTimes[1]);
			return findFirst(sql.toString(),productStandardID,procurementPlanId);
		}
	}

    /**
     * 获取规格编号和同一规格的个数
//     * @param createTimes
     * @return
     */
//	public List<ProcurementPlanDetail> getPSIDAndPSCount(String [] createTimes){
	public List<ProcurementPlanDetail> getPSIDAndPSCount(String procurementPlanId){
        StringBuilder sql=new StringBuilder();
        sql.append("SELECT ppd.product_standard_id,");
        sql.append("count(ppd.product_standard_id) as pscount ");
        sql.append("FROM b_procurement_plan_detail ppd ");
        sql.append("where 1=1 ");
		// ccz 2018-5-28 修改了采购计划详细的获取的条件转成橙采购计划编号
		sql.append("AND ppd.procurement_plan_id = ? ");
//		sql.append("AND ppd.create_time BETWEEN ? and ? ");
        sql.append("GROUP BY ppd.product_standard_id ");
        sql.append("ORDER BY ppd.create_time DESC");
//	    return find(sql.toString(),createTimes[0],createTimes[1]);
	    return find(sql.toString(),procurementPlanId);
    }

	/**
	 * 根据时间和产品规格编号获取采购计划编号和采购人员
//	 * @param createTimes
	 * @param psID
	 * @return
	 */
//	public List<ProcurementPlanDetail> getPPDIDAndProcurementID(String [] createTimes,Integer psID){
	public List<ProcurementPlanDetail> getPPDIDAndProcurementID(String procurementPlanId,Integer psID){
		StringBuilder sql=new StringBuilder();
		sql.append("select ppd.id,ppd.procurement_id from b_procurement_plan_detail ppd ");
		sql.append("where ppd.product_standard_id = ? ");
		// ccz 2018-5-28 修改了采购计划详细的获取的条件转成橙采购计划编号
//		sql.append("and ppd.create_time BETWEEN ? and ? ");
		sql.append("and ppd.procurement_plan_id = ? ");
//		return find(sql.toString(),psID,createTimes[0],createTimes[1]);
		return find(sql.toString(),psID,procurementPlanId);
	}

    /**
     * 根据商品规格编号获取采购计划详情
     * @param productStandardID
     * @return
     */
//    public ProcurementPlanDetail getPPlanDetail(Integer productStandardID,String [] createTimes,Integer procurementID){
    public ProcurementPlanDetail getPPlanDetail(Integer productStandardID,String procurementPlanId,Integer procurementID){
        StringBuilder sql =new StringBuilder();
        List list=new ArrayList();
        sql.append("select ppd.id,ppd.product_id,ppd.product_standard_id,ppd.procurement_id, ");
        sql.append("ppd.product_name,ppd.product_standard_name,ppd.sell_price,ppd.inventory_num,ppd.procurement_num,ppd.product_standard_num, ");
        sql.append("ppd.procurement_need_price,ppd.procurement_total_price,ppd.procurement_need_price,ppd.order_remark,ppd.create_time,ppd.update_time,ppd.procurement_plan_id ");
        sql.append("from b_procurement_plan_detail ppd ");
        sql.append("where ppd.product_standard_id = ? ");
		sql.append(" and ppd.procurement_plan_id = ? ");
//        sql.append("and ppd.create_time BETWEEN ? and ? ");
        list.add(productStandardID);
//        list.add(createTimes[0]);
//        list.add(createTimes[1]);
		list.add(procurementPlanId);
        if(procurementID!=null && procurementID>0){
            sql.append("AND ppd.procurement_id =? ");
            list.add(procurementID);
        }
        return findFirst(sql.toString(),list.toArray());
    }


    /**
     * 根据日期获取所有的采购订单详情
     * @param
     * @return
     */
//    public List<ProcurementPlanDetail> getAllPPlanDetail(String [] createTimes){
    public List<ProcurementPlanDetail> getAllPPlanDetail(String procurementPlanId){
        StringBuilder sql =new StringBuilder();
        List list=new ArrayList();
        sql.append("select ppd.id,ppd.product_id,ppd.product_standard_id,ppd.procurement_id, ");
        sql.append("ppd.product_name,ppd.product_standard_name,ppd.sell_price,ppd.inventory_num,ppd.procurement_num,ppd.product_standard_num, ");
        sql.append("ppd.procurement_need_price,ppd.procurement_total_price,ppd.procurement_need_price,ppd.order_remark,ppd.create_time,ppd.update_time,ppd.procurement_plan_id ");
        sql.append("from b_procurement_plan_detail ppd ");
        sql.append("where 1=1 ");
        // ccz 2018-5-25 修改获取采购计划详细的时间条件为采购计划编号
		sql.append(" and ppd.procurement_plan_id = ? ");
		list.add(procurementPlanId);
//        sql.append("and ppd.create_time BETWEEN ? and ? ");
//        list.add(createTimes[0]);
//        list.add(createTimes[1]);
        return find(sql.toString(),list.toArray());
    }

    /**
     * 根据时间删除所有的采购计划
     * @param createTimes
     */
    public void delAllPPlanDetailByTime(String [] createTimes){
        String sql="DELETE from b_procurement_plan_detail where create_time BETWEEN ? and ?";
        Db.update(sql,createTimes[0],createTimes[1]);
    }

	/**
	 * 根据采购计划编号删除采购计划详细数据
	 * @param procurementPlanId 采购计划编号
	 */
	public void delAllPlanDetailByPPlanId(String procurementPlanId) {
		String sql = "DELETE from b_procurement_plan_detail where procurement_plan_id = ?";
		Db.update(sql, procurementPlanId);
	}


	/**
	 * 获取采购缺货
	 * @param procurementId
	 * @param productName
	 * @param productStandardName
	 * @param createTimes
	 * @return
	 */
	public Page<ProcurementPlanDetail> getProcuementStoreout(int pageNum, int pageSize, String orderBy, boolean isASC,String procurementId,String productName,String productStandardName,String [] createTimes) {
		List list = new ArrayList();
		String sql = "SELECT ppd.product_name,ppd.product_standard_name,ppd.product_standard_id,ppd.product_standard_num,ppd.procurement_num,\n" +
				"\t\t\tppd.procurement_need_price,ppd.sell_price,pd.create_time,ppd.inventory_num,\n" +
				" (select count(1) from b_order_detail od where (od.actual_send_goods_num <= 0 or od.actual_send_goods_num is NULL) and ppd.product_standard_id = od.product_standard_id) as storeoutNum, " +
				" (select count(1) from b_order_detail od where ppd.product_standard_id = od.product_standard_id) as orderDetailTotalNum, " +

				"\t(\n" +
				"\t\tIFNULL((\n" +
				"\t\tSELECT cid.inventory_price from b_check_inventory ci\n" +
				"\t\t\tINNER JOIN b_check_inventory_detail cid on ci.id = cid.check_inventory_id\n" +
				"\t\t\twhere 1=1 and cid.product_standard_id = 13\n" +
				"\t\t\tand DATE_FORMAT(ci.create_time,'%Y-%m-%d') = DATE_FORMAT('2018-5-12','%Y-%m-%d')\n" +
				"\t\t),(\n" +
				"\t\tSELECT pwd.put_average_price from b_put_warehouse pw\n" +
				"\t\t\tINNER JOIN b_put_warehouse_detail pwd on pw.id = pwd.put_id\n" +
				"\t\t\twhere 1=1 and pwd.product_standard_id = 124\n" +
				"\t\t\tand DATE_FORMAT(pw.create_time,'%Y-%m-%d') = DATE_FORMAT('2018-05-24','%Y-%m-%d') \n" +
				"\t\t))\n" +
				"\t) AS costPrice,"+

				" (SELECT u.nick_name from a_user u where u.id = ppd.procurement_id) as procurement_name, "+
				" ppd.procurement_total_price ";

		StringBuilder sb = new StringBuilder();
		sb.append("\tfrom b_procurement_plan_detail ppd\n");
		sb.append("\tINNER join b_procurement_plan pd on ppd.procurement_id \n");
		sb.append("\twhere 1=1 \n");
		if (StrKit.notBlank(procurementId)) {
			sb.append("\tand ppd.procurement_id = ?\n");
			list.add(procurementId);
		}
		if (StrKit.notBlank(productName)) {
			sb.append("\tand ppd.product_name like ?\n");
			list.add("%" + productName + "%");
		}
		if (StrKit.notBlank(productStandardName)) {
			sb.append("\tand ppd.product_standard_name like ?\n");
			list.add("%" + productStandardName + "%");
		}
		if(createTimes!=null && !"".equals(createTimes) && StrKit.notBlank(createTimes[0]) && StrKit.notBlank(createTimes[1]) && createTimes.length == 2){
			sb.append("\tand pd.create_time between ? and ?\n");
			list.add(createTimes[0]);
			list.add(createTimes[1]);
		}
		System.out.println("----------采购缺货 start--------------");
		System.out.println(sql);
		System.out.println(sb.toString());
		if (list != null) {
			for (int i = 0; i < list.size(); i++) {
				System.out.println(list.get(i));
			}
		}
		System.out.println(list.toArray());
		System.out.println("---------------采购缺货 end-------------");
		return paginate(pageNum,pageSize,sql,sb.toString(), list.toArray());
	}
}
