package com.lwq.dao;

import com.lwq.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface CommentMapper {
    List<Comment > selectCommentsByEntity(int entityType,int entityId,int offset , int limit);

    int selectCountByEntity(int entityType,int entityId);

    int insertComment(Comment comment);

    Comment selectCommentById(int id);

    int selectCountByUserId(int userId);

    //我的评论
    List<Comment> selectCommentByUserId(int userId, int offset, int limit);
}
