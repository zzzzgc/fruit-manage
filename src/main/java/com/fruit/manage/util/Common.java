package com.fruit.manage.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.jfinal.kit.Base64Kit;
import com.jfinal.kit.HashKit;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.PropKit;
import com.jfinal.log.Log4jLog;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.ehcache.CacheKit;

public class Common {
	private static Log4jLog log = Log4jLog.getLog(Common.class);
	
	public static void main(String[] args) {
		//System.out.println(htmlToText("<p style='margin-top: 0px; margin-bottom: 0px; padding: 0px 0px 15px; text-indent: 2em; color: rgb(43, 43, 43); font-family: Arial, 宋体; font-size: 14px; line-height: 26.6px; white-space: normal; widows: 1; background-color: rgb(255, 255, 255);'>【12月3日 个性玩爆全场】</p><p style='margin-top: 0px; margin-bottom: 0px; padding: 0px 0px 15px; text-indent: 2em; color: rgb(43, 43, 43); font-family: Arial, 宋体; font-size: 14px; line-height: 26.6px; white-space: normal; widows: 1; background-color: rgb(255, 255, 255);'>《天子剑》群战PK，除了活力四射的激战外，更有各种酷炫新坐骑等你来挑选!各种奇珍异兽应有尽有，驾驭神兽，在千军万马中冲杀，就是这个feel，倍儿爽!谁说群战就只是装备技能的大比拼?为群战而来，《天子剑》就要个性玩!</p><p class='p-image' style='margin-top: 0px; margin-bottom: 0px; padding: 0px 0px 15px; color: rgb(43, 43, 43); font-family: Arial, 宋体; font-size: 14px; line-height: 26.6px; white-space: normal; widows: 1; text-align: center; background-color: rgb(255, 255, 255);'><img src='http://192.168.0.82:10099/image_service/data/45/194/bd979ef6a651b41eb6940f4317f22ba2.jpg' width='558' height='322' alt='4.jpg' style='border: 0px; vertical-align: middle;'/></p><p style='margin-top: 0px; margin-bottom: 0px; padding: 0px 0px 15px; text-indent: 2em; color: rgb(43, 43, 43); font-family: Arial, 宋体; font-size: 14px; line-height: 26.6px; white-space: normal; widows: 1; background-color: rgb(255, 255, 255);'>《天子剑》12月3日19点新版新服即将开启，更多精彩新内容也即将揭开面纱，在这个热血战场，你是不可缺少的哟，赶紧下载游戏预创建角色，更有豪礼免费赢取!</p><p><br/></p>"));
		//System.out.println(getBigDecimal("3.33"));
		//System.out.println((int)Double.parseDouble("33.33"));
		 
		System.err.println(generEhcacheKey("123", "sdfls", 2222));
	}
	
	/**
	 * 将list转换为sql的in查询字符串
	 * @param list
	 * @return '1','2',''		没有数据时返回''
	 */
	public static String listToSqlIn(List<String> list){
		StringBuffer sqlIn = new StringBuffer("'");
		Iterator<String> ite = list.iterator();
		while(ite.hasNext()){
			sqlIn.append(ite.next()+"','");
		}
		sqlIn.append("'");
		return sqlIn.toString();
	}
	
	/**
	 * 将list转换为sql的in查询字符串
	 * 
	 * @param resData
	 * @param columnName
	 * @return '1','2',''		没有数据时返回''
	 */
	public static String recordListToSqlIn(List<Record> resData,String columnName){
		StringBuffer sqlIn = new StringBuffer("'");
		Iterator<Record> ite = resData.iterator();
		while(ite.hasNext()){
			Record rowData = ite.next();
			sqlIn.append(rowData.get(columnName)+"','");
		}
		sqlIn.append("'");
		return sqlIn.toString();
	}
	
	/**
	 * 将数组转换成sql的in查询字符串
	 * @return '1','2',''		没有数据时返回''
	 */
	public static String arrayToSqlIn(String[] array){
		StringBuffer sqlIn = new StringBuffer("'");
		for(int i=0;i<array.length;i++){
			sqlIn.append(array[i]+"','");
		}
		sqlIn.append("'");
		return sqlIn.toString();
	}
	
