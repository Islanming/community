package com.example.community.controller;

import com.example.community.entity.User;
import com.example.community.service.UserService;
import com.example.community.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * 登录注册
 * @author Lenovo
 */
@Controller
public class LoginController implements CommunityConstant {

    @Autowired
    private UserService userService;

    /**
     * 跳转到注册页面
     * @return
     */
    @RequestMapping(value = "/register",method = RequestMethod.GET)
    public String getRegisterPage(){
        return "/site/register";
    }

    /**
     * 跳转到登录页面
     * @return
     */
    @RequestMapping(value = "/login",method = RequestMethod.GET)
    public String getLoginPage(){
        return "/site/login";
    }

    /**
     * 注册处理
     * springMvc会把参数中的user加入到model中，前端可以用user.xxx来引用数据
     * @param model
     * @param user
     * @return
     */
    @RequestMapping(value = "/register",method = RequestMethod.POST)
    public String register(Model model, User user){
        Map<String,Object> map = userService.register(user);
        if(map == null || map.isEmpty()){
            model.addAttribute("msg","注册成功，我们已经向你的邮箱发送了一封激活邮件，请尽快打开邮件激活！");
            model.addAttribute("target","/index");
            return "/site/operate-result";
        }else {
            model.addAttribute("usernameMsg",map.get("usernameMsg"));
            model.addAttribute("passwordMsg",map.get("passwordMsg"));
            model.addAttribute("emailMsg",map.get("emailMsg"));
            return "/site/register";
        }

    }

    /**
     * 激活连接：http://localhost:8080/community/activation/101/code
     * @param model
     * @param userId
     * @param code
     * @return
     */
    @RequestMapping(path = "/activation/{userId}/{code}",method = RequestMethod.GET)
    public String activation(Model model,@PathVariable("userId") int userId,@PathVariable("code") String code){
        int activation = userService.activation(userId, code);
        if(activation == ACTIVATION_SUCCESS){
            model.addAttribute("msg","激活成功，你的账号已经可以正常使用了！");
            model.addAttribute("target","/login");
        }else if(activation == ACTIVATION_REPEAT){
            model.addAttribute("msg","无效操作，该账号已经激活过了！");
            model.addAttribute("target","/index");
        }else{
            model.addAttribute("msg","激活失败，您提供的激活码不正确！");
            model.addAttribute("target","/index");
        }
        return "/site/operate-result";
    }



}
