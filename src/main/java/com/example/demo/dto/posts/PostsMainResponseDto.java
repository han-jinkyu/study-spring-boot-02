package com.example.demo.dto.posts;

import com.example.demo.domains.posts.Posts;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * PostsMainResponseDto
 */
@Getter
public class PostsMainResponseDto {
    private Long id;
    private String title;
    private String author;
    private String modifiedDate;

    /**
     * Constructor
     * @param entity 게시글 엔티티
     */
    public PostsMainResponseDto(Posts entity) {
        this.id = entity.getId();
        this.title = entity.getTitle();
        this.author = entity.getAuthor();
        this.modifiedDate = toStringDateTime(entity.getModifiedDate());
    }

    /**
     * DateTime을 스트링으로 변환하기
     * @param modifiedDate 스트링으로 변환할 DateTime
     * @return 스트링으로 변환된 DateTime
     */
    private String toStringDateTime(LocalDateTime modifiedDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return Optional.ofNullable(modifiedDate)
                .map(formatter::format)
                .orElse("");
    }
}
