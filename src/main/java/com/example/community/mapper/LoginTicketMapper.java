package com.example.community.mapper;

import com.example.community.entity.LoginTicket;
import org.apache.ibatis.annotations.*;

@Mapper
@Deprecated
public interface LoginTicketMapper {

    /**
     *增加登录信息
     * @param loginTicket
     * @return
     */
    @Insert({
            "insert into login_ticket(user_id, ticket, status, expired) " +
                    "values (#{userId},#{ticket},#{status},#{expired})",
    })
    @Options(useGeneratedKeys = true,keyProperty = "id")
    int insertLoginTicket(LoginTicket loginTicket);

    /**
     * 根据登录凭证查询登录记录
     * @param ticket
     * @return
     */
    @Select({"select id, user_id, ticket, status, expired " +
            "from login_ticket " +
            "where ticket = #{ticket}"})
    LoginTicket selectByTicket(String ticket);

    /**
     * 根据登录凭证修改登录状态
     * @param ticket
     * @param status
     * @return
     */
    @Update({"update login_ticket set status=#{status} " +
            "where ticket = #{ticket}"})
    int updateStatus(String ticket,int status);


}
