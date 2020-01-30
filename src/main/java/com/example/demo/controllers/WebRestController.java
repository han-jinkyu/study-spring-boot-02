package com.example.demo.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * WebRestController
 */
@RestController
public class WebRestController {

    @GetMapping(value = "/hello")
    public String hello() {
        return "Hello World!";
    }
}