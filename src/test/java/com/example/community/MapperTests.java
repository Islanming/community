package com.example.community;

import com.example.community.entity.*;
import com.example.community.mapper.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MapperTests {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private CommentMapper commentMapper;
    @Autowired
    private MessageMapper messageMapper;

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

    @Test
    public void testInsertLoginTicket(){
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(101);
        loginTicket.setTicket("abcd");
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis()+1000*60*10));

        loginTicketMapper.insertLoginTicket(loginTicket);
    }

    @Test
    public void testSelectByTicket(){
        String ticket = "abcd";
        LoginTicket loginTicket = loginTicketMapper.selectByTicket(ticket);
        System.out.println(loginTicket);
    }

    @Test
    public void testUpdateStatus(){
        String ticket = "abcd";
        loginTicketMapper.updateStatus(ticket,1);
    }

    @Test
    public void testInsertDiscussPost(){
        DiscussPost discussPost = new DiscussPost();
        discussPost.setUserId(162);
        discussPost.setTitle("测试");
        discussPost.setContent("测试的内容");
        discussPost.setType(1);
        discussPost.setStatus(1);
        discussPost.setCreateTime(new Date());
        discussPost.setCommentCount(999);
        discussPost.setScore(100);
        discussPostMapper.insertDiscussPost(discussPost);
        System.out.println(discussPost);
    }

    @Test
    public void testSelectCommentsByEntity(){
        List<Comment> comments = commentMapper.selectCommentsByEntity(1, 228, 0, 99);
        System.out.println(comments);

    }

    @Test
    public void testSelectLetters(){
        List<Message> list = messageMapper.selectConversations(111, 0, 20);
        for (Message message:list) {
            System.out.println(message);
        }
        System.out.println(messageMapper.selectConversationCount(111));

        List<Message> list1 = messageMapper.selectLetters("111_112", 0, 99);
        for (Message message:list1) {
            System.out.println(message);
        }
        System.out.println(messageMapper.selectLetterCount("111_112"));

        System.out.println(messageMapper.selectLetterUnreadCount(111,null));
        System.out.println(messageMapper.selectLetterUnreadCount(111,"111_112"));
    }
}
