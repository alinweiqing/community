package com.lwq.dao;

import com.lwq.entity.DiscussPost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DiscussPostMapper {
    List<DiscussPost> selectDiscussPosts(int userId,int offse,int limit , int orderMode);//页面分页offse每一页起始行号，limit显示多少条,orderMode 0最新，1热度

    //@Param注解用于给参数取别名
    //如果只有一个参数，并且在<if>里使用，则必须加别名。
    int selectDiscussPostRows(@Param("userId") int userId);//查询行数

    //发布帖子
    int insertDiscussPost(DiscussPost discussPost);

    //查询帖子详情
    DiscussPost findDiscussPostById(int id);

    //更新帖子数量
    int updateCommentCount(int id,int commentCount);

    //修改帖子类型
    int updateType(int id,int type);

    //修改帖子状态
    int updateStatus(int id ,int status );

    //计算帖子分数
    int updateScore(int id, double score);



}
