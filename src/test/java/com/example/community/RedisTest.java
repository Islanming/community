package com.example.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testRedis(){

        //测试字符串类型
        String redisStringKey = "test:count";
        ValueOperations operations = redisTemplate.opsForValue();
        operations.set(redisStringKey,1);
        System.out.println("==========String==========");
        System.out.println(operations.get(redisStringKey));
        System.out.println(operations.increment(redisStringKey));
        System.out.println(operations.decrement(redisStringKey));
        System.out.println();

        //测试hash类型
        String redisHashKey = "test:user";

        HashOperations hashOperations = redisTemplate.opsForHash();
        hashOperations.put(redisHashKey,"id",1);
        hashOperations.put(redisHashKey,"username","ming");
        System.out.println("==========Hash==========");
        System.out.println(hashOperations.get(redisHashKey,"id"));
        System.out.println(hashOperations.get(redisHashKey,"username"));
        System.out.println();

        //测试列表类型
        String redisListKey = "test:ids";
        ListOperations listOperations = redisTemplate.opsForList();
        listOperations.leftPush(redisListKey,111);
        listOperations.leftPush(redisListKey,112);
        listOperations.leftPush(redisListKey,113);

        System.out.println("==========List==========");
        System.out.println(listOperations.size(redisListKey));
        System.out.println(listOperations.index(redisListKey,1));
        System.out.println(listOperations.range(redisListKey,0,2));

        System.out.println(listOperations.leftPop(redisListKey));
        System.out.println(listOperations.leftPop(redisListKey));
        System.out.println(listOperations.leftPop(redisListKey));
        System.out.println();

        //测试集合类型
        String redisSetKey = "test:teachers";
        SetOperations setOperations = redisTemplate.opsForSet();
        setOperations.add(redisSetKey,"刘备","关羽","张飞","周瑜","诸葛亮","孙权");
        System.out.println("==========Sets==========");
        System.out.println(setOperations.size(redisSetKey));
        System.out.println(setOperations.pop(redisSetKey));
        System.out.println(setOperations.members(redisSetKey));
        System.out.println();

        //测试有序集合类型
        String redisSortedSetKey = "test:students";
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
        zSetOperations.add(redisSortedSetKey,"悟空",99);
        zSetOperations.add(redisSortedSetKey,"八戒",88);
        zSetOperations.add(redisSortedSetKey,"悟净",80);
        zSetOperations.add(redisSortedSetKey,"唐僧",100);
        System.out.println();

        System.out.println("===========zSet==========");
        System.out.println(zSetOperations.zCard(redisSortedSetKey));
        System.out.println(zSetOperations.score(redisSortedSetKey,"悟空"));
        System.out.println(zSetOperations.reverseRank(redisSortedSetKey,"悟空"));
        System.out.println(zSetOperations.range(redisSortedSetKey,0,2));
        System.out.println(zSetOperations.reverseRange(redisSortedSetKey,0,2));
        System.out.println();

        //测试公用api
        redisTemplate.delete("test:user");

        System.out.println("==========test==========");

        redisTemplate.expire("test:students",2, TimeUnit.MINUTES);


    }

    //多次访问同一个key简化方法
    @Test
    public void testBoundOperation(){
        String redisStringKey = "test:count";
        BoundValueOperations boundValueOperations = redisTemplate.boundValueOps(redisStringKey);
        boundValueOperations.set(2);
        boundValueOperations.increment();
        boundValueOperations.increment();
        boundValueOperations.increment();
        boundValueOperations.increment();
        boundValueOperations.increment();
        boundValueOperations.decrement();
        System.out.println(boundValueOperations.get());
    }

    /**
     * 测试Redis的事务，一般采用编程式事务
     */
    @Test
    public void testTransaction(){
        Object obj = redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String redisKey = "test:tx";
                //事务的开始
                operations.multi();

                operations.opsForSet().add(redisKey,"zhangsan");
                operations.opsForSet().add(redisKey,"lisi");
                operations.opsForSet().add(redisKey,"wangwu");

                //为空，查询无效，因为Redis的事务是将命令放到队列里面，等到事务提交时同一执行处理，故在事务提交前查询不到
                System.out.println(operations.opsForSet().members(redisKey));
                //提交事务
                return operations.exec();

            }
        });
        System.out.println(obj);
    }

}
