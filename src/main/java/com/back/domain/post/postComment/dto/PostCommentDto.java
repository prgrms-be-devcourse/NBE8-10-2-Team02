package com.back.domain.post.postComment.dto;


import com.back.domain.post.postComment.entity.PostComment;

import java.time.LocalDateTime;
public record PostCommentDto(
            int id,
            String content,
            LocalDateTime createdDate,
            LocalDateTime modifyDate,
            int postId
    ) {
        public PostCommentDto(PostComment postComment){
            this(
                    postComment.getId(),
                    postComment.getContent(),
                    postComment.getCreateDate(),
                    postComment.getModifyDate(),
                    postComment.getPost().getId()
            );
        }
    }
