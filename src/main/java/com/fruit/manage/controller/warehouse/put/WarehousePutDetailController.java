package com.fruit.manage.controller.warehouse.put;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.constant.UserTypeConstant;
import com.fruit.manage.controller.common.CommonController;
import com.fruit.manage.controller.procurement.PlanDetailController;
import com.fruit.manage.model.*;
import com.fruit.manage.util.Constant;
import com.fruit.manage.util.DateAndStringFormat;
import com.fruit.manage.util.excelRd.ExcelRd;
import com.fruit.manage.util.excelRd.ExcelRdException;
import com.fruit.manage.util.excelRd.ExcelRdRow;
import com.fruit.manage.util.excelRd.ExcelRdTypeEnum;
import com.jfinal.aop.Before;
import com.jfinal.log.Logger;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.omg.CORBA.INTERNAL;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;


/**
 * @author partner
 * @date 2018/4/1 16:24
 */
public class WarehousePutDetailController extends BaseController {
    private Logger logger = Logger.getLogger(WarehousePutDetailController.class);

    /**
     * 根据条件获取带分页的数据集合
     */
    public void getAllInfo() {
        int pageNum = getParaToInt("pageNum", 1);
        int pageSize = getParaToInt("pageSize", 10);
        Map map = new HashMap();
        String orderBy = getPara("prop");
        // ascending为升序，其他为降序
        boolean isASC = "ascending".equals(getPara("order"));
        String productName = getPara("product_name");
        String productId = getPara("product_id");
        String productStandardName = getPara("product_standard_name");
        String productStandardId = getPara("product_standard_id");
        String putId = getPara("put_id");
        map.put("productName", productName);
        map.put("productId", productId);
        map.put("productStandardName", productStandardName);
        map.put("productStandardId", productStandardId);
        map.put("putId", putId);
        Page<PutWarehouseDetail> lists = PutWarehouseDetail.dao.getAllInfo(pageNum, pageSize, orderBy, isASC, map);
        renderJson(lists);
    }

