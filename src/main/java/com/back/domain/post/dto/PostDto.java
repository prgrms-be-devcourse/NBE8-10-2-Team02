package com.back.domain.post.dto;

import com.back.domain.post.post.entity.Post;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

public record PostDto (

    int id,
    String title,
    String content,
    @CreatedDate
    LocalDateTime createDate,
    LocalDateTime modifyDate
) {
    public PostDto(Post post) {
        this(
        post.getId(),
        post.getTitle(),
        post.getContent(),
        post.getCreateDate(),
        post.getModifyDate()
        );
    }
}