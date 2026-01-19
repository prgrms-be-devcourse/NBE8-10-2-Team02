package com.back.domain.post.post.service;

import com.back.domain.member.member.entity.Member;
import com.back.domain.post.dto.PostModifyRequest;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.repository.PostRepository;
import com.back.domain.post.postComment.entity.PostComment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {
    private final PostRepository postRepository;


    public List<Post> findAll() {
        return postRepository.findAll();
    }

    public Post write(String title, String content) {
        Post post = new Post(title, content);
        return postRepository.save(post);

    }


    public void modify(Post post, PostModifyRequest request) {
        post.modify(request.title(), request.content());
    }

    public Optional<Post> findById(int id) {
        return postRepository.findById(id);
    }

    public List<Post> searchByTitle(String keyword){
        return postRepository.findByTitleContainingIgnoreCase(keyword);
    }

    public PostComment writeComment(Post post, String content) {
        return post.addComment(content);
    }

    public boolean deleteComment(Post post, PostComment postComment) {
        return post.deleteComment(postComment);
    }

    public void modifyComment(PostComment postComment, String content) {
        postComment.modify(content);
    }

    public void delete(Post post) {
        postRepository.delete(post);
    }

    public void flush() {
        postRepository.flush();
    }
}
