package com.asif.linkedin.feed_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String FEED_KEY_PREFIX = "feed:";

    public List<String> getFeed(String userId, int page, int pageSize) {
        log.info("Getting feed for the user: {}", userId);

        String feedKey = FEED_KEY_PREFIX + userId;

        //Pagination post range
        int start = page * pageSize;
        int end = start + pageSize - 1;

        List<Object> postIds = redisTemplate.opsForList().range(feedKey, start, end);

        if (postIds == null || postIds.isEmpty()) {
            log.info("Feed cache empty for the user: {}", userId);
            return new ArrayList<>();
        }

        List<String> result = postIds.stream()
                .map(Object::toString)
                .toList();

        log.info("Returning {} posts for the user: {}", postIds.size(), userId);
        return result;
    }

    /**
     * clear feed for a user
     */
    public void clearFeed(String userId) {
        String feedKey = FEED_KEY_PREFIX + userId;
        redisTemplate.delete(feedKey);
        log.info("Feed cache cleared for the user: {}", userId);
    }
}
