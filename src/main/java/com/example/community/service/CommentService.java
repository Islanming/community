package com.example.community.service;

import com.example.community.entity.Comment;
import com.example.community.mapper.CommentMapper;
import com.example.community.util.CommunityConstant;
import com.example.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

/**
 * @author Lenovo
 */
@Service
public class CommentService implements CommunityConstant {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostService discussPostService;

    /**
     * 根据实体类型和实体id查询评论，并进行分页
     * @param entityType
     * @param entityId
     * @param offset
     * @param limit
     * @return
     */
    public List<Comment> findCommentsByEntity(int entityType,int entityId,int offset,int limit){
        return commentMapper.selectCommentsByEntity(entityType,entityId,offset,limit);
    }

    /**
     * 根据实体类型和实体id查询评论的数量
     * @param entityType
     * @param entityId
     * @return
     */
    public int findCommentCountByEntity(int entityType,int entityId){
        return commentMapper.selectCommentCountByEntity(entityType,entityId);
    }

    /**
     * 添加评论
     * @param comment
     * @return
     */
    @Transactional(isolation = Isolation.READ_COMMITTED,propagation = Propagation.REQUIRED,rollbackFor = Exception.class)
    public int addComment(Comment comment){
        if(comment==null){
            throw new IllegalArgumentException("参数不能为空！");
        }

        //添加评论
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        int rows = commentMapper.insertComment(comment);

        //更新帖子评论数量
        if(comment.getEntityType()==ENTITY_TYPE_POST){
            int count = commentMapper.selectCommentCountByEntity(comment.getEntityType(), comment.getId());
            discussPostService.updateCommentCount(comment.getUserId(),count);
        }

        return rows;

    }

    public Comment findCommentById(int id){
        return commentMapper.selectCommentById(id);
    }

}
