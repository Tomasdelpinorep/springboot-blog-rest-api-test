package com.springboot.blog.comments;

import com.springboot.blog.entity.Comment;
import com.springboot.blog.entity.Post;
import com.springboot.blog.exception.BlogAPIException;
import com.springboot.blog.exception.ResourceNotFoundException;
import com.springboot.blog.payload.CommentDto;
import com.springboot.blog.repository.CommentRepository;
import com.springboot.blog.repository.PostRepository;
import com.springboot.blog.service.CommentService;
import com.springboot.blog.service.impl.CommentServiceImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {
    @Mock
    static CommentRepository commentRepository;
    @Mock
    static PostRepository postRepository;
    static ModelMapper mapper = new ModelMapper();
    static Post p = new Post();
    static Comment comment = new Comment();
    static CommentDto c = new CommentDto();

    @InjectMocks
    static CommentService commentService = new CommentServiceImpl(commentRepository, postRepository, mapper);

    @BeforeAll
    static void init() {
        c.setId(1L);
        c.setName("Comment name");
        c.setBody("Comment body");
        c.setEmail("email@email.com");

        p.setId(1L);

        comment = mapper.map(c, Comment.class);

        comment.setPost(p);
        p.setComments(Set.of(comment));
    }
    @Test
    void createCommentTest() {
        when(p.getId()).thenReturn(1L);
        when(postRepository.findById(p.getId())).thenReturn(Optional.ofNullable(p));
        when(commentRepository.save(Mockito.any(Comment.class))).thenReturn(comment);

        CommentDto result = commentService.createComment(p.getId(),c);

        assertEquals("Comment name", result.getName());
        verify(commentRepository, times(1)).save(Mockito.any(Comment.class));
        verify(postRepository).findById(eq(1L));
    }

    @Test
    void createComment_WhenPostNotFound_Test(){
        Post wrongPost = Mockito.mock(Post.class);

        assertThrows(ResourceNotFoundException.class, () -> commentService.createComment(wrongPost.getId(),c));
    }

    @Test
    void getCommentsByPostIdTest() {
        when(commentRepository.findByPostId(p.getId())).thenReturn(List.of(comment));

        List<CommentDto> result = commentService.getCommentsByPostId(p.getId());

        assertEquals("Comment name", result.get(0).getName());
        verify(commentRepository, times(1)).findByPostId(p.getId());
    }

    @Test
    void getCommentById_WhenCommentBelongsToPost_Test() {
        when(postRepository.findById(p.getId())).thenReturn(Optional.ofNullable(p));
        when(commentRepository.findById(c.getId())).thenReturn(Optional.ofNullable(comment));

        CommentDto result = commentService.getCommentById(p.getId(),comment.getId());

        assertNotNull(result);
        assertEquals("Comment name", result.getName());
        verify(postRepository, times(1)).findById(p.getId());
        verify(commentRepository,times(1)).findById(comment.getId());
    }

    @Test
    void getCommentById_WhenCommentDoesntBelongToPost_Test() {
        Post wrongPost = Mockito.mock(Post.class);

        when(wrongPost.getId()).thenReturn(2L);
        when(postRepository.findById(wrongPost.getId())).thenReturn(Optional.of(wrongPost));
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.ofNullable(comment));

        assertThrows(BlogAPIException.class, () -> commentService.getCommentById(wrongPost.getId(), comment.getId()));
    }

    @Test
    void getCommentById_WhenPostNotFound_Test(){
        Post wrongPost = Mockito.mock(Post.class);

        assertThrows(ResourceNotFoundException.class, () -> commentService.getCommentById(wrongPost.getId(), c.getId()));
    }

    @Test
    void getCommentById_WhenCommentNotFound_Test(){
        Comment wrongComment = Mockito.mock(Comment.class);

        assertThrows(ResourceNotFoundException.class, ()-> commentService.getCommentById(p.getId(), wrongComment.getId()));
    }

    @Test
    void updateComment_WhenCommmentBelongsToPost_Test() {
        CommentDto commentRequest = new CommentDto();
        commentRequest.setName("New name");

        when(postRepository.findById(p.getId())).thenReturn(Optional.ofNullable(p));
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.ofNullable(comment));
        when(commentRepository.save(comment)).thenReturn(comment);

        CommentDto updatedComment = commentService.updateComment(p.getId(),comment.getId(),commentRequest);

        assertEquals("New name", updatedComment.getName());
    }

    @Test
    void updateComment_WhenCommmentDoesntBelongToPost_Test() {
        CommentDto commentRequest = new CommentDto();
        commentRequest.setName("New name");

        Post wrongPost = Mockito.mock(Post.class);

        when(wrongPost.getId()).thenReturn(2L);
        when(postRepository.findById(wrongPost.getId())).thenReturn(Optional.of(wrongPost));
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.ofNullable(comment));


        assertThrows(BlogAPIException.class, () -> commentService.updateComment(wrongPost.getId(),comment.getId(),commentRequest));
    }

    @Test
    void updateComment_WhenPostNotFound_Test(){
        CommentDto commentRequest = new CommentDto();
        commentRequest.setName("New name");

        assertThrows(ResourceNotFoundException.class, () -> commentService.updateComment(p.getId(),comment.getId(), commentRequest));
    }

    @Test
    void updateComment_WhenCommentNotFound_Test(){
        CommentDto commentRequest = new CommentDto();
        commentRequest.setName("New name");

        when(postRepository.findById(p.getId())).thenReturn(Optional.ofNullable(p));

        assertThrows(ResourceNotFoundException.class, () -> commentService.updateComment(p.getId(),comment.getId(), commentRequest));
        verify(postRepository, times(1)).findById(p.getId());
    }

    @Test
    void deleteComment_WhenCommentBelongsToPost_Test() {
        when(postRepository.findById(p.getId())).thenReturn(Optional.ofNullable(p));
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.ofNullable(comment));

        commentService.deleteComment(p.getId(),comment.getId());

        verify(commentRepository,times(1)).delete(comment);
        // No se borra de la tabla Posts
        assertFalse(p.getComments().stream().anyMatch(commentInPost -> commentInPost.getId() == comment.getId()));
    }

    @Test
    void deleteComment_WhenCommentDoesntBelongToPost_Test() {
        Post wrongPost = Mockito.mock(Post.class);

        when(wrongPost.getId()).thenReturn(2L);
        when(postRepository.findById(wrongPost.getId())).thenReturn(Optional.of(wrongPost));
        when(commentRepository.findById(comment.getId())).thenReturn(Optional.ofNullable(comment));

        assertThrows(BlogAPIException.class, () -> commentService.deleteComment(wrongPost.getId(),comment.getId()));
    }


    @Test
    void deleteComment_WhenPostNotFound_Test() {
        Post postNotFound = Mockito.mock(Post.class);

        assertThrows(ResourceNotFoundException.class, () -> commentService.deleteComment(postNotFound.getId(), c.getId()));
    }

    @Test
    void deleteComment_WhenCommentNotFound_Test() {
        Comment commentNotFound = Mockito.mock(Comment.class);

        assertThrows(ResourceNotFoundException.class, () -> commentService.deleteComment(p.getId(), commentNotFound.getId()));
    }
}