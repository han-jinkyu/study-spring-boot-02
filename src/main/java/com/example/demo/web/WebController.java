package com.example.demo.web;

import com.example.demo.service.PostsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.AllArgsConstructor;

/**
 * WebController
 */
@Controller
@AllArgsConstructor
public class WebController {

    /**
     * 게시글 관련 Service
     */
    private PostsService postsService;

    /**
     * 메인페이지로 접속한다
     * @return 메인 페이지 템플릿 이름
     */
    @GetMapping(value = "/")
    public String main(Model model) {
        model.addAttribute("posts", postsService.findAllDesc());
        return "main";
    }
}