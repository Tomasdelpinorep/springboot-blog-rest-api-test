package com.springboot.blog.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.blog.entity.Category;
import com.springboot.blog.entity.Post;
import com.springboot.blog.payload.CommentDto;
import com.springboot.blog.payload.PostDto;
import com.springboot.blog.payload.PostResponse;
import com.springboot.blog.service.PostService;
import com.springboot.blog.service.impl.PostServiceImpl;
import io.jsonwebtoken.lang.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.awt.print.Pageable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PostControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    PostServiceImpl postService;

    private Category category;
    private PostDto postDto;
    private Post post;

    ModelMapper mapper = new ModelMapper();

    @BeforeEach
    void setUp() {

        category = new Category();
        postDto = new PostDto();
//        postDto2 = new PostDto();
        post = new Post();

        CommentDto commentDto = new CommentDto();

        category = new Category();
        category.setId(1L);
        category.setName("Example Category");


        postDto.setId(1L);
        postDto.setTitle("titulo 1");
        postDto.setDescription("eferfdvdvdffdvdbtdbtbtrbtb");
        postDto.setContent("wefewf");
        postDto.setCategoryId(category.getId());

//        PostDto postDto2 = new PostDto();
//        postDto2.setId(2L);
//        postDto2.setTitle("titulo 2");
//        postDto2.setDescription("eferfewferfergfef");
//        postDto2.setContent("wefewf");
//        postDto2.setCategoryId(category.getId());

        post = mapper.map(postDto, Post.class);
    }

    @Test
    @WithMockUser(username = "Fran", roles = {"USER", "ADMIN"})
    void createPost() throws Exception {
//        PostDto postDtoDB = new PostDto();
//        postDtoDB.setId(1L);
//        postDtoDB.setTitle("titulo 1");
//        postDtoDB.setDescription("efer");
//        postDtoDB.setContent("wefewf");
//        postDtoDB.setCategoryId(category.getId());

        when(postService.createPost(any())).thenReturn(postDto);

        mvc.perform(post("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postDto))
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("titulo 1")));


    }

    @Test
    @WithMockUser(username = "Fran", roles = {"USER", "ADMIN"})
    void createPostBadRequest() throws Exception {
        PostDto postDtoDB = new PostDto();
        postDtoDB.setId(1L);
        postDtoDB.setTitle("titulo 1");
        postDtoDB.setDescription("eferf");
        postDtoDB.setContent("wefewf");
        postDtoDB.setCategoryId(category.getId());

        when(postService.createPost(any())).thenReturn(postDtoDB);

        mvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDtoDB))
                )
                .andExpect(status().isBadRequest());


    }

    @Test
    @WithMockUser(username = "Fran", roles = {"USER", "ADMIN"})
    void getAllPosts() throws Exception {

//        List.of(postDto, postDto2);

//        PostResponse postResponse = new PostResponse(, 0, 10, 2, 1, true);

//        when(postService.getAllPosts(0, 10, "title", "asc")).thenReturn();
//
//        mvc.perform(get("/api/posts")
//                .contentType(MediaType.APPLICATION_JSON)
//        )
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$", hasSize(2)));
//
    }

    @Test
    void getPostById() {
    }

    @Test
    void updatePost() {
    }

    @Test
    void deletePost() {
    }

    @Test
    void getPostsByCategory() {
    }
}