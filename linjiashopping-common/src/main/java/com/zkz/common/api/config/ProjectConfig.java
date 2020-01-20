package com.zkz.common.api.config;

import java.util.HashMap;
import java.util.Map;

public class ProjectConfig {

    //Redis信息
    public static final String REDISHOST = "127.0.0.1";
    public static final int REDISPORT = 6379;
    public static final String REDISPASS = "";


    public static final String TOKENJWT = "jwt:";//记录令牌  有效期 30分钟
    public static final String TOKENPHONE = "user:";//记录手机号对应的令牌  有效期 30分钟
    public static final String USERLOGINCOUNT = "uc:";// uc:手机号  次数  10分钟
    public static final String USERSD = "userfreeze:";//手机号 所有冻结的手机号
    public static final String SIGNKEY = "usign:ljb";//记录每天签到  Set集合 内容 手机号
    public static final String TOKENHEAD = "usertoken";//请求消息头的名称，记录令牌  前端
    public static final String SMSPRELIMIT = "smspre:";//记录手机号一分钟只能发一次 smspre:180515990152 key
    public static final String SMSPREDAY = "smsday:";//记录手机号一天只能发20条 smsday:18515990152
    public static final String SMSCODE = "smscode:";//短信验证码  3分钟
    public static final String EMAILCODE = "emailcode:";//email验证码  3分钟
    public static final String EMAILUSE = "emailuse:";//email验证码  3分钟


    //短信配置
    public static final String SMSKEY = "d45ea6370e006cc8265d6847685f9c26";
    public static final String SMSTEMPID = "166095";

    //激活地址
    public static final String AESKEYACTIVECODE = "Jua2JnaixqcY0gEjemnNqQ==";


    //密码加密 采用RSA
    public static final String PASSRSAPUB = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAIPcObwNMNMsOpJB6XUt7/MyQ0croXAQ7pBxqnnY5yTmHf48Yz+0o9310xPTm3n6NvKB66QKAtsc18ULrkgznSkCAwEAAQ==";
    public static final String PASSRSAPRI = "MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEAg9w5vA0w0yw6kkHpdS3v8zJDRyuhcBDukHGqedjnJOYd/jxjP7Sj3fXTE9Obefo28oHrpAoC2xzXxQuuSDOdKQIDAQABAkAl64xS7zwuTpbdfsUDpN1PhBHjAsIfd08UDQwolF8MthIuXtVc3epw7au+x1RUMcsY+Ve4O/6erU2XVw40uOCxAiEA0XsiqRf+BPFoqPje3TqdXfRuSj9hjRTYrKDTkRz7nvUCIQChJGYwmoADQCrQXpXIUavGXBQ+73Cprgj/PvEfFkk85QIhALHA9gFcTX1uR+wR+jLpeP1v22J/c8yeYtnhQoWBfXY9AiB8/jHyE3Wmj9hl5mhCiO84HuZpZus6As/hxV3dIjlO7QIgYEYa9iYf4c6xT/++OxrD9/aH+v3VSnyDccHSrms1/f8=";


    public static Map<Integer, String> projects;

    static {
        projects = new HashMap<Integer, String>();
        projects.put(10001, "电商");

    }
    /*飞鸽短信配置*/

    public static final String FEIGEACCOUNT = "17744636884";//飞鸽账户信息
    public static final String FEIGEPSW = "9928f561c6da02a7189014f3b";//飞鸽密钥
    public static final String FEIGECONTENT = "感谢您注册逗比科技网络有限公司，本次的验证码为：";//短信模板
    public static final String FEIGECONTENT1 = "，请在五分钟内完成注册！";
    public static final String FEIGESIGNID = "203146"; //签名id

    /* 腾讯短信配置*/
    public static final int TXAPPID = 1400295029;
    public static final String TXAPPKEY = "9ff91d87c2cd7cd0ea762f141975d1df37481d48700d70ac37470aefc60f9bad";
    public static final int TXTEMPlATEID = 493143;
    public static final String TXSMSSIGN = "逗比网络";
}
