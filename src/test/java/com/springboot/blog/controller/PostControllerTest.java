package com.springboot.blog.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.blog.entity.Category;
import com.springboot.blog.entity.Post;
import com.springboot.blog.exception.ResourceNotFoundException;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.awt.print.Pageable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test"})
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

        category = new Category();
        category.setId(1L);
        category.setName("Example Category");

        postDto.setId(1L);
        postDto.setTitle("titulo 1");
        postDto.setDescription("eferfdvdvdffdvdbtdbtbtrbtb");
        postDto.setContent("wefewf");
        postDto.setCategoryId(category.getId());

        post = mapper.map(postDto, Post.class);
    }

    @Test
    @WithMockUser(username = "Fran", roles = {"ADMIN"})
    void createPost() throws Exception {

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
    @WithMockUser(username = "Fran", roles = {"ADMIN"})
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
    @WithMockUser(username = "Fran", roles = {"ADMIN"})
    void getAllPosts() throws Exception {

        PostResponse postResponse = new PostResponse(List.of(postDto), 0, 10, 2, 1, true);

        when(postService.getAllPosts(0, 10, "id", "asc")).thenReturn(postResponse);

        MvcResult mvcResult = mvc.perform(get("/api/posts")
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andReturn();
        System.out.println(mvcResult.getResponse().getContentAsString());
        String actualResponse = mvcResult.getResponse().getContentAsString();
        assertThat(actualResponse).isEqualToIgnoringWhitespace(objectMapper.writeValueAsString(postResponse));


    }

    @Test
    void getPostById() throws Exception {

        when(postService.getPostById(anyLong())).thenReturn(postDto);

        mvc.perform(get("/api/posts/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()));

    }

    @Test
    void getPostByIdNotFound() throws Exception {


        when(postService.getPostById(anyLong())).thenThrow(new ResourceNotFoundException("Post", "id", 1));

        mvc.perform(get("/api/posts/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound());

    }


    @Test
    @WithMockUser(username = "Fran", roles = {"ADMIN"})
    void updatePost() throws Exception{

        PostDto postDtoEdit = new PostDto();
        postDtoEdit.setId(1L);
        postDtoEdit.setTitle("titulo editado");
        postDtoEdit.setDescription("eferfdvdvdffdvdbtdbtbtrbtb");
        postDtoEdit.setContent("wefewf");
        postDtoEdit.setCategoryId(category.getId());

        when(postService.updatePost(postDtoEdit, 1L)).thenReturn(postDtoEdit);

        mvc.perform(put("/api/posts/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(this.objectMapper.writeValueAsString(postDtoEdit))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.title", is("titulo editado")));

    }

    @Test
    void deletePost() {



    }

    @Test
    void getPostsByCategory() {
    }
}