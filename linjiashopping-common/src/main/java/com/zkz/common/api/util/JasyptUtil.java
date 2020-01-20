package com.zkz.common.api.util;

import org.jasypt.util.text.BasicTextEncryptor;

public class JasyptUtil {
    public static void main(String[] args) {
        BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
        //加密所需的salt(盐)
        textEncryptor.setPassword("G0CvDz7oJn6");
        //要加密的数据（数据库的用户名或密码）
        String username = textEncryptor.encrypt("1512125546@qq.com");
        String password = textEncryptor.encrypt("");
        System.out.println("username:" + username);
        System.out.println("password:" + password);
        System.out.println(textEncryptor.decrypt("sMvhmneq5uSmkogtV6P3Fg=="));
        System.out.println(textEncryptor.decrypt(username));
        System.out.println("______________________");


    }
}
