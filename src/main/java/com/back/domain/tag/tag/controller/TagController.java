package com.back.domain.tag.tag.controller;

import com.back.domain.tag.tag.dto.CreateTagRequest;
import com.back.domain.tag.tag.dto.TagDto;
import com.back.domain.tag.tag.entity.Tag;
import com.back.domain.tag.tag.service.TagService;
import com.back.global.exception.ServiceException;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/tags")
public class TagController {

    private final TagService tagService;

    @GetMapping
    public List<TagDto> tags(){
        return tagService.findAll().stream()
                .map(TagDto::new)
                .toList();
    }

    @PostMapping
    public RsData<TagDto> create(@RequestBody CreateTagRequest req) {
        Tag tag = tagService.create(req.content());
        return new RsData<>("201-1", "태그가 생성되었습니다.", new TagDto(tag));
    }

//    @PutMapping("/{id}")
//    @Transactional
//    @Operation(summary = "수정")
//    public RsData<Void> modify(
//            @PathVariable int id,
//            @Valid @RequestBody TagModifyRequest reqBody
//    ) {
//
//        Tag tag = tagService.findById(id).get();
//
//        tagService.modify(tag, reqBody.content());
//
//        return new RsData<>(
//                "200-1",
//                "%d번 태그가 수정되었습니다.".formatted(id)
//        );
//    }

    @DeleteMapping("/{id}")
    public RsData<Void> delete(
            @PathVariable int id
    ){
        Tag tag = tagService.findById(id)
                .orElseThrow(()-> new ServiceException("404-1", "해당 태그를 찾을 수 없습니다."));

        tagService.delete(tag);

        return new RsData<>(
                "200-1",
                "%d번 태그가 삭제되었습니다.".formatted(id)
        );
    }

    @PostMapping("/igdb/{igdbId}")
    public RsData<List<TagDto>> createFromIgdb(@PathVariable long igdbId){
        List<Tag> tags = tagService.createTagsFromIgdb(igdbId);
        List<TagDto> tagDtos = tags.stream().map(TagDto::new).toList();

        return new RsData<>("201-2","IGDB 정보로 태그가 생성되었습니다.", tagDtos);
    }
}
