package com.example.community.service;

import com.example.community.entity.DiscussPost;
import com.example.community.mapper.DiscussPostMapper;
import com.example.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @author Lenovo
 */
@Service
public class DiscussPostService {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<DiscussPost> findDiscussPosts(int userId,int offset,int limit){
        return discussPostMapper.selectDiscussPosts(userId,offset,limit);
    }

    public int findDiscussPostRows(int userId){
        return discussPostMapper.selectDiscussPostRows(userId);
    }

    public int addDiscussPost(DiscussPost post){
        //判空处理
        if(post == null){
            throw new IllegalArgumentException("参数不能为空！");
        }

        //转义HTML标签,防止显示到浏览器出现显示错误，只需要处理标题和内容
        post.setTitle(HtmlUtils.htmlEscape(post.getTitle()));
        post.setContent(HtmlUtils.htmlEscape(post.getContent()));

        //过滤敏感词,只需要处理标题和内容
        post.setTitle(sensitiveFilter.filter(post.getTitle()));
        post.setContent(sensitiveFilter.filter(post.getContent()));

        return discussPostMapper.insertDiscussPost(post);
    }


    /**
     * 根据id查询帖子
     * @param id
     * @return
     */
    public DiscussPost findDiscussPostById(int id){
        return discussPostMapper.selectDiscussPostById(id);
    }

    /**
     * 修改评论数量，用于增加评论后修改对应帖子的评论数
     * @param id
     * @param commentCount
     * @return
     */
    public int updateCommentCount(int id,int commentCount){
        return discussPostMapper.updateCommentCount(id, commentCount);
    }

    public int updateType(int id,int type){
        return discussPostMapper.updateType(id, type);
    }

    public int updateStatus(int id,int status){
        return discussPostMapper.updateStatus(id, status);
    }

}
