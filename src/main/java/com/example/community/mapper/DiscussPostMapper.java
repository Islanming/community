package com.example.community.mapper;

import com.example.community.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper{

    /**
     * 查询对应用户的帖子,显示第offset到limit条
     * @param userId
     * @param offset 当前页的起始行
     * @param limit 末行
     * @return
     */
    List<DiscussPost> selectDiscussPosts(int userId,int offset,int limit);

    /**
     * 查询帖子的数目
     * 其中 @Param 用于给参数取别名，
     * 如果只有一个参数，且在<if>里面使用，则必须加别名,在xml文件中也要声明返回类型
     * @param userId
     * @return
     */
    int selectDiscussPostRows(@Param("userId") int userId);


    /**
     * 插入帖子
     * @param discussPost
     * @return
     */
    int insertDiscussPost(DiscussPost discussPost);


    /**
     * 根据id查询帖子
     * @param id
     * @return
     */
    DiscussPost selectDiscussPostById(int id);

}
