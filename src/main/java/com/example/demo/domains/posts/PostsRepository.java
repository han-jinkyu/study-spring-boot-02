package com.example.demo.domains.posts;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.stream.Stream;

/**
 * PostsRepository
 */
public interface PostsRepository extends JpaRepository<Posts, Long> {

    /**
     * 모든 게시글를 ID 역순으로 취득하기
     * @return 역순으로 정렬된 모든 게시글
     */
    @Query("SELECT p FROM Posts p ORDER BY p.id DESC")
    Stream<Posts> findAllDesc();
}