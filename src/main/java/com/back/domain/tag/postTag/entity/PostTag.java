package com.back.domain.tag.postTag.entity;

import com.back.domain.post.post.entity.Post;
import com.back.domain.tag.tag.entity.Tag;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@NoArgsConstructor
@Table(
        name = "post_tag",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"post_id", "tag_id"})
        }
)
public class PostTag extends BaseEntity {

    @ManyToOne(fetch = LAZY)
    private Post post;

    @ManyToOne(fetch = LAZY)
    private Tag tag;

    public PostTag(Post post, Tag tag) {
        this.post = post;
        this.tag = tag;
    }
}
