package com.asif.linkedin.search_service.controller;

import com.asif.linkedin.search_service.model.PostDocument;
import com.asif.linkedin.search_service.model.UserDocument;
import com.asif.linkedin.search_service.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /**
     * Search by people name, headline, location, etc.,
     */
    @GetMapping("/people")
    public ResponseEntity<List<UserDocument>> searchPeople(@RequestParam String query) {
        return ResponseEntity.ok(searchService.searchPeople(query));
    }

    /**
     * Search by skills
     */
    @GetMapping("/skills")
    public ResponseEntity<List<UserDocument>> searchBySkill(@RequestParam String skill) {
        return ResponseEntity.ok(searchService.searchBySkill(skill));
    }

    /**
     * Search posts by content
     */
    @GetMapping("/posts")
    public ResponseEntity<List<PostDocument>> searchPosts(@RequestParam String query) {
        return ResponseEntity.ok(searchService.searchPosts(query));
    }
}
