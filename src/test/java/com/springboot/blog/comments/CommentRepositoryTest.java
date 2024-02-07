package com.springboot.blog.comments;

import com.springboot.blog.entity.Comment;
import com.springboot.blog.repository.CommentRepository;
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
@Testcontainers
@Sql(value = "classpath:import-category.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "classpath:import-posts.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "classpath:import-comments.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "classpath:delete-comments.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
class CommentRepositoryTest {

    @Autowired
    CommentRepository commentRepository;

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))
            .withUsername("testUser")
            .withPassword("testPassword")
            .withDatabaseName("testDB");

    @Test
    void findPostByValidIdTest(){
        List<Comment> result = commentRepository.findByPostId(1000L);

        assertEquals(5, result.size());
        assertEquals(result.get(0).getName(), "Creighton");
    }

    @Test
    void findPostByInvalidIdTest(){
        List<Comment> result = commentRepository.findByPostId(10L);

        assertEquals(0, result.size());
    }
}