package com.back.domain.tag.tag.service;

import com.back.domain.tag.tag.entity.Tag;
import com.back.domain.tag.tag.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class TagService {
    private final TagRepository tagRepository;

    @Transactional
    public Tag create(String content) {
        tagRepository.findBycontent(content)
                .ifPresent(t -> {
                    throw new IllegalArgumentException("이미 존재하는 태그입니다.");
                });

        return tagRepository.save(new Tag(content));
    }

    public List<Tag> findAll(){
        return tagRepository.findAll();
    }

    public Optional<Tag> findById(int id) {
        return tagRepository.findById(id);
    }

    public void delete(Tag content) {
        tagRepository.delete(content);
    }

    public void modify(Tag tag, String content) {
        tag.modify(content);
    }
}
