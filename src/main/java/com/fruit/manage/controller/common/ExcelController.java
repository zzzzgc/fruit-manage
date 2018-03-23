package com.fruit.manage.controller.common;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.model.ProcurementQuota;
import com.fruit.manage.model.ProductStandard;
import com.fruit.manage.model.User;
import com.fruit.manage.util.Constant;
import com.fruit.manage.util.ExcelCommon;
import com.fruit.manage.util.excel.ExcelException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * excel导入导出集合
 * <p>
 * 1.可以使用通用的excel文件导入导出(提高复用率)
 * 2.各个模块的excel文件导入导出(方便整理各个模块的excel)
 * 3.使用相同文件路径的excel导入导出(统一规范保存路径)
 * <p>
 * 文件上传/文件下载 路径,
 * 在JFConfig的
 * me.setBaseUploadPath(baseUploadPath) / me.setBaseDownloadPath(baseDownloadPath)
 * 设置setBaseDownloadPath即可将基础路径指向项目根径之外，方便单机多实例部署。当该路径参数设置为相对路径时，则是以项目根为基础的相对路径。
 * 即当renderFlie('/xxx/xx')的时候   '/'  =  baseDownloadPath
 * '/xxx/xx' = baseDownloadPath/xxx/xx
 *
 * @author ZGC
 * @date 2018-03-23 11:54
 **/
public class ExcelController extends BaseController {
    /**
     * baseDownloadPath = static
     * so 目前的basePath = 项目根目录/WEB-INF/excel
     * excel目录存放目录
     */
    private static String basePath;
    /**
     * 存放excel的目录
     */
    private static String excelFolder = "excel";

    static {
        //必须存在WEB-INF
        String webInfFolder = ExcelController.class.getResource("/../").getPath();
        // 获取excel目录存放目录
        basePath = webInfFolder + File.separator + excelFolder;
        File file = new File(basePath);
        if (!file.exists()) {
            file.mkdir();
        }
        System.out.println("basePath:" + basePath);
    }

    /**
     * 这是Demo ,使用的时候请修改修饰符为public
     */
    protected void test() {
        String fileName = "商品库信息.txt";
        System.out.println("basePath:" + basePath);
        File file = new File(basePath + File.separator + fileName);
        try {
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    throw new RuntimeException(fileName + "文件创建失败");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        renderFile(file, "文件名.png");
    }

    /**
     * 获取系统中的所有商品规格信息
     */
    public void getProductStandardAllInfoExcel() {
        Integer uid = getSessionAttr(Constant.SESSION_UID);
        String fileName = "商品库信息大全.xlsx";
        String[] headers = new String[6];
        headers[0] = "商品名";
        headers[1] = "商品编码";
        headers[2] = "规格名";
        headers[3] = "规格编码";
        headers[4] = "采购姓名";
        headers[5] = "采购人编码";

        List<ProductStandard> list = ProductStandard.dao.getProductStandardAllInfo();
        List<String[]> tables = new ArrayList<>();
        for (ProductStandard productStandard : list) {
            String[] info = new String[6];
            info[0] = productStandard.get("product_name");
            info[1] = productStandard.get("product_id") + "";
            info[2] = productStandard.getName();
            info[3] = productStandard.getId() + "";
            info[4] = "";
            info[5] = "";
            tables.add(info);
        }
        File file = null;
        try {
            String filePath = ExcelCommon.createExcelModul(basePath, fileName, "商品库信息大全", User.dao.findById(uid).getName(), headers, tables);
            // xls表头顺序：商品名，规格名，规格编码，采购姓名，采购人id

            // TODO
            file = new File(filePath);

//            ExcelCommon.excelRd(filePath,2,1);
            if (!file.exists()) {
                throw new RuntimeException("basePath:" + basePath + ",fileName:" + fileName + "  文件不存在");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        renderFile(file);
    }


}
