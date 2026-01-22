package com.back.domain.post.post.repository;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.postComment.entity.PostComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Integer> {
    Page<Post> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);
    Page<Post> findByPostTags_Tag_Content(String content, Pageable pageable);
}
