package com.lwq.Controller;

import com.lwq.entity.Comment;
import com.lwq.entity.DiscussPost;
import com.lwq.entity.Page;
import com.lwq.entity.User;
import com.lwq.service.*;
import com.lwq.util.CommunityConstant;
import com.lwq.util.CommunityUtil;
import com.lwq.util.HostHolder;
import com.qiniu.util.Auth;
import com.qiniu.util.StringMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserController implements CommunityConstant {

    private  static  final Logger logger= LoggerFactory.getLogger(UserController.class);

    @Value("${community.path.upload}")
    private String uploadPath;


    @Value("${community.path.domain}")
    private  String domain;

    @Value("${server.servlet.context-path}")
    private  String contextPath;

    @Autowired
    private UserService userService;

    @Autowired
    private HostHolder hostHolder;

    @Autowired
    private LikeService likeService;

    @Autowired
    private FollowService followService;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private CommentService commentService;

    @Value("${qiniu.key.access}")
    private String accessKey;

    @Value("${qiniu.key.secret}")
    private String secretkey;

    @Value("${qiniu.bucket.header.name}")
    private String headerBucketName;

    @Value("${qiniu.bucket.header.url}")
    private String headerBucketUrl;



    @RequestMapping(path = "/setting",method = RequestMethod.GET)
    public String getSettingPage(Model model){
        //上传文件名称
        String fileName=CommunityUtil.generateUUID();
        //设置响应信息
        StringMap policy=new StringMap();
        policy.put("returnBody",CommunityUtil.getJSONString(0));
        //生成上传凭证
        Auth auth=Auth.create(accessKey,secretkey);
        String uploadToken = auth.uploadToken(headerBucketName,fileName,3600,policy);

        model.addAttribute("uploadToken",uploadToken);
        model.addAttribute("fileName",fileName);

        return  "/site/setting";
    }

    //更新头像路径
    @RequestMapping(path = "/header/url",method = RequestMethod.POST)
    @ResponseBody
    public String updateHeaderUrl(String fileName){
        if(StringUtils.isBlank(fileName)) {
            return CommunityUtil.getJSONString(1,"文件名不能为空!");
        }

        String url=headerBucketUrl+"/"+fileName;
        userService.updateHeader(hostHolder.getUser().getId(),url);

        return CommunityUtil.getJSONString(0);

    }


    //上传云服务废弃
    //上传必须为post
    @RequestMapping(path = "/upload", method = RequestMethod.POST)
    public String uploadHeader(MultipartFile headerImage, Model model) {
        if (headerImage == null) {
            model.addAttribute("error", "您还没有选择图片!");
            return "/site/setting";
        }

        String fileName = headerImage.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        if (StringUtils.isBlank(suffix)) {
            model.addAttribute("error", "文件的格式不正确!");
            return "/site/setting";
        }

        // 生成随机文件名
        fileName = CommunityUtil.generateUUID() + suffix;
        // 确定文件存放的路径
        File dest = new File(uploadPath + "/" + fileName);
        try {
            // 存储文件
            headerImage.transferTo(dest);
        } catch (IOException e) {
            logger.error("上传文件失败: " + e.getMessage());
            throw new RuntimeException("上传文件失败,服务器发生异常!", e);
        }

        // 更新当前用户的头像的路径(web访问路径)
        // http://localhost:8080/community/user/header/xxx.png
        User user = hostHolder.getUser();
        String headerUrl = domain + contextPath + "/user/header/" + fileName;
        userService.updateHeader(user.getId(), headerUrl);

        return "redirect:/index";
    }


    //上传云服务废弃
    @RequestMapping(path = "/header/{fileName}", method = RequestMethod.GET)
    public void getHeader(@PathVariable("fileName") String fileName, HttpServletResponse response) {
        // 服务器存放路径
        fileName = uploadPath + "/" + fileName;
        // 文件后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 响应图片
        response.setContentType("image/" + suffix);
        try (
                FileInputStream fis = new FileInputStream(fileName);
                OutputStream os = response.getOutputStream();
        ) {
            byte[] buffer = new byte[1024];
            int b = 0;
            while ((b = fis.read(buffer)) != -1) {
                os.write(buffer, 0, b);
            }
        } catch (IOException e) {
            logger.error("读取头像失败: " + e.getMessage());
        }
    }


    @RequestMapping(path = "/password",method = RequestMethod.POST)
    public  String  updatePassword(String oldPassword, String newPassword, String newPasswordConfim, Model model, @CookieValue("ticket") String ticket){

        Map<String, Object> map = userService.updatePassword(oldPassword,newPassword,newPasswordConfim);
        if (map==null||map.isEmpty()){
            userService.logout(ticket);
            return "redirect:/login";
        }
        model.addAttribute("oldPasswordMsg",map.get("oldPasswordMsg"));
        model.addAttribute("newPasswordMsg",map.get("newPasswordMsg"));
        model.addAttribute("newPasswordConfimMsg",map.get("newPasswordConfimMsg"));

        return "/site/setting";


    }

    //个人主页
    @RequestMapping(path = "/profile/{userId}",method = RequestMethod.GET)
    public String getProfilePage(@PathVariable("userId") int userId,Model model){
        User user = userService.findUserById(userId);
        if(user==null){
            throw new RuntimeException("改用户不存在！");
        }

        //用户
        model.addAttribute("user",user);
        //点赞数量
        int likecount = likeService.findUserLikeCount(userId);
        model.addAttribute("likecount",likecount);

        // 关注数量
        long followeeCount = followService.findFolloweeCount(userId, ENTITY_TYPE_USER);
        model.addAttribute("followeeCount", followeeCount);
        // 粉丝数量
        long followerCount = followService.findFollowerCount(ENTITY_TYPE_USER, userId);
        model.addAttribute("followerCount", followerCount);
        // 是否已关注
        boolean hasFollowed = false;
        if (hostHolder.getUser() != null) {
            hasFollowed = followService.hasFollowed(hostHolder.getUser().getId(), ENTITY_TYPE_USER, userId);
        }
        model.addAttribute("hasFollowed", hasFollowed);


        return "/site/profile";
    }

    //我的帖子
    @RequestMapping(path = "/mypost",method = RequestMethod.GET)
    public String toMyPost(Model model, Page page) {
        // 获取当前登录用户
        User curUser = hostHolder.getUser();
        model.addAttribute("user", curUser);

        // 设置分页信息
        page.setLimit(5);
        page.setRows(discussPostService.findDiscussPostRows(curUser.getId()));
        page.setPath("/user/mypost");


        // 查询某用户发布的帖子
        List<DiscussPost> discussPosts = discussPostService.findDiscussPosts(curUser.getId(), page.getOffset(), page.getLimit(),0);
        List<Map<String, Object>> list = new ArrayList<>();
        if (discussPosts != null) {
            for (DiscussPost post : discussPosts) {
                Map<String, Object> map = new HashMap<>();
                map.put("post", post);
                // 点赞数量
                long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, post.getId());
                map.put("likeCount", likeCount);

                list.add(map);
            }
        }
        // 帖子数量
        int postCount = discussPostService.selectCountByUserId(curUser.getId());
        model.addAttribute("postCount", postCount);
        model.addAttribute("discussPosts", list);

        return "site/my-post";
    }


    //我的评论
    @RequestMapping(path = "/mycomment",method = RequestMethod.GET)
    public String toMyReply(Model model, Page page) {
        // 获取当前登录用户
        User curUser = hostHolder.getUser();
        model.addAttribute("user", curUser);

        // 设置分页信息
        page.setLimit(5);
        page.setRows(commentService.selectCountByUserId(curUser.getId()));
        page.setPath("/user/mycomment");

        // 获取用户所有评论 (而不是回复,所以在 sql 里加一个条件 entity_type = 1)
        List<Comment> comments = commentService.selectCommentByUserId(curUser.getId(), page.getOffset(), page.getLimit());
        List<Map<String, Object>> list = new ArrayList<>();
        if (comments != null) {
            for (Comment comment : comments) {
                Map<String, Object> map = new HashMap<>();
                map.put("comment", comment);

                // 根据实体 id 查询对应的帖子标题
                String discussPostTitle = discussPostService.findDiscussPostById(comment.getEntityId()).getTitle();
                map.put("discussPostTitle", discussPostTitle);

                list.add(map);
            }
        }
        // 回复的数量
        int commentCount = commentService.selectCountByUserId(curUser.getId());
        model.addAttribute("commentCount", commentCount);

        model.addAttribute("comments", list);
        return "site/my-reply";
    }

}
