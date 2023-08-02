package com.example.community;

import com.example.community.entity.DiscussPost;
import com.example.community.service.DiscussPostService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class CaffeineTests {

    @Autowired
    private DiscussPostService postService;

    /**
     * 往帖子列表插入30万条数据，便于后面进行性能测试
     */
    @Test
    public void initDataForTest(){
        for (int i = 0; i < 300000; i++) {
            DiscussPost post = new DiscussPost();
            post.setUserId(111);
            post.setTitle("Caffeine测试");
            post.setContent("阿巴阿巴阿巴阿巴阿巴阿巴阿巴阿巴测试");
            post.setCreateTime(new Date());
            post.setScore(Math.random() * 2000);
            postService.addDiscussPost(post);
        }
    }

    /**
     * 测试缓存是否只要初始化一次，即只查询一次数据库
     */
    @Test
    public void testCache(){
        System.out.println(postService.findDiscussPosts(0,0,10,1));
        System.out.println(postService.findDiscussPosts(0,0,10,1));
        System.out.println(postService.findDiscussPosts(0,0,10,1));
        System.out.println(postService.findDiscussPosts(0,0,10,0));

    }



}
