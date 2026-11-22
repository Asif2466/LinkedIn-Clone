package com.asif.linkedin.feed_service.controller;

import com.asif.linkedin.feed_service.service.FeedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    /**
     *Get Paginated feed for a user
     * Returns a list of Post IDs
     * Client fetch full Post details using Post Service
     */
    @GetMapping("/{userId}")
    public ResponseEntity<List<String>> getFeed(
            @PathVariable String userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ){
        return ResponseEntity.ok(feedService.getFeed(userId, page, pageSize));
    }

    /**
     * clear feed cache -- useful for testing
     */
    @DeleteMapping("/{userId}/cache")
    public ResponseEntity<String> clearFeed(@PathVariable String userId){
        feedService.clearFeed(userId);
        return ResponseEntity.ok("Feed Cache cleared");
    }
}
