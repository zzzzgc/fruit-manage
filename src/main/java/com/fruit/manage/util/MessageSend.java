package com.fruit.manage.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fruit.manage.model.Param;
import com.jfinal.kit.PropKit;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author partner
 * @date 2018/4/9 14:29
 */
public class MessageSend {
    private static Map<String, Integer> phoneMap = new HashMap<>();
    /**
     * 时间戳接口配置
     */
    public static final String TIMESTAMP = "https://api.mysubmail.com/service/timestamp";
    public static final String TYPE_MD5 = "md5";
    public static final String TYPE_SHA1 = "sha1";
    /**
     * API 请求接口配置
     */
    private static final String URL="https://api.mysubmail.com/message/send";

    /**
     * 使用配置信息对指定的人进行发送
     * @param toPhone
     * @param content
     */
    public static boolean sendMessage(String toPhone,String content){
        String appId = Param.dao.getParam("message.appId");
        String appKey = Param.dao.getParam("message.appKey");
        String signType = Param.dao.getParam("message.signType");
        return sendMessage(appId,appKey,toPhone,content,signType);
    }

    public static void main(String[] args) {
        String appId = "22527";
        String appKey = "43trtdfgdfgfdgdfgdf";
        String signType = "md5";
        String toPhone = "18718840426";
        String content = "【fruit水果测试】你好，你的验证码是1234";
        sendMessage(appId,appKey,toPhone,content,signType);
    }

    /**
     * 获取参数进行发送短信
     * @param appId
     * @param appKey
     * @param toPhone
     * @param content
     * @param signType
     */
    public static boolean sendMessage(String appId, String appKey, String toPhone,String content, String signType) {
        TreeMap<String, Object> requestData = new TreeMap<String, Object>();

        /**
         *  签名验证方式
         *  详细说明可参考 SUBMAIL 官网，开发文档 → 开始 → API 授权与验证机制
         */
        requestData.put("appid", appId);
        requestData.put("content", content);
        requestData.put("to", toPhone);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        @SuppressWarnings("deprecation")
        ContentType contentType = ContentType.create(HTTP.PLAIN_TEXT_TYPE,HTTP.UTF_8);
        for(Map.Entry<String, Object> entry: requestData.entrySet()){
            String key = entry.getKey();
            Object value = entry.getValue();
            if(value instanceof String){
                builder.addTextBody(key, String.valueOf(value),contentType);
            }
        }
        if(TYPE_MD5.equals(signType)|| TYPE_SHA1.equals(signType)){
            String timestamp = getTimestamp();
            requestData.put("timestamp", timestamp);
            requestData.put("sign_type", signType);
            String signStr = appId + appKey + RequestEncoder.formatRequest(requestData) + appId + appKey;
            System.out.println(signStr);
            builder.addTextBody("timestamp", timestamp);
            builder.addTextBody("sign_type", signType);
            builder.addTextBody("signature", RequestEncoder.encode(signType, signStr), contentType);
        }else{
            builder.addTextBody("signature", appKey, contentType);
        }
        /**
         * http post 请求接口
         * 成功返回 status: success,其中 fee 参数为短信费用 ，credits 参数为剩余短信余额
         * 详细的 API 错误日志请访问 SUBMAIL 官网 → 开发文档 → DEBUG → API 错误代码
         */
        HttpPost httpPost = new HttpPost(URL);
        httpPost.addHeader("charset", "UTF-8");
        httpPost.setEntity(builder.build());
        try {
            CloseableHttpClient closeableHttpClient = HttpClientBuilder.create().build();
            HttpResponse response = closeableHttpClient.execute(httpPost);
            HttpEntity httpEntity = response.getEntity();
            if (httpEntity != null) {
                String jsonStr = EntityUtils.toString(httpEntity, "UTF-8");
                System.out.println(jsonStr);
                com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(jsonStr);
                String status=jsonObject.get("status")+"";
                System.out.println("status:" + status);
                if ("success".equals(status)) {
                    System.out.println("send msg success");
                    return true;
                }
                return false;
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

//    private static String getMessageCode() {
//
//    }

    /**
     * 获取时间戳
     * @return
     */
    private static String getTimestamp(){
        CloseableHttpClient closeableHttpClient = HttpClientBuilder.create().build();
        HttpGet httpget = new HttpGet(TIMESTAMP);
        try{
            HttpResponse response = closeableHttpClient.execute(httpget);
            HttpEntity httpEntity = response.getEntity();
            String jsonStr = EntityUtils.toString(httpEntity,"UTF-8");
            if(jsonStr != null){
                JSONObject json = JSON.parseObject(jsonStr);
                // 切换成ali的json工具
//                JSONObject json = JSONObject.fromObject(jsonStr);
                return json.getString("timestamp");
            }
            closeableHttpClient.close();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

}
