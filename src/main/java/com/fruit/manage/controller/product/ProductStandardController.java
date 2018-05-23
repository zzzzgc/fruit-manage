package com.fruit.manage.controller.product;

import java.util.ArrayList;
import java.util.Date;

import com.fruit.manage.model.ProcurementQuota;
import com.fruit.manage.model.Product;
import com.fruit.manage.model.User;
import com.fruit.manage.util.Constant;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.model.ProductStandard;
import com.jfinal.kit.JsonKit;

public class ProductStandardController extends BaseController {
    private static Logger log = Logger.getLogger(ProductController.class);

    public void getData() {
        String prop = getPara("prop");
        Integer productId = getParaToInt("productId");
        String order = getPara("order");
        if (productId == null) {
            renderJson(new ArrayList<>());
            return;
        }
        // TODO 对数据过滤
        renderJson(ProductStandard.dao.list(productId, prop, "descending".equals(order)));
    }

    public void changeStatus(int productId, int status) {
        Integer[] ids = getParaValuesToInt("ids");
        log.info("修改商品规格(" + StringUtils.join(ids, ",") + ")状态为:" + status);// TODO 获取当前登录用户
        if (ids == null || ids.length == 0) {
            renderErrorText("商品规格ID不能为空");
            return;
        }

        renderResult(ProductStandard.dao.changeStatusOne(productId, ids, status));
    }

    public void info() {
        Integer id = getParaToInt("id");
        renderJson(ProductStandard.dao.findFirst("SELECT " +
                "ps.id, " +
                "ps.product_id, " +
                "ps.`name`, " +
                "ps.sub_title, " +
                "ps.original_price, " +
                "ps.sell_price, " +
                "ps.weight_price, " +
                "ps.cost_price, " +
                "ps.shipping_fee, " +
                "ps.carton_weight, " +
                "ps.fruit_weight, " +
                "ps.gross_weight, " +
                "ps.purchase_quantity_min, " +
                "ps.purchase_quantity_max, " +
                "ps.buy_start_time, " +
                "ps.buy_end_time, " +
                "ps.sort_purchase, " +
                "ps.sort_sold_out, " +
                "ps.sort_split, " +
                "ps.stock, " +
                "ps.`status`, " +
                "ps.is_default, " +
                "ps.create_time, " +
                "ps.update_time, " +
                "pq.procurement_id, " +
                "pq.procurement_name " +
                "FROM b_product_standard ps LEFT JOIN b_procurement_quota pq ON pq.product_standard_id = ps.id " +
                "WHERE ps.id = ? ", id));
    }

    public void save() {
        Integer uid = getSessionAttr(Constant.SESSION_UID);
        ProductStandard productStandard = getModel(ProductStandard.class, "", true);
        Integer procurementId = getParaToInt("procurement_id");
        log.info("保存规格数据：" + JsonKit.toJson(productStandard));
        productStandard.setPurchaserUid(procurementId);
        if (!ProductStandard.dao.save(productStandard)) {
            renderErrorText("保存商品规格异常");
            return;
        }

        ProcurementQuota procurementQuota = ProcurementQuota.dao.getProcurementQuotaByProductStandardId(productStandard.getId());
        User user = User.dao.getUserById(procurementId);
        if (procurementQuota == null) {
            Product product = Product.dao.getById(productStandard.getProductId());

            procurementQuota = new ProcurementQuota();
            procurementQuota.setCreateUserId(uid);
            procurementQuota.setCreateUserName(user.getNickName());
            procurementQuota.setProductId(productStandard.getProductId());
            procurementQuota.setProductName(product.getName());
            procurementQuota.setProductStandardId(productStandard.getId());
            procurementQuota.setProductStandardName(productStandard.getName());
            procurementQuota.setProcurementId(procurementId);
            procurementQuota.setProcurementName(user.getNickName());
            procurementQuota.setProcurementPhone(user.getPhone());
            procurementQuota.setCreateTime(new Date());
            procurementQuota.setUpdateTime(new Date());
            procurementQuota.save();
        } else {
            procurementQuota.setProcurementId(procurementId);
            procurementQuota.setProcurementName(user.getNickName());
            procurementQuota.setProcurementPhone(user.getPhone());
            procurementQuota.setUpdateTime(new Date());
            procurementQuota.update();
        }
        renderNull();
    }
}
