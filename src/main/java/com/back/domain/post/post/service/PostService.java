package com.back.domain.post.post.service;

import com.back.domain.post.dto.PostModifyRequest;
import com.back.domain.post.post.entity.Post;
import com.back.domain.post.post.repository.PostRepository;
import com.back.domain.post.postComment.PostCommentRepository;
import com.back.domain.post.postComment.entity.PostComment;
import com.back.domain.tag.tag.entity.Tag;
import com.back.domain.tag.tag.service.TagService;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {
    private final PostRepository postRepository;
    private  final TagService tagService;
    private final PostCommentRepository postCommentRepository;


    public Page<Post> findAll(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    public Post write(String title, String content, List<String> tagNames) {
        Post post = new Post(title, content);
        Post savedPost = postRepository.save(post);

        if (tagNames != null && !tagNames.isEmpty()) {
            tagNames.forEach(tagName -> {

                Tag tag = tagService.getOrCreate(tagName);

                savedPost.addTag(tag);
            });
        }

        return savedPost;
    }


    public void modify(Post post, PostModifyRequest request) {
        post.modify(request.title(), request.content());
    }

    public Optional<Post> findById(int id) {
        return postRepository.findById(id);
    }

    public Page<Post> searchByTitle(String keyword, Pageable pageable){
        return postRepository.findByTitleContainingIgnoreCase(keyword, pageable);
    }

    public Page<Post> searchByTagName(String tagName, Pageable pageable){
        return postRepository.findByPostTags_Tag_Content(tagName, pageable);
    }

    public PostComment writeComment(Post post, String content, Integer parentCommentId) {
        PostComment comment = new PostComment();
        comment.setPost(post);
        comment.setContent(content);

        if(parentCommentId != null){
            PostComment parent = postCommentRepository.findById(parentCommentId)
                    .orElseThrow(()->new ServiceException("404-1", "댓글을 찾을 수 없습니다."));
            comment.setParent(parent);

            if(parent.getParent() != null){
                throw new ServiceException("400-3", "대댓글에는 답글을 달 수 없습니다.");
            }
            comment.setParent(parent);
        }
        return postCommentRepository.save(comment);
    }

    public void deleteComment(PostComment postComment) {
        if (!postComment.getChildren().isEmpty()) {
            postComment.markAsDeleted();
        }
        else {
            PostComment parent = postComment.getParent();

            if (parent != null) {
                parent.getChildren().remove(postComment);
            }
            postCommentRepository.delete(postComment);

            if (parent != null && parent.isDeleted() && parent.getChildren().isEmpty()) {
                postCommentRepository.delete(parent);
            }
        }
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

    public void addTag(int id, int tagId) {
        Post post = findById(id)
                .orElseThrow(() -> new ServiceException("404-1", "게시글을 찾을 수 없습니다.")); //

        Tag tag = tagService.findById(tagId)
                .orElseThrow(() -> new ServiceException("404-2", "태그를 찾을 수 없습니다.")); //

        post.addTag(tag);
    }

    public void deleteTag(Post post, Tag tag){
        boolean removed = post.deleteTag(tag);

        if (!removed) {
            throw new ServiceException("400-2", "게시글에 존재하지 않는 태그입니다.");
        }
    }




}
