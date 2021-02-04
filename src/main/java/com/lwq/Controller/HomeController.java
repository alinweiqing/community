package com.lwq.Controller;

import com.lwq.entity.DiscussPost;
import com.lwq.entity.Page;
import com.lwq.entity.User;
import com.lwq.service.DiscussPostService;
import com.lwq.service.LikeService;
import com.lwq.service.UserService;
import com.lwq.util.CommunityConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class HomeController implements CommunityConstant {

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private UserService userService;

    @RequestMapping(path = "/index" ,method=RequestMethod.GET)
    public String getIndexPage(Model model, Page page,
                               @RequestParam(name = "orderMode", defaultValue = "0") int orderMode){

        page.setRows(discussPostService.findDiscussPostRows(0));
        page.setPath("/index?orderMode=" + orderMode);

        List<DiscussPost> list = discussPostService.findDiscussPosts(0,page.getOffset(),page.getLimit(),orderMode);
        List<Map<String,Object>> doscissPosts=new ArrayList<>();
        if(list !=null){
            for (DiscussPost post:list){
                Map<String ,Object>  map = new HashMap<>();
                map.put("post",post);
                User user=userService.findUserById(post.getUserId());
                map.put("user",user);

                long likeCount=likeService.findEntityLikeCount(ENTITY_TYPE_POST,post.getId());
                map.put("likecount",likeCount);
                doscissPosts.add(map);
                model.addAttribute("orderMode", orderMode);
            }
        }

        model.addAttribute("discussPosts",doscissPosts);
        return "index";
    }

    @RequestMapping(path = "/denied",method = RequestMethod.GET)
    public String getDeniedPage(){
        return "/error/404";
    }

}
