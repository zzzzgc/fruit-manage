package com.fruit.manage.controller;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.model.Product;
import com.fruit.manage.model.Test;
import com.fruit.manage.util.DataResult;
import com.jfinal.core.ActionHandler;
import com.jfinal.kit.JsonKit;
import org.apache.log4j.Logger;
import org.apache.shiro.authz.annotation.RequiresPermissions;

import java.util.Date;
public class TestController extends BaseController{
    private Logger log = Logger.getLogger(getClass());

    public static void main(String[] args) {
    }

   @RequiresPermissions("test:query")
    public void getData() {
        Integer pageNum=getParaToInt("pageNum");
        Integer pageSize=getParaToInt("pageSize");
        Integer id=getParaToInt("id");
        String testname=getPara("testname");
        String prop=getPara("prop");
        String order=getPara("order");
        renderJson(Test.dao.page(pageNum, pageSize, id, testname, prop, order));
    }

    //修改数据
    @RequiresPermissions("test:edit")
    public void info() {
        Integer id=getParaToInt("id");
        if(id==null || "".equals(id)){
            renderErrorText("Id不能为空!");
            return;
        }
        Test test=Test.dao.getTestById(id);
        if(test==null || ("").equals(test)){
            renderErrorText("不存在ID为"+id+"的数据");
            return;
        }
        renderJson(test);

    }

    //添加数据
    @RequiresPermissions("test:save")
    public void save() {
       Test test=getModel(Test.class,"",true);
       log.info(String.format("测试信息:test=%s",JsonKit.toJson(test)));
       renderResult(Test.dao.save(test));
    }
    //删除数据
    @RequiresPermissions("test:delete")
    public void delete (){
       String [] ids =getParaValues("ids");
       if(ids==null || ids.length==0){
           renderErrorText("参数错误!");
           return;
       }
       DataResult<Object> result= Test.dao.delete(ids);
        if(result.isSuccessCode()){
            renderNull();
        }else{
            renderErrorText(result.getMsg());
        }
    }
}
