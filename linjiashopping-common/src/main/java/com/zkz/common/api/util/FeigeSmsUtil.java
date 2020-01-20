package com.zkz.common.api.util;

import com.zkz.common.api.config.ProjectConfig;
import lombok.Data;
import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 飞鸽短信.
 */
public class FeigeSmsUtil {

    public static boolean mobileQuery(String phone, int code) {
        try {
            CloseableHttpClient client = null;
            CloseableHttpResponse response = null;
            try {
                List<BasicNameValuePair> formparams = new ArrayList<>();
                formparams.add(new BasicNameValuePair("Account", ProjectConfig.FEIGEACCOUNT));
                formparams.add(new BasicNameValuePair("Pwd", ProjectConfig.FEIGEPSW));//登录后台 首页显示
                formparams.add(new BasicNameValuePair("Content", ProjectConfig.FEIGECONTENT + code + ProjectConfig.FEIGECONTENT1));
                formparams.add(new BasicNameValuePair("Mobile", phone));
                formparams.add(new BasicNameValuePair("SignId", ProjectConfig.FEIGESIGNID));//登录后台 添加签名获取id

                HttpPost httpPost = new HttpPost("http://api.feige.ee/SmsService/Send");
                httpPost.setEntity(new UrlEncodedFormEntity(formparams, "UTF-8"));
                client = HttpClients.createDefault();
                response = client.execute(httpPost);
                HttpEntity entity = response.getEntity();
                String result = EntityUtils.toString(entity);
                System.out.println(result);
            } finally {
                if (response != null) {
                    response.close();
                }
                if (client != null) {
                    client.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }


    @Data
    public static class FeigeSmsResult {
        private String Code;
        private String Message;
        private String SendId;
        private String InvalidCount;
        private String SuccessCount;
        private String BlackCount;
    }
}
