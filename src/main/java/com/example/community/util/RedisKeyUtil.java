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
     * key前缀,验证码
     */
    private static final String PREFIX_KAPTCHA = "kaptcha";

    /**
     * key前缀，登录凭证
     */
    private static final String PREFIX_TICKET = "ticket";

    /**
     *  key前缀，用户信息
     */
    private static final String PREFIX_USER = "user";

    /**
     *  key前缀，独立访客
     */
    private static final String PREFIX_UV = "uv";

    /**
     *  key前缀，日活跃用户
     */
    private static final String PREFIX_DAU = "dau";

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


    /**
     * 登录的验证码
     * @param owner 随机生成的字符串，用于标识未登录前的某用户
     * @return
     */
    public static String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    /**
     * 登录凭证
     * @param ticket
     * @return
     */
    public static String getTicketKey(String ticket){
        return PREFIX_TICKET + SPLIT + ticket;
    }

    /**
     * 用户信息
     * @param userId
     * @return
     */
    public static String getUserKey(int userId){
        return PREFIX_USER + SPLIT + userId;
    }

    /**
     * 单日uv
     * @return
     */
    public static String getUVKey(String date){
        return PREFIX_UV + SPLIT + date;
    }

    /**
     * 区间uv
     * @return
     */
    public static String getUVKey(String startDate,String endDate){
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    /**
     * 单日活跃用户
     * @return
     */
    public static String getDAUKey(String date){
        return PREFIX_DAU + SPLIT + date;
    }

    /**
     * 区间活跃用户
     * @return
     */
    public static String getDAUKey(String startDate,String endDate){
        return PREFIX_DAU + SPLIT + startDate + SPLIT + endDate;
    }

}
