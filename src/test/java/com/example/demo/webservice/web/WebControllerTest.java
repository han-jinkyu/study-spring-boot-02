package com.example.demo.webservice.web;

import static org.junit.Assert.assertThat;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * WebControllerTest
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class WebControllerTest {

    /**
     * Test를 위한 RestTemplate
     */
    @Autowired
    private TestRestTemplate restTemplate;

    /**
     * 메인페이지 불러오기
     */
    @Test
    public void loadMainPage() {
        // when
        String body = this.restTemplate.getForObject("/", String.class);

        // then
        assertThat(body, containsString("스프링부트로 시작하는 웹 서비스"));
    }
}