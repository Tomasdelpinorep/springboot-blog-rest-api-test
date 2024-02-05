package com.springboot.blog.comments;
import com.springboot.blog.entity.Comment;
import com.springboot.blog.payload.CommentDto;
import com.springboot.blog.security.JwtTokenProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
import org.testcontainers.utility.DockerImageName;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles({"test"})
@Testcontainers
@Sql(value = "classpath:import-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "classpath:import-roles.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "classpath:import-posts.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "classpath:import-comments.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "classpath:delete-comments.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class CommentIntegrationTests {
    @Autowired
    TestRestTemplate restTemplate;
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    String token;
    CommentDto c = new CommentDto();
    HttpHeaders header = new HttpHeaders();

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))
            .withUsername("postgres")
            .withPassword("12345678")
            .withDatabaseName("test");

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
    void getCommentsByPostIdWithValidIdTest(){
        // No es necesario usar "exchange" porque no devuelve ResponseEntity
        // y por tanto devuelve un 200 OK aunque no se haya encontrado nada
        Comment[] comments = restTemplate.getForObject("/api/v1/posts/{postId}/comments", Comment[].class, 1);
        Assertions.assertEquals(5, comments.length);
    }

    @Test
    void createCommentValidPostIdTest(){
        HttpHeaders header = new HttpHeaders();
        header.setBearerAuth(token);
        HttpEntity<CommentDto> entity = new HttpEntity<>(c,header);

        ResponseEntity<CommentDto> response = restTemplate.exchange("/api/v1/posts/{postId}/comments",
                HttpMethod.POST, entity, CommentDto.class, 1);

        Assertions.assertEquals(HttpStatus.CREATED, response.getStatusCode());
        Assertions.assertEquals("name", response.getBody().getName());
    }

    @Test
    void createCommentInvalidPostId(){
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
                1,1);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("Creighton", Objects.requireNonNull(response.getBody()).getName());
    }

    @Test
    void getCommentsByInValidIdTest(){
        HttpEntity<HttpHeaders> entity = new HttpEntity<>(header);

        ResponseEntity<CommentDto> response = restTemplate.exchange("/api/v1/posts/{postId}/comments/{id}",
                HttpMethod.GET,
                entity,
                CommentDto.class,
                1,10);

        Assertions.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void updateCommentValidUpdateTest(){
        c.setName("updated name");
        HttpEntity<CommentDto> entity = new HttpEntity<>(c,header);

        ResponseEntity<CommentDto> response = restTemplate.exchange("/api/v1/posts/{postId}/comments/{id}",
                HttpMethod.PUT,
                entity,
                CommentDto.class,
                1,1);

        Assertions.assertEquals(HttpStatus.OK, response.getStatusCode());
        Assertions.assertEquals("updated name", Objects.requireNonNull(response.getBody()).getName());
    }

    @Test
    void updateCommentInValidUpdateTest(){
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
}
