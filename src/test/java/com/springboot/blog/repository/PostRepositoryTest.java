package com.springboot.blog.repository;

import com.springboot.blog.entity.Post;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles({"test"})
//@ActiveProfiles({"postgres", "spring-data-jpa"})
@Testcontainers
@Sql(value = "classpath:import-category.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "classpath:import-posts.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class PostRepositoryTest {

    @Autowired
    PostRepository postRepository;

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))
            .withUsername("postgres")
            .withPassword("12345678")
            .withDatabaseName("test");
    @Test
    void findByCategoryId() {

        List<Post> posts = postRepository.findByCategoryId(1L);

        System.out.println(posts);

        assertEquals(1, posts.size());

    }
}