package com.back.domain.post.postComment.entity;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.post.entity.Post;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.*;

@Entity
@Getter
@NoArgsConstructor
public class PostComment extends BaseEntity {
    private String content;
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createDate;
    @LastModifiedDate
    private LocalDateTime modifyDate;

    @ManyToOne(fetch = LAZY)
    private Post post;

//    @ManyToOne(fetch = LAZY)
//    private Member author;

    public PostComment(Post post, String content) {
        this.post = post;
        this.content = content;
    }

    public void modify(String content) {
        this.content = content;
    }
}
