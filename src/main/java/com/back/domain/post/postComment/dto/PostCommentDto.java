package com.back.domain.post.postComment.dto;


import com.back.domain.post.postComment.entity.PostComment;

import java.time.LocalDateTime;
import java.util.List;

public record PostCommentDto(
            int id,
            String content,
            List<PostCommentDto> children,
            boolean deleted,
            LocalDateTime createdDate,
            LocalDateTime modifyDate,
            int postId
    ) {
        public PostCommentDto(PostComment postComment){
            this(
                    postComment.getId(),
                    postComment.isDeleted() ? "삭제된 댓글입니다." : postComment.getContent(),
                    postComment.getChildren()
                                    .stream()
                                            .map(PostCommentDto::new)
                                                    .toList(),
                    postComment.isDeleted(),
                    postComment.getCreateDate(),
                    postComment.getModifyDate(),
                    postComment.getPost().getId()
            );
        }
    }
