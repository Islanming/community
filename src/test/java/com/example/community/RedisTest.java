package com.example.community;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
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

    /**
     * 测试redis的HyperLoglog数据类型
     * 统计20万个重复数据的独立总数
     */
    @Test
    public void testHyperLoglog(){
        String redisKey = "test:hll:01";

        for (int i = 0; i < 100000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey,i);
        }

        for (int i = 0; i < 100000; i++) {
            int r = (int) (Math.random()*100000+1);
            redisTemplate.opsForHyperLogLog().add(redisKey,r);
        }

        long size = redisTemplate.opsForHyperLogLog().size(redisKey);
        System.out.println(size);
    }

    /**
     *  测试redis的HyperLoglog数据类型
     *  将三组数据合并，再统计合并后的重复数据的独立总数
     */
    @Test
    public void testHyperLogUnion(){
        String redisKey2 = "test:hll:02";
        for (int i = 0; i < 10000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey2,i);
        }
        String redisKey3 = "test:hll:03";
        for (int i = 5001; i < 15000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey3,i);
        }
        String redisKey4 = "test:hll:04";
        for (int i = 10001; i < 20000; i++) {
            redisTemplate.opsForHyperLogLog().add(redisKey4,i);
        }

        String unionKey = "test:hll:union";
        redisTemplate.opsForHyperLogLog().union(unionKey,redisKey2,redisKey3,redisKey4);

        long size = redisTemplate.opsForHyperLogLog().size(unionKey);
        System.out.println(size);
    }


    /**
     * 测试redis的Bitmap数据类型
     * 统计一组数据的布尔值
     */
    @Test
    public void testBitMap(){
        String redisKey = "test:bm:01";

        // 记录
        redisTemplate.opsForValue().setBit(redisKey,1,true);
        redisTemplate.opsForValue().setBit(redisKey,4,true);
        redisTemplate.opsForValue().setBit(redisKey,7,true);

        // 查询
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey,2));

        // 统计
        Object execute = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.bitCount(redisKey.getBytes());
            }
        });

        System.out.println(execute);
    }

    /**
     * 测试redis的Bitmap数据类型
     * 统计三组数据的布尔值，并对这三组数据做or运算
     */
    @Test
    public void testBitMapOperation(){
        String k2 = "test:bm:02";
        redisTemplate.opsForValue().setBit(k2,0,true);
        redisTemplate.opsForValue().setBit(k2,1,true);
        redisTemplate.opsForValue().setBit(k2,2,true);

        String k3 = "test:bm:03";
        redisTemplate.opsForValue().setBit(k3,2,true);
        redisTemplate.opsForValue().setBit(k3,3,true);
        redisTemplate.opsForValue().setBit(k3,4,true);

        String k4 = "test:bm:04";
        redisTemplate.opsForValue().setBit(k4,4,true);
        redisTemplate.opsForValue().setBit(k4,5,true);
        redisTemplate.opsForValue().setBit(k4,6,true);

        String k = "test:bm:or";
        Object execute = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.bitOp(RedisStringCommands.BitOperation.OR,
                        k.getBytes(),k2.getBytes(),k3.getBytes(),k4.getBytes());
                return connection.bitCount(k.getBytes());
            }
        });

        System.out.println(execute);

        System.out.println(redisTemplate.opsForValue().getBit(k,0));
        System.out.println(redisTemplate.opsForValue().getBit(k,1));
        System.out.println(redisTemplate.opsForValue().getBit(k,2));
        System.out.println(redisTemplate.opsForValue().getBit(k,3));
        System.out.println(redisTemplate.opsForValue().getBit(k,4));
        System.out.println(redisTemplate.opsForValue().getBit(k,5));
        System.out.println(redisTemplate.opsForValue().getBit(k,6));
    }
}
