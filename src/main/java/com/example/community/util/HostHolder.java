package com.example.community.util;

import com.example.community.entity.User;
import org.springframework.stereotype.Component;

/**
 * 持有用户信息，用于替代session对象
 * @author Lenovo
 */
@Component
public class HostHolder {

    private ThreadLocal<User> users = new ThreadLocal<>();

    public void setUser(User user){
        users.set(user);
    }

    public User getUser(){
        return users.get();
    }

    public void clear(){
        users.remove();
    }
}
