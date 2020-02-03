package com.example.demo.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import lombok.AllArgsConstructor;

/**
 * WebController
 */
@Controller
@AllArgsConstructor
public class WebController {

    /**
     * 메인페이지로 접속한다
     * @return 메인 페이지 템플릿 이름
     */
    @GetMapping(value = "/")
    public String main() {
        return "main";
    }
}