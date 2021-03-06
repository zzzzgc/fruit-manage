package com.fruit.manage.model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fruit.manage.model.base.BaseProduct;
import com.fruit.manage.util.Common;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class Product extends BaseProduct<Product> {
    public static final Product dao = new Product().dao();
    public static final int STATUS_USE = 1;
    public static final int STATUS_UNUSED = 0;
    public static final int STATUS_DELETE = -1;

    public Page<Record> page(Integer pageNum, Integer pageSize, Integer productId, String productName, Integer status, Date start, Date end, String orderProp, String orderType) {
        if (pageNum == null || pageNum <= 0) {
            pageNum = 1;
        }
        if (pageSize == null || pageSize <= 0) {
            pageSize = 100;
        }
        if (pageSize > 1000) {
            pageSize = 1000;
        }
        String selectSql = "SELECT id,name,country,brand,fruit_type,sort,fresh_time,fruit_des,store_way,create_time,update_time,total_sell_num,week_sell_num,status,CASE img WHEN \"\" THEN \"缺少图片\" WHEN  NULL THEN \"缺少图片\" ELSE NULL END AS errorInfo ";
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("FROM b_product WHERE 1=1 ");
        if (productId != null) {
            sql.append("AND id=? ");
            params.add(productId);
        }
        if (StrKit.notBlank(productName)) {
            sql.append("AND name like ? ");
            params.add("%" + productName + "%");
        }
        if (status != null) {
            sql.append("AND status=? ");
            params.add(status);
        } else {
            sql.append("AND status!=-1 ");
        }
        if (start != null) {
            sql.append("AND create_time>=? ");
            params.add(start);
        }
        if (end != null) {
            sql.append("AND create_time<=? ");
            params.add(end);
        }
        if (StrKit.isBlank(orderProp)) {
            orderProp = "update_time";
        }
        sql.append("ORDER BY ").append(orderProp).append(" ");
        if ("descending".equals(orderType)) {
            sql.append("DESC ");
        }
        return Db.paginate(pageNum, pageSize, selectSql, sql.toString(), params.toArray());
    }

    public Product getById(int productId) {
        return findFirst("select id,name,fruit_type,country,province,sort,brand,total_sell_num,week_sell_num,measure_unit,status,fresh_time,fruit_des,store_way from b_product where id=?", productId);
    }

    public boolean changeStatus(Integer[] ids, int status) {
        return Db.tx(new IAtom() {
            @Override
            public boolean run() throws SQLException {
                Db.update("update b_product set status=? where id in (" + Common.arrayToSqlIn(ids) + ")", status);
                if (status != 1) {// 如果不为上架，则修改该商品下所有规格的状态
                    ProductStandard.dao.changeStatus(ids, status);
                }
                return true;
            }
        });
    }

    public boolean save(Product product, String[] imgs, String[] keywords, Integer[] typesId, Integer[] recommends) {
        return Db.tx(new IAtom() {
            @Override
            public boolean run() throws SQLException {
                boolean result = false;
                product.setUpdateTime(new Date());
                if (product.getId() != null) {
                    result = product.update();
                } else {
                    product.setCreateTime(new Date());
                    result = product.save();
                }
                if (!result) {
                    return false;
                }
                result = ProductImg.dao.saveProductImg(true, product.getId(), 1, imgs);
                if (!result) {
                    return false;
                }
                result = ProductKeyword.dao.saveProductKeyword(true, product.getId(), keywords);
                if (!result) {
                    return false;
                }
                result = ProductType.dao.saveProductType(true, product.getId(), typesId);
                if (!result) {
                    return false;
                }
                result = ProductRecommend.dao.saveProductRecommend(true, product.getId(), recommends);
                if (!result) {
                    return false;
                }
                return true;
            }
        });
    }

    /**
     * 用于获取用户模糊查询商品的商品列表,不可以加载未上架的
     *
     * @param queryString
     * @return
     */
    public List<Product> getProductNameByQueryString(String queryString) {
        String sql = "SELECT p.`name` AS product_name,p.brand,p.id AS product_id FROM b_product p  WHERE p.`name` LIKE CONCAT('%',?,'%') AND p.`status` = 1 ";
        return dao.find(sql, queryString);
    }

    /**
     * 获取产品ID，用于判断是否存在该商品
     *
     * @param productName
     * @param productStandardID
     * @return
     */
    public Product getProductIDByPNameAndPSID(String productName, Integer productStandardID) {
        String sql = "select p.id,ps.sell_price from b_product p,b_product_standard ps where 1=1 \n" +
                "and p.id = ps.product_id " +
                "and p.`name` = ?  " +
                "and ps.id= ? ";
        return findFirst(sql, productName, productStandardID);
    }

    /**
     * 获取产品ID，用于判断是否存在该商品
     *
     * @param productStandardID
     * @return
     */
    public Product getProductIDByPSID(Integer productStandardID) {
        String sql = "select p.id,ps.sell_price from b_product p,b_product_standard ps where 1=1 \n" +
                "and p.id = ps.product_id " +
                "and ps.id= ? ";
        return findFirst(sql, productStandardID);
    }

    /**
     * 增加该商品的购买量
     *
     * @return
     */
    public boolean increaseSellNum(Integer productId) {
        return Db.update("UPDATE b_product SET total_sell_num = total_sell_num + 1,week_sell_num = week_sell_num  + 1 WHERE id = ? ", productId) == 1;
    }

    /**
     * 修改该商品的购买量
     *
     * @return
     */
    public boolean updateSellNum(Integer productId, Integer changeNum) {
        return Db.update("UPDATE b_product SET total_sell_num = total_sell_num + ?,week_sell_num = week_sell_num  + ? WHERE id = ? ", changeNum, changeNum, productId) == 1;
    }

    /**
     * 新增商品的封装
     */
    public Product addProduct( String name, String country, Long sort, String brand, String measureUnit, Integer status, String img, String fruitDes) {
        Product product = new Product();
        product.setName(name);
        product.setCountry(country);
        product.setSort(sort);
        product.setBrand(brand);
        product.setMeasureUnit(measureUnit);
        product.setStatus(status);
        product.setImg(img);
        product.setFruitDes(fruitDes);
        product.setCreateTime(new Date());
        product.setUpdateTime(new Date());
        product.save();
        return product;
//        product.setFruitType(fruitType);
//        product.setProvince(province);
//        product.setFreshTime(freshTime);
//        product.setFreshExpireTime(freshExpireTime);
//        product.setStoreWay(storeWay);
//        product.setTotalSellNum(totalSellNum);
//        product.setWeekSellNum(weekSellNum);

    }
}
