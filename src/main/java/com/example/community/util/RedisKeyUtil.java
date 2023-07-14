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
     * key前缀，目标（被关注的实体）
     */
    private static final String PREFIX_FOLLOWEE = "followee";

    /**
     * key前缀，粉丝（发起关注的人）
     */
    private static final String PREFIX_FOLLOWER = "follower";

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


    /**
     * 某个用户关注的实体
     * followee:userId:entityType -> zset(entityId,now)
     * @param userId
     * @param entityType
     * @return
     */
    public static String getFolloweeKey(int userId,int entityType){
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    /**
     * 某个实体拥有的粉丝
     * follower:entityType:entityId -> zset(userId,now)
     * @param entityType
     * @param entityId
     * @return
     */
    public static String getFollowerKey(int entityType,int entityId){
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }

}