	/**
	 * 将list的数组转换为二维数组，主要用于Db.batch()，批量处理
	 * 
	 * @param list
	 * @return
	 */
	public static Object[][] listTo2Array(List<Object[]> list){
		if(list.size() == 0){
			return null;
		}
		int size = list.get(0).length;
		Object[][] paramsArr = new Object[list.size()][size];
		for(int i=0;i<list.size();i++){
			paramsArr[i] = list.get(i);
		}
		return paramsArr;
	}
	
	/**
	 * sql like时防止注入
	 * @param srcStr
	 * @return
	 */
	public static String queryLike(String srcStr) {
		//适用于sqlserver
//		result = StringUtils.replace(result, "[", "[[]");
//		result = StringUtils.replace(result, "_", "[_]");
//		result = StringUtils.replace(result, "%", "[%]");
//		result = StringUtils.replace(result, "^", "[^]");
		//适用于mysql
		srcStr = StringUtils.replace(srcStr, "\\", "\\\\");
		srcStr = StringUtils.replace(srcStr, "'", "\\'");
		srcStr = StringUtils.replace(srcStr, "_", "\\_");
		srcStr = StringUtils.replace(srcStr, "%", "\\%");
		
		return "%" + srcStr + "%";
	}
	
	public static String nullToBlank(Object obj){
		return null == obj?"":obj.toString();
	}
	public static Object blankToNull(Object obj){
		if(null != obj && StringUtils.isBlank(obj.toString())){
			return null;
		}
		return obj;
	}
	
	/**
	 * 校验手机号码合法性
	 */
	public static boolean mobileVerify(String phone){
		if(StringUtils.isNotBlank(phone)){
			String regExpMobile = PropKit.get("regex.phone", "^1[3-9]\\d{9}$");
			Pattern p = Pattern.compile(regExpMobile);
			Matcher m = p.matcher(phone);
			return m.find();
		}else{
			return false;
		}
	}
	
	/**
	 * 
	 * @param qq
	 * @return
	 */
	public static boolean qqVerify(String qq){
		if(StringUtils.isNotBlank(qq)){
			String regExpQQ = PropKit.get("regex.qq", "^[1-9][0-9]{4,11}");
			Pattern p = Pattern.compile(regExpQQ);
			Matcher m = p.matcher(qq);
			return m.find();
		}else{
			return false;
		}
	}
	
	/**
	 * 生成ehcache的key值，
	 * 注意：此处是以调用方的类包名+方法名生成缓存key的前缀，所以如果一个方法里调用了两次，此处要自己传递一个args作为区分
	 * 
	 * @param args	参数为基本数据类型或者其包装类
	 * @return
	 */
	public static String generEhcacheKey(Object... args){
		StackTraceElement[] elements = Thread.currentThread().getStackTrace();
		String keyPrefix = null;
		if(elements.length > 2){// 获取调用方的类名及方法名，作为key的前缀，防止key重复
			StackTraceElement element = elements[2];
			keyPrefix = element.getClassName()+"."+ element.getMethodName();
		}else{// 通常情况下，length大于2的
			keyPrefix = UUID.randomUUID().toString().replaceAll("-", "");
			log.warn("生成cacheKey时，调用方参数有误："+JsonKit.toJson(elements));
		}
		
		String split = ",";
		StringBuffer key = new StringBuffer(keyPrefix + "(");
		for(Object arg : args) {
			String val = "null";
			if(arg != null) {
				val = JsonKit.toJson(arg);
			}
			key.append(Base64Kit.encode(val) + split);
        }
		if(args != null && args.length > 0) {
			key.replace(key.length() - split.length(), key.length(), ")");
		} else {
			key.append(")");
		}
		String md5CacheKey = HashKit.md5(key.toString());
		log.info("生成的cache key="+md5CacheKey+", 原缓存值为："+key.toString());
		return md5CacheKey;
	}
	
