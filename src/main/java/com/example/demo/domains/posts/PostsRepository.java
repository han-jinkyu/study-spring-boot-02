package com.example.demo.domains.posts;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * PostsRepository
 */
public interface PostsRepository extends JpaRepository<Posts, Long> {
}