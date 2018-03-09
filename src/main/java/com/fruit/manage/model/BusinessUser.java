package com.fruit.manage.model;

import com.fruit.manage.model.base.BaseBusinessUser;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;

import java.sql.SQLException;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class BusinessUser extends BaseBusinessUser<BusinessUser> {
	public static final BusinessUser dao = new BusinessUser().dao();

	public void updateBusinessUserSaleIDByUid(Integer uid,Integer saleId){
		Db.tx(new IAtom(){
			@Override
			public boolean run() throws SQLException {
				String sql="update b_business_user bu set bu.a_user_sales_id=? where bu.id=?";
				Db.update(sql,saleId,uid);
				return true;
			}
		});
	}
}
