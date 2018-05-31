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
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.omg.CORBA.INTERNAL;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
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
        Db.tx(new IAtom() {
            @Override
            public boolean run() throws SQLException {
                Integer tableIndex = 0;
                Integer rowIndex = 1;
                try {
                    String fileName = getPara("fileName");
                    Integer putId = getParaToInt("putId");
                    String filePath = CommonController.FILE_PATH + File.separator + fileName;
                    // 入库总数量
                    BigDecimal putAllCount = new BigDecimal(0);
                    // 入库类型总数量
                    Map<Integer, String> putAllTypeCount = new HashMap<>();
                    // 入库总费用
                    BigDecimal putAllTotalCost = new BigDecimal(0);
                    Date currentTime = new Date();
                    String startTime = null;
                    String endTime = null;
//        Iterator<ExcelRdRow> iterator = WarehousePutDetailController.readExcel(filePath).iterator();
                    List<List<ExcelRdRow>> listExcelRdRows = readExcelMultiTable(filePath);
                    for (int i = 0; i < listExcelRdRows.size(); i++) {
                        Iterator<ExcelRdRow> iterator = listExcelRdRows.get(i).iterator();
                        Integer count = 0;
                        tableIndex++;
                        rowIndex=1;
                        while (iterator.hasNext()) {
                            rowIndex++;
                            count++;
                            ExcelRdRow excelRdRow = iterator.next();
                            List<Object> list = excelRdRow.getRow();
                            if (count == 1 && i == 0) {
                                String createTimeStr = ((String) list.get(0)).split(" ")[0].substring(5);
                                startTime = createTimeStr + " 00:00:00";
                                endTime = createTimeStr + " 23:59:59";
                            }

                            if (count > 2) {
                                // ccz 2018-04-25 startTime endTime 暂时无用
//                    String procurementName = User.dao.getNickNameById(Integer.parseInt(list.get(8)+""));
                                String procurementName = list.get(8) + "";
                                Integer procurementId = User.dao.getUserIdByName(list.get(8) + "");
//                    PutWarehouseDetail putWarehouseDetail = PutWarehouseDetail.dao.getPutDetailByPSIDAndProcurementId(Integer.parseInt((list.get(2)) + ""), Integer.parseInt((list.get(8)) + ""), startTime, endTime, putId);
                                PutWarehouseDetail putWarehouseDetail = PutWarehouseDetail.dao.getPutDetailByPSIDAndProcurementId(Integer.parseInt((list.get(2)) + ""), procurementId, startTime, endTime, putId);

                                if (putWarehouseDetail == null) {
                                    putWarehouseDetail = new PutWarehouseDetail();
                                    // 计算总价和入库单价
                                    BigDecimal procurementPrice = new BigDecimal(list.get(4) + "");
                                    BigDecimal putNum = new BigDecimal(Integer.parseInt(list.get(7) + ""));
                                    BigDecimal procurementTotalPrice = procurementPrice.multiply(putNum);
                                    BigDecimal boothCost = new BigDecimal(list.get(6) + "");
                                    putAllCount = putAllCount.add(putNum);
                                    putAllTypeCount.put(Integer.parseInt(list.get(2) + ""), "countNeed");
                                    // 弃用
                                    putAllTotalCost = putAllTotalCost.add(procurementTotalPrice.add(boothCost));
                                    if (BigDecimal.ZERO.compareTo(putNum) == 0) {
                                        continue;
                                    }
                                    //入库单价 = （入库总价+摊位费）/ 入库数量
                                    BigDecimal averagePrice = (procurementTotalPrice.add(boothCost)).divide(putNum, 2, BigDecimal.ROUND_HALF_DOWN);

                                    // 根据商品规格编号获取商品编号
                                    Integer productId = ProductStandard.dao.getProductIdByPSId(Integer.parseInt(list.get(2) + ""));
                                    putWarehouseDetail.setProductId(productId);
                                    //给入库详细信息赋值
                                    putWarehouseDetail.setProductName(list.get(0) + "");
                                    putWarehouseDetail.setProductStandardName(list.get(1) + "");
                                    putWarehouseDetail.setProductStandardId(Integer.parseInt(list.get(2) + ""));
                                    putWarehouseDetail.setProductWeight(list.get(3)==null?"":(list.get(3)+""));
                                    putWarehouseDetail.setProcurementPrice(procurementPrice);
                                    putWarehouseDetail.setProcurementTotalPrice(procurementTotalPrice);
                                    putWarehouseDetail.setBoothCost(boothCost);
                                    putWarehouseDetail.setPutNum(Integer.parseInt(list.get(7) + ""));
                                    putWarehouseDetail.setProcurementId(procurementId);
                                    putWarehouseDetail.setPutAveragePrice(averagePrice);
                                    putWarehouseDetail.setPutId(putId);
                                    putWarehouseDetail.setCreateTime(currentTime);
                                    putWarehouseDetail.setProcurementName(procurementName);
                                    putWarehouseDetail.save();

                                    // 根据商品规格编号修改商品规格的库存量
                                    updateProductStandardStore(putWarehouseDetail.getProductStandardId(), putWarehouseDetail.getPutNum(), putWarehouseDetail.getProductStandardName(), putWarehouseDetail.getProductId(), putWarehouseDetail.getProductName());

                                    PutWarehouse putWarehouse = PutWarehouse.dao.getPutWarehouseById(putId);
                                    putWarehouse.setPutTime(currentTime);
                                    putWarehouse.setUpdateTime(currentTime);
                                    putWarehouse.setPutNum(putAllCount.intValue());
                                    putWarehouse.setPutTypeNum(putAllTypeCount.size());
                                    putWarehouse.setPutTotalPrice(putWarehouse.getPutTotalPrice().add(procurementTotalPrice).add(boothCost));
                                    putWarehouse.update();
                                } else {
                                    // 执行修改操作
                                    BigDecimal boothCost = new BigDecimal(list.get(6) + "");
                                    BigDecimal putNumUpdate = new BigDecimal(Integer.parseInt(list.get(7) + ""));
                                    if (putNumUpdate!=null && putNumUpdate.intValue() <=0) {
                                        continue;
                                    }
                                    BigDecimal procurementTotalPrice = putNumUpdate.multiply(putWarehouseDetail.getProcurementPrice());
                                    // PutNum相差的值
                                    Integer differPutNum = putWarehouseDetail.getPutNum() - putNumUpdate.intValue();
                                    // 摊位分相差值
                                    BigDecimal differBoothCost = putWarehouseDetail.getBoothCost().subtract(boothCost);
                                    // 入库数量的相差值
                                    BigDecimal differPutTotalPrice = (putWarehouseDetail.getProcurementPrice().multiply(new BigDecimal(putWarehouseDetail.getPutNum()))).subtract(putWarehouseDetail.getProcurementPrice().multiply(putNumUpdate));

                                    putWarehouseDetail.setPutNum(putNumUpdate.intValue());
                                    putWarehouseDetail.setProcurementTotalPrice(procurementTotalPrice);

                                    putWarehouseDetail.setPutAveragePrice((procurementTotalPrice.add(boothCost)).divide(putNumUpdate, 2, BigDecimal.ROUND_HALF_DOWN));
                                    putWarehouseDetail.setUpdateTime(new Date());
                                    putWarehouseDetail.setProcurementId(procurementId);
                                    putWarehouseDetail.setProductWeight(list.get(3)==null?"":(list.get(3)+ ""));
                                    putWarehouseDetail.setBoothCost(boothCost);
                                    putWarehouseDetail.setProcurementName(procurementName);
                                    putWarehouseDetail.update();

                                    // 根据商品规格编号修改商品规格的库存量
                                    updateProductStandardStore(putWarehouseDetail.getProductStandardId(), differPutNum.intValue(), putWarehouseDetail.getProductStandardName(), putWarehouseDetail.getProductId(), putWarehouseDetail.getProductName());

                                    PutWarehouse putWarehouse = PutWarehouse.dao.getPutWarehouseById(putWarehouseDetail.getPutId());
                                    putWarehouse.setPutNum(putWarehouse.getPutNum() - differPutNum);
                                    putWarehouse.setUpdateTime(new Date());
                                    // 计算修改了之后总价的价格修改
                                    putWarehouse.setPutTotalPrice((putWarehouse.getPutTotalPrice().subtract(differBoothCost)).subtract(differPutTotalPrice));
                                    putWarehouse.update();
                                }
                            }
                        }

                    }
                    renderNull();
                    return true;
                } catch (Exception e) {
                    // 弹出错误信息
                    excelRenderErrorInfo(tableIndex,rowIndex,e.getMessage());
                    e.printStackTrace();
                    return false;
                }
            }
        });
    }

    /**
     * 输出Excel的错误信息
     *
     * @param tableIndex
     * @param rowIndex
     * @param errorMsg
     */
    public void excelRenderErrorInfo(Integer tableIndex, Integer rowIndex, String errorMsg) {
        if (tableIndex == 0) {
            renderErrorText(errorMsg);
        } else {
            renderErrorText("第" + tableIndex + "张表，第" + rowIndex + "行数据出现异常\n异常信息是：" + errorMsg);
        }
    }

    /**
     * 读取Excel多表
     *
     * @param filePath
     * @return
     */
    public List<List<ExcelRdRow>> readExcelMultiTable(String filePath) {
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
        ExcelRd excelRd = new ExcelRd(filePath);
        excelRd.setStartRow(1);
        excelRd.setStartCol(0);
        excelRd.setTypes(types);
        List<List<ExcelRdRow>> lists = null;
        try {
            lists = excelRd.analysisXlsxMultiTable();
        } catch (ExcelRdException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lists;
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

            putWarehouseDetail2.setPutAveragePrice((procurementTotalPrice.add(boothCost)).divide(putNumUpdate, 2, BigDecimal.ROUND_HALF_DOWN));
            putWarehouseDetail2.setUpdateTime(new Date());
            putWarehouseDetail2.setProcurementId(putWarehouseDetail.getProcurementId());
            putWarehouseDetail2.setProductWeight(putWarehouseDetail.getProductWeight());
            putWarehouseDetail2.setBoothCost(putWarehouseDetail.getBoothCost());
            putWarehouseDetail2.setPutRemark(putWarehouseDetail.getPutRemark());

//        putWarehouseDetail2.setPutAveragePrice((procurementTotalPrice.add(putWarehouseDetail2.getBoothCost())).divide(new BigDecimal(putWarehouseDetail2.getPutNum())));
            putWarehouseDetail2.update();

            // 根据商品规格编号修改商品规格的库存量
            updateProductStandardStore(putWarehouseDetail2.getProductStandardId(), differPutNum.intValue(), putWarehouseDetail2.getProductStandardName(), putWarehouseDetail2.getProductId(), putWarehouseDetail2.getProductName());

            PutWarehouse putWarehouse = PutWarehouse.dao.getPutWarehouseById(putWarehouseDetail.getPutId());
            // TODO 减号（-）是否要改为（+）
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
            // 根据商品规格编号获取商品编号
            Integer productId = ProductStandard.dao.getProductIdByPSId(putWarehouseDetail.getProductStandardId());
            putWarehouseDetail.setProductId(productId);
            BigDecimal procurementPrice = putWarehouseDetail.getProcurementPrice();
            BigDecimal procurementTotalPrice = procurementPrice.multiply(new BigDecimal(putNum));
            Product product = Product.dao.getById(putWarehouseDetail.getProductId());
            ProductStandard productStandard = ProductStandard.dao.getProductStandardById(putWarehouseDetail.getProductStandardId());
            putWarehouseDetail.setProductStandardName(productStandard.getName());
            User user = User.dao.getUserById(putWarehouseDetail.getProcurementId());
            putWarehouseDetail.setProcurementName(user.getNickName());
            putWarehouseDetail.setProductName(product.getName());
            putWarehouseDetail.setProcurementTotalPrice(procurementTotalPrice);
            // 计算入库单价
            putWarehouseDetail.setPutAveragePrice((procurementTotalPrice.add(putWarehouseDetail.getBoothCost())).divide(new BigDecimal(putNum), 2, BigDecimal.ROUND_HALF_DOWN));
            putWarehouseDetail.setCreateTime(new Date());
            putWarehouseDetail.save();

            // 根据商品规格编号修改商品规格的库存量
            updateProductStandardStore(putWarehouseDetail.getProductStandardId(), putNum, putWarehouseDetail.getProductStandardName(), putWarehouseDetail.getProductId(), putWarehouseDetail.getProductName());

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
     *
     * @param psId
     * @param putNum
     * @return
     */
    public boolean updateProductStandardStore(Integer psId, Integer putNum, String productStandardName, Integer productId, String productName) {
        ProductStandard productStandard = ProductStandard.dao.getProductStandardById(psId);
        productStandard.setStock(productStandard.getStock() + putNum);
        Integer uid = getSessionAttr(Constant.SESSION_UID);
        return productStandard.update(UserTypeConstant.A_USER, uid, productStandard.getStock() + putNum, productStandard.getStock(), "0", productStandardName, productId, productName);
    }
}
