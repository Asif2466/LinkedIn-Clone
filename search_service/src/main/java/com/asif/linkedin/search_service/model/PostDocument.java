package com.asif.linkedin.search_service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * Document for post search
 * Indexed when a post is created
 * Enables full text search across all post content.
 */
@Document(indexName = "posts")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PostDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String authorId;

    @Field(type = FieldType.Text)
    private String content;

    private String imageUrl;
    private String createdAt;
}
