package com.example.demo.service;

import com.example.demo.domains.posts.Posts;
import com.example.demo.domains.posts.PostsRepository;
import com.example.demo.dto.posts.PostsSaveRequestDto;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PostsServiceTest
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class PostsServiceTest {

    /**
     * PostsService
     */
    @Autowired
    private PostsService postsService;

    /**
     * PostsRepository
     */
    @Autowired
    private PostsRepository postsRepository;

    /**
     * 테스트 후 모두 삭제한다
     */
    @After
    public void cleanup() {
        postsRepository.deleteAll();
    }

    /**
     * DTO 데이터가 포스트 테이블에 저장된다
     */
    @Test
    public void dtoData_toPostsTable_saved() {
        // given
        PostsSaveRequestDto dto = PostsSaveRequestDto.builder()
                .author("hjk1019@gmail.com")
                .content("테스트 본문")
                .title("테스트 타이틀")
                .build();

        // when
        postsService.save(dto);

        // then
        Posts posts = postsRepository.findAll().get(0);
        assertThat(posts.getAuthor()).isEqualTo(dto.getAuthor());
        assertThat(posts.getContent()).isEqualTo(dto.getContent());
        assertThat(posts.getTitle()).isEqualTo(dto.getTitle());
    }
}
