package com.example.demo.webservice.web;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

/**
 * WebRestControllerTest
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class WebRestControllerTest {

    /**
     * TestRestTemplate
     */
    @Autowired
    private TestRestTemplate testRestTemplate;

    /**
     * Profile을 확인한다
     */
    @Test
    public void checkProfile() {
        // when
        String profile = testRestTemplate.getForObject("/profile", String.class);

        // then
        assertThat(profile).isEqualTo("local"); // 기본환경이 local로 되어 있기 때문에
    }
}
