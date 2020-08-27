package com.yuan.community.service;

import com.yuan.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    // 点赞
    public void like(int userId, int entityType, int entityId, int entityUserId) {
//        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
//        // 判断该用户是否已点过赞
//        boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
//        if (isMember) {
//            redisTemplate.opsForSet().remove(entityLikeKey, userId);
//        } else {
//            redisTemplate.opsForSet().add(entityLikeKey, userId);
//        }
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                // 整合实体类得到点赞的redis的key值
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                // 整合用户得到点赞的redis的key值
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                // 判断当前用户是否对这个实体点过赞
                boolean isMember = operations.opsForSet().isMember(entityLikeKey, userId);

                // redis进入事务，redis会进入阻塞状态，不再响应任何别的客户端的请求，直到发出multi命令的客户端再发出exec命令为止
                operations.multi();

                if (isMember) {
                    // 若用户已点过赞，那么说明此次操作为取消赞操作，在赞集合中移除该项数据
                    operations.opsForSet().remove(entityLikeKey, userId);
                    // 实体所属用户的获赞量减一
                    operations.opsForValue().decrement(userLikeKey);
                } else {
                    operations.opsForSet().add(entityLikeKey, userId);
                    operations.opsForValue().increment(userLikeKey);
                    operations.opsForValue().get(userLikeKey);
                }

                return operations.exec();
            }
        });
    }

    // 查询某实体被点赞的数量
    public long findEntityLikeCount(int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }

    // 查询某人对某实体的点赞状态
    public int findEntityLikeStatus(int userId, int entityType, int entityId) {
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }

    // 查询某个用户获得的赞
    public int findUserLikeCount(int userId) {
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count.intValue();
    }

}
