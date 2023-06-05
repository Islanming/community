package com.example.community.mapper;

import com.example.community.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Lenovo
 */
@Mapper
public interface UserMapper {
    /**
     * 根据id查询用户信息
     * @param id
     * @return
     */
    User selectById(int id);

    /**
     * 根据用户名查询用户信息
     * @param username
     * @return
     */
    User selectByName(String username);

    /**
     * 根据邮箱查询用户信息
     * @param email
     * @return
     */
    User selectByEmail(String email);

    /**
     * 插入用户信息
     * @param user
     * @return
     */
    int insertUser(User user);

    /**
     * 根据id更新用户状态
     * @param id
     * @param status
     * @return
     */
    int updateStatus(int id,int status);

    /**
     * 根据id更改用户头像
     * @param id
     * @param headerUrl
     * @return
     */
    int updateHeader(int id,String headerUrl);

    /**
     *根据id更改用户密码
     * @param id
     * @param password
     * @return
     */
    int updatePassword(int id,String password);

}
