package com.example.community.service;

import com.example.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 点赞业务
 * @author Lenovo
 */
@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 点赞
     * @param userId
     * @param entityType
     * @param entityId
     */
    public void like(int userId,int entityType,int entityId){
        // 生成key
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);

        // 判断是否已点赞
        Boolean isMenber = redisTemplate.opsForSet().isMember(entityLikeKey, userId);

        if(isMenber){
            // 已点赞则取消点赞，将对应userId从set中删除
            redisTemplate.opsForSet().remove(entityLikeKey,userId);
        } else {
            // 未点赞则进行点赞，将对应userId添加到set中
            redisTemplate.opsForSet().add(entityLikeKey,userId);
        }

    }

    /**
     * 查询某实体点赞的数量
     * @return
     */
    public long findEntityLikeCount(int entityType,int entityId){
        // 生成key
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    /**
     * 查询某人对某实体的点赞状态
     * @param userId
     * @param entityType
     * @param entityId
     * @return 返回int类型是为了方便后序拓展，如添加 踩 状态
     */
    public int findEntityLikeStatus(int userId,int entityType,int entityId){
        // 生成key
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType,entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey,userId)?1:0;
    }

}
