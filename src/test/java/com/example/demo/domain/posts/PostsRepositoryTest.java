package com.example.demo.domain.posts;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.util.List;

import com.example.demo.domains.posts.Posts;
import com.example.demo.domains.posts.PostsRepository;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * PostsRepositoryTest
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class PostsRepositoryTest {

    @Autowired
    PostsRepository postsRepository;

    /**
     * 청소한다
     */
    @After
    public void cleanup() {
        postsRepository.deleteAll();
    }

    /**
     * 게시글 등록하고 불러오기
     */
    @Test
    public void savePosts_loadPosts() {
        // given
        postsRepository.save(
            Posts.builder()
                .title("테스트 게시글")
                .content("테스트 본문")
                .author("hjk1019@gmail.com")
                .build());

        // when
        List<Posts> postsList = postsRepository.findAll();

        // then
        Posts posts = postsList.get(0);
        assertThat(posts.getTitle(), is("테스트 게시글"));
        assertThat(posts.getContent(), is("테스트 본문"));
    }

    /**
     * BaseTimeEntity 등록
     */
    @Test
    public void BaseTimeEntity_add() {
        // given
        LocalDateTime now = LocalDateTime.now();
        postsRepository.save(
            Posts.builder()
                .title("테스트 게시글")
                .content("테스트 본문")
                .author("hjk1019@gmail.com")
                .build());
        
        // when
        List<Posts> postsList = postsRepository.findAll();

        // then
        Posts posts = postsList.get(0);
        assertTrue(posts.getCreatedDate().isAfter(now));
        assertTrue(posts.getModifiedDate().isAfter(now));
    }
}