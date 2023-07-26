package com.example.community.util;

/**
 * 激活状态
 * @author Lenovo
 */
public interface CommunityConstant {
    /**
     * 激活成功
     */
    int ACTIVATION_SUCCESS = 0;

    /**
     * 重复激活
     */
    int ACTIVATION_REPEAT = 1;

    /**
     * 激活失败
     */
    int ACTIVATION_FAIL = 2;


    /**
     * 默认状态下的登录凭证的超时时间
     */
    int DEFAULT_EXPIRED_SECONDS = 3600 * 12;


    /**
     * 记住状态下的登录凭证的超时时间
     */
    int REMEMBER_EXPIRED_SECONDS = 3600 * 24 * 10;

    /**
     * 实体类型：帖子
     */
    int ENTITY_TYPE_POST = 1;

    /**
     * 实体类型：评论
     */
    int ENTITY_TYPE_COMMENT = 2;

    /**
     * 实体类型：用户
     */
    int ENTITY_TYPE_USER = 3;

    /**
     * 主题：评论
     */
    String TOPIC_COMMENT = "comment";

    /**
     * 主题：点赞
     */
    String TOPIC_LIKE = "like";

    /**
     * 主题：关注
     */
    String TOPIC_FOLLOW = "follow";

    /**
     * 主题：发帖
     */
    String TOPIC_PUBLISH = "publish";

    /**
     * 系统用户ID
     */
    int SYSTEM_USER_ID = 1;

    /**
     * 权限：普通用户
     */
    String AOTHORITY_USER = "user";

    /**
     * 权限：管理员
     */
    String AOTHORITY_ADMIN = "admin";

    /**
     * 权限：版主
     */
    String AOTHORITY_MODERATOR = "moderator";
}
