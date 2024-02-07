package com.springboot.blog.service.impl;

import com.springboot.blog.entity.Category;
import com.springboot.blog.entity.Comment;
import com.springboot.blog.entity.Post;
import com.springboot.blog.payload.CategoryDto;
import com.springboot.blog.payload.CommentDto;
import com.springboot.blog.payload.PostDto;
import com.springboot.blog.payload.PostResponse;
import com.springboot.blog.repository.CategoryRepository;
import com.springboot.blog.repository.PostRepository;
import com.springboot.blog.service.PostService;
import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles({"test"})
class PostServiceImplTest {

    @Mock
    PostRepository postRepository;

   @Mock
   CategoryRepository categoryRepository;

   ModelMapper mapper = new ModelMapper();

   @InjectMocks
   PostService postService = new PostServiceImpl(postRepository, mapper, categoryRepository);
//   static PostService postService = new PostServiceImpl(postRepository, mapper, categoryRepository);

    private Category category;
    private PostDto postDto;
    private Post post;

    @BeforeEach
    void init (){

        category = new Category();
        postDto = new PostDto();
        post = new Post();

        CommentDto commentDto = new CommentDto();

        category = new Category();
        category.setId(1L);
        category.setName("Example Category");


        postDto.setId(1L);
        postDto.setTitle("titulo 1");
        postDto.setDescription("eferf");
        postDto.setContent("wefewf");
        postDto.setCategoryId(category.getId());

        post = mapper.map(postDto, Post.class);
//        post.setCategory(category);
    }

    @Test
    void createPost() {

        when(categoryRepository.findById(anyLong())).thenReturn(Optional.ofNullable(category));
        when(postRepository.save(any(Post.class))).thenReturn(post);

        PostDto result = postService.createPost(postDto);

        assertEquals("titulo 1", result.getTitle());

    }

    @Test
    void getAllPosts() {
        List<Post> posts = Arrays.asList(
                new Post(1L, "Título 1", "des 1", "fewrfoerre", null, null),
                new Post(2L, "Título 2", "des 2", "erfref", null, null)
        );
        Page<Post> postPage = new PageImpl<>(posts);

        when(postRepository.findAll(any(Pageable.class))).thenReturn(postPage);

        PostResponse result = postService.getAllPosts(0, 10, "title", "ASC");

        assertEquals(0, result.getPageNo());
        assertEquals(2, result.getPageSize());
        assertEquals(posts.size(), result.getTotalElements());
        assertEquals(1, result.getTotalPages());
    }

    @Test
    void getPostById() {

        when(postRepository.findById(anyLong())).thenReturn(Optional.of(post));

        PostDto result = postService.getPostById(1L);

        assertEquals(1, result.getId());
    }

    @Test
    void updatePost() {
//        Post post = new Post(1L, "Título 1", "des 1", "fewrfoerre", null, null);
        Post postedit = new Post(1L, "Editado", "des 1", "fewrfoerre", null, null);

        when(postRepository.findById(anyLong())).thenReturn(Optional.of(post));
        when(categoryRepository.findById(anyLong())).thenReturn(Optional.ofNullable(category));
        when(postRepository.save(any(Post.class))).thenReturn(postedit);

        PostDto result = postService.updatePost(postDto, 1L);

        assertEquals(1, result.getId());
        assertEquals("Editado", result.getTitle());

    }

    @Test
    void deletePostById() {

        when(postRepository.findById(anyLong())).thenReturn(Optional.ofNullable(post));
        postService.deletePostById(1L);

        verify(postRepository, times(1)).findById(1L);
        verify(postRepository, times(1)).delete(post);

    }

    @Test
    void getPostsByCategory() {

        when(categoryRepository.findById(anyLong())).thenReturn(Optional.ofNullable(category));

        List<Post> postList = new ArrayList<>();

        when(postRepository.findByCategoryId(anyLong())).thenReturn(postList);

        List<PostDto> result = postService.getPostsByCategory(1L);

        assertEquals(postList.size(), result.size());
        
    }
}