package com.springboot.blog.comments;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.blog.entity.Comment;
import com.springboot.blog.entity.Post;
import com.springboot.blog.payload.CommentDto;
import com.springboot.blog.service.impl.CommentServiceImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles({"test"})
class CommentControllerTest {

    @Autowired
    MockMvc mvc;
    @MockBean
    CommentServiceImpl commentService;
    @Autowired
    ObjectMapper objectMapper;

    static ModelMapper mapper = new ModelMapper();
    static Comment comment = new Comment();
    static Post p = Mockito.mock(Post.class);
    static CommentDto c = new CommentDto();

    @BeforeAll
    static void init(){
        c.setName("name");
        c.setBody("body that must be of at least 10 characters");
        c.setEmail("email@email.com");

        comment = mapper.map(c,Comment.class);
        comment.setPost(p);
    }

//    @BeforeEach
//    void setUp() throws Exception{
//        ResultActions resultActions = this.mvc
//                .perform(post("/api/auth/login")
//                        .with(httpBasic("Tom","12345")));
//        MvcResult mvcResult = resultActions.andDo(print()).andReturn();
//        String contentAsString = mvcResult.getRequest().getContentAsString();
//        JSONObject json = new JSONObject(contentAsString);
//        token = "Bearer" + json.getJSONObject("data").getString("token");
//    }


    @Test
    @WithMockUser(username="Tom", roles = {"USER","ADMIN"})
    void createCommentTest() throws Exception {
        when(commentService.createComment(p.getId(),c)).thenReturn(c);

        mvc.perform(post("/api/v1/posts/{postId}/comments",p.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(c)))
//                        .header("Authorization",token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$",notNullValue()))
                .andExpect(jsonPath("$.name",is("name")));
    }

    @Test
    @WithMockUser(username = "Tom",roles = {"USER","ADMIN"})
    void getCommentsByPostIdTest() throws Exception {
        when(commentService.getCommentsByPostId(anyLong())).thenReturn(List.of(c));

        mvc.perform(get("/api/v1/posts/{postId}/comments",p.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$",notNullValue()))
                .andExpect(jsonPath("$",hasSize(1)))
                .andExpect(jsonPath("$[0].name",is("name")));
    }

    @Test
    @WithMockUser(username = "Tom",roles = {"USER","ADMIN"})
    void getCommentByIdTest() throws Exception{
        when(commentService.getCommentById(anyLong(),anyLong())).thenReturn(c);

        mvc.perform(get("/api/v1/posts/{postId}/comments/{commentId}", p.getId(), c.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$",notNullValue()))
                .andExpect(jsonPath("$.name",is("name")));
    }

    @Test
    @WithMockUser(username = "Tom",roles = {"USER","ADMIN"})
    void updateCommentTest() throws Exception {
        CommentDto updatedComment = new CommentDto();
        updatedComment.setName("updated name");
        updatedComment.setBody("updated body with more than 10 characters");
        updatedComment.setEmail("updatedemail@email.com");

        when(commentService.updateComment(anyLong(),anyLong(),Mockito.any(CommentDto.class)))
                .thenReturn(updatedComment);

        mvc.perform(put("/api/v1/posts/{postId}/comments/{commentId}", p.getId(), c.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedComment)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$",notNullValue()))
                .andExpect(jsonPath("$.name",is("updated name")));

    }

    @Test
    @WithMockUser(username = "Tom",roles = {"USER","ADMIN"})
    void deleteCommentTest() throws Exception {
        doNothing().when(commentService).deleteComment(anyLong(),anyLong());

        mvc.perform(delete("/api/v1/posts/{postId}/comments/{commentId}",p.getId(),c.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Comment deleted successfully"));

        verify(commentService,times(1)).deleteComment(anyLong(),anyLong());
        verifyNoMoreInteractions(commentService);
    }

}