package com.example.community.controller;

import com.example.community.annotation.LoginRequired;
import com.example.community.entity.User;
import com.example.community.service.FollowService;
import com.example.community.util.CommunityUtil;
import com.example.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class FollowController {

    @Autowired
    private FollowService followService;

    @Autowired
    private HostHolder hostHolder;

    /**
     * 关注
     * @param entityType
     * @param entityId
     * @return
     */
    @RequestMapping(path = "/follow",method = RequestMethod.POST)
    @ResponseBody
    @LoginRequired
    public String follow(int entityType,int entityId){
        User user = hostHolder.getUser();

        followService.follow(user.getId(),entityType,entityId);

        return CommunityUtil.getJSONString(0,"关注成功!");
    }


    /**
     * 取消关注
     * @param entityType
     * @param entityId
     * @return
     */
    @RequestMapping(path = "/unfollow",method = RequestMethod.POST)
    @ResponseBody
    @LoginRequired
    public String unfollow(int entityType,int entityId){
        User user = hostHolder.getUser();

        followService.unfollow(user.getId(),entityType,entityId);

        return CommunityUtil.getJSONString(0,"取消关注成功!");
    }



}
