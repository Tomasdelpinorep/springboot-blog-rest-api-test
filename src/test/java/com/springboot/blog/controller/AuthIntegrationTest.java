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

    RegisterDto registerDto;
    LoginDto loginDto;
    HttpHeaders header = new HttpHeaders();

    @BeforeEach
    void SetUp() {
        testRestTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());

        registerDto = new RegisterDto();
        loginDto = new LoginDto();

        registerDto.setName("Andres");
        registerDto.setEmail("andres@gmail.com");
        registerDto.setPassword("123456789");

        //loginDto.setUsernameOrEmail("pepeillo");
        //loginDto.setPassword("123456789");
    }

    @Test
    void loginTest() {
        loginDto.setUsernameOrEmail("pepeillo");
        loginDto.setPassword("123456789");
        header.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginDto> request = new HttpEntity<>(loginDto, header);
        ResponseEntity<JWTAuthResponse> result = testRestTemplate.postForEntity("/api/auth/login", request, JWTAuthResponse.class);
        Assertions.assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void loginBadPasswordTest() {
        loginDto.setUsernameOrEmail("pepeillo");
        loginDto.setPassword("33");
        header.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginDto> request = new HttpEntity<>(loginDto, header);
        ResponseEntity<JWTAuthResponse> result = testRestTemplate.postForEntity("/api/auth/login", request, JWTAuthResponse.class);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    }

    @Test
    void loginBadUsernameTest() {
        loginDto.setUsernameOrEmail("Pepon");
        header.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginDto> request = new HttpEntity<>(loginDto, header);
        ResponseEntity<JWTAuthResponse> result = testRestTemplate.postForEntity("/api/auth/login", request, JWTAuthResponse.class);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    }

    @Test
    void loginNullTest() {
        header.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginDto> request = new HttpEntity<>(loginDto, header);
        ResponseEntity<JWTAuthResponse> result = testRestTemplate.postForEntity("/api/auth/login", request, JWTAuthResponse.class);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    }

    @Test
    void registerTest() {
        registerDto.setUsername("andresito");
        header.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegisterDto> request = new HttpEntity<>(registerDto, header);
        ResponseEntity<String> result = testRestTemplate.postForEntity("/api/auth/register", request, String.class);
        Assertions.assertEquals(HttpStatus.CREATED, result.getStatusCode());
    }

    @Test
    void registerAlreadyExistTest() {
        registerDto.setUsername("pepeillo");
        header.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegisterDto> request = new HttpEntity<>(registerDto, header);
        ResponseEntity<String> result = testRestTemplate.postForEntity("/api/auth/register", request, String.class);
        Assertions.assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void registerNullTest() {
        header.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegisterDto> request = new HttpEntity<>(registerDto, header);
        ResponseEntity<String> result = testRestTemplate.postForEntity("/api/auth/register", request, String.class);
        Assertions.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    }



}