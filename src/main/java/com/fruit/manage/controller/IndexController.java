package com.fruit.manage.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fruit.manage.base.BaseController;

public class IndexController extends BaseController {
	
//	public void index() {
//		renderJson(new DataResult<String>(-100, "测试"));
//	}

	public void getType() {
		List<String> list = Arrays.asList(new String[]{"type1", "type2", "type3"});
		renderJson(list);
	}

	public void getTopMenu() {
		List<Map<String, Object>> datas = new ArrayList<Map<String, Object>>();
		Map<String, Object> d1 = new HashMap<>();
		d1.put("id", "1");
		d1.put("name", "菜单1");
		datas.add(d1);
		Map<String, Object> d2 = new HashMap<>();
		d2.put("id", "2");
		d2.put("name", "菜单2");
		datas.add(d2);
		renderJson(datas);
	}
}
