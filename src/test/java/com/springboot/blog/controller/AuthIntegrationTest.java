package com.springboot.blog.controller;

import com.springboot.blog.entity.User;
import com.springboot.blog.payload.JWTAuthResponse;
import com.springboot.blog.payload.LoginDto;
import com.springboot.blog.payload.PostDto;
import com.springboot.blog.payload.RegisterDto;
import com.springboot.blog.security.JwtTokenProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.Arrays;
import java.util.Collection;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles({"integration-test"})
//@Testcontainers
@Sql(value = "classpath:import-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class AuthIntegrationTest {
    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    String token;
    RegisterDto registerDto;
    LoginDto loginDto;
    ModelMapper modelMapper = new ModelMapper();
    HttpHeaders header = new HttpHeaders();

    @BeforeEach
    void SetUp() {
        testRestTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());

        registerDto = new RegisterDto();
        loginDto = new LoginDto();

        registerDto.setName("Andres");
        registerDto.setEmail("andres@gmail.com");
        registerDto.setUsername("andresito");
        registerDto.setPassword("123456789");

        loginDto.setUsernameOrEmail("pepeillo");
        loginDto.setPassword("123456789");
    }

    @Test
    void registerTest() {
        HttpEntity<RegisterDto> entity = new HttpEntity<>(registerDto);
        ResponseEntity<String> result = testRestTemplate.exchange("/api/auth/register",
                HttpMethod.POST, entity, String.class
        );
        Assertions.assertEquals(HttpStatus.CREATED, result.getStatusCode());
    }



}