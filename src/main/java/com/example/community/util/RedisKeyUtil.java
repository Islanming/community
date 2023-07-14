package com.example.community.util;

/**
 * Redis的key生成工具类
 * @author Lenovo
 */
public class RedisKeyUtil {

    /**
     * redis中的key命名习惯用冒号间隔，相当于平时的下划线
     */
    private static final String SPLIT = ":";

    /**
     *  key的前缀,记录实体收到的赞
     */
    private static final String PREFIX_ENTITY_LIKE = "like:entity";

    /**
     * key前缀,用于记录某一用户收到的赞
     */
    private static final String PREFIX_USER_LIKE = "like:user";

    /**
     * 某个实体的赞
     * key：like:entity:entityType:entityId
     * 用set集合存，存userId，方便后序知道谁点赞，和统计点赞数
     * @param entityType
     * @param entityId
     * @return
     */
    public static String getEntityLikeKey(int entityType,int entityId){
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    /**
     * 某个用户收到的赞
     * like:user:userId -> int
     * @param userId
     * @return
     */
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE + SPLIT + userId;
    }

}
