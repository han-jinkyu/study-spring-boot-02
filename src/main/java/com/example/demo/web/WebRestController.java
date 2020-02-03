package com.example.demo.web;

import com.example.demo.domains.posts.PostsRepository;
import com.example.demo.dto.posts.PostsSaveRequestDto;

import com.example.demo.service.PostsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;

/**
 * WebRestController
 */
@RestController
@AllArgsConstructor // 생성자로 Bean을 주입하기 위해 사용
public class WebRestController {

    /**
     * PostsService
     */
    private PostsService postsService;

    /**
     * 헬로 월드 표시하기
     * @return 리스폰스바디에 들어갈 문자열
     */
    @GetMapping(value = "/hello")
    public String hello() {
        return "Hello World!";
    }

    /**
     * Posts DTO를 저장한다
     * @param dto 저장할 Posts DTO
     */
    @PostMapping(value = "/posts")
    public void savePosts(@RequestBody PostsSaveRequestDto dto) {
        postsService.save(dto);
    }
}