    public static List<ExcelRdRow> readExcel(String filePath) {
        ExcelRd excelRd = new ExcelRd(filePath);
        excelRd.setStartRow(1);
        excelRd.setStartCol(0);
        ExcelRdTypeEnum[] types = {
                ExcelRdTypeEnum.STRING,
                ExcelRdTypeEnum.STRING,
                ExcelRdTypeEnum.INTEGER,
                ExcelRdTypeEnum.DOUBLE,
                ExcelRdTypeEnum.DOUBLE,
                ExcelRdTypeEnum.DOUBLE,
                ExcelRdTypeEnum.DOUBLE,
                ExcelRdTypeEnum.INTEGER,
                ExcelRdTypeEnum.INTEGER
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
     * 导入信息
     */
    @Before(Tx.class)
    public void exportInfo() {
        String fileName = getPara("fileName");
        Integer putId = getParaToInt("putId");
        String filePath = CommonController.FILE_PATH + File.separator + fileName;
        Iterator<ExcelRdRow> iterator = WarehousePutDetailController.readExcel(filePath).iterator();
        Integer count = 0;
        // 入库总数量
        BigDecimal putAllCount = new BigDecimal(0);
        // 入库类型总数量
        Map<Integer, String> putAllTypeCount = new HashMap<>();
        // 入库总费用
        BigDecimal putAllTotalCost = new BigDecimal(0);
        Date currentTime = new Date();
        String startTime = null;
        String endTime = null;
        while (iterator.hasNext()) {
            count++;
            ExcelRdRow excelRdRow = iterator.next();
            List<Object> list = excelRdRow.getRow();
            if(count==1){
                String createTimeStr= ((String)list.get(0)).split(" ")[0].substring(5);
                startTime = createTimeStr+" 00:00:00";
                endTime = createTimeStr+" 23:59:59";
            }
            if (count > 2) {
                PutWarehouseDetail putWarehouseDetail = PutWarehouseDetail.dao.getPutDetailByPSIDAndProcurementId((Integer) (list.get(2)), (Integer) (list.get(8)),startTime,endTime,putId);
                if (putWarehouseDetail == null) {
                    putWarehouseDetail=new PutWarehouseDetail();
                    // 计算总价和入库单价
                    BigDecimal procurementPrice = new BigDecimal(list.get(4) + "");
                    BigDecimal putNum = new BigDecimal((Integer) list.get(7));
                    BigDecimal procurementTotalPrice = procurementPrice.multiply(putNum);
                    BigDecimal boothCost = new BigDecimal(list.get(6) + "");
                    putAllCount = putAllCount.add(putNum);
                    putAllTypeCount.put((Integer) list.get(2), "countNeed");
                    // 弃用
                    putAllTotalCost = putAllTotalCost.add(procurementTotalPrice.add(boothCost));
                    //入库单价 = （入库总价+摊位费）/ 入库数量
                    BigDecimal averagePrice = (procurementTotalPrice.add(boothCost)).divide(putNum);

                    //给入库详细信息赋值
                    putWarehouseDetail.setProductName(list.get(0) + "");
                    putWarehouseDetail.setProductStandardName(list.get(1) + "");
                    putWarehouseDetail.setProductStandardId((Integer) list.get(2));
                    putWarehouseDetail.setProductWeight((Double) list.get(3));
                    putWarehouseDetail.setProcurementPrice(procurementPrice);
                    putWarehouseDetail.setProcurementTotalPrice(procurementTotalPrice);
                    putWarehouseDetail.setBoothCost(boothCost);
                    putWarehouseDetail.setPutNum((Integer) list.get(7));
                    putWarehouseDetail.setProcurementId((Integer) list.get(8));
                    putWarehouseDetail.setPutAveragePrice(averagePrice);
                    putWarehouseDetail.setPutId(putId);
                    putWarehouseDetail.setCreateTime(currentTime);
                    putWarehouseDetail.save();

                    // 根据商品规格编号修改商品规格的库存量
                    updateProductStandardStore(putWarehouseDetail.getProductStandardId(),putWarehouseDetail.getPutNum());


                    PutWarehouse putWarehouse = PutWarehouse.dao.getPutWarehouseById(putId);
                    putWarehouse.setPutTime(currentTime);
                    putWarehouse.setUpdateTime(currentTime);
                    putWarehouse.setPutNum(putAllCount.intValue());
                    putWarehouse.setPutTypeNum(putAllTypeCount.size());
                    putWarehouse.setPutTotalPrice(putWarehouse.getPutTotalPrice().add(procurementTotalPrice).add(boothCost));
                    putWarehouse.update();
                }else {
                    // 执行修改操作
                    BigDecimal boothCost = new BigDecimal(list.get(6)+"");
                    BigDecimal putNumUpdate = new BigDecimal((Integer)list.get(7));
                    BigDecimal procurementTotalPrice = putNumUpdate.multiply(putWarehouseDetail.getProcurementPrice());
                    // PutNum相差的值
                    Integer differPutNum = putWarehouseDetail.getPutNum() - putNumUpdate.intValue();
                    // 摊位分相差值
                    BigDecimal differBoothCost = putWarehouseDetail.getBoothCost().subtract(boothCost);
                    // 入库数量的相差值
                    BigDecimal differPutTotalPrice = (putWarehouseDetail.getProcurementPrice().multiply(new BigDecimal(putWarehouseDetail.getPutNum()))).subtract(putWarehouseDetail.getProcurementPrice().multiply(putNumUpdate));

                    putWarehouseDetail.setPutNum(putNumUpdate.intValue());
                    putWarehouseDetail.setProcurementTotalPrice(procurementTotalPrice);

                    putWarehouseDetail.setPutAveragePrice((procurementTotalPrice.add(boothCost)).divide(putNumUpdate));
                    putWarehouseDetail.setUpdateTime(new Date());
                    putWarehouseDetail.setProcurementId((Integer) list.get(8));
                    putWarehouseDetail.setProductWeight((Double) list.get(3));
                    putWarehouseDetail.setBoothCost(boothCost);
                    putWarehouseDetail.update();

                    // 根据商品规格编号修改商品规格的库存量
                    updateProductStandardStore(putWarehouseDetail.getProductStandardId(), differPutNum.intValue());

                    PutWarehouse putWarehouse = PutWarehouse.dao.getPutWarehouseById(putWarehouseDetail.getPutId());
                    putWarehouse.setPutNum(putWarehouse.getPutNum() - differPutNum);
                    putWarehouse.setUpdateTime(new Date());
                    // 计算修改了之后总价的价格修改
                    putWarehouse.setPutTotalPrice((putWarehouse.getPutTotalPrice().subtract(differBoothCost)).subtract(differPutTotalPrice));
                    putWarehouse.update();
                }
            }
        }
        renderNull();
    }

    /**
     * 根据入库详细编号获取入库详细信息
     */
    public void getPutWarehouseById() {
        Integer id = getParaToInt("id");
        renderJson(PutWarehouseDetail.dao.getPutDetailById(id));
    }

    /**
     * 修改入库信息
     */
    @Before(Tx.class)
    public void updatePutDetail() {
        PutWarehouseDetail putWarehouseDetail = getModel(PutWarehouseDetail.class, "", true);
        // 执行修改操作
        if (putWarehouseDetail.getId() != null && putWarehouseDetail.getId() != 0) {

            BigDecimal boothCost = putWarehouseDetail.getBoothCost();
            BigDecimal putNumUpdate = new BigDecimal(putWarehouseDetail.getPutNum());
            BigDecimal procurementTotalPrice = putNumUpdate.multiply(putWarehouseDetail.getProcurementPrice());
            PutWarehouseDetail putWarehouseDetail2 = PutWarehouseDetail.dao.getPutDetailById(putWarehouseDetail.getId());
            // PutNum相差的值
            Integer differPutNum = putNumUpdate.intValue() - putWarehouseDetail2.getPutNum();
            // 摊位分相差值
            BigDecimal differBoothCost = putWarehouseDetail2.getBoothCost().subtract(putWarehouseDetail.getBoothCost());
            // 入库数量的相差值
            BigDecimal differPutTotalPrice = (putWarehouseDetail2.getProcurementPrice().multiply(new BigDecimal(putWarehouseDetail2.getPutNum()))).subtract(putWarehouseDetail2.getProcurementPrice().multiply(new BigDecimal(putWarehouseDetail.getPutNum())));

            putWarehouseDetail2.setPutNum(putWarehouseDetail.getPutNum());
            putWarehouseDetail2.setProcurementTotalPrice(procurementTotalPrice);

            putWarehouseDetail2.setPutAveragePrice((procurementTotalPrice.add(boothCost)).divide(putNumUpdate));
            putWarehouseDetail2.setUpdateTime(new Date());
            putWarehouseDetail2.setProcurementId(putWarehouseDetail.getProcurementId());
            putWarehouseDetail2.setProductWeight(putWarehouseDetail.getProductWeight());
            putWarehouseDetail2.setBoothCost(putWarehouseDetail.getBoothCost());
            putWarehouseDetail2.setPutRemark(putWarehouseDetail.getPutRemark());

//        putWarehouseDetail2.setPutAveragePrice((procurementTotalPrice.add(putWarehouseDetail2.getBoothCost())).divide(new BigDecimal(putWarehouseDetail2.getPutNum())));
            putWarehouseDetail2.update();

            // 根据商品规格编号修改商品规格的库存量
            updateProductStandardStore(putWarehouseDetail2.getProductStandardId(), differPutNum.intValue());

            PutWarehouse putWarehouse = PutWarehouse.dao.getPutWarehouseById(putWarehouseDetail.getPutId());
            putWarehouse.setPutNum(putWarehouse.getPutNum() - differPutNum);
            putWarehouse.setUpdateTime(new Date());
            // 计算修改了之后总价的价格修改
            putWarehouse.setPutTotalPrice((putWarehouse.getPutTotalPrice().subtract(differBoothCost)).subtract(differPutTotalPrice));
            putWarehouse.update();
            renderNull();
        } else {
            Date currentTime = new Date();
            // 执行添加操作
            Integer putNum = putWarehouseDetail.getPutNum();
            BigDecimal procurementPrice = putWarehouseDetail.getProcurementPrice();
            BigDecimal procurementTotalPrice = procurementPrice.multiply(new BigDecimal(putNum));
            Product product=Product.dao.getById(putWarehouseDetail.getProductId());
            ProductStandard productStandard = ProductStandard.dao.getProductStandardById(putWarehouseDetail.getProductStandardId());
            putWarehouseDetail.setProductStandardName(productStandard.getName());
            User user = User.dao.getUserById(putWarehouseDetail.getProcurementId());
            putWarehouseDetail.setProcurementName(user.getName());
            putWarehouseDetail.setProductName(product.getName());
            putWarehouseDetail.setProcurementTotalPrice(procurementTotalPrice);
            // 计算入库单价
            putWarehouseDetail.setPutAveragePrice((procurementTotalPrice.add(putWarehouseDetail.getBoothCost())).divide(new BigDecimal(putNum)));
            putWarehouseDetail.setCreateTime(new Date());
            putWarehouseDetail.save();

            // 根据商品规格编号修改商品规格的库存量
            updateProductStandardStore(putWarehouseDetail.getProductStandardId(), putNum);

            PutWarehouse putWarehouse = PutWarehouse.dao.getPutWarehouseById(putWarehouseDetail.getPutId());
            putWarehouse.setPutTime(currentTime);
            putWarehouse.setUpdateTime(currentTime);
            putWarehouse.setPutNum(new BigDecimal(putWarehouse.getPutNum()).add(new BigDecimal(putNum)).intValue());
            // 根据入库编号获取入库总共有多少中类型
            Integer putAllTypeCount = PutWarehouseDetail.dao.getAllTypeCountByPutId(putWarehouseDetail.getPutId());
            putWarehouse.setPutTypeNum(putAllTypeCount);
            putWarehouse.setPutTotalPrice(putWarehouse.getPutTotalPrice().add(procurementTotalPrice).add(putWarehouseDetail.getBoothCost()));
            putWarehouse.update();
            renderNull();
        }
    }

    /**
     * 根据商品规格编号（product_standard_id）修改商品规格的库存
     * @param psId
     * @param putNum
     * @return
     */
    public boolean updateProductStandardStore(Integer psId,Integer putNum){
        ProductStandard productStandard = ProductStandard.dao.getProductStandardById(psId);
        productStandard.setStock(productStandard.getStock() + putNum);
        Integer uid = getSessionAttr(Constant.SESSION_UID);
        return productStandard.update(UserTypeConstant.A_USER,uid,productStandard.getStock()+putNum,productStandard.getStock(),"0");
    }
}