package com.springboot.blog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.blog.payload.LoginDto;
import com.springboot.blog.payload.RegisterDto;
import com.springboot.blog.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test"})
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    AuthServiceImpl authService;

    @Autowired
    ObjectMapper objectMapper;
    static LoginDto loginDto = new LoginDto();
    static RegisterDto registerDto = new RegisterDto();

    @BeforeEach
    void setUp() {
        registerDto.setName("Andres");
        registerDto.setEmail("andres@gmail.com");
        registerDto.setUsername("andresito");
        registerDto.setPassword("123456789");

        loginDto.setUsernameOrEmail("pepeillo");
        loginDto.setPassword("123456789");
    }

    @Test
    void loginTest() throws Exception {
        String token = "wasd";
        Mockito.when(authService.login(Mockito.any())).thenReturn(token);

        mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(loginDto))
                ).andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.accessToken", is(token)));

    }

    @Test
    void registerTest() throws Exception {
        String response = "User registered successfully!.";
        Mockito.when(authService.register(Mockito.any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDto))
                ).andExpect(status().isCreated())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$", is(response)));
    }
}