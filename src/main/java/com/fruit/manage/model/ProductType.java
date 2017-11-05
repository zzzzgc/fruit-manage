package com.fruit.manage.model;

import com.fruit.manage.model.base.BaseProductType;
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
public class ProductType extends BaseProductType<ProductType> {
	public static final ProductType dao = new ProductType().dao();

	public List<Integer> getProductTypes(int productId) {
		List<ProductType> list = find("select type_id from b_product_type where product_id=?", productId);
		List<Integer> typeList = new ArrayList<>();
		for(ProductType t : list) {
			typeList.add(t.getTypeId());
		}
		return typeList;
	}

	public boolean saveProductType(boolean delete, int productId, Integer[] types) {
		return Db.tx(new IAtom() {
			@Override
			public boolean run() throws SQLException {
				if(delete) {
					Db.update("delete from b_product_type where product_id=?", productId);
				}
				List<Object[]> params = new ArrayList<>();
				for(Integer type : types) {
					params.add(new Object[]{productId, type});
				}
				String sql = "insert into b_product_type(product_id, type_id, create_time) values(?,?,now())";
				Db.batch(sql, Common.listTo2Array(params), params.size());
				return true;
			}
		});
	}
}
