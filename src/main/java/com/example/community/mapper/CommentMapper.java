package com.example.community.mapper;

import com.example.community.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Lenovo
 */
@Mapper
public interface CommentMapper {


    List<Comment> selectCommentsByEntity(int entityType,int entityId,int offset,int limit);

    int selectCommentCountByEntity(int entityType,int entityId);


}
