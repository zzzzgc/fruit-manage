package com.fruit.manage.constant;

/**
 * @Author: ZGC
 * @Date Created in 16:05 2018/5/4
 */
public enum RoleKeyCode {
    /**
     * 超级管理员
     */
    SUP_ADMIN(1),
    /**
     * 预留
     */
    SHOP_ADMIN(2),
    /**
     * 普通管理员
     */
    SIMPLE_ADMIN(3),
    /**
     * 销售
     */
    SALES(4),
    /**
     * 采购
     */
    PROCUREMENT(5),
    /**
     * 运营
     */
    OPERATOR(6),
    /**
     * 人事
     */
    PERSONNEL(7),
    /**
     * 仓库
     */
    WAREHOUSE(8),
    ;

    Integer roleId;

    RoleKeyCode(Integer roleId) {
        this.roleId = roleId;
    }

    public Integer getRoleId() {
        return roleId;
    }
}
