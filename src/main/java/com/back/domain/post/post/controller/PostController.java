package com.back.domain.post.post.controller;

import com.back.domain.member.member.entity.Member;
import com.back.domain.member.member.service.MemberService;
import com.back.domain.post.dto.PostCreateRequest;
import com.back.domain.post.dto.PostDto;
import com.back.domain.post.dto.PostModifyRequest;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import com.back.domain.tag.tag.entity.Tag;
import com.back.domain.tag.tag.service.TagService;
import com.back.global.exception.ServiceException;
import com.back.global.rsData.RsData;
import com.back.global.security.CustomAuthenticationFilter;
import com.back.global.security.SecurityUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService postService;
    private final TagService tagService;
    private final MemberService memberService;

    @GetMapping
    public RsData<Page<PostDto>> getItems(
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC)Pageable pageable
            ){
        Page<Post> items = postService.findAll(pageable);

        Page<PostDto> postDtos = items.map(PostDto::new);
        return new RsData<>("200-1", "게시글 목록 조회", postDtos);
    }

    @GetMapping("/{id}")
    public PostDto getItem(@PathVariable int id) {
        Post post = postService.findById(id)
                .orElseThrow(()-> new ServiceException("404-1", "해당 게시글을 찾을 수 없습니다."));

        return new PostDto(post);
    }

    @PostMapping
    public RsData<PostDto> create(
            @AuthenticationPrincipal SecurityUser user,
            @RequestBody @Valid PostCreateRequest request
    ) {
        if(user == null){
            throw new ServiceException("401-1", "로그인이 필요합니다.");
        }
        Member author = memberService.findById(user.getId())
                .orElseThrow(()->new ServiceException("404-1", "회원 정보를 찾을 수 없습니다."));

        Post post = postService.write(author, request.title(), request.content(), request.tags());
        return new RsData<>("201-1", "게시글이 작성되었습니다.", new PostDto(post));
    }

    @PutMapping("/{id}")
    public RsData<PostDto> modify(
            @PathVariable int id,
            @AuthenticationPrincipal SecurityUser user,
            @RequestBody @Valid PostModifyRequest request
    ) {
        Post post = postService.findById(id)
                .orElseThrow(()-> new ServiceException("404-1", "해당 게시글을 찾을 수 없습니다."));

        Member author = memberService.findById(user.getId()).get();

        postService.checkPermission(post, author);

        postService.modify(post, request);

        return new RsData<>("200-1", "%d번 게시글이 수정되었습니다.".formatted(id),new PostDto(post));
    }

    @DeleteMapping("/{id}")
    public RsData<Void> delete(
            @PathVariable int id,
            @AuthenticationPrincipal SecurityUser user
    ){
        Post post = postService.findById(id)
                        .orElseThrow(()-> new ServiceException("404-1", "해당 게시글을 찾을 수 없습니다."));

        Member author = memberService.findById(user.getId()).get();

        postService.checkPermission(post, author);

        postService.delete(post);

        return new RsData<>(
                "200-1",
                "%d번 글이 삭제되었습니다.".formatted(id)
        );
    }

    @GetMapping("/search")
    public RsData<Page<PostDto>> searchByTitle(
            @RequestParam String keyword,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ){

        Page<Post> page = postService.searchByTitle(keyword, pageable);
        Page<PostDto> postDtos = page.map(PostDto :: new);

        return new RsData<>("200-1", "제목 조회", postDtos);
    }

    @GetMapping("/tag")
    public RsData<Page<PostDto>> searchByTag(
            @RequestParam String tagName,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable
    ){
        Page<PostDto> postDtos = postService.searchByTagName(tagName, pageable)
                .map(PostDto::new);

        return new RsData<>("200-1", "태그 조회", postDtos);
    }

    @PostMapping("/{id}/tags/{tagId}")
    public RsData<Void> addTag(
            @PathVariable int id,
            @PathVariable int tagId
    ){
        postService.addTag(id, tagId);

        return new RsData<>(
                "200-1",
                "%d번 게시글에 %d번 태그가 추가되었습니다.".formatted(id, tagId)
        );
    }

    @DeleteMapping("/{id}/tags/{tagId}")
    public RsData<Void> deleteTag(
            @PathVariable int id,
            @PathVariable int tagId
    ){
        Post post = postService.findById(id)
                .orElseThrow(()-> new ServiceException("404-1", "해당 게시글을 찾을 수 없습니다."));
        Tag tag = tagService.findById(tagId)
                .orElseThrow(()-> new ServiceException("404-2", "해당 태그를 찾을 수 없습니다."));

        postService.deleteTag(post, tag);

        return new RsData<>(
                "200-1",
                "태그가 게시글에서 제거되었습니다."
        );
    }




}
