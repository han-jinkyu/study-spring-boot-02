package com.example.demo.service;

import com.example.demo.domains.posts.PostsRepository;
import com.example.demo.dto.posts.PostsSaveRequestDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

/**
 * PostsService
 */
@AllArgsConstructor
@Service
public class PostsService {

    /**
     * PostsRepository
     */
    private PostsRepository postsRepository;

    /**
     * 게시글 등록하기
     * @param dto 등록할 게시글 DTO
     * @return 등록한 게시글이 가지게 된 ID
     */
    @Transactional
    public Long save(PostsSaveRequestDto dto) {
        return postsRepository.save(dto.toEntity()).getId();
    }
}
