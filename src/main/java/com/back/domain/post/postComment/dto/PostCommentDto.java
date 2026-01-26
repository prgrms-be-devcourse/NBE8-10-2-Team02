package com.back.domain.post.postComment.dto;


import com.back.domain.post.postComment.entity.PostComment;

import java.time.LocalDateTime;
import java.util.List;

public record PostCommentDto(
            int id,
            int authorId,
            String authorName,
            String content,
            List<PostCommentDto> children,
            Integer parentId,
            boolean deleted,
            LocalDateTime createdDate,
            LocalDateTime modifyDate,
            int postId
    ) {
        public PostCommentDto(PostComment postComment){
            this(
                    postComment.getId(),
                    postComment.getAuthor().getId(),
                    postComment.getAuthor().getNickname(),
                    postComment.isDeleted() ? "삭제된 댓글입니다." : postComment.getContent(),
                    postComment.getChildren()
                                    .stream()
                                            .map(PostCommentDto::new)
                                                    .toList(),
                    postComment.getParent() != null ? postComment.getParent().getId() : null,
                    postComment.isDeleted(),
                    postComment.getCreateDate(),
                    postComment.getModifyDate(),
                    postComment.getPost().getId()
            );
        }
    }
