package com.fruit.manage.util;

import java.io.Serializable;

public class DataResult<T> implements Serializable {
	private static final long serialVersionUID = 7933714516649991146L;

	public static final int CODE_SUCCESS = 0;
	public static final int CODE_FAIL = -99;

	private int successCode = CODE_SUCCESS;
	private int code;
	private T data;
	private String msg = "";

	public DataResult() {}

	/**
	 * 初始化一个失败的DataBean
	 * 
	 * @param msg
	 */
	public DataResult(String msg) {
		this.code = CODE_FAIL;
		this.msg = msg;
	}

	/**
	 * 初始化一个给定code的DataBean
	 * 
	 * @param msg
	 */
	public DataResult(int code) {
		this.code = code;
	}

	public DataResult(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}

	public static <T> DataResult<T> getSuccessData(T data) {
		DataResult<T> obj = new DataResult<>(CODE_SUCCESS);
		obj.setData(data);
		obj.setSuccessCode(CODE_SUCCESS);
		return obj;
	}

	public static <T> DataResult<T> getErrorData(String msg) {
		return new DataResult<>(msg);
	}

	public boolean isSuccessCode() {
		return this.code == successCode;
	}

	/**
	 * 注意以下方法序列化（json）时会使用到，即标准的get/set，不要删除
	 * @return
	 */
	public int getSuccessCode() {
		return successCode;
	}

	public void setSuccessCode(int successCode) {
		this.successCode = successCode;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

}
