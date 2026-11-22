package com.asif.linkedin.search_service.service;

import com.asif.linkedin.search_service.model.PostDocument;
import com.asif.linkedin.search_service.model.UserDocument;
import com.asif.linkedin.search_service.repository.PostSearchRepository;
import com.asif.linkedin.search_service.repository.UserSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchEventConsumer {

    private final UserSearchRepository userSearchRepository;
    private final PostSearchRepository postSearchRepository;

    @KafkaListener(topics = "user.created")
    public void consumeUserCreatedEvent(@Payload Map<String, Object> payload) {
        try{
            log.info("Indexing new user: {}", payload.get("userId"));

            UserDocument userDocument = UserDocument.builder()
                    .id(payload.get("userId").toString())
                    .firstName(payload.get("firstName").toString())
                    .lastName(payload.get("lastName").toString())
                    .email(payload.get("email").toString())
                    .headline(payload.get("headline").toString())
                    .location(payload.get("location").toString())
                    .build();
            userSearchRepository.save(userDocument);
            log.info("User indexed: {}", payload.get("userId"));
        } catch (Exception e) {
            log.error("Error while consuming user.created event: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "user.updated")
    public void consumeUserUpdatedEvent(@Payload Map<String, Object> payload) {
        try{
            String userId = payload.get("userId").toString();
            log.info("Updated user index: {}", userId);

            userSearchRepository.findById(userId).ifPresent(
                    doc -> {
                        doc.setFirstName(payload.get("firstName").toString());
                        doc.setLastName(payload.get("lastName").toString());
                        doc.setHeadline(payload.get("headline").toString());
                        doc.setLocation(payload.get("location").toString());

                        if(payload.get("skills") != null){
                            doc.setSkills((List<String>) payload.get("skills"));
                        }

                        userSearchRepository.save(doc);
                        log.info("User index updated: {}", userId);
                    }
            );
        } catch (Exception e) {
            log.error("Error while consuming user.updated event: {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "post.created")
    public void consumePostCreatedEvent(@Payload Map<String, Object> payload) {
        try{
            PostDocument postDocument = PostDocument.builder()
                    .id(payload.get("postId").toString())
                    .authorId(payload.get("authorId").toString())
                    .content(payload.get("content").toString())
                    .imageUrl(payload.get("imageUrl").toString())
                    .createdAt(payload.get("createdAt").toString())
                    .build();
            postSearchRepository.save(postDocument);

            log.info("Post indexed: {}", payload.get("postId"));

        } catch (Exception e) {
            log.error("Error while consuming post created event: {}", e.getMessage());
        }
    }


}
