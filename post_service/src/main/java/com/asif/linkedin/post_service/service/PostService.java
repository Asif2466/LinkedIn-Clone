package com.asif.linkedin.post_service.service;

import com.asif.linkedin.post_service.entity.Comment;
import com.asif.linkedin.post_service.entity.Like;
import com.asif.linkedin.post_service.entity.Post;
import com.asif.linkedin.post_service.repository.CommentRepository;
import com.asif.linkedin.post_service.repository.LikeRepository;
import com.asif.linkedin.post_service.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final PostRepository postRepository;
    private final LikeRepository likeRepository;
    private final CommentRepository commentRepository;

    private final S3Service s3Service;

    private static final String POST_CREATED = "post.created";
    private static final String POST_LIKED = "post.liked";
    private static final String POST_COMMENTED = "post.commented";

    /**
     * Create Post
     * Optionally upload image to S3
     * Publish post.created event to Kafka
     * Feed Service and Search Service consumes it.
     */
    public Post createPost(String authorId, String content, MultipartFile image) {
        log.info("Creating post for the user: {}", authorId);

        Post post = Post.builder()
                .authorId(authorId)
                .content(content)
                .build();

        if(image != null && !image.isEmpty()) {
            String imageUrl = s3Service.uploadFile(image, "posts/"+authorId);
            post.setImageUrl(imageUrl);
        }

        Post savedPost = postRepository.save(post);
        log.info("Post created: {}", savedPost.getId());

        //publish post.created event to kafka -- Feed Service & Search service consumes it
        Map<String,Object> postCreatedEvent = new HashMap<>();
        postCreatedEvent.put("authorId", authorId);
        postCreatedEvent.put("postId", savedPost.getId());
        postCreatedEvent.put("content", savedPost.getContent());
        postCreatedEvent.put("imageUrl", savedPost.getImageUrl());
        postCreatedEvent.put("createdAt", savedPost.getCreatedAt().toString());
        kafkaTemplate.send(POST_CREATED, savedPost.getId(), postCreatedEvent);

        log.info("post.created event published: {}", savedPost.getId());
        return savedPost;
    }

    public Post getPost(String postId) {
        return postRepository.findById(postId).orElseThrow(
                () -> new RuntimeException("post not found: "+postId)
        );
    }
    
    public List<Post> getUserPosts(String userId) {
        return postRepository.findByAuthorIdOrderByCreatedAtDesc(userId);
    }

    public String likePost(String postId, String userId) {

        Post post = getPost(postId);

        if(likeRepository.existsByPostIdAndUserId(postId, userId)) {
            likeRepository.findByPostIdAndUserId(postId, userId)
                    .ifPresent(likeRepository::delete);
            post.setLikesCount(post.getLikesCount()-1);
            postRepository.save(post);
            return "Post Disliked";
        }

        Like like = Like.builder()
                .postId(postId)
                .userId(userId)
                .build();
        likeRepository.save(like);
        post.setLikesCount(post.getLikesCount()+1);
        postRepository.save(post);

        //Publish post.liked event
        Map<String,Object> postLikedEvent = new HashMap<>();
        postLikedEvent.put("postId", postId);
        postLikedEvent.put("userId", userId);
        postLikedEvent.put("authorId", post.getAuthorId());
        kafkaTemplate.send(POST_LIKED, postId, postLikedEvent);

        return "Post Liked"; 
    }

    /**
     * Add a comment to a post
     */
    public Comment addComment(String postId, String authorId, String content) {
        Post post = getPost(postId);

        Comment comment = Comment.builder()
                .postId(postId)
                .authorId(authorId)
                .content(content)
                .build();

        Comment savedComment = commentRepository.save(comment);

        post.setCommentCount(post.getCommentCount()+1);
        postRepository.save(post);

        //Publish post.commented event
        Map<String,Object> postCommentedEvent = new HashMap<>();
        postCommentedEvent.put("postId", postId);
        postCommentedEvent.put("authorId", authorId); // -> author who is commenting
        postCommentedEvent.put("commentId", savedComment.getId());
        postCommentedEvent.put("postAuthorId", post.getAuthorId());
        kafkaTemplate.send(POST_COMMENTED, postId, postCommentedEvent);

        return savedComment;
    }

    /**
     * Get all comments for a post
     */
    public List<Comment> getAllComments(String postId) {
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId);
    }

    /**
     * Delete post
     */
    public void deletePost(String postId, String userId) {
        Post post = getPost(postId);

        if(!userId.equals(post.getAuthorId())) {
            throw new RuntimeException("Not authorized to delete the post.");
        }

        postRepository.delete(post);
        log.info("Post deleted: {}", post.getId());
    }
}
