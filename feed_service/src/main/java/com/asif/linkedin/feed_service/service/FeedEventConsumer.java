package com.asif.linkedin.feed_service.service;

import com.asif.linkedin.feed_service.client.UserServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedEventConsumer {

    private final RedisTemplate<String, Object> redisTemplate;
    private final UserServiceClient userServiceClient;

    @Value("${feed.max-size}")
    private int maxFeedSize;

    private static final String FEED_KEY_PREFIX = "feed:";

    /**
     * Consume post.created event
     * When a user creates a post - push that post to all connections feed.
     *
     * This is how linkedIn/Twitter works
     */
    @KafkaListener(topics = "post.created")
    public void consumePostCreatedEvent(@Payload Map<String, Object> payload){
        try {
            String postId = payload.get("postId").toString();
            String authorId = payload.get("authorId").toString();

            // Get all connections from the post author
            List<Map<String, Object>> connections = userServiceClient.getConnections(authorId);

            // Push the post to each connection feed.
            for (Map<String, Object> connection : connections) {
                String connectionId = connection.get("id").toString();
                String feedKey = FEED_KEY_PREFIX + connectionId; // here, we should use connection.receiverId

                // Add post to feed (as sorted set - score = timestamp)
                redisTemplate.opsForList().leftPush(feedKey, postId);

                redisTemplate.opsForList().trim(feedKey, 0, maxFeedSize);
                log.info("Post {} pushed to feed of user: {}", postId, connectionId);
            }

            // Push notification to the own post user
            String feedKey = FEED_KEY_PREFIX + authorId;
            redisTemplate.opsForList().leftPush(feedKey, postId);
            redisTemplate.opsForList().trim(feedKey, 0, maxFeedSize);

        } catch (Exception e) {
            log.error("Error while consuming post created event: "+e.getMessage());
        }
    }
}
