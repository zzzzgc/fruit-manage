package com.fruit.manage.model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.fruit.manage.constant.RoleKeyCode;
import com.jfinal.plugin.activerecord.Record;
import org.apache.commons.lang3.StringUtils;

import com.fruit.manage.model.base.BaseUser;
import com.fruit.manage.util.Common;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.IAtom;
import com.jfinal.plugin.activerecord.Page;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class User extends BaseUser<User> {
    public static final User dao = new User().dao();

    /**
     * 根据用户登录名+密码查询用户（用于验证登录）
     *
     * @param userName 非空
     * @param password 非空
     * @return
     */
    public User getUser(String userName, String password) {
        if (StringUtils.isAllBlank(userName, password)) {
            return null;
        }
        return dao.findFirst("select * from a_user where name = ? and pass = ?", userName, password);
    }

    /**
     * 根据用户登录名查询用户
     *
     * @param userName 非空
     * @return
     */
    public User getUser(String userName) {
        if (StringUtils.isBlank(userName)) {
            return null;
        }
        return dao.findFirst("select * from a_user where name = ?", userName);
    }

    /**
     * 分页查询用户列表
     *
     * @param userName
     * @param pageNum
     * @param pageSize
     * @return
     */
    public Page<User> getData(String userName, int pageNum, int pageSize, String orderBy, boolean isASC) {
        List<Object> params = new ArrayList<Object>();
        StringBuffer sql = new StringBuffer();
        sql.append("from a_user where 1=1 ");
        if (StrKit.notBlank(userName)) {
            sql.append("and name like ? ");
            params.add("%" + userName + "%");
        }
        orderBy = StrKit.isBlank(orderBy) ? "create_time" : orderBy;
        sql.append("order by " + orderBy + " " + (isASC ? "" : "desc "));
        return paginate(pageNum, pageSize, "select * ", sql.toString(), params.toArray());
    }

    /**
     * 保存用户（如果关联的角色不为空，会同时保存关联的角色）
     *
     * @param model
     * @param roleIds
     */
    public boolean save(User model, String[] roleIds) {
        return Db.tx(new IAtom() {
            @Override
            public boolean run() throws SQLException {
                if (model.getId() == null) {
                    model.setCreateTime(new Date());
                }
                model.setUpdateTime(new Date());
                if (model.getId() == null) {
                    model.save();//保存用户
                } else {
                    model.update();//更新用户
                }
                Db.update("delete from a_user_role where user_id = ?", model.getId());
                if (roleIds != null && roleIds.length > 0) {
                    List<Object[]> params = new ArrayList<>();
                    for (String roleId : roleIds) {
                        params.add(new Object[]{model.getId(), roleId});
                    }
                    String sql = "insert into a_user_role(user_id, role_id) values(?,?)";
                    Db.batch(sql, Common.listTo2Array(params), params.size());
                }
                return true;
            }
        });
    }

    /**
     * 通过Roke_key获取所有销售人员信息
     *
     * @return
     */
    public List<User> getAllUserByRoleKey() {
        String sql = "SELECT u.id,u.`name`,u.nick_name,u.phone from a_user_role ur,a_user u,a_role r " +
                " where ur.user_id=u.id and ur.role_id=r.id and role_key='sales'";
        return find(sql);
    }

    /**
     * 根据商户ID获取销售ID
     *
     * @param businessInfoID
     * @return
     */
    public User getSaleUserIDByBusinessInfoID(Integer businessInfoID) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT u.id ");
        sql.append("from a_user_role ur,a_user u,a_role r ");
        sql.append("where ur.user_id=u.id and ur.role_id=r.id and role_key='sales' ");
        sql.append("and u.id = ( ");
        sql.append("select bu.a_user_sales_id from b_business_user bu,b_business_info bi where bu.id=bi.u_id and bi.id=?)");
        return dao.findFirst(sql.toString(), businessInfoID);
    }

    public User getUserById(Integer id) {
        String sql = "select * from a_user where id = ? ";
        return findFirst(sql, id);
    }

    /**
     * 判断是否是销售人员
     *
     * @return 是不是销售 true 是  false 不是
     */
    public boolean isSales(Integer uid) {

        String sql = "SELECT COUNT(1) FROM a_user au INNER JOIN a_user_role aur ON aur.user_id = au.id WHERE aur.role_id = ? AND au.id = ?";
        Record first = Db.findFirst(sql);
        String count = first.getStr("count(1)");
        User user = dao.findById(uid);
        Integer isSales = user.getIsSales();
        if (isSales == 1) {
            return true;
        }
        return false;
    }

    /**
     * 判断用户角色
     * @return 是不是销售 true 是  false 不是
     */
    public boolean isRole(Integer uid, Integer roleId) {
        String sql = "SELECT * FROM a_user au INNER JOIN a_user_role aur ON aur.user_id = au.id WHERE aur.role_id = ? AND au.id = ?";
        List<User> users = dao.find(sql, roleId, uid);
        if (users != null && users.size() > 0) {
            return true;
        }
        return false;
    }

    /**
     * 获取所有指定角色的用户
     * @return 所有拥有该角色的用户
     */
    public List<User> getUsersByRoleId(RoleKeyCode roleKeyCode) {
        return dao.find("SELECT * FROM a_user au INNER JOIN a_user_role ur ON ur.user_id = au.id WHERE ur.role_id = ?", roleKeyCode.getRoleId());
    }


    /**
     * 获取所有的用户编号和用户名
     *
     * @return 所有的用户编号和用户名
     * TODO 这个方面不可取，必须要根据角色ID去获取
     */
    public List<User> getAllUser() {
        String sql = "select u.id,u.`name`,u.nick_name from a_user u";
        return find(sql);
    }

    /**
     * 根据用户姓名获取用户编号
     *
     * @param name
     * @return
     */
    public Integer getUserIdByName(String name) {
        String sql = "SELECT id from a_user where `nick_name` like ? ";
        return Db.queryInt(sql, "%" + name + "%");
    }

    /**
     * 根据用户工号获取用户昵称
     *
     * @param id
     * @return
     */
    public String getNickNameById(Integer id) {
        String sql = "SELECT nick_name from a_user where id = ? ";
        return Db.queryStr(sql, id);
    }
}
