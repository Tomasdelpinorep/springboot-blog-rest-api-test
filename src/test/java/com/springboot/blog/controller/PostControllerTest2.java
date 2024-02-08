package com.springboot.blog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.blog.entity.Category;
import com.springboot.blog.entity.Post;
import com.springboot.blog.exception.ResourceNotFoundException;
import com.springboot.blog.payload.PostDto;
import com.springboot.blog.service.impl.PostServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test"})
class PostControllerTest2 {

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
    void createPost() throws Exception{

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
    @WithMockUser(username = "Fran", roles = "ADMIN")
    void createPostBadRequest() throws Exception{
        PostDto postDto1 = new PostDto();
        postDto1.setId(1L);
        postDto1.setTitle("titulo 1");
        postDto1.setDescription("eferfd");
        postDto1.setContent("wefewf");
        postDto1.setCategoryId(category.getId());

        when(postService.createPost(any(PostDto.class))).thenReturn(postDto1);

        mvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto1))
                )
                .andExpect(status().isBadRequest());

    }

    @Test
    void createPostBadRequestUnauthorized() throws Exception{
        PostDto postDto1 = new PostDto();
        postDto1.setId(1L);
        postDto1.setTitle("titulo 1");
        postDto1.setDescription("eferfd");
        postDto1.setContent("wefewf");
        postDto1.setCategoryId(category.getId());

        when(postService.createPost(any(PostDto.class))).thenReturn(postDto1);

        mvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postDto1))
                )
                .andExpect(status().isUnauthorized());

    }

    @Test
    void getAllPosts() {

    }

    @Test
    void getPostById() throws Exception{

        when(postService.getPostById(anyLong())).thenReturn(postDto);

        mvc.perform(get("/api/posts/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    void getPostByIdNotFound() throws Exception{

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

        when(postService.updatePost(postDtoEdit, 1)).thenReturn(postDtoEdit);

        mvc.perform(put("/api/posts/{id}", 1)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(postDtoEdit))
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", notNullValue()));
    }

    @Test
    void deletePost() {
    }

    @Test
    void getPostsByCategory() {
    }
}