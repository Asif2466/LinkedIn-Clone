package com.asif.linkedin.feed_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

@FeignClient(name = "user_service", url = "${user.service.url}")
public interface UserServiceClient {

    @GetMapping("/{userId}/connections")
    List<Map<String, Object>> getConnections(@PathVariable String userId);
}
