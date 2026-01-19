package com.back.domain.tag.postTag.repository;

import com.back.domain.tag.postTag.entity.PostTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostTagRepository extends JpaRepository<PostTag, Integer> {
}
