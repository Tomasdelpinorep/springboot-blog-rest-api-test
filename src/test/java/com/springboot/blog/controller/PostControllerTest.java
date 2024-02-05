package com.springboot.blog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.blog.entity.Category;
import com.springboot.blog.entity.Post;
import com.springboot.blog.payload.CommentDto;
import com.springboot.blog.payload.PostDto;
import com.springboot.blog.service.PostService;
import com.springboot.blog.service.impl.PostServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;

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
        post = new Post();

        CommentDto commentDto = new CommentDto();

        category = new Category();
        category.setId(1L);
        category.setName("Example Category");


        postDto.setId(1L);
        postDto.setTitle("titulo 1");
        postDto.setDescription("eferf");
        postDto.setContent("wefewf");
        postDto.setCategoryId(category.getId());

        post = mapper.map(postDto, Post.class);
    }

    @Test
    void createPost() {
    }

    @Test
    void getAllPosts() {
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