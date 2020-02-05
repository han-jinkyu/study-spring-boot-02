package com.example.demo.service;

import com.example.demo.domains.posts.PostsRepository;
import com.example.demo.dto.posts.PostsMainResponseDto;
import com.example.demo.dto.posts.PostsSaveRequestDto;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


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

    /**
     * 모든 게시글을 ID 역순으로 취득한다
     * @return ID 역순으로 정렬된 게시글 리스트
     */
    @Transactional(readOnly = true) // readOnly는 트랜잭션 범위를 남겨 조회속도가 개선된다
    public List<PostsMainResponseDto> findAllDesc() {
        return postsRepository.findAllDesc()
            .map(PostsMainResponseDto::new)
            .collect(Collectors.toList());
    }
}
