package com.asif.linkedin.search_service.repository;

import com.asif.linkedin.search_service.model.UserDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface UserSearchRepository extends ElasticsearchRepository<UserDocument, String> {

    @Query("{\"multi_match\":{\"query\": \"?0\", "+
           "\"fields\": [\"firstName\", \"lastName\","+
           "\"headline\", \"location\"]}}")
    List<UserDocument> searchPeople(String query);

    /**
     * Search by specific skill
     */
    List<UserDocument> findBySkillsContaining(String skill);
}
