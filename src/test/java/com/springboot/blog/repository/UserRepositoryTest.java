package com.springboot.blog.repository;

import com.springboot.blog.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@Testcontainers
@ActiveProfiles({"test"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(value = "classpath:create-user.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = "classpath:delete-user.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class UserRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer(DockerImageName.parse("postgres:16-alpine"))
            .withUsername("postgres")
            .withPassword("12345678")
            .withDatabaseName("myblog");

    @Autowired
    private UserRepository userRepository;

    @Test
    public void existUserByUsername (){
        Assertions.assertTrue(userRepository.existsByUsername("Nadya_User"));
        Assertions.assertTrue(userRepository.existsByUsername("pepeillo"));
        Assertions.assertFalse(userRepository.existsByUsername("Pepeillo"));
        Assertions.assertFalse(userRepository.existsByUsername("usuario"));
    }

    @Test
    public void findUserByUsernameOrEmailTest (){
        User u = new User(1L, "Pepe", "pepeillo", "pepeillo@gmail.com", "123456789", null);
        Assertions.assertTrue(userRepository.findByUsernameOrEmail("pepeillo", "pepeillo@gmail.com").isPresent());
        Assertions.assertTrue(userRepository.findByUsernameOrEmail("pepeillo", "user@gmail.com").isPresent());
        Assertions.assertEquals(u.getId(), userRepository.findByUsernameOrEmail("pepeillo", "pepeillo@gmail.com").get().getId());
        Assertions.assertTrue(userRepository.findByUsernameOrEmail("user", "pepeillo@gmail.com").isPresent());
        Assertions.assertFalse(userRepository.findByUsernameOrEmail("user", "user@gmail.com").isPresent());
        Exception exception = assertThrows(IncorrectResultSizeDataAccessException.class, () ->
                userRepository.findByUsernameOrEmail("pepeillo", "rmacmichael0@theglobeandmail.com"));
    }

    @Test
    public void findUserByUsernameTest (){
        User u = new User(1L, "Pepe", "pepeillo", "pepeillo@gmail.com", "123456789", null);
        Assertions.assertTrue(userRepository.findByUsername("pepeillo").isPresent());
        Assertions.assertFalse(userRepository.findByUsername("Pepeillo").isPresent());
        Assertions.assertEquals(u.getId(), userRepository.findByUsername("pepeillo").get().getId());
        Assertions.assertFalse(userRepository.findByUsername("pepeillo@gmail.com").isPresent());
    }

    @Test
    public void existUserByEmail (){
        Assertions.assertTrue(userRepository.existsByEmail("ndivers1@reuters.com"));
        Assertions.assertTrue(userRepository.existsByEmail("pepeillo@gmail.com"));
        Assertions.assertFalse(userRepository.existsByEmail("pepeillo@gail.com"));
        Assertions.assertFalse(userRepository.existsByEmail("usuario@gmail.com"));
    }
}
