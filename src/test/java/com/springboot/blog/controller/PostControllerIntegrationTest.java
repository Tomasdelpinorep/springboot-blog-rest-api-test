package com.springboot.blog.controller;

import com.springboot.blog.entity.Category;
import com.springboot.blog.entity.Post;
import com.springboot.blog.payload.PostDto;
import com.springboot.blog.payload.PostResponse;
import com.springboot.blog.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.apache.commons.lang3.ObjectUtils;
import org.testcontainers.utility.DockerImageName;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles({"integration-test"})
//@Testcontainers
@Sql(value = "classpath:import-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "classpath:import-category.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "classpath:import-posts.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class PostControllerIntegrationTest {

    @Autowired
    TestRestTemplate restTemplate;
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    String token;

    private Category category;
    private PostDto postDto;

    private Post post;

    ModelMapper mapper = new ModelMapper();
    HttpHeaders header = new HttpHeaders();


    @BeforeEach
    void setUp() {
        Collection<GrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"));
        Authentication auth = new UsernamePasswordAuthenticationToken("Cliff_User","06821235",authorities);

        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        token = jwtTokenProvider.generateToken(auth);
        header.setBearerAuth(token);

        category = new Category();
        postDto = new PostDto();
        post = new Post();

        category = new Category();
        category.setId(1L);
        category.setName("Example Category");

        postDto.setId(1);
        postDto.setTitle("titulo 1");
        postDto.setDescription("eferfdvdvdffdvdbtdbtbtrbtb");
        postDto.setContent("wefewf");
        postDto.setCategoryId(category.getId());

        post = mapper.map(postDto, Post.class);
    }

    @Test
    void createPost() {
        HttpHeaders auth = new HttpHeaders();
        auth.setBearerAuth(token);
        HttpEntity<PostDto> entity = new HttpEntity<>(postDto, auth);

        ResponseEntity<PostDto> result = restTemplate.exchange("/api/posts",
                HttpMethod.POST, entity, PostDto.class
                );
        System.err.println(result);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
    }

    @Test
    void createPostBadRequest() {
        HttpHeaders auth = new HttpHeaders();
        auth.setBearerAuth(token);
        HttpEntity<PostDto> entity = new HttpEntity<>(new PostDto(), auth);

        ResponseEntity<PostDto> result = restTemplate.exchange("/api/posts",
                HttpMethod.POST, entity, PostDto.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void getAllPosts() {

        PostResponse result = restTemplate.getForObject("/api/posts", PostResponse.class);
        assertEquals(0, result.getPageNo());
        assertEquals(10, result.getPageSize());
        assertEquals(1, result.getTotalPages());

    }

    @Test
    void getPostById() {

        HttpEntity<PostDto> entity = new HttpEntity<>(postDto);

        ResponseEntity<PostDto> result = restTemplate.exchange("/api/posts/{id}",
                HttpMethod.GET, entity, PostDto.class, 1000
                );

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(1000L, result.getBody().getId());

    }

    @Test
    void getPostByIdNotFound() {

        HttpEntity<PostDto> entity = new HttpEntity<>(postDto);

        ResponseEntity<PostDto> result = restTemplate.exchange("/api/posts/{id}",
                HttpMethod.GET, entity, PostDto.class, 5
        );

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    void updatePost() {

       HttpHeaders auth = new HttpHeaders();
       auth.setBearerAuth(token);
       HttpEntity<PostDto> entity = new HttpEntity<>(postDto, auth);

       ResponseEntity<PostDto> result = restTemplate.exchange("/api/posts/{id}",
               HttpMethod.PUT, entity, PostDto.class, 1000
               );

       assertEquals(HttpStatus.OK, result.getStatusCode());

    }

    @Test
    void updatePostNotFound() {

        HttpHeaders auth = new HttpHeaders();
        auth.setBearerAuth(token);
        HttpEntity<PostDto> entity = new HttpEntity<>(postDto, auth);

        ResponseEntity<PostDto> result = restTemplate.exchange("/api/posts/{id}",
                HttpMethod.PUT, entity, PostDto.class, 10000
        );

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    void updatePostBadRequest() {

        HttpHeaders auth = new HttpHeaders();
        auth.setBearerAuth(token);
        HttpEntity<PostDto> entity = new HttpEntity<>(new PostDto(), auth);

        ResponseEntity<PostDto> result = restTemplate.exchange("/api/posts/{id}",
                HttpMethod.PUT, entity, PostDto.class, 1000
        );

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void deletePost() {
        HttpHeaders auth = new HttpHeaders();
        auth.setBearerAuth(token);
        HttpEntity<String> entity = new HttpEntity<>(auth);

        ResponseEntity<String> result = restTemplate.exchange("/api/posts/{id}",
                HttpMethod.DELETE, entity, String.class, 1000
                );

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }

    @Test
    void getPostsByCategory() {
        HttpEntity<List<PostDto>> entity = new HttpEntity<>(List.of(postDto));

        ResponseEntity<PostDto[]> result = restTemplate.exchange("/api/posts/category/{id}",
                HttpMethod.GET, entity, PostDto[].class, 1
                );

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }
}