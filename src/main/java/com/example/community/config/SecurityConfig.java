package com.example.community.config;

import com.example.community.util.CommunityConstant;
import com.example.community.util.CommunityUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Lenovo
 */
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter implements CommunityConstant {

    @Override
    public void configure(WebSecurity webSecurity) throws Exception{
        // 设置忽略静态资源的访问，不需要拦截
        webSecurity.ignoring().antMatchers("/resources/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        // 授权
        http.authorizeRequests()
                .antMatchers(
                        "/user/setting",
                        "/user/upload",
                        "/discuss/add",
                        "/comment/add/**",
                        "/letter/**",
                        "/notice/**",
                        "/like",
                        "/follow",
                        "/unfollow"
                )
                .hasAnyAuthority(
                        AOTHORITY_USER,
                        AOTHORITY_ADMIN,
                        AOTHORITY_MODERATOR
                )
                .antMatchers(
                        "/discuss/top",
                        "/discuss/wonderful"
                )
                .hasAnyAuthority(
                        AOTHORITY_MODERATOR
                )
                .antMatchers(
                        "/discuss/delete",
                        "/data/**"
                )
                .hasAnyAuthority(
                        AOTHORITY_ADMIN
                )
                .anyRequest().permitAll()
                        // 不使用CSRF授权
                        .and().csrf().disable();

        // 权限不够配置
        http.exceptionHandling()
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    // 没有登录时的处理
                    @Override
                    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
                        String xRequestHeader = request.getHeader("x-requested-with");
                        if("XMLHttpRequest".equals(xRequestHeader)){
                            //异步请求,返回一个普通字符串，需要前端用$.Json()方法进行转换
                            response.setContentType("application/plain;charset=utf-8");
                            //输出流
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403,"你还没有登录！"));
                        } else {
                            // 非异步请求，重定向到登录页面
                            response.sendRedirect(request.getContextPath()+"/login");
                        }
                    }
                })
                .accessDeniedHandler(new AccessDeniedHandler() {
                    // 权限不足时的处理
                    @Override
                    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
                        String xRequestHeader = request.getHeader("x-requested-with");
                        if("XMLHttpRequest".equals(xRequestHeader)){
                            //异步请求,返回一个普通字符串，需要前端用$.Json()方法进行转换
                            response.setContentType("application/plain;charset=utf-8");
                            //输出流
                            PrintWriter writer = response.getWriter();
                            writer.write(CommunityUtil.getJSONString(403,"你没有访问此功能的权限！"));
                        } else {
                            // 非异步请求，重定向相应错误页面（404）
                            response.sendRedirect(request.getContextPath()+"/denied");
                        }
                    }
                });

        // Security 底层默认会拦截/logout请求，进行退出处理
        // 覆盖它默认的逻辑，才能执行我们自己的退出代码
        // 改其拦截的退出登录的路径为一个不存在的页面就行
        http.logout().logoutUrl("/securitylogout");

    }
}
