package com.back.domain.post.post.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.dto.PostCreateRequest;
import com.back.domain.post.dto.PostDto;
import com.back.domain.post.dto.PostModifyRequest;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;

    @GetMapping
    public List<PostDto> getItems(){
        List<Post> items = postService.findAll();

        return items.stream()
                .map(PostDto::new)
                .toList();
    }

    @GetMapping("/{id}")
    public PostDto getItem(@PathVariable int id) {
        Post post = postService.findById(id).get();

        return new PostDto(post);
    }

    @PostMapping
    public PostDto create(
            @RequestBody @Valid PostCreateRequest request
    ) {
        Post post = postService.write(
                request.title(),
                request.content()
        );

        return new PostDto(post);
    }

    @PutMapping("/{id}")
    public PostDto modify(
            @PathVariable int id,
            @RequestBody @Valid PostModifyRequest request
    ) {
        Post post = postService.findById(id).get();

        postService.modify(post, request);

        return new PostDto(post);
    }

    @DeleteMapping("/{id}")
    public RsData<Void> delete(
            @PathVariable int id
    ){
        Post post = postService.findById(id).get();

        postService.delete(post);

        return new RsData<>(
                "200-1",
                "%d번 글이 삭제되었습니다.".formatted(id)
        );
    }

    @GetMapping("/search")
    public List<PostDto> search(
            @RequestParam String keyword
    ){
        return postService.searchByTitle(keyword)
                .stream()
                .map(PostDto::new)
                .toList();
    }


}
