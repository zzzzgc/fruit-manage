package com.fruit.manage.model;

import com.fruit.manage.model.base.BaseProductRecommend;
import com.fruit.manage.util.Common;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class ProductRecommend extends BaseProductRecommend<ProductRecommend> {
    public static final ProductRecommend dao = new ProductRecommend().dao();

    public int[] getProductRecommend(int productId) {
        List<ProductRecommend> list = find("select type from b_product_recommend where product_id=?", productId);
        int[] recommends = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            recommends[i] = list.get(i).getType();
        }
        return recommends;
    }

    public boolean saveProductRecommend(boolean delete, int productId, Integer[] recommends) {
        return Db.tx(new IAtom() {
            @Override
            public boolean run() throws SQLException {
                if (delete) {
                    Db.update("delete from b_product_recommend where product_id=?", productId);
                }
                List<Object[]> params = new ArrayList<>();
                if (recommends != null && recommends.length > 0) {

                    for (Integer recommend : recommends) {
                        params.add(new Object[]{productId, recommend});
                    }
                    String sql = "insert into b_product_recommend(product_id, type, create_time) values(?,?,now())";
                    Db.batch(sql, Common.listTo2Array(params), params.size());
                }
                return true;
            }
        });
    }
}
