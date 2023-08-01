package com.example.community.service;

import com.example.community.entity.LoginTicket;
import com.example.community.entity.User;
import com.example.community.mapper.LoginTicketMapper;
import com.example.community.mapper.UserMapper;
import com.example.community.util.CommunityConstant;
import com.example.community.util.CommunityUtil;
import com.example.community.util.MailClient;
import com.example.community.util.RedisKeyUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserMapper userMapper;

    /**
     * 注入邮件客户端
     */
    @Autowired
    private MailClient mailClient;

    /**
     * 注入模板引擎
     */
    @Autowired
    private TemplateEngine templateEngine;

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 域名
     */
    @Value("${community.path.domain}")
    private String domain;

    /**
     * 项目名
     */
    @Value("${server.servlet.context-path}")
    private String contextPath;


    public User findUserById(int id){
//        return userMapper.selectById(id);
        // 优先从缓存找
        User user = getCache(id);
        if(user == null){
            // 找不到则进行初始化
            user = initCache(id);
        }
        return user;
    }

    /**
     * 注册
     * @param user
     * @return
     */
    public Map<String,Object> register(User user){
        Map<String,Object> map = new HashMap<>();

        //空值处理
        if(user==null){
            throw new IllegalArgumentException("参数不能为空！");
        }
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg","账号不能为空");
            return map;
        }
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg","密码不能为空！");
            return map;
        }
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg","邮箱不能为空！");
            return map;
        }

        //验证账号
         if(userMapper.selectByName(user.getUsername())!=null){
             map.put("usernameMsg","该账号已存在！");
             return map;
         }

        //验证邮箱
        if(userMapper.selectByEmail(user.getEmail())!=null){
            map.put("emailMsg","该邮箱已被注册！");
            return map;
        }

        //注册用户
        //设置加在密码后面的随机码，5位就行
        user.setSalt(CommunityUtil.generateUUID().substring(0,5));
        user.setPassword(CommunityUtil.md5(user.getPassword()+user.getSalt()));
        user.setType(0);
        user.setStatus(0);
        user.setActivationCode(CommunityUtil.generateUUID());
        user.setHeaderUrl(String.format("http://image.nowcader.com/head/%dt.png",new Random().nextInt(1000)));
        user.setCreateTime(new Date());
        userMapper.insertUser(user);

        //发送激活邮件
        Context context = new Context();
        context.setVariable("email",user.getEmail());
        //激活连接,http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" +user.getActivationCode();
        context.setVariable("url",url);
        String content = templateEngine.process("/mail/activation",context);
        mailClient.sendMail(user.getEmail(),"激活账号",content);

        return map;
    }

    /**
     * 激活账号
     * @param userId 用户id
     * @param code 激活码
     * @return
     */
    public int activation(int userId, String code){
        User user = userMapper.selectById(userId);
        if(user.getStatus()==1){
            return ACTIVATION_REPEAT;
        }
        if(user.getActivationCode().equals(code)){
            userMapper.updateStatus(userId,1);
            //修改了用户数据则清空缓存
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        }
        return ACTIVATION_FAIL;
    }


    /**
     * 登录时的用户名，密码的验证及设置登录凭证的过期时间
     * @param username
     * @param password
     * @param expiredSeconds
     * @return
     */
    public Map<String,Object> login(String username,String password,int expiredSeconds){
        Map<String,Object> map = new HashMap<>();

        //空值判断
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg","账号不能为空！");
            return map;
        }
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg","密码不能为空！");
            return map;
        }

        //验证账号
        User user = userMapper.selectByName(username);
        if(user == null){
            map.put("usernameMsg","该账号不存在！");
            return map;
        }

        //验证账号激活状态
        if(user.getStatus()==0){
            map.put("usernameMsg","该账号未激活！");
            return map;
        }

        //验证密码
        String key = CommunityUtil.md5(password+user.getSalt());
        if(!user.getPassword().equals(key)){
            map.put("passwordMsg","账号或密码错误！");
            return map;
        }

        //生成登录凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
//        loginTicketMapper.insertLoginTicket(loginTicket);

        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        redisTemplate.opsForValue().set(redisKey,loginTicket);

        map.put("ticket",loginTicket.getTicket());

        return map;
    }

    /**
     * 将登录凭证状态修改为1，即失效，退出登录状态
     * @param ticket
     */
    public void logout(String ticket){
//        loginTicketMapper.updateStatus(ticket,1);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        loginTicket.setStatus(1);
        redisTemplate.opsForValue().set(redisKey,loginTicket);
    }

    /**
     * 查询登录信息
     * @param ticket
     * @return
     */
    public LoginTicket findLoginTicket(String ticket){
//        return loginTicketMapper.selectByTicket(ticket);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        return loginTicket;
    }

    /**
     * 更改用户头像URL
     * @param userId
     * @param headerUrl
     * @return
     */
    public int updateHeader(int userId,String headerUrl){
//        return userMapper.updateHeader(userId,headerUrl);
        int i = userMapper.updateHeader(userId, headerUrl);
        //修改了用户数据则清空缓存
        clearCache(userId);
        return i;
    }

    /**
     * 修改用户密码
     * @param userId
     * @param password
     * @return
     */
    public int updatePassword(int userId,String password){
        return userMapper.updatePassword(userId,password);
    }

    public User findUserByName(String username){
        return userMapper.selectByName(username);
    }

    /**
     * 优先从缓存中取值
     * @param userId
     * @return
     */
    private User getCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    /**
     * 取不到时，初始化缓存数据
     * @param userId
     * @return
     */
    private User initCache(int userId){
        User user = userMapper.selectById(userId);
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.opsForValue().set(redisKey,user,3600, TimeUnit.SECONDS);
        return user;
    }

    /**
     * 数据变更时清除缓存数据
     * @param userId
     */
    private void clearCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        redisTemplate.delete(redisKey);
    }

    /**
     * 获取用户权限
     * @param userId
     * @return
     */
    public Collection<? extends GrantedAuthority> getAuthority(int userId){
        User user = this.findUserById(userId);

        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()){
                    case 1:
                        return AOTHORITY_ADMIN;
                    case 2:
                        return AOTHORITY_MODERATOR;
                    default:
                        return AOTHORITY_USER;
                }
            }
        });
        return list;
    }

//    /**
//     * @Async 注解可以让该方法在多线程的环境下，被异步调用
//     */
//    @Async
//    public void execute1(){
//        logger.debug("execute1");
//    }
//
//    /**
//     * @Scheduled 注解可以让该方法在多线程的环境下，被定时地异步调用
//     */
//    @Scheduled(initialDelay = 10000,fixedDelay = 1000)
//    public void execute2(){
//        logger.debug("execute2");
//    }

}
