package com.springboot.blog.service.impl;

import com.springboot.blog.entity.Role;
import com.springboot.blog.entity.User;
import com.springboot.blog.exception.BlogAPIException;
import com.springboot.blog.payload.LoginDto;
import com.springboot.blog.payload.RegisterDto;
import com.springboot.blog.repository.RoleRepository;
import com.springboot.blog.repository.UserRepository;
import com.springboot.blog.security.JwtTokenProvider;
import com.springboot.blog.service.AuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles({"test"})
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private RoleRepository roleRepository;
    @InjectMocks
    private AuthServiceImpl authService;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    private User user;
    private LoginDto loginDto;
    private LoginDto loginDto2;
    private RegisterDto registerDto;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        user = new User(1L, "Pepe", "pepeillo", "pepeillo@gmail.com", "123456789", null);
        loginDto = new LoginDto("pepeillo", "123456789");
        loginDto2 = new LoginDto("nonexist", "3213123132");
        registerDto = new RegisterDto("Ale", "jimenez", "jimenezale@gmail.com", "123456789");
        authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginDto.getUsernameOrEmail(), loginDto.getPassword()));
    }

    @Test
    void loginTest() {
        Mockito.when(authenticationManager.authenticate(Mockito.any())).thenReturn(authentication);
        Mockito.when(jwtTokenProvider.generateToken(Mockito.any())).thenReturn("wasd");

        assertEquals("wasd", authService.login(loginDto2));
    }

    @Test
    void registerTest() {
        Mockito.when(userRepository.existsByUsername(registerDto.getUsername())).thenReturn(false);
        Mockito.when(userRepository.existsByEmail(registerDto.getEmail())).thenReturn(false);
        Mockito.when(roleRepository.findByName("ROLE_USER")).thenReturn(Optional.of(new Role((short) 1, "USER")));
        Assertions.assertEquals("User registered successfully!.", authService.register(registerDto));
    }

    @Test
    void registerTestUsernameAlreadyExist() {
        Mockito.when(userRepository.existsByUsername(registerDto.getUsername())).thenReturn(true);
        BlogAPIException exception = Assertions.assertThrows(BlogAPIException.class, () ->
                authService.register(registerDto));
    }

    @Test
    void registerTestEmailAlreadyExist() {
        Mockito.when(userRepository.existsByEmail(registerDto.getEmail())).thenReturn(true);
        BlogAPIException exception = Assertions.assertThrows(BlogAPIException.class, () ->
                authService.register(registerDto));
    }
}