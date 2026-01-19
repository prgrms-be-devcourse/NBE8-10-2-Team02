package com.back.domain.tag.tag.repository;

import com.back.domain.tag.tag.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Integer> {
    Optional<Tag> findBycontent(String content);
}
