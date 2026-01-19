package com.back.global.initData;

import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.transaction.annotation.Transactional;

@Configuration
@RequiredArgsConstructor
public class BaseInitData {
//    @Autowired
//    @Lazy
//    private BaseInitData self;
//
//    private final PostService postService;
//    @Bean
//    ApplicationRunner baseInitDataApplicationRunner() {
//        return args -> {
//            self.work1();
//        };
//    }
//
//    @Transactional
//    public void work1() {
//
//        Post post1 = postService.write("첫 번째 글", "첫 번째 내용입니다.");
//        Post post2 = postService.write("두 번째 글", "두 번째 내용입니다.");
//        Post post3 = postService.write("세 번째 글", "세 번째 내용입니다.");
//    }
}