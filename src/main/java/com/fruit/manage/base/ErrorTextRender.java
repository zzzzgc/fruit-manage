package com.fruit.manage.base;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.jfinal.render.Render;
import com.jfinal.render.RenderException;

public class ErrorTextRender extends Render {
	protected static final String contentType = "text/plain; charset=" + getEncoding();
	
	protected int errorCode;
	protected String errorText;
	
	public ErrorTextRender(int errorCode, String errorText) {
		this.errorCode = errorCode;
		this.errorText = errorText;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void render() {
		String encodeText = "";
		PrintWriter writer = null;
		try {
			encodeText = URLEncoder.encode(errorText, "utf-8");
			response.setStatus(getErrorCode(), encodeText);// 针对部分前端ajax框架（axios），错误消息设置到响应体里，页面上是获取不到的（jquery可以），需要使用该方式
//			response.sendError(getErrorCode(), encodeText);// 和上面的不同，这里会返回html格式，而上面仅会返回错误文本

			response.setHeader("Pragma", "no-cache");
	        response.setHeader("Cache-Control", "no-cache");
	        response.setDateHeader("Expires", 0);
			response.addHeader("Error-Text", encodeText);

			response.setContentType(contentType);
	        writer = response.getWriter();
	        writer.write(encodeText);
	        writer.flush();
		} catch (IOException e) {
			throw new RenderException(e);
		}
		finally {
			if (writer != null)
				writer.close();
		}
	}
	
	public int getErrorCode() {
		return errorCode;
	}
}
