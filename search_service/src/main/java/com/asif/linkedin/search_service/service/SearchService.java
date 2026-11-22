package com.asif.linkedin.search_service.service;

import com.asif.linkedin.search_service.model.PostDocument;
import com.asif.linkedin.search_service.model.UserDocument;
import com.asif.linkedin.search_service.repository.PostSearchRepository;
import com.asif.linkedin.search_service.repository.UserSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final UserSearchRepository userSearchRepository;
    private final PostSearchRepository postSearchRepository;

    public List<UserDocument> searchPeople(String query) {
        log.info("Searching for people: {}", query);
        return userSearchRepository.searchPeople(query);
    }

    public List<UserDocument> searchBySkill(String skill) {
        log.info("Searching people by skill: {}", skill);
        return userSearchRepository.findBySkillsContaining(skill);
    }

    public List<PostDocument> searchPosts(String query) {
        log.info("Searching for posts: {}", query);
        return postSearchRepository.searchPosts(query);
    }
}
