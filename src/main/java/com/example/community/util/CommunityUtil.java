package com.example.community.util;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.UUID;

/**
 * @author Lenovo
 */
public class CommunityUtil {

    /**
     * 生成随机字符串
     * @return
     */
    public static String generateUUID(){
        return UUID.randomUUID().toString().replaceAll("-","");
    }

    /**
     * MD5加密，提高安全性
     * 最好是传入在原字符串基础上加上一段随机字符串后的字符串，更安全
     * @param key
     * @return
     */
    public static String md5(String key){
        if(StringUtils.isBlank(key)){
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

}