	/**
	 * 去除double类型字符串后面的.和多余的0
	 * @param s
	 * @return
	 */
	public static String getDouble(String s){
		if(s.indexOf(".") > 0){
            s = s.replaceAll("0+?$", "");//去掉多余的0
            s = s.replaceAll("[.]$", "");//如最后一位是.则去掉  
        }  
        return s;
	}
	
	/**
	 * 向url后追加参数
	 * @param url
	 * @param key
	 * @param value
	 * @return
	 */
	public static void appendUrlParam(StringBuffer urlBuffer, String key, String value){
		if(urlBuffer == null || urlBuffer.length() == 0){
			return;
		}
		if (urlBuffer.indexOf("?") == -1) {
			urlBuffer.append("?");
		} else if(!"&".endsWith(urlBuffer.substring(urlBuffer.length()-1))){
			urlBuffer.append("&");
		}
		urlBuffer.append(key + "=" + value);
	}
	
	public static void appendUrlParams(StringBuffer urlBuffer, Map<String, String[]> params, String... excludes){
		appendUrlParams(urlBuffer, params, 600, excludes);
	}
	/**
	 * 
	 * @param urlBuffer
	 * @param params
	 * @param maxParamsLength 参数最大长度，如果超出这个长度，就不再追加参数，防止因调用方程序有问题，一直请求，导致追加参数过长
	 * @param excludes
	 */
	public static void appendUrlParams(StringBuffer urlBuffer, Map<String, String[]> params, int maxParamsLength, String... excludes){
		if(urlBuffer == null || urlBuffer.length() == 0){
			return;
		}
		if(params == null || params.size() == 0){
			return;
		}
		List<String> excludeList = new ArrayList<String>();
		if(excludes != null){
			excludeList.addAll(Arrays.asList(excludes));
		}
		int startLength = urlBuffer.length();
		for(String key : params.keySet()){
			if(excludeList.contains(key)){
				continue;
			}
			if(params.get(key) == null || params.get(key).length==0){
				if((urlBuffer.length() - startLength + key.length() + 1) > maxParamsLength){// 判断追加参数后，追加的参数长度是否大于设置的最大长度，如果大于，则停止追加
					return;
				}
				appendUrlParam(urlBuffer, key, "");
			}else{
				for(String v : params.get(key)){
					if((urlBuffer.length() - startLength + key.length() + v.length() + 1) > maxParamsLength){// 判断追加参数后，追加的参数长度是否大于设置的最大长度，如果大于，则停止追加
						return;
					}
					appendUrlParam(urlBuffer, key, v);
				}
			}
		}
	}
	
	/**
	 * 处理特殊字符，如表情字符，无法保存到数据库，需要先替换特殊字符
	 * 
	 * 将特殊字符替换为*
	 * 
	 * 如：aa的😁😁😁😁123   替换后为：aa的****123
	 * 
	 * @param text
	 * @return
	 */
	public static String replaceDbStr(String text){
		if(StringUtils.isBlank(text)){
			return text;
		}
		byte[] conbyte = text.getBytes();
		byte[] newByte = new byte[conbyte.length];
		int length = 0;
        for (int i = 0; i < conbyte.length; i++) {
        	newByte[length] = conbyte[i];
        	if((conbyte[i] & 0xF8) == 0xF0){
        		newByte[length] = 0x2a;
        		i += 3;
        	}
        	length ++;
        }
        
        text = new String(newByte).trim();
        return text;
	}
	
	/**
	 * 清除指定的cacheKey，用于开发环境清除缓存
	 * @param cacheKeys
	 */
	public static void removeCache(String... cacheKeys){
		for(String key : cacheKeys){
			CacheKit.removeAll(key);
		}
	}
	
	/**
	 * 时间格式化
	 * 
	 * @param date
	 *            时间
	 * @param format
	 *            时间格式
	 */
	public static String fmtTime(Date date, String format) {
		SimpleDateFormat fmt = new SimpleDateFormat(format);
		return fmt.format(date);
	}

