package com.example.community.service;

import com.example.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
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
     * @param userId 点赞者id
     * @param entityType 被点赞实体类型
     * @param entityId 被点赞实体id
     * @param entityUserId 被点赞人
     */
    public void like(int userId,int entityType,int entityId,int entityUserId){

        //当前业务会连续两次进行更新操作，故需要保持事务性，Redis的最好用编程式事务
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                //查询要放在事务之外，否者无效;      判断是否已点赞
                boolean isMember = operations.opsForSet().isMember(entityLikeKey, userId);
                //开启事务
                operations.multi();
                if(isMember){
                    operations.opsForSet().remove(entityLikeKey,userId);
                    operations.opsForValue().decrement(userLikeKey);
                } else {
                    operations.opsForSet().add(entityLikeKey,userId);
                    operations.opsForValue().increment(userLikeKey);
                }
                //提交事务
                return operations.exec();
            }
        });

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

    /**
     * 查询某用户收到的赞
     * @param userId
     * @return
     */
    public int findUserLikeCount(int userId){
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0:count.intValue();
    }

}
