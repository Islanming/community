package com.example.community.controller;

import com.example.community.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Date;

/**
 * 数据统计
 * @author Lenovo
 */
@Controller
public class DateController {

    @Autowired
    private DataService dataService;

    /**
     * 打开数据统计页面
     * 请求方式设置post也可以请求是为了方便post请求的转发
     * @return
     */
    @RequestMapping(path = "/data",method = {RequestMethod.GET,RequestMethod.POST})
    public String getDataPage(){
        return "/site/admin/data";
    }

    /**
     * 统计网站的UV
     * @param start
     * @param end
     * @param model
     * @return
     */
    @RequestMapping(path = "/data/uv",method = RequestMethod.POST)
    public String getUV(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd")Date end, Model model){
        long uv = dataService.calculateUV(start,end);
        model.addAttribute("uvResult",uv);
        model.addAttribute("uvStartDate",start);
        model.addAttribute("uvEndDate",end);
        // 转发，如果/date中还有需要的逻辑时则可以复用，直接返回模板则不能用到
        return "forward:/data";
    }


    /**
     * 统计活跃用户
     * @param start
     * @param end
     * @param model
     * @return
     */
    @RequestMapping(path = "/data/dau",method = RequestMethod.POST)
    public String getDAU(@DateTimeFormat(pattern = "yyyy-MM-dd") Date start,
                        @DateTimeFormat(pattern = "yyyy-MM-dd")Date end, Model model){
        long dau = dataService.calculateDAU(start,end);
        model.addAttribute("dauResult",dau);
        model.addAttribute("dauStartDate",start);
        model.addAttribute("dauEndDate",end);
        // 转发，如果/date中还有需要的逻辑时则可以复用，直接返回模板则不能用到
        return "forward:/data";
    }


}