	/**
	 * 随机抽取某个数字区间的一个数字
	 * 
	 * @param startNum
	 *            包含
	 * @param endNum
	 *            包含
	 * @param excludeNum
	 *            要排除的数字
	 * @return 如果因传的数据有问题导致，无法获取数据，则返回-1
	 */
	public static int getRandomNum(int startNum, int endNum, Integer[] excludeNum) {
		if (excludeNum == null) {
			excludeNum = new Integer[] {};
		}
		if (endNum < startNum) {
			return -1;
		}
		Set<Integer> newNum = new HashSet<Integer>();
		for (int num : excludeNum) {
			if (num >= startNum && num <= endNum) {
				newNum.add(num);
			}
		}

		if ((endNum - startNum + 1) <= excludeNum.length) {// 排除了所有数据，直接返回-1
			return -1;
		}
		if (1.0 * excludeNum.length / (endNum - startNum + 1) > 0.8) {// 如果排除的数量大于80%，则使用该方式生成
			List<Integer> newNumList = new ArrayList<Integer>();
			for (int i = startNum; i <= endNum; i++) {
				newNumList.add(i);
			}
			return newNumList.get((int) (Math.random() * newNumList.size()));
		} else {
			Date startTime = new Date();
			while (true) {
				int num = startNum + (int) (Math.random() * (endNum - startNum + 1));
				if (!newNum.contains(num)) {
					return num;
				}
				if (new Date().getTime() - startTime.getTime() > 5 * 1000) {// 无法生成数据
					return -1;
				}
			}
		}
	}

	// 提取文本框的文字
	public static String htmlToText(String inputString) {
		String htmlStr = inputString; // 含html标签的字符串
		String textStr = "";
		// java.util.regex.Pattern p_script;
		// java.util.regex.Matcher m_script;
		java.util.regex.Pattern p_style;
		java.util.regex.Matcher m_style;
		java.util.regex.Pattern p_html;
		java.util.regex.Matcher m_html;
		try {
			// String regEx_script =
			// "<[\\s]*?script[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?script[\\s]*?>";
			// //定义script的正则表达式{或<script[^>]*?>[\\s\\S]*?<\\/script> }
			String regEx_style = "<[\\s]*?style[^>]*?>[\\s\\S]*?<[\\s]*?\\/[\\s]*?style[\\s]*?>"; // 定义style的正则表达式{或<style[^>]*?>[\\s\\S]*?<\\/style>
																									// }
			String regEx_html = "<[^>]+>"; // 定义HTML标签的正则表达式
			// p_script =
			// Pattern.compile(regEx_script,Pattern.CASE_INSENSITIVE);
			// m_script = p_script.matcher(htmlStr);
			// htmlStr = m_script.replaceAll(""); //过滤script标签
			p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
			m_style = p_style.matcher(htmlStr);
			htmlStr = m_style.replaceAll(""); // 过滤style标签
			p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
			m_html = p_html.matcher(htmlStr);
			htmlStr = m_html.replaceAll(""); // 过滤html标签
			textStr = htmlStr;
		} catch (Exception e) {
			e.fillInStackTrace();
		}
		return textStr;// 返回文本字符串
	}
	 
	/**
	 * 检测参数是否合法，非法时抛出异常
	 * 
	 * @param expression
	 *            为false时抛出异常
	 * @param errorMessageTemplate
	 * @param errorMessageArgs
	 */
	public static void checkArgument(boolean expression, String errorMessageTemplate, Object... errorMessageArgs) {
		if (!expression) {
			throw new IllegalArgumentException(String.format(errorMessageTemplate, errorMessageArgs));
		}
	}

	/**
	 * 非法状态检测
	 * 
	 * @param expression
	 *            为false时抛出异常
	 * @param errorMessageTemplate
	 * @param errorMessageArgs
	 */
	public static void checkStatus(boolean expression, String errorMessageTemplate, Object... errorMessageArgs) {
		if (!expression) {
			throw new IllegalStateException(String.format(errorMessageTemplate, errorMessageArgs));
		}
	}
}
