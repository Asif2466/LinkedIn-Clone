package com.asif.linkedin.notification_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    /**
     * Consume user.created event and send welcome notification
     */
    @KafkaListener(topics = "user.created")
    public void consumeUserCreatedEvent(@Payload Map<String, Object> payload) {
        try{
            String userId = (String) payload.get("userId");
            String email = (String) payload.get("email");
            String firstName = (String) payload.get("firstName");
            log.info("Sending welcome notification to user: {}", userId);

            sendNotification(userId, "Welcome to LinkedIn!",
                    String.format("Hi %s, welcome to LinkedIn! Your account has been created." +
                            "Start connecting with Professionals.", firstName));

        } catch (Exception e) {
            log.error("Error occurred while sending welcome notification: {}", e.getMessage());
        }
    }

    /**
     * Consume connection.request event and send notification to the receiver
     */
    @KafkaListener(topics = "connection.request")
    public void consumeConnectionRequestEvent(@Payload Map<String, Object> payload) {
        try {
            String requesterId = (String) payload.get("requesterId");
            String receiverId = (String) payload.get("receiverId");
            log.info("Sending connection request notification to user: {}", receiverId);

            sendNotification(receiverId, "New Connection Request",
                    String.format("You have a new connection request from user: %s", requesterId));

        } catch (Exception e) {
            log.error("Error occurred while sending connection request notification: {}", e.getMessage());
        }
    }

    /**
     * Consume connection.accepted event and send notification to the requester
     */
    @KafkaListener(topics = "connection.accepted")
    public void consumeConnectionAcceptedEvent(@Payload Map<String, Object> payload) {
        try {
            String requesterId = (String) payload.get("requesterId");
            String receiverId = (String) payload.get("receiverId");
            log.info("Sending connection accepted notification to user: {}", requesterId);

            sendNotification(requesterId, "Connection Accepted",
                    String.format("Your connection request to user: %s has been accepted.", receiverId));

        } catch (Exception e) {
            log.error("Error occurred while sending connection accepted notification: {}", e.getMessage());
        }
    }

    /**
     * Consume post.liked event and send notification to the author
     */
    @KafkaListener(topics = "post.liked")
    public void consumePostLikedEvent(@Payload Map<String, Object> payload) {
        try {
            String postId = (String) payload.get("postId");
            String userId = (String) payload.get("userId");
            String authorId = (String) payload.get("authorId");
            log.info("Sending post liked notification to user: {}", authorId);

            sendNotification(authorId, "Post Liked",
                    String.format("Your post: %s has been liked by user: %s", postId, userId));

        } catch (Exception e) {
            log.error("Error occurred while sending post liked notification: {}", e.getMessage());
        }
    }

    /**
     * Consume post.commented event and send notification to the author
     */
    @KafkaListener(topics = "post.commented")
    public void consumePostCommentedEvent(@Payload Map<String, Object> payload) {
        try {
            String postId = (String) payload.get("postId");
            String authorId = (String) payload.get("authorId"); // -> author who is commenting
            String postAuthorId = (String) payload.get("postAuthorId");
            log.info("Sending post commented notification to user: {}", postAuthorId);

            sendNotification(postAuthorId, "New Comment on Your Post",
                    String.format("Your post: %s has a new comment from user: %s", postId, authorId));
        }
        catch (Exception e) {
            log.error("Error occurred while sending post commented notification: {}", e.getMessage());
        }
    }


    private void sendNotification(String userId, String title, String message) {
        // Here you can implement the logic to send notification to the user
        // For example, you can use email service or push notification service
//        log.info("Notification sent to user: {} | Title: {} | Message: {}", userId, title, message);
        log.info("----------------------------------------------------------------------------");
        log.info("NOTIFICATION SENT");
        log.info("To User: {}", userId);
        log.info("Title: {}", title);
        log.info("Message: {}", message);
        log.info("----------------------------------------------------------------------------");
    }
}
