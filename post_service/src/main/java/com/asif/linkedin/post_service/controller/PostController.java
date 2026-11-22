package com.asif.linkedin.post_service.controller;

import com.asif.linkedin.post_service.entity.Comment;
import com.asif.linkedin.post_service.entity.Post;
import com.asif.linkedin.post_service.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    //create post
    @PostMapping
    public ResponseEntity<Post> createPost(
            @RequestParam String authorId,
            @RequestParam String content,
            @RequestParam(required = false) MultipartFile image
    ){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postService.createPost(authorId, content, image));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<Post> getPost(@PathVariable String postId){
        return ResponseEntity.ok(postService.getPost(postId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Post>> getUserPosts(
            @PathVariable String userId
    ){
        return ResponseEntity.ok(postService.getUserPosts(userId));
    }

    @PostMapping("/{postId}/like")
    public ResponseEntity<String> likePost(
            @PathVariable String postId,
            @RequestParam String userId
    ){
        return ResponseEntity.ok(postService.likePost(postId, userId));
    }

    @PostMapping("/{postId}/comment")
    public ResponseEntity<Comment> addComment(
            @PathVariable String postId,
            @RequestParam String authorId,// author who is commenting
            @RequestParam String content
    ){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(postService.addComment(postId, authorId, content));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<String> deletePost(@PathVariable String postId, @RequestParam String userId){
        postService.deletePost(postId, userId);

        return ResponseEntity.ok("Post deleted.");
    }

    @GetMapping("/{postId}/comments")
    public ResponseEntity<List<Comment>> getAllComments(@PathVariable String postId){
        return ResponseEntity.ok(postService.getAllComments(postId));
    }
}
