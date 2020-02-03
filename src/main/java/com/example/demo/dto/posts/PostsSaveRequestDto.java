package com.example.demo.dto.posts;

import com.example.demo.domains.posts.Posts;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * PostsSaveRequestDto
 * #Controller에서 사용되는 값은 기본생성자+Setter로 만들어진다.
 */
@Getter
@Setter
@NoArgsConstructor
public class PostsSaveRequestDto {

    private String title;
    private String content;
    private String author;

    /**
     * Constructor
     * @param title 제목
     * @param content 본문
     * @param author 글쓴이
     */
    @Builder
    public PostsSaveRequestDto(String title, String content, String author) {
        this.title = title;
        this.content = content;
        this.author = author;
    }

    /**
     * Entity로 변경한다
     * @return Entity객체
     */
    public Posts toEntity() {
        return Posts.builder()
            .title(title)
            .content(content)
            .author(author)
            .build();
    }
}