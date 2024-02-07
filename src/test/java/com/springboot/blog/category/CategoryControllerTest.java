package com.springboot.blog.category;

import com.springboot.blog.controller.CategoryController;
import com.springboot.blog.entity.Category;
import com.springboot.blog.payload.CategoryDto;
import com.springboot.blog.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
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

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    private Category c;

    @BeforeEach
    public void setUp(){
        categoryDto= new CategoryDto(1L,"Name","description");
    }

    @Test
    @WithMockUser(username = "pepeillo", roles = {"ADMIN"})
    void createCategoryTestControllerIsValid201() throws Exception{
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
    void getCategoryByIdTestController() throws Exception {
        Category category= new Category();
        Long categoryId= 1L;
        category.setId(categoryId);
        CategoryDto categoryDto= new CategoryDto();
        categoryDto.setId(category.getId());
        CategoryDto cat1= categoryService.getCategory(categoryDto.getId());

        when(categoryService.getCategory(categoryId)).thenReturn(cat1);
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/categories/{id}", categoryId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());


    }
    @Test
    void getCategories() throws Exception {
        CategoryDto categoryDto = new CategoryDto(1L, "name", "description");
        List<CategoryDto> categoryList = List.of(
                categoryDto
        );
        Mockito.when(categoryService.getAllCategories()).thenReturn(categoryList);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$",hasSize(1)))
                .andExpect(content().json(objectMapper.writeValueAsString(categoryList)));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    void updateCategoryTestIsValid200() throws Exception {
        CategoryDto updateCategory = new CategoryDto();
        updateCategory.setName("new name");
        updateCategory.setDescription("new description");

        when(categoryService.updateCategory( Mockito.any(CategoryDto.class), anyLong()))
                .thenReturn(updateCategory);

        mockMvc.perform(put("/api/v1/categories/{id}", c.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateCategory)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$",notNullValue()))
                .andExpect(jsonPath("$.description",is("new description")));

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
