package com.asif.linkedin.post_service.repository;

import com.asif.linkedin.post_service.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, String> {
    List<Comment> findByPostIdOrderByCreatedAtDesc(String postId);
}
