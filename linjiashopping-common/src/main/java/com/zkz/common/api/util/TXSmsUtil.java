package com.zkz.common.api.util;

import com.github.qcloudsms.SmsSingleSender;
import com.github.qcloudsms.SmsSingleSenderResult;
import com.github.qcloudsms.httpclient.HTTPException;
import com.zkz.common.api.config.ProjectConfig;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * 腾讯短信工具类
 */
public class TXSmsUtil {
    public static boolean mobileQuery(String phone, ArrayList<String> code) {
        try {
            SmsSingleSender ssender = new SmsSingleSender(ProjectConfig.TXAPPID, ProjectConfig.TXAPPKEY);
            SmsSingleSenderResult result = ssender.sendWithParam("86", phone,
                    ProjectConfig.TXTEMPlATEID, code, ProjectConfig.TXSMSSIGN, "", "");
            System.out.println(result);
        } catch (HTTPException e) {
            // HTTP 响应码错误
            e.printStackTrace();
        } catch (JSONException e) {
            // JSON 解析错误
            e.printStackTrace();
        } catch (IOException e) {
            // 网络 IO 错误
            e.printStackTrace();
        }
        return true;
    }
}
