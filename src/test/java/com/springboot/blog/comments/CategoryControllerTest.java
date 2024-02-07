package com.springboot.blog.comments;

import com.springboot.blog.controller.CategoryController;
import com.springboot.blog.payload.CategoryDto;
import com.springboot.blog.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@SpringBootTest
@ActiveProfiles({"test"})
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private CategoryDto categoryDto;

    @BeforeEach
    public void setUp(){
        categoryDto= new CategoryDto(1L,"Name","description");
    }

    @Test
    @WithMockUser(username = "pepeillo", roles = {"ADMIN"})
    void createCategoryTestControllerIsValid() throws Exception{
        when(categoryService.addCategory(categoryDto)).thenReturn(categoryDto);

        mockMvc.perform(post("/api/v1/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(categoryDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", notNullValue()))
                .andExpect(jsonPath("$.description", is("description")));
    }
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void createdCategoryTestControllerHttp400() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/categories")
                        .content(objectMapper.writeValueAsString(null))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        verify(categoryService, never()).addCategory(categoryDto);

    }
    @Test
    @WithMockUser(username = "pepeillo", roles = {"USER", "ADMIN"})
    void getCategoryByIdTestController(){

    }
    @Test
    void notAuthorizedTest401() throws Exception{
        mockMvc.perform(MockMvcRequestBuilders.post("/api/v1/categories")
                        .content(objectMapper.writeValueAsString(categoryDto))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
        verify(categoryService, never()).addCategory(categoryDto);

    }
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteCategoryTest200() throws Exception {
        Long categoryId = 1L;

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/categories/{id}", categoryId))
                .andExpect(status().isOk());

        verify(categoryService).deleteCategory(categoryId);
    }
    @Test
    @WithMockUser(roles = {"ADMIN"})
    void deleteCategoryNotExistAdminTest400() throws Exception {
        Long falseCategoryId = 34567L;

        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/categories/{id}","null"))
                .andExpect(status().isBadRequest());

        verify(categoryService, never()).deleteCategory(falseCategoryId);
    }

    @Test
    @WithMockUser()
    void deleteCategoryUserRoleTest401() throws Exception {
        Long categoryId = 1L;
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/v1/categories/{id}", categoryId))
                .andExpect(status().isUnauthorized());

        verify(categoryService, never()).deleteCategory(categoryId);
    }
}
