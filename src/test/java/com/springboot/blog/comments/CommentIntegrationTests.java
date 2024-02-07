package com.springboot.blog.comments;
import com.springboot.blog.entity.Comment;
import com.springboot.blog.entity.Post;
import com.springboot.blog.payload.CommentDto;
import com.springboot.blog.repository.PostRepository;
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

import java.util.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles({"integration-test"})
@Sql(value = "classpath:import-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "classpath:import-category.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "classpath:import-posts.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "classpath:import-comments.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "classpath:delete-comments.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class CommentIntegrationTests {
    @Autowired
    TestRestTemplate restTemplate;
    @Autowired
    PostRepository postRepository;
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @Autowired
    ModelMapper mapper;

    String token;
    CommentDto c = new CommentDto();
    HttpHeaders header = new HttpHeaders();

    @BeforeEach
    void setup(){
        Collection<GrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
        Authentication auth = new UsernamePasswordAuthenticationToken("pepeillo","123456789",authorities);

        restTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        token = jwtTokenProvider.generateToken(auth);
        header.setBearerAuth(token);

        c.setId(1L);
        c.setName("name");
        c.setBody("body over 10 characters long");
        c.setEmail("email@email.com");
    }

    @Test
    void getCommentsByPostId_WithValidId_Test(){
        // No es necesario usar "exchange" porque no devuelve ResponseEntity
        // y por tanto devuelve un 200 OK aunque no se haya encontrado nada
        Comment[] comments = restTemplate.getForObject("/api/v1/posts/{postId}/comments", Comment[].class, 1000);
        Assertions.assertEquals(5, comments.length);
        Assertions.assertEquals("Creighton", comments[0].getName());
    }

    @Test
    void getCommentsByPostId_WithInvalidId_Test(){
        Comment[] comments = restTemplate.getForObject("/api/v1/posts/{postId}/comments", Comment[].class, 10);
        Assertions.assertEquals(0, comments.length);
    }

    @Test
    void createComment_ValidPostId_Test(){
        HttpHeaders header = new HttpHeaders();
        header.setBearerAuth(token);
        HttpEntity<CommentDto> entity = new HttpEntity<>(c,header);

        ResponseEntity<CommentDto> response = restTemplate.exchange("/api/v1/posts/{postId}/comments",
                HttpMethod.POST, entity, CommentDto.class, 1000);

        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assertions.assertEquals("name", Objects.requireNonNull(response.getBody()).getName());
    }

    @Test
    void createComment_InvalidPostId_Test(){
        HttpHeaders header = new HttpHeaders();
        header.setBearerAuth(token);
        HttpEntity<CommentDto> entity = new HttpEntity<>(c,header);

        ResponseEntity<CommentDto> response = restTemplate.exchange("/api/v1/posts/{postId}/comments",
                HttpMethod.POST, entity, CommentDto.class, 10);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getCommentsByValidIdTest(){
        HttpEntity<HttpHeaders> entity = new HttpEntity<>(header);

        ResponseEntity<CommentDto> response = restTemplate.exchange("/api/v1/posts/{postId}/comments/{id}",
                HttpMethod.GET,
                entity,
                CommentDto.class,
                1000,1);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Creighton", Objects.requireNonNull(response.getBody()).getName());
    }

    @Test
    void getCommentsBy_InvalidId_Test(){
        HttpEntity<HttpHeaders> entity = new HttpEntity<>(header);

        ResponseEntity<CommentDto> response = restTemplate.exchange("/api/v1/posts/{postId}/comments/{id}",
                HttpMethod.GET,
                entity,
                CommentDto.class,
                1,10);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void updateComment_ValidComment_Test(){
        c.setName("updated name");
        HttpEntity<CommentDto> entity = new HttpEntity<>(c,header);

        ResponseEntity<CommentDto> response = restTemplate.exchange("/api/v1/posts/{postId}/comments/{id}",
                HttpMethod.PUT,
                entity,
                CommentDto.class,
                1000,1);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("updated name", Objects.requireNonNull(response.getBody()).getName());
    }

    @Test
    void updateComment_InvalidComment_Test(){
        c.setName(null);
        HttpEntity<CommentDto> entity = new HttpEntity<>(c,header);

        ResponseEntity<CommentDto> response = restTemplate.exchange("/api/v1/posts/{postId}/comments/{id}",
                HttpMethod.PUT,
                entity,
                CommentDto.class,
                1,1);

        Assertions.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assertions.assertEquals("Name should not be null or empty", Objects.requireNonNull(response.getBody()).getName());
    }

    @Test
    void updateComment_InvalidPostId_Test(){
        HttpEntity<CommentDto> entity = new HttpEntity<>(c,header);

        ResponseEntity<CommentDto> response = restTemplate.exchange("/api/v1/posts/{postId}/comments/{id}",
                HttpMethod.PUT,
                entity,
                CommentDto.class,
                10,1);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteComment_Valid_Test(){
        HttpEntity<HttpHeaders> entity = new HttpEntity<>(header);
        Optional<Post> pOpt = postRepository.findById(1000L);
        Post p = pOpt.get();
        Comment comment = mapper.map(c, Comment.class);

        p.setComments(Set.of(comment));

        ResponseEntity<String> response = restTemplate.exchange("/api/v1/posts/{postId}/comments/{id}",
                HttpMethod.DELETE,
                entity,
                String.class,
                1000,1);

        boolean matches = p.getComments().stream().anyMatch(commentInPost -> commentInPost.getId() == 1);

        Assertions.assertEquals(HttpStatus.OK,response.getStatusCode());
        // Falla porque no se borra de la tabla de Posts, aunque sí de la de Comments
        Assertions.assertFalse(matches);
    }

    @Test
    void deleteComment_InvalidCommentId_Test(){
        HttpEntity<HttpHeaders> entity = new HttpEntity<>(header);

        ResponseEntity<String> response = restTemplate.exchange("/api/v1/posts/{postId}/comments/{id}",
                HttpMethod.DELETE,
                entity,
                String.class,
                1,10);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void deleteComment_InvalidPostId_Test(){
        HttpEntity<HttpHeaders> entity = new HttpEntity<>(header);

        ResponseEntity<String> response = restTemplate.exchange("/api/v1/posts/{postId}/comments/{id}",
                HttpMethod.DELETE,
                entity,
                String.class,
                10,1);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
