package com.example.community.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.File;

/**
 * 初始化创建wk生成图片的保存目录
 */
@Configuration
public class WkConfig {

    private static final Logger logger = LoggerFactory.getLogger(WkConfig.class);

    /**
     * 生成图片的保存路径
     */
    @Value("${wk.image.storage}")
    private String wkImageStorage;

    /**
     * 初始化生成目录
     */
    @PostConstruct
    public void init(){
        File file = new File(wkImageStorage);
        if(!file.exists()){
            file.mkdir();
            logger.info("创建wk图片目录：" + wkImageStorage);
        }
    }

}
