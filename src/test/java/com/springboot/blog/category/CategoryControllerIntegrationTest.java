package com.springboot.blog.category;

import com.springboot.blog.entity.Category;
import com.springboot.blog.payload.CategoryDto;
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
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles({"integration-test"})
@Sql(value = "classpath:import-users.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "classpath:import-category.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "classpath:delete-category.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class CategoryControllerIntegrationTest {

    @Autowired
    TestRestTemplate testRestTemplate;
    @Autowired
    JwtTokenProvider jwtTokenProvider;

    String token;
    CategoryDto categoryDto;

    Category category;
    HttpHeaders header= new HttpHeaders();

    ModelMapper modelMapper = new ModelMapper();

    @BeforeEach
    void setUp(){
        Collection<GrantedAuthority> authorities = Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"));
        Authentication auth = new UsernamePasswordAuthenticationToken("Cliff_User","06821235",authorities);

        testRestTemplate.getRestTemplate().setRequestFactory(new HttpComponentsClientHttpRequestFactory());
        token = jwtTokenProvider.generateToken(auth);
        header.setBearerAuth(token);

        category= new Category();
        CategoryDto categoryDto= new CategoryDto();

        categoryDto.setId(1L);
        categoryDto.setName("name");
        categoryDto.setDescription("description");

        category= modelMapper.map(categoryDto, Category.class);
    }
    @Test
    void createdCategoryTest201(){
        HttpHeaders auth= new HttpHeaders();
        auth.setBearerAuth(token);
        HttpEntity<CategoryDto> category = new HttpEntity<>(categoryDto, auth);

        ResponseEntity<CategoryDto> result= testRestTemplate.exchange("",
                HttpMethod.POST, category, CategoryDto.class);
        System.err.println(result);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
    }
    @Test
    void createCategoryTestBadRequest() {
        HttpHeaders auth = new HttpHeaders();
        auth.setBearerAuth(token);
        HttpEntity<PostDto> category = new HttpEntity<>(new PostDto(), auth);

        ResponseEntity<PostDto> result = testRestTemplate.exchange("/api/v1/categories",
                HttpMethod.POST, category, PostDto.class
        );

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }
    @Test
    void findCategoryById_200OK(){
        HttpEntity<CategoryDto> category = new HttpEntity<>(categoryDto);

        ResponseEntity<CategoryDto> result= testRestTemplate.exchange("/api/v1/categories{id}",
                HttpMethod.GET, category, CategoryDto.class, 10);

        assertEquals(HttpStatus.OK, result.getStatusCode());
    }
    @Test
    void findCategoryById_404NotFound() {

        HttpEntity<CategoryDto> category = new HttpEntity<>(categoryDto);

        ResponseEntity<CategoryDto> result = testRestTemplate.exchange("/api/v1/categories{id}",
                HttpMethod.GET, category, CategoryDto.class, 13
        );

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }
    @Test
    void updateCategory_200OK() {

        HttpHeaders auth = new HttpHeaders();
        auth.setBearerAuth(token);
        HttpEntity<CategoryDto> category = new HttpEntity<>(categoryDto, auth);

        ResponseEntity<CategoryDto> result = testRestTemplate.exchange("/api/v1/categories{id}",
                HttpMethod.PUT, category, CategoryDto.class, 100
        );

        assertEquals(HttpStatus.OK, result.getStatusCode());

    }
    @Test
    void updateCategory_404NotFound() {

        HttpHeaders auth = new HttpHeaders();
        auth.setBearerAuth(token);
        HttpEntity<CategoryDto> category = new HttpEntity<>(categoryDto, auth);

        ResponseEntity<CategoryDto> result = testRestTemplate.exchange("/api/v1/categories{id}",
                HttpMethod.PUT, category, CategoryDto.class, 10000
        );

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }
    @Test
    void updateCategory_BadRequest() {

        HttpHeaders auth = new HttpHeaders();
        auth.setBearerAuth(token);
        HttpEntity<CategoryDto> category = new HttpEntity<>(new CategoryDto(), auth);

        ResponseEntity<CategoryDto> result = testRestTemplate.exchange("/api/v1/categories{id}",
                HttpMethod.PUT, category, CategoryDto.class, 1000
        );

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }


}
