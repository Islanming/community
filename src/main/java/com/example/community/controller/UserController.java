package com.example.community.controller;

import com.example.community.annotation.LoginRequired;
import com.example.community.entity.User;
import com.example.community.service.FollowService;
import com.example.community.service.LikeService;
import com.example.community.service.UserService;
import com.example.community.util.CommunityConstant;
import com.example.community.util.CommunityUtil;
import com.example.community.util.HostHolder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.domain}")
    private String domain;

    @Value("${community.path.upload}")
    private String uploadPath;

    @Value("${server.servlet.context-path}")
    private String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    /**
     * 访问用户信息设置页面
     * @return
     */
    @LoginRequired
    @RequestMapping(path = "/setting",method = RequestMethod.GET)
    public String getSettingPage(){
        return "/site/setting";
    }

    /**
     * 上传并修改用户头像，并储存到服务器
     * @param headerImage
     * @param model
     * @return
     */
    @LoginRequired
    @RequestMapping(path = "/upload",method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model){
        //空值处理
        if(headerImage == null){
            model.addAttribute("error","您还没有选择图片！");
            return "/site/setting";
        }

        //截取文件格式并判断
        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if(StringUtils.isBlank(suffix)){
            model.addAttribute("error","文件格式不正确！");
            return "/site/setting";
        }

        //生成随机文件名
        fileName = CommunityUtil.generateUUID() + suffix;

        //确定文件存放路径
        File dest = new File(uploadPath + "/" +fileName);
        try {
            //存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败："+e.getMessage());
            throw new RuntimeException("上传文件失败，服务器发生异常！");
        }

        // 更新当前用户头像的路径（web访问路径）
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(),headerUrl);

        return "redirect:/index";
    }


    /**
     * 获取用户头像
     * @param fileName
     * @param response
     */
    @RequestMapping(path = "/header/{fileName}",method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response){
        //服务器存放路径
        fileName = uploadPath + "/" + fileName;

        //文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));

        //响应图片
        response.setContentType("image/"+suffix);
        try (
                //java7语法，相当于最后会在finally中关闭流（如果需要的话）
                OutputStream outputStream = response.getOutputStream();
                FileInputStream fileInputStream = new FileInputStream(fileName);
                ){

            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fileInputStream.read(buffer)) != -1){
                outputStream.write(buffer,0,b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败："+ e.getMessage());
        }

    }

    /**
     * 修改用户密码
     * @param oldPassword
     * @param newPassword
     * @param model
     * @return
     */
    @LoginRequired
    @RequestMapping(path = "/change",method = RequestMethod.POST)
    public String updatePassword(String oldPassword,String newPassword,Model model){
        //空值处理
        if(StringUtils.isBlank(oldPassword)){
            model.addAttribute("error","原密码不能为空！");
            return "/site/setting";
        }
        if(StringUtils.isBlank(newPassword)){
            model.addAttribute("error","新密码不能为空！");
            return "/site/setting";
        }

        //判断新旧密码是否重复
        if(oldPassword.equals(newPassword)){
            model.addAttribute("error","新密码与原密码重复！");
            return "/site/setting";
        }

        //判断旧密码是否正确
        User user = hostHolder.getUser();
        String op = CommunityUtil.md5(oldPassword+user.getSalt());
        if(!op.equals(user.getPassword())){
            model.addAttribute("error","原密码错误，请重新输入！");
            return "/site/setting";
        }

        //修改密码
        String np = CommunityUtil.md5(newPassword+user.getSalt());
        userService.updatePassword(user.getId(),np);

        return "redirect:/index";

    }

    /**
     * 个人主页
     * @param userId
     * @param model
     * @return
     */
    @RequestMapping(path = "/profile/{userId}",method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId,Model model){
        User user = userService.findUserById(userId);
        if(user == null){
            throw new RuntimeException("该用户不存在！");
        }

        // 用户
        model.addAttribute("user",user);

        //点赞数量
        int likeCount = likeService.findUserLikeCount(userId);
        model.addAttribute("likeCount",likeCount);

        //查询关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount",followeeCount);

        //粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount",followerCount);

        //是否已关注
        boolean hasFollowed = false;
        if(hostHolder.getUser()!=null){
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(),ENTITY_TYPE_USER,userId);
        }
        model.addAttribute("hasFollowed",hasFollowed);

        return "/site/profile";
    }



}
