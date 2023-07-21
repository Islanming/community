package com.example.community.mapper;

import com.example.community.entity.Message;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Lenovo
 */
@Mapper
public interface MessageMapper {

    /**
     * 查询当前用户的会话列表，针对每一个会话的最新一条私信，支持分页
     * @param userId
     * @param offset
     * @param limit
     * @return
     */
    List<Message> selectConversations(int userId,int offset,int limit);

    /**
     * 查询当前用户的会话数量
     * @param userId
     * @return
     */
    int selectConversationCount(int userId);

    /**
     * 查询某个会话包含的私信列表
     * @param conversationId
     * @param offset
     * @param limit
     * @return
     */
    List<Message> selectLetters(String conversationId,int offset,int limit);

    /**
     * 查询某个会话的私信数量
     * @param conversationId
     * @return
     */
    int selectLetterCount(String conversationId);

    /**
     * 查询未读的私信数量，可以查用户未读，也可查某会话未读，可根据conversationId的动态拼成实现
     * @param userId
     * @param conversationId
     * @return
     */
    int selectLetterUnreadCount(int userId,String conversationId);


    /**
     * 增加一条私信
     * @param message
     * @return
     */
    int insertMessage(Message message);

    /**
     * 修改状态
     * @param ids
     * @param status
     * @return
     */
    int updateStatus(List<Integer> ids,int status);

    /**
     * 查询某个主题下最新的通知
     * @param userId
     * @param topic
     * @return
     */
    Message selectLatestNotice(int userId,String topic);

    /**
     * 查询某主题所包含的通知数量
     * @param userId
     * @param topic
     * @return
     */
    int selectNoticeCount(int userId,String topic);

    /**
     * 查询某主题下的未读的通知数量
     * @param userId
     * @param topic
     * @return
     */
    int selectNoticeUnreadCount(int userId,String topic);

    /**
     * 查询某个主题的所包含的通知列表，支持分页
     * @param userId
     * @param topic
     * @param offset
     * @param limit
     * @return
     */
    List<Message> selectNotices(int userId,String topic,int offset,int limit);

}
