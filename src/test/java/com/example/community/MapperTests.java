package com.example.community;

import com.example.community.entity.DiscussPost;
import com.example.community.entity.User;
import com.example.community.mapper.DiscussPostMapper;
import com.example.community.mapper.UserMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Test
    public void testSelectUser(){
        User user = userMapper.selectById(101);
        System.out.println(user);

        User user1 = userMapper.selectByName("bbb");
        System.out.println(user1);

        User user2 = userMapper.selectByEmail("nowcoder117@sina.com");
        System.out.println(user2);
    }

    @Test
    public void testInsertUser(){
        User user = new User();
        user.setUsername("铭哥");
        int i = userMapper.insertUser(user);
        System.out.println(i);
        System.out.println(user.getId());
    }

    @Test
    public void testUpdateUser(){
        int i = userMapper.updateStatus(150, 1);
        int i1 = userMapper.updateHeader(150, "莫得");
        int i2 = userMapper.updatePassword(150, "123456");
        System.out.println(i+i1+i2);
    }

    @Test
    public void testSelectDiscussPost(){
        List<DiscussPost> discussPosts = discussPostMapper.selectDiscussPosts(0, 0, 10);
        for (DiscussPost discussPost: discussPosts) {
            System.out.println(discussPost);
        }

        int postRows = discussPostMapper.selectDiscussPostRows(0);
        System.out.println(postRows);
    }
}
