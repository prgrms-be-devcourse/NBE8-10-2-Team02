package com.back.domain.tag.tag.dto;

import com.back.domain.post.post.entity.Post;
import com.back.domain.tag.tag.entity.Tag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

public record TagDto (
        int id,
    String content,
    LocalDateTime createDate,
    LocalDateTime modifyDate
) {

    public TagDto(Tag tag) {
        this(
                tag.getId(),
                tag.getContent(),
                tag.getCreateDate(),
                tag.getModifyDate()
        );
    }
}