package com.fruit.manage.controller;

import java.util.ArrayList;
import java.util.List;

import com.fruit.manage.base.BaseController;
import com.fruit.manage.util.ImgUtil;
import com.jfinal.upload.UploadFile;

public class CommonController extends BaseController {
	
	/**
	 * 上传图片公共方法
	 */
	public void upload(){
		List<UploadFile> fileList = null;
		try{
			fileList = getFiles("file");
		}catch(Exception e) {
			renderNull();
			return;
		}
		if(fileList.size() == 1){
			String picUrl=ImgUtil.upImg(fileList.get(0), getRequest());
			renderText(picUrl);
			return;
		}
		List<String> uploadText = new ArrayList<String>();
		for(UploadFile f:fileList){
			String upImg = ImgUtil.upImg(f, getRequest());
			uploadText.add(upImg);
		}
		renderJson(uploadText);
	}
	
}



