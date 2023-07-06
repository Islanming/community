package com.example.community.mapper;

import com.example.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Lenovo
 */
@Mapper
public interface CommentMapper {


    /**
     * 根据实体类型和实体id查询评论，并进行分页
     * @param entityType
     * @param entityId
     * @param offset
     * @param limit
     * @return
     */
    List<Comment> selectCommentsByEntity(int entityType,int entityId,int offset,int limit);

    /**
     * 根据实体类型和实体id查询评论的数量
     * @param entityType
     * @param entityId
     * @return
     */
    int selectCommentCountByEntity(int entityType,int entityId);

    /**
     * 添加评论
     * @param comment
     * @return
     */
    int insertComment(Comment comment);
}
