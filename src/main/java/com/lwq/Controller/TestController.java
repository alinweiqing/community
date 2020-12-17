package com.lwq.Controller;

import com.lwq.util.CommunityUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Controller
public class TestController {


    @RequestMapping(path = "/cookei/set",method = RequestMethod.GET)
    @ResponseBody
    public String cookei(HttpServletResponse response){
        Cookie cookie = new Cookie("code", CommunityUtil.generateUUID());
        cookie.setPath("/community");
        cookie.setMaxAge(600);
        response.addCookie(cookie);

        return "seiCookei";
    }

    @RequestMapping(path = "/cookei/get",method = RequestMethod.GET)
    @ResponseBody
    public String getcookei(@CookieValue("code") String code){
        System.out.println(code);
        return "getcookei";
    }

    @RequestMapping(path = "/session/set",method = RequestMethod.GET)
    @ResponseBody
    public String session(HttpSession session){
        session.setAttribute("id",1);
        session.setAttribute("name","session");

        return "set session";
    }

    @RequestMapping(path = "/session/get",method = RequestMethod.GET)
    @ResponseBody
    public String getsession(HttpSession session){
        System.out.println(session.getAttribute("id"));
        System.out.println(session.getAttribute("name"));

        return "get session";
    }


    //ajax示例
    @RequestMapping(path = "/ajax",method = RequestMethod.POST)
    @ResponseBody
    public String testAjax(String name,int age){
        System.out.println(name);
        System.out.println(age);
        return CommunityUtil.getJSONString(0,"操作成功");
    }

}
