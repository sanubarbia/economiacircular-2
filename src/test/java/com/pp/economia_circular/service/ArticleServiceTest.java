package com.pp.economia_circular.service;

import com.pp.economia_circular.DTO.ArticleCreateDto;
import com.pp.economia_circular.DTO.ArticleResponseDto;
import com.pp.economia_circular.DTO.ArticleSearchDto;
import com.pp.economia_circular.entity.Articulo;
import com.pp.economia_circular.entity.Usuario;
import com.pp.economia_circular.repositories.ArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @Mock
    private JWTService authService;

    @InjectMocks
    private ArticleService articleService;

    private Usuario testUser;
    private Articulo testArticulo;
    private ArticleCreateDto createDto;

    @BeforeEach
    void setUp() {
        testUser = new Usuario();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setNombre("Test");
        testUser.setApellido("User");
        testUser.setRol("USER");

        testArticulo = new Articulo();
        testArticulo.setId(1L);
        testArticulo.setTitulo("Test Article");
        testArticulo.setDescripcion("Test Description");
        testArticulo.setCategoria(Articulo.CategoriaArticulo.ELECTRONICOS);
        testArticulo.setCondicion(Articulo.CondicionArticulo.USADO);
        testArticulo.setEstado(Articulo.EstadoArticulo.DISPONIBLE);
        testArticulo.setUsuario(testUser);
        testArticulo.setCreadoEn(LocalDateTime.now());
        testArticulo.setActualizadoEn(LocalDateTime.now());

        createDto = new ArticleCreateDto();
        createDto.setTitle("Test Article");
        createDto.setDescription("Test Description");
        createDto.setCategory(Articulo.CategoriaArticulo.ELECTRONICOS);
        createDto.setCondition(Articulo.CondicionArticulo.USADO);
    }

    @Test
    void createArticle_Success() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(articleRepository.save(any(Articulo.class))).thenReturn(testArticulo);

        // Act
        ArticleResponseDto result = articleService.createArticle(createDto);

        // Assert
        assertNotNull(result);
        assertEquals("Test Article", result.getTitle());
        assertEquals("Test Description", result.getDescription());
        assertEquals(Articulo.CategoriaArticulo.ELECTRONICOS, result.getCategory());
        verify(articleRepository, times(1)).save(any(Articulo.class));
    }

    @Test
    void createArticle_UserNotAuthenticated_ThrowsException() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> articleService.createArticle(createDto));
        assertEquals("Usuario no autenticado", exception.getMessage());
        verify(articleRepository, never()).save(any());
    }

    @Test
    void getArticleById_Success() {
        // Arrange
        when(articleRepository.findById(1L)).thenReturn(Optional.of(testArticulo));

        // Act
        ArticleResponseDto result = articleService.getArticleById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test Article", result.getTitle());
    }

    @Test
    void getArticleById_NotFound_ThrowsException() {
        // Arrange
        when(articleRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> articleService.getArticleById(999L));
        assertEquals("Artículo no encontrado", exception.getMessage());
    }

    @Test
    void getAllArticles_Success() {
        // Arrange
        List<Articulo> articles = Arrays.asList(testArticulo);
        when(articleRepository.findAvailableArticles()).thenReturn(articles);

        // Act
        List<ArticleResponseDto> result = articleService.getAllArticles();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Article", result.get(0).getTitle());
    }

    @Test
    void getAllArticles_WithPagination_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<Articulo> articlePage = new PageImpl<>(Arrays.asList(testArticulo));
        when(articleRepository.findAvailableArticles(pageable)).thenReturn(articlePage);

        // Act
        Page<ArticleResponseDto> result = articleService.getAllArticles(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Article", result.getContent().get(0).getTitle());
    }

    @Test
    void getArticlesByUser_Success() {
        // Arrange
        when(articleRepository.findByUsuario_Id(1L)).thenReturn(Arrays.asList(testArticulo));

        // Act
        List<ArticleResponseDto> result = articleService.getArticlesByUser(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getUserId());
    }

    @Test
    void getMyArticles_Success() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(articleRepository.findByUsuario_Id(1L)).thenReturn(Arrays.asList(testArticulo));

        // Act
        List<ArticleResponseDto> result = articleService.getMyArticles();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void getMyArticles_NotAuthenticated_ThrowsException() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> articleService.getMyArticles());
        assertEquals("Usuario no autenticado", exception.getMessage());
    }

    @Test
    void getArticlesByCategory_Success() {
        // Arrange
        when(articleRepository.findByCategoriaAndEstado(
            Articulo.CategoriaArticulo.ELECTRONICOS, 
            Articulo.EstadoArticulo.DISPONIBLE))
            .thenReturn(Arrays.asList(testArticulo));

        // Act
        List<ArticleResponseDto> result = articleService.getArticlesByCategory(
            Articulo.CategoriaArticulo.ELECTRONICOS);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Articulo.CategoriaArticulo.ELECTRONICOS, result.get(0).getCategory());
    }

    @Test
    void searchArticles_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        ArticleSearchDto searchDto = new ArticleSearchDto("Test", 
            Articulo.CategoriaArticulo.ELECTRONICOS, 
            Articulo.CondicionArticulo.USADO);
        Page<Articulo> articlePage = new PageImpl<>(Arrays.asList(testArticulo));
        
        when(articleRepository.searchArticles(
            "Test", 
            Articulo.CategoriaArticulo.ELECTRONICOS, 
            Articulo.CondicionArticulo.USADO, 
            pageable))
            .thenReturn(articlePage);

        // Act
        Page<ArticleResponseDto> result = articleService.searchArticles(searchDto, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Test Article", result.getContent().get(0).getTitle());
    }

    @Test
    void updateArticle_Success() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(testArticulo));
        when(articleRepository.save(any(Articulo.class))).thenReturn(testArticulo);

        ArticleCreateDto updateDto = new ArticleCreateDto();
        updateDto.setTitle("Updated Title");
        updateDto.setDescription("Updated Description");
        updateDto.setCategory(Articulo.CategoriaArticulo.LIBROS);
        updateDto.setCondition(Articulo.CondicionArticulo.REACONDICIONADO);

        // Act
        ArticleResponseDto result = articleService.updateArticle(1L, updateDto);

        // Assert
        assertNotNull(result);
        verify(articleRepository, times(1)).save(any(Articulo.class));
    }

    @Test
    void updateArticle_NotOwner_ThrowsException() {
        // Arrange
        Usuario otherUser = new Usuario();
        otherUser.setId(2L);
        
        when(authService.getCurrentUser()).thenReturn(otherUser);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(testArticulo));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> articleService.updateArticle(1L, createDto));
        assertEquals("No tienes permisos para editar este artículo", exception.getMessage());
    }

    @Test
    void deleteArticle_Success() {
        // Arrange
        when(authService.getCurrentUser()).thenReturn(testUser);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(testArticulo));
        when(articleRepository.save(any(Articulo.class))).thenReturn(testArticulo);

        // Act
        articleService.deleteArticle(1L);

        // Assert
        verify(articleRepository, times(1)).save(any(Articulo.class));
        assertEquals(Articulo.EstadoArticulo.ELIMINADO, testArticulo.getEstado());
    }

    @Test
    void deleteArticle_NotOwner_ThrowsException() {
        // Arrange
        Usuario otherUser = new Usuario();
        otherUser.setId(2L);
        
        when(authService.getCurrentUser()).thenReturn(otherUser);
        when(articleRepository.findById(1L)).thenReturn(Optional.of(testArticulo));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> articleService.deleteArticle(1L));
        assertEquals("No tienes permisos para eliminar este artículo", exception.getMessage());
    }

    @Test
    void getMostViewedArticles_Success() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        when(articleRepository.findMostViewedArticles(pageable))
            .thenReturn(Arrays.asList(testArticulo));

        // Act
        List<ArticleResponseDto> result = articleService.getMostViewedArticles(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }
}